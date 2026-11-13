# Runs targeted checks and packaging without starting Minecraft.
# Usage: powershell -File tools\verify_all.ps1 [-SkipTests] [-SkipBuild]

param(
    [switch]$SkipTests,
    [switch]$SkipBuild,
    [string[]]$TestClasses = @(
        'com.occultism.occultism.resource.CleanroomBuildContractTest',
        'com.occultism.occultism.ritual.RitualLifecycleTest',
        'com.occultism.occultism.resource.PatchouliBookPortTest'
    ),
    [string]$WorkspaceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'
$python = $env:DSH_PYTHON
if (-not $python) {
    if (Test-Path -LiteralPath 'D:\conda_envs\envs\work\python.exe') {
        $python = 'D:\conda_envs\envs\work\python.exe'
    } else {
        $python = 'python'
    }
}

$results = [ordered]@{}

function Invoke-Step {
    param([string]$Name, [scriptblock]$Action)
    Write-Host ""
    Write-Host "=== $Name ===" -ForegroundColor Cyan
    try {
        & $Action
        $code = $LASTEXITCODE
    } catch {
        Write-Host $_ -ForegroundColor Red
        $code = 1
    }
    $results[$Name] = $code
    if ($code -ne 0) {
        Write-Host "--- $Name FAILED (exit $code) ---" -ForegroundColor Red
    }
}

Push-Location -LiteralPath $WorkspaceRoot
try {
    if (-not $SkipTests) {
        if ($TestClasses.Count -eq 0) {
            throw 'Provide at least one targeted test class; full-suite runs are not enabled here.'
        }
        $testArguments = @('test', '--console=plain', '--quiet')
        foreach ($testClass in $TestClasses) {
            $testArguments += @('--tests', $testClass)
        }
        Invoke-Step 'compile + targeted tests' {
            & (Join-Path $WorkspaceRoot 'gradlew-local.bat') @testArguments
        }
    }
    Invoke-Step 'patchouli book validation' {
        & $python -B -m tools.verify_patchouli_book
    }
    Invoke-Step 'book regeneration is clean' {
        & $python -B -m tools.generate_patchouli_book --check
    }
    Invoke-Step 'asset validation' {
        & powershell -NoProfile -File 'tools\verify_assets.ps1'
    }
    if (-not $SkipBuild) {
        Invoke-Step 'build the jars' {
            & (Join-Path $WorkspaceRoot 'gradlew-local.bat') assemble --console=plain --quiet
        }
        Invoke-Step 'validate the built jars' {
            & $python -B -m tools.verify_build_jar
        }
    }
} finally {
    Pop-Location
}

Write-Host ""
Write-Host '--- verification summary ---'
$failed = 0
foreach ($name in $results.Keys) {
    $state = if ($results[$name] -eq 0) { 'PASS' } else { 'FAIL'; $script:failed++ }
    Write-Host ("  {0,-34} {1}" -f $name, $state)
}
if ($failed -gt 0) {
    Write-Host "$failed step(s) failed." -ForegroundColor Red
    exit 1
}
Write-Host 'All requested verifications passed.' -ForegroundColor Green
exit 0

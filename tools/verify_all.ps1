# Run every verification this repo has, in one place, with one exit code.
#
# The individual checks are documented in 交接.md. Running them by hand invites
# forgetting one - and a check nobody runs is worse than no check, because the
# gaps it would have caught stay invisible. This wrapper exists so "is this tree
# sound?" is a single command.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File tools\verify_all.ps1
#   powershell -ExecutionPolicy Bypass -File tools\verify_all.ps1 -SkipBuild   # skip jar checks
#   powershell -ExecutionPolicy Bypass -File tools\verify_all.ps1 -SkipTests   # skip the test suite
#
# Exit code 0 only when every step passed.

param(
    [switch]$SkipTests,
    [switch]$SkipBuild,
    [string]$WorkspaceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Continue'

$python = $env:DSH_PYTHON
if (-not $python) {
    $candidates = @(
        'C:\Users\12705\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe',
        'python'
    )
    foreach ($candidate in $candidates) {
        if ($candidate -eq 'python' -or (Test-Path -LiteralPath $candidate)) {
            $python = $candidate
            break
        }
    }
}

$results = [ordered]@{}

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host ""
    Write-Host "=== $Name ===" -ForegroundColor Cyan
    & $Action
    $code = $LASTEXITCODE
    $results[$Name] = $code
    if ($code -ne 0) {
        Write-Host "--- $Name FAILED (exit $code) ---" -ForegroundColor Red
    }
    return $code
}

Push-Location -LiteralPath $WorkspaceRoot
try {
    if (-not $SkipTests) {
        Invoke-Step 'compile + full test suite' {
            & (Join-Path $WorkspaceRoot 'gradlew-local.bat') test --console=plain --quiet
        } | Out-Null
    }

    Invoke-Step 'patchouli book validation' {
        & $python 'tools\verify_patchouli_book.py'
    } | Out-Null

    Invoke-Step 'book regeneration is clean' {
        & $python 'tools\generate_patchouli_book.py' --check
    } | Out-Null

    Invoke-Step 'asset validation' {
        & powershell -NoProfile -ExecutionPolicy Bypass -File 'tools\verify_assets.ps1'
    } | Out-Null

    if (-not $SkipBuild) {
        Invoke-Step 'build the jars' {
            & (Join-Path $WorkspaceRoot 'gradlew-local.bat') build --console=plain --quiet
        } | Out-Null

        Invoke-Step 'validate the built jars' {
            & $python 'tools\verify_build_jar.py'
        } | Out-Null
    }
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "--- verification summary ---"
$failed = 0
foreach ($name in $results.Keys) {
    $state = if ($results[$name] -eq 0) { 'PASS' } else { 'FAIL'; $script:failed++ }
    Write-Host ("  {0,-34} {1}" -f $name, $state)
}

if ($failed -gt 0) {
    Write-Host ""
    Write-Host "$failed step(s) failed." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host 'All verifications passed.' -ForegroundColor Green
exit 0

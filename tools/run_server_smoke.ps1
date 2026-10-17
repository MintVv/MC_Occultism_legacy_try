# Dedicated-server smoke test for the 1.12 port.
#
# The unit tests cover registries, ritual data, the book and the GUI code, but nothing has ever
# started a real server. This runs `runServer` headlessly, waits for the server to finish loading
# and reports what the log says, so a startup regression cannot hide behind green unit tests.
#
# It does NOT accept the Minecraft EULA for you. If run/eula.txt is missing or not accepted the
# script stops and prints what to do; accepting it is your decision.
#
# Exit codes: 0 = server came up and stopped cleanly, 1 = startup failed or timed out,
#             2 = EULA not accepted (nothing was started).

param(
    [int]$TimeoutSeconds = 300,
    [string]$WorkspaceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'

$runDir = Join-Path $WorkspaceRoot 'run'
$eulaFile = Join-Path $runDir 'eula.txt'
$serverLog = Join-Path $runDir 'logs/latest.log'
$gradlew = Join-Path $WorkspaceRoot 'gradlew-local.bat'

function Write-Step($message) { Write-Host "==> $message" }

if (-not (Test-Path -LiteralPath $eulaFile)) {
    Write-Host "run/eula.txt does not exist - the dedicated server will refuse to start." -ForegroundColor Yellow
    Write-Host ''
    Write-Host 'To enable the smoke test, create that file with this single line:'
    Write-Host ''
    Write-Host '    eula=true'
    Write-Host ''
    Write-Host 'That records your acceptance of the Minecraft EULA, so this script deliberately does not'
    Write-Host 'write it for you. Re-run this script afterwards.'
    exit 2
}

if ((Get-Content -LiteralPath $eulaFile -Raw) -notmatch 'eula\s*=\s*true') {
    Write-Host 'run/eula.txt exists but does not accept the EULA (expected "eula=true").' -ForegroundColor Yellow
    exit 2
}

# A stale log would make the evidence check read the previous run.
if (Test-Path -LiteralPath $serverLog) {
    Remove-Item -LiteralPath $serverLog -Force
}

Write-Step "starting the dedicated server (timeout ${TimeoutSeconds}s)"
$started = Get-Date
$job = Start-Job -ScriptBlock {
    param($dir)
    Set-Location -LiteralPath $dir
    & (Join-Path $dir 'gradlew-local.bat') runServer --console=plain 2>&1
} -ArgumentList $WorkspaceRoot

$ready = $false
$deadline = $started.AddSeconds($TimeoutSeconds)
while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 2
    if (Test-Path -LiteralPath $serverLog) {
        $content = Get-Content -LiteralPath $serverLog -Raw -ErrorAction SilentlyContinue
        if ($content -and ($content -match 'Done \([\d.]+s\)! For help')) { $ready = $true; break }
        if ($content -and ($content -match 'A fatal error has been detected|crash report|Exception in server tick loop')) {
            break
        }
    }
    if ($job.State -ne 'Running') { break }
}

Write-Step 'stopping the server'
Stop-Job -Job $job -ErrorAction SilentlyContinue
Remove-Job -Job $job -Force -ErrorAction SilentlyContinue
# RFG runs the server in a child JVM; stop anything still holding the run directory.
Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine -match 'net\.minecraft\.server|GradleStartServer' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }

if (-not (Test-Path -LiteralPath $serverLog)) {
    Write-Host 'FAILED: the server never wrote run/logs/latest.log' -ForegroundColor Red
    exit 1
}

$log = Get-Content -LiteralPath $serverLog -Raw
$checks = [ordered]@{
    'Forge loaded'          = $log -match 'Forge Mod Loader version 14\.23\.5\.2847'
    'Occultism loaded'      = $log -match 'Hello From Occultism|Occultism'
    'ritual table loaded'   = $log -match 'Loaded \d+ Occultism ritual recipes'
    'server reached Done'   = $ready
    'no fatal error'        = -not ($log -match 'A fatal error has been detected')
    'no missing registry'   = -not ($log -match 'Missing registr|registry entry is null')
}

Write-Host ''
Write-Host '--- smoke test evidence ---'
$failed = 0
foreach ($name in $checks.Keys) {
    $state = if ($checks[$name]) { 'PASS' } else { 'FAIL'; $failed++ }
    Write-Host ("  {0,-24} {1}" -f $name, $state)
}

if ($log -match 'Loaded (\d+) Occultism ritual recipes') {
    Write-Host ("  ritual definitions loaded: {0}" -f $Matches[1])
}
Write-Host ("  log: {0}" -f $serverLog)

if ($failed -gt 0) {
    Write-Host ''
    Write-Host '--- last 40 log lines ---' -ForegroundColor Red
    Get-Content -LiteralPath $serverLog -Tail 40
    exit 1
}

Write-Host ''
Write-Host 'Server smoke test passed.' -ForegroundColor Green
exit 0

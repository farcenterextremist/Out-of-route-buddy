# Loop health check (liveness) — run at loop start and at start of every phase.
# Usage: .\scripts\automation\loop_health_check.ps1 -Quick
#        .\scripts\automation\loop_health_check.ps1 -Quick -Gate  (exit 1 blocks next phase)
# See: docs/automation/LOOP_HEALTH_CHECKS.md

param(
    [switch]$Quick,   # liveness only (default when used in loops)
    [switch]$Gate     # exit 1 on first failure (strict phase gate)
)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }

$ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$AutomationDir = Join-Path $RepoRoot "docs\automation"
$StateFile = Join-Path $AutomationDir "loop_health_state.json"

$checks = @{
    repo = $false
    gradle = $false
    app = $false
    docs = $false
    writable = $false
}
$allOk = $true
$firstFailure = ""

# 1. Repo root exists and is directory
if (Test-Path $RepoRoot -PathType Container) { $checks.repo = $true } else {
    $allOk = $false; if (-not $firstFailure) { $firstFailure = "repo root missing or not a directory" }
    if ($Gate) { Write-Host "HEALTH FAIL: $firstFailure"; exit 1 }
}

# 2. Gradle wrapper exists
$gradlew = Join-Path $RepoRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) { $gradlew = Join-Path $RepoRoot "gradlew" }
if (Test-Path $gradlew) { $checks.gradle = $true } else {
    $allOk = $false; if (-not $firstFailure) { $firstFailure = "gradlew.bat / gradlew not found" }
    if ($Gate) { Write-Host "HEALTH FAIL: $firstFailure"; exit 1 }
}

# 3. App and app/src exist
$appDir = Join-Path $RepoRoot "app"
$appSrc = Join-Path $appDir "src"
if ((Test-Path $appDir -PathType Container) -and (Test-Path $appSrc -PathType Container)) { $checks.app = $true } else {
    $allOk = $false; if (-not $firstFailure) { $firstFailure = "app or app/src missing" }
    if ($Gate) { Write-Host "HEALTH FAIL: $firstFailure"; exit 1 }
}

# 4. docs/automation exists
if (Test-Path $AutomationDir -PathType Container) { $checks.docs = $true } else {
    $allOk = $false; if (-not $firstFailure) { $firstFailure = "docs/automation missing" }
    if ($Gate) { Write-Host "HEALTH FAIL: $firstFailure"; exit 1 }
}

# 5. docs/automation writable (try to create/write state file)
$writable = $false
if ($checks.docs) {
    try {
        $testFile = Join-Path $AutomationDir "loop_health_check_write_test.tmp"
        "ok" | Set-Content -Path $testFile -Force -ErrorAction Stop
        Remove-Item $testFile -Force -ErrorAction SilentlyContinue
        $writable = $true
    } catch { }
}
$checks.writable = $writable
if (-not $writable) {
    $allOk = $false; if (-not $firstFailure) { $firstFailure = "docs/automation not writable" }
    if ($Gate) { Write-Host "HEALTH FAIL: $firstFailure"; exit 1 }
}

# Optional: last pulse age (if pulse_log exists and is very old, warn)
$lastPulse = $null
$pulseLog = Join-Path $AutomationDir "pulse_log.txt"
if (Test-Path $pulseLog) {
    $lastWrite = (Get-Item $pulseLog).LastWriteTimeUtc
    $lastPulse = $lastWrite.ToString("yyyy-MM-ddTHH:mm:ssZ")
}

# Write state file
$status = if ($allOk) { "ok" } else { "degraded" }
$state = @{
    lastCheck = $ts
    status = $status
    checks = $checks
    note = "liveness"
} | ConvertTo-Json -Compress
try {
    $state | Set-Content -Path $StateFile -Force -Encoding UTF8
} catch { }

# Console
if ($allOk) {
    Write-Host "[$ts] Loop health (liveness): OK"
} else {
    Write-Host "[$ts] Loop health (liveness): DEGRADED - $firstFailure"
    exit 1
}
exit 0

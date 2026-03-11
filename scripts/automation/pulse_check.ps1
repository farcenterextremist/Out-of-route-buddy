# Run from repo root: .\scripts\automation\pulse_check.ps1
# Optional: -Quick (skip full test run, only log timestamp and status)
# Optional: -Note "your one-line progress note"

param(
    [switch]$Quick,
    [string]$Note = ""
)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$PulseLog = Join-Path $RepoRoot "docs\automation\pulse_log.txt"
$PlanPath = Join-Path $RepoRoot "docs\automation\8_HOUR_IMPROVEMENT_PLAN.md"
$ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Ensure log directory exists
$logDir = Split-Path -Parent $PulseLog
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$line = "---"
Add-Content -Path $PulseLog -Value $line
Add-Content -Path $PulseLog -Value "[$ts] PULSE"

# 1. Unit tests (unless -Quick)
$testResult = "skipped"
if (-not $Quick) {
    try {
        $out = & .\gradlew.bat :app:testDebugUnitTest --no-daemon 2>&1 | Out-String
        if ($LASTEXITCODE -eq 0) {
            if ($out -match "(\d+)\s+tests?\s+completed") { $testResult = "$($Matches[1]) passed" }
            else { $testResult = "passed" }
        } else {
            if ($out -match "(\d+)\s+failed") { $testResult = "$($Matches[1]) FAILED" }
            else { $testResult = "FAILED" }
        }
    } catch {
        $testResult = "error: $($_.Exception.Message)"
    }
} else {
    $testResult = "quick run (skipped)"
}
Add-Content -Path $PulseLog -Value "  tests: $testResult"

# 2. Lint (every 2nd pulse or when not -Quick) — use file timestamp to alternate
$doLint = -not $Quick
if ($doLint) {
    try {
        $lintOut = & .\gradlew.bat :app:lintDebug --no-daemon 2>&1 | Out-String
        if ($lintOut -match "(\d+)\s+errors") { Add-Content -Path $PulseLog -Value "  lint: $($Matches[1]) errors" }
        elseif ($lintOut -match "BUILD SUCCESSFUL") { Add-Content -Path $PulseLog -Value "  lint: 0 errors" }
        else { Add-Content -Path $PulseLog -Value "  lint: run completed" }
    } catch {
        Add-Content -Path $PulseLog -Value "  lint: error"
    }
} else {
    Add-Content -Path $PulseLog -Value "  lint: skipped"
}

# 3. Note
if ($Note) { Add-Content -Path $PulseLog -Value "  note: $Note" }
Add-Content -Path $PulseLog -Value "  objective: shippable product (see docs/automation/IMPROVEMENT_LOOP_ROUTINE.md)"
Add-Content -Path $PulseLog -Value ""

# 4. Loop listener (record pulse event for data/improvement)
$ListenerScript = Join-Path $PSScriptRoot "loop_listener.ps1"
if (Test-Path $ListenerScript) {
    try {
        $metricsJson = @{ tests = $testResult } | ConvertTo-Json -Compress
        & $ListenerScript -Event "pulse" -Note $Note -Metrics $metricsJson 2>$null
    } catch { }
}

# Console summary
Write-Host "[$ts] Pulse recorded -> $PulseLog"
Write-Host "  tests: $testResult"
Write-Host "  log: $PulseLog"

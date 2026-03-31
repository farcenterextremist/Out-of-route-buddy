# Run from repo root: .\scripts\automation\pulse_check.ps1
# Optional: -Quick (skip full test run, only log timestamp and status)
# Optional: -UseSimpleDebugCleanup (run the consolidated debug/cleanup sweep instead of separate test + lint steps)
# Optional: -Note "your one-line progress note"
# Optional: -RunId run-YYYYMMdd-HHmmss (keeps pulse tied to one logical run)

param(
    [switch]$Quick,
    [switch]$UseSimpleDebugCleanup,
    [string]$Note = "",
    [string]$RunId = ""
)

$ErrorActionPreference = "Continue"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

$PulseLog = Join-Path $RepoRoot "docs\automation\pulse_log.txt"
$ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Ensure log directory exists
$logDir = Split-Path -Parent $PulseLog
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$line = "---"
Add-Content -Path $PulseLog -Value $line
Add-Content -Path $PulseLog -Value "[$ts] PULSE"

# 1. Readiness checks (unless -Quick)
$testResult = "skipped"
$lintResult = "skipped"
$detektResult = "not run"
$runMode = if ($Quick) {
    "quick"
} elseif ($UseSimpleDebugCleanup) {
    "simple_debug_cleanup"
} else {
    "standard"
}

if (-not $Quick -and $UseSimpleDebugCleanup) {
    $debugScript = Join-Path $PSScriptRoot "run_simple_debug_cleanup.ps1"
    try {
        & $debugScript
        if ($LASTEXITCODE -eq 0) {
            $testResult = "included in simple debug cleanup"
            $lintResult = "included in simple debug cleanup"
            $detektResult = "included in simple debug cleanup"
        } else {
            $testResult = "FAILED via simple debug cleanup"
            $lintResult = "check script output"
            $detektResult = "check script output"
        }
    } catch {
        $testResult = "error: $($_.Exception.Message)"
        $lintResult = "not completed"
        $detektResult = "not completed"
    }
} elseif (-not $Quick) {
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

    try {
        $lintOut = & .\gradlew.bat :app:lintDebug --no-daemon 2>&1 | Out-String
        if ($lintOut -match "(\d+)\s+errors") { $lintResult = "$($Matches[1]) errors" }
        elseif ($lintOut -match "BUILD SUCCESSFUL") { $lintResult = "0 errors" }
        else { $lintResult = "run completed" }
    } catch {
        $lintResult = "error"
    }
} else {
    $testResult = "quick run (skipped)"
    $lintResult = "quick run (skipped)"
}

# 2. Log results
Add-Content -Path $PulseLog -Value "  mode: $runMode"
Add-Content -Path $PulseLog -Value "  tests: $testResult"
Add-Content -Path $PulseLog -Value "  lint: $lintResult"
Add-Content -Path $PulseLog -Value "  detekt: $detektResult"

# 3. Note
if ($Note) { Add-Content -Path $PulseLog -Value "  note: $Note" }
Add-Content -Path $PulseLog -Value "  objective: shippable product (see docs/automation/IMPROVEMENT_LOOP_ROUTINE.md)"
Add-Content -Path $PulseLog -Value ""

# 4. Loop listener (record pulse event for data/improvement)
$ListenerScript = Join-Path $PSScriptRoot "loop_listener.ps1"
if (Test-Path $ListenerScript) {
    try {
        $metricsJson = @{
            mode = $runMode
            tests = $testResult
            lint = $lintResult
            detekt = $detektResult
        } | ConvertTo-Json -Compress
        $listenerParams = @{
            Event = "pulse"
            Note = $Note
            Metrics = $metricsJson
        }
        if ($RunId) {
            $listenerParams["RunId"] = $RunId
        }
        & $ListenerScript @listenerParams 2>$null
    } catch { }
}

# Console summary
Write-Host "[$ts] Pulse recorded -> $PulseLog"
Write-Host "  mode: $runMode"
Write-Host "  tests: $testResult"
Write-Host "  lint: $lintResult"
Write-Host "  detekt: $detektResult"
Write-Host "  log: $PulseLog"

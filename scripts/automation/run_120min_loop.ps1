# Improvement Loop: pulse every 30 min for 2 hours.
# Run from repo root: .\scripts\automation\run_120min_loop.ps1
# Stop anytime with Ctrl+C.
# ROUTINE: See docs/automation/IMPROVEMENT_LOOP_ROUTINE.md
# COMMON SENSE: docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md
# ALLOWLIST: Add commands from docs/automation/LOOP_MASTER_ALLOWLIST.md at loop start for full autonomy.

param(
    [int]$DurationMinutes = 120,
    [int]$PulseIntervalMinutes = 30,
    [switch]$UseSimpleDebugCleanupFirstPulse,
    [string]$RunId = ""
)

$ErrorActionPreference = "Continue"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

$PulseScript = Join-Path $RepoRoot "scripts\automation\pulse_check.ps1"
$ListenerScript = Join-Path $RepoRoot "scripts\automation\loop_listener.ps1"
$StartScript = Join-Path $RepoRoot "scripts\automation\start_loop_run.ps1"
$PlanPath   = Join-Path $RepoRoot "docs\automation\IMPROVEMENT_LOOP_ROUTINE.md"
$PulseLog   = Join-Path $RepoRoot "docs\automation\pulse_log.txt"

if (-not $RunId) {
    $RunId = New-LoopRunId -Prefix "run"
}
if (Test-Path $StartScript) {
    & $StartScript -Loop "improvement" -RunId $RunId -EmitStartEvent -Note "Duration=$DurationMinutes min, pulse every $PulseIntervalMinutes min" | Out-Null
} elseif (Test-Path $ListenerScript) {
    & $ListenerScript -Event "loop_start" -Note "Duration=$DurationMinutes min, pulse every $PulseIntervalMinutes min" -RunId $RunId
}

$endTime = (Get-Date).AddMinutes($DurationMinutes)
$summaryReminder = (Get-Date).AddMinutes([Math]::Max(0, $DurationMinutes - 30))

Write-Host "120-minute improvement loop started. Pulse every $PulseIntervalMinutes min. End at $($endTime.ToString('HH:mm'))."
Write-Host "Plan: $PlanPath"
if ($UseSimpleDebugCleanupFirstPulse) {
    Write-Host "First pulse will use the consolidated simple debug + cleanup sweep."
}
Write-Host ""

$pulseCount = 0
while ((Get-Date) -lt $endTime) {
    $now = Get-Date
    $pulseCount++
    Write-Host "[$($now.ToString('HH:mm:ss'))] Pulse #$pulseCount"
    if ($UseSimpleDebugCleanupFirstPulse -and $pulseCount -eq 1) {
        & $PulseScript -UseSimpleDebugCleanup -RunId $RunId
    } else {
        & $PulseScript -RunId $RunId
    }
    if ($LASTEXITCODE -ne 0) { Write-Host "Pulse script reported non-zero exit." }

    # In the last 30 min: remind to write summary
    if ($now -ge $summaryReminder -and -not $script:summaryReminded) {
        Write-Host ""
        Write-Host ">>> Phase 4: Write improvement summary to docs/automation/IMPROVEMENT_LOOP_SUMMARY_<date>.md <<<"
        $script:summaryReminded = $true
    }

    $nextPulse = $now.AddMinutes($PulseIntervalMinutes)
    if ($nextPulse -gt $endTime) { break }
    $sleepSec = [Math]::Min([int]($nextPulse - $now).TotalSeconds, $PulseIntervalMinutes * 60)
    Write-Host "Next pulse in $([Math]::Round($sleepSec/60)) min. (Ctrl+C to stop)"
    Start-Sleep -Seconds $sleepSec
}

if (Test-Path $ListenerScript) {
    & $ListenerScript -Event "loop_end" -Note "Pulses=$pulseCount" -RunId $RunId
}
Write-Host ""
Write-Host "Improvement Loop complete. Review pulse_log.txt and write summary per Phase 4 in IMPROVEMENT_LOOP_ROUTINE.md."
Write-Host "Then use .\scripts\automation\finish_loop_run.ps1 once the summary path and next steps are ready."

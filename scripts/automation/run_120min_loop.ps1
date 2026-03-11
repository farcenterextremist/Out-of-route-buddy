# Improvement Loop: pulse every 30 min for 2 hours.
# Run from repo root: .\scripts\automation\run_120min_loop.ps1
# Stop anytime with Ctrl+C.
# ROUTINE: See docs/automation/IMPROVEMENT_LOOP_ROUTINE.md
# COMMON SENSE: docs/automation/IMPROVEMENT_LOOP_COMMON_SENSE.md
# ALLOWLIST: Add commands from docs/automation/LOOP_MASTER_ALLOWLIST.md at loop start for full autonomy.

param(
    [int]$DurationMinutes = 120,
    [int]$PulseIntervalMinutes = 30
)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$PulseScript = Join-Path $RepoRoot "scripts\automation\pulse_check.ps1"
$PlanPath   = Join-Path $RepoRoot "docs\automation\IMPROVEMENT_LOOP_ROUTINE.md"
$PulseLog   = Join-Path $RepoRoot "docs\automation\pulse_log.txt"

$endTime = (Get-Date).AddMinutes($DurationMinutes)
$summaryReminder = (Get-Date).AddMinutes([Math]::Max(0, $DurationMinutes - 30))

Write-Host "120-minute improvement loop started. Pulse every $PulseIntervalMinutes min. End at $($endTime.ToString('HH:mm'))."
Write-Host "Plan: $PlanPath"
Write-Host ""

$pulseCount = 0
while ((Get-Date) -lt $endTime) {
    $now = Get-Date
    $pulseCount++
    Write-Host "[$($now.ToString('HH:mm:ss'))] Pulse #$pulseCount"
    & $PulseScript
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

Write-Host ""
Write-Host "Improvement Loop complete. Review pulse_log.txt and write summary per Phase 4 in IMPROVEMENT_LOOP_ROUTINE.md."

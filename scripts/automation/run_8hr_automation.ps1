# 8-hour automation: pulse every 30 min, then generate ship instructions in the last 2 hours.
# Run from repo root: .\scripts\automation\run_8hr_automation.ps1
# Stop anytime with Ctrl+C.

param(
    [int]$DurationHours = 8,
    [int]$PulseIntervalMinutes = 30
)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$PulseScript = Join-Path $RepoRoot "scripts\automation\pulse_check.ps1"
$PlanPath   = Join-Path $RepoRoot "docs\automation\8_HOUR_IMPROVEMENT_PLAN.md"
$PulseLog   = Join-Path $RepoRoot "docs\automation\pulse_log.txt"
$Desktop    = [Environment]::GetFolderPath("Desktop")
$ShipFile   = Join-Path $Desktop "OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt"

$endTime = (Get-Date).AddHours($DurationHours)
$planningStart = (Get-Date).AddHours([Math]::Max(0, $DurationHours - 2))   # Last 2 hours = planning

Write-Host "8-hour automation started. Pulse every $PulseIntervalMinutes min. End at $($endTime.ToString('HH:mm'))."
Write-Host "Planning phase (generate ship instructions) starts at $($planningStart.ToString('HH:mm'))."
Write-Host "Ship instructions will be written to: $ShipFile"
Write-Host ""

$pulseCount = 0
while ((Get-Date) -lt $endTime) {
    $now = Get-Date
    $pulseCount++
    Write-Host "[$($now.ToString('HH:mm:ss'))] Pulse #$pulseCount"
    & $PulseScript
    if ($LASTEXITCODE -ne 0) { Write-Host "Pulse script reported non-zero exit." }

    # In the "planning" window: generate ship instructions once
    if ($now -ge $planningStart -and -not $script:shipFileWritten) {
        Write-Host "Planning phase: generating ship instructions..."
        & (Join-Path $RepoRoot "scripts\automation\write_ship_instructions.ps1") -OutFile $ShipFile
        $script:shipFileWritten = $true
        Write-Host "Ship instructions written to: $ShipFile"
    }

    $nextPulse = $now.AddMinutes($PulseIntervalMinutes)
    if ($nextPulse -gt $endTime) { break }
    $sleepSec = [Math]::Min([int]($nextPulse - $now).TotalSeconds, $PulseIntervalMinutes * 60)
    Write-Host "Next pulse in $([Math]::Round($sleepSec/60)) min. (Ctrl+C to stop)"
    Start-Sleep -Seconds $sleepSec
}

Write-Host "Automation run complete."
if (-not $script:shipFileWritten) {
    Write-Host "Planning phase was not reached. Run write_ship_instructions.ps1 manually to generate $ShipFile"
}

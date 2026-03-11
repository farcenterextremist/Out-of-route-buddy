# Improvement Loop Listener — Record events for better data and loop improvement.
# Run from repo root: .\scripts\automation\loop_listener.ps1 -Event <event> -Phase <phase> -Note <note> -Metrics <json>
# Events: loop_start, phase_start, phase_end, pulse, metrics, loop_end
# Data is appended to docs/automation/loop_events.jsonl (JSONL format, one JSON object per line).

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("loop_start", "phase_start", "phase_end", "pulse", "metrics", "loop_end")]
    [string]$Event,

    [string]$Phase = "",
    [string]$Note = "",
    [string]$Metrics = "{}",
    [string]$RunId = "",
    [string]$OutFile = ""
)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }

if ($OutFile) {
    $EventsFile = $OutFile
} else {
    $EventsFile = Join-Path $RepoRoot "docs\automation\loop_events.jsonl"
}
$logDir = Split-Path -Parent $EventsFile
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$ts = Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ"
if (-not $RunId) { $RunId = "run-$((Get-Date).ToString('yyyyMMdd-HHmm'))" }

$payloadObj = [PSCustomObject]@{
    ts     = $ts
    event  = $Event
    run_id = $RunId
}
if ($Phase) { $payloadObj | Add-Member -NotePropertyName "phase" -NotePropertyValue $Phase -Force }
if ($Note) { $payloadObj | Add-Member -NotePropertyName "note" -NotePropertyValue $Note -Force }
if ($Metrics -and $Metrics -ne "{}") {
    try {
        $metricsObj = $Metrics | ConvertFrom-Json
        $payloadObj | Add-Member -NotePropertyName "metrics" -NotePropertyValue $metricsObj -Force
    } catch {
        $payloadObj | Add-Member -NotePropertyName "metrics_raw" -NotePropertyValue $Metrics -Force
    }
}
$payload = $payloadObj | ConvertTo-Json -Compress

Add-Content -Path $EventsFile -Value $payload -NoNewline
Add-Content -Path $EventsFile -Value ""

Write-Host "[$ts] Loop listener: $Event" -NoNewline
if ($Phase) { Write-Host " phase=$Phase" -NoNewline }
if ($Note) { Write-Host " note=$Note" -NoNewline }
Write-Host " -> $EventsFile"

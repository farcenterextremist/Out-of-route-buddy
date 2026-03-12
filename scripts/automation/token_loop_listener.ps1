# Token Reduction Loop Listener — Record token-loop events for analysis and improvement.
# Run from repo root: .\scripts\automation\token_loop_listener.ps1 -Event <event> -Step <step> -Note <note> -Metrics <json>
# Or with metrics from file (avoids PowerShell quoting): -MetricsPath path\to\metrics.json
# Events: token_loop_start, step_start, step_end, token_loop_end
# Data is appended to docs/automation/token_loop_events.jsonl (JSONL format).

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("token_loop_start", "step_start", "step_end", "token_loop_end")]
    [string]$Event,

    [string]$Step = "",
    [string]$Note = "",
    [string]$Metrics = "{}",
    [string]$MetricsPath = "",
    [string]$RunId = "",
    [string]$OutFile = ""
)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }

if ($OutFile) {
    $EventsFile = $OutFile
} else {
    $EventsFile = Join-Path $RepoRoot "docs\automation\token_loop_events.jsonl"
}
$logDir = Split-Path -Parent $EventsFile
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

$ts = Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ"
if (-not $RunId) { $RunId = "token-$((Get-Date).ToString('yyyyMMdd-HHmm'))" }

$payloadObj = [PSCustomObject]@{
    ts     = $ts
    event  = $Event
    run_id = $RunId
}
if ($Step) { $payloadObj | Add-Member -NotePropertyName "step" -NotePropertyValue $Step -Force }
if ($Note) { $payloadObj | Add-Member -NotePropertyName "note" -NotePropertyValue $Note -Force }
$metricsJson = $Metrics
if ($MetricsPath -and (Test-Path $MetricsPath)) {
    $metricsJson = Get-Content -Path $MetricsPath -Raw -ErrorAction SilentlyContinue
    if (-not $metricsJson) { $metricsJson = "{}" }
}
if ($metricsJson -and $metricsJson -ne "{}") {
    try {
        $metricsObj = $metricsJson | ConvertFrom-Json
        $payloadObj | Add-Member -NotePropertyName "metrics" -NotePropertyValue $metricsObj -Force
    } catch {
        $payloadObj | Add-Member -NotePropertyName "metrics_raw" -NotePropertyValue $metricsJson -Force
    }
}
$payload = $payloadObj | ConvertTo-Json -Compress

Add-Content -Path $EventsFile -Value $payload -NoNewline
Add-Content -Path $EventsFile -Value ""

Write-Host "[$ts] Token loop: $Event" -NoNewline
if ($Step) { Write-Host " step=$Step" -NoNewline }
if ($Note) { Write-Host " note=$Note" -NoNewline }
Write-Host " -> $EventsFile"

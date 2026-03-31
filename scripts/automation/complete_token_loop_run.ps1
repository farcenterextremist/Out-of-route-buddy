# Standardized token-loop completion helper.
# Run from repo root:
# .\scripts\automation\complete_token_loop_run.ps1 -RunId token-20260316-1200 -SummaryPath docs/automation/TOKEN_LOOP_SUMMARY_2026-03-16.md

param(
    [Parameter(Mandatory = $true)]
    [string]$RunId,

    [Parameter(Mandatory = $true)]
    [string]$SummaryPath,

    [string[]]$NextSteps = @(),
    [string]$Checkpoint = "",
    [string]$ContextFile = "",
    [string]$EventsFile = "",
    [string]$LatestFile = "",
    [string]$Note = "Completed via complete_token_loop_run.ps1",
    [string]$Metrics = "{}",
    [string]$MetricsPath = "",
    [string]$ListenerOutFile = "",
    [switch]$RunContinuityTests,
    [switch]$AllowMissingSummaryPath
)

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$finishScript = Join-Path $RepoRoot "scripts\automation\finish_loop_run.ps1"
$listenerScript = Join-Path $RepoRoot "scripts\automation\token_loop_listener.ps1"

if (-not (Test-Path $finishScript)) {
    throw "finish_loop_run.ps1 not found."
}

if (-not (Test-Path $listenerScript)) {
    throw "token_loop_listener.ps1 not found."
}

$finishParams = @{
    Loop = "token"
    RunId = $RunId
    SummaryPath = $SummaryPath
    NextSteps = $NextSteps
}

if ($Checkpoint) {
    $finishParams["Checkpoint"] = $Checkpoint
}
if ($ContextFile) {
    $finishParams["ContextFile"] = $ContextFile
}
if ($EventsFile) {
    $finishParams["EventsFile"] = $EventsFile
}
if ($LatestFile) {
    $finishParams["LatestFile"] = $LatestFile
}
if ($RunContinuityTests) {
    $finishParams["RunContinuityTests"] = $true
}
if ($AllowMissingSummaryPath) {
    $finishParams["AllowMissingSummaryPath"] = $true
}

$finishResult = & $finishScript @finishParams
if (-not $finishResult -or -not $finishResult.shared_state_updated) {
    throw "Token loop finish wrapper did not confirm shared-state update."
}

$listenerParams = @{
    Event = "token_loop_end"
    RunId = $RunId
    Note = $Note
}

if ($MetricsPath) {
    $listenerParams["MetricsPath"] = $MetricsPath
} elseif ($Metrics -and $Metrics -ne "{}") {
    $listenerParams["Metrics"] = $Metrics
}

if ($ListenerOutFile) {
    $listenerParams["OutFile"] = $ListenerOutFile
}

& $listenerScript @listenerParams | Out-Null

$result = [ordered]@{
    run_id = $RunId
    summary_path = $SummaryPath
    shared_state_updated = $true
    token_loop_end_recorded = $true
    finish_result = $finishResult
}

Write-Host "Token loop completion helper finished -> RunId: $RunId"
[PSCustomObject]$result

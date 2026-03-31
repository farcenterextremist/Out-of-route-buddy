# Standardized loop finish wrapper.
# Run from repo root:
# .\scripts\automation\finish_loop_run.ps1 -Loop improvement -RunId run-20260316-123000 -SummaryPath docs/automation/IMPROVEMENT_LOOP_SUMMARY_2026-03-16.md -RunContinuityTests

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("improvement", "token", "cyber", "cyber_security", "synthetic_data", "file_organizer")]
    [string]$Loop,

    [Parameter(Mandatory = $true)]
    [string]$RunId,

    [Parameter(Mandatory = $true)]
    [string]$SummaryPath,

    [string[]]$NextSteps = @(),
    [string]$Checkpoint = "",
    [string]$ContextFile = "",
    [string]$EventsFile = "",
    [string]$LatestFile = "",
    [string[]]$EfficiencyTargetPaths = @(),
    [switch]$RunContinuityTests,
    [switch]$AllowMissingSummaryPath
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

$CanonicalLoop = Resolve-LoopCanonicalName -Loop $Loop
$summaryPathOnDisk = if ([System.IO.Path]::IsPathRooted($SummaryPath)) {
    $SummaryPath
} else {
    Join-Path $RepoRoot $SummaryPath
}

if (-not $AllowMissingSummaryPath -and -not (Test-Path $summaryPathOnDisk)) {
    throw "Summary path does not exist on disk: $SummaryPath"
}

$writerScript = Join-Path $RepoRoot "scripts\automation\write_loop_shared_state.ps1"
$writerParams = @{
    Loop = $CanonicalLoop
    RunId = $RunId
    SummaryPath = $SummaryPath
    NextSteps = $NextSteps
}
if ($Checkpoint) {
    $writerParams["Checkpoint"] = $Checkpoint
}
if ($EventsFile) {
    $writerParams["EventsFile"] = $EventsFile
}
if ($LatestFile) {
    $writerParams["LatestFile"] = $LatestFile
}

& $writerScript @writerParams

$continuityStatus = "not_run"
if ($RunContinuityTests) {
    $continuityScript = Join-Path $RepoRoot "scripts\automation\run_loop_continuity_tests.ps1"
    & $continuityScript
    if ($LASTEXITCODE -ne 0) {
        throw "Loop continuity suite failed after shared-state write."
    }
    $continuityStatus = "passed"
}

$efficiencyScript = Join-Path $RepoRoot "scripts\automation\measure_loop_efficiency.ps1"
$efficiencyWriterScript = Join-Path $RepoRoot "scripts\automation\write_loop_efficiency_block.ps1"
$efficiencyResult = $null
if (Test-Path $efficiencyScript) {
    $efficiencyParams = @{
        Quiet = $true
        PassThru = $true
    }
    if ($continuityStatus -eq "passed") {
        $efficiencyParams["ContinuityStatus"] = "pass"
    }
    $efficiencyResult = & $efficiencyScript @efficiencyParams
}

if ($efficiencyResult -and (Test-Path $efficiencyWriterScript)) {
    $targets = @($SummaryPath)
    if ($EfficiencyTargetPaths.Count -gt 0) {
        $targets += $EfficiencyTargetPaths
    }

    $normalizedTargets = $targets |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        Select-Object -Unique

    foreach ($target in $normalizedTargets) {
        $targetOnDisk = if ([System.IO.Path]::IsPathRooted($target)) {
            $target
        } else {
            Join-Path $RepoRoot $target
        }
        if (Test-Path $targetOnDisk) {
            & $efficiencyWriterScript `
                -TargetPath $targetOnDisk `
                -RunId $RunId `
                -Score $efficiencyResult.score `
                -Grade $efficiencyResult.grade `
                -ProgressBar $efficiencyResult.progress_bar `
                -WhyItMoved "Recorded automatically from loop automation evidence after finish wrapper execution." | Out-Null
        }
    }
}

if ($ContextFile -and (Test-Path $ContextFile)) {
    $contextObj = Get-Content -Path $ContextFile -Raw | ConvertFrom-Json
    $contextTable = [ordered]@{}
    foreach ($prop in $contextObj.PSObject.Properties) {
        $contextTable[$prop.Name] = $prop.Value
    }
    $contextTable["finished_at"] = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    $contextTable["summary_path"] = $SummaryPath
    $contextTable["continuity_status"] = $continuityStatus
    $contextTable["next_steps"] = @($NextSteps)
    if ($Checkpoint) {
        $contextTable["checkpoint"] = $Checkpoint
    }
    if ($efficiencyResult) {
        $contextTable["loop_efficiency_score"] = $efficiencyResult.score
        $contextTable["loop_efficiency_grade"] = $efficiencyResult.grade
        $contextTable["loop_efficiency_progress_bar"] = $efficiencyResult.progress_bar
    }
    $contextJson = $contextTable | ConvertTo-Json -Depth 10
    Set-Content -Path $ContextFile -Value $contextJson -Encoding UTF8
}

$result = [ordered]@{
    loop = $CanonicalLoop
    run_id = $RunId
    summary_path = $SummaryPath
    shared_state_updated = $true
    continuity_status = $continuityStatus
    loop_efficiency_score = if ($efficiencyResult) { $efficiencyResult.score } else { $null }
    loop_efficiency_grade = if ($efficiencyResult) { $efficiencyResult.grade } else { $null }
    loop_efficiency_progress_bar = if ($efficiencyResult) { $efficiencyResult.progress_bar } else { $null }
    manual_follow_up = @(
        "Append or verify the loop ledger block",
        "Handle hub deposit and hub/README update when the output is polished",
        "Include the Loop Consistency Check block in the final summary"
    )
}

Write-Host "Loop finish wrapper completed for '$CanonicalLoop' -> RunId: $RunId"
if ($efficiencyResult) {
    Write-Host "Loop efficiency: $($efficiencyResult.score)/100 $($efficiencyResult.progress_bar)"
}

[PSCustomObject]$result

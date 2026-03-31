# Readonly audit for loop shared-state drift.
# Run from repo root: .\scripts\automation\audit_loop_shared_state.ps1

param(
    [string]$EventsFile = "",
    [string]$LatestDir = "",
    [string]$ReportPath = "",
    [string]$StatePath = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

function Add-ReportIssue {
    param(
        [System.Collections.Generic.List[object]]$Bucket,
        [string]$Severity,
        [string]$Loop,
        [string]$Message
    )

    $Bucket.Add([PSCustomObject]@{
        severity = $Severity
        loop = $Loop
        message = $Message
    })
}

function Get-LatestRunIdValue {
    param($Obj)

    if ($Obj -and ($Obj.PSObject.Properties.Name -contains "run_id") -and $Obj.run_id) {
        return [string]$Obj.run_id
    }
    if ($Obj -and ($Obj.PSObject.Properties.Name -contains "last_run_id") -and $Obj.last_run_id) {
        return [string]$Obj.last_run_id
    }
    return $null
}

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
Set-Location $RepoRoot

if (-not $EventsFile) {
    $EventsFile = Join-Path $RepoRoot "docs\automation\loop_shared_events.jsonl"
}
if (-not $LatestDir) {
    $LatestDir = Join-Path $RepoRoot "docs\automation\loop_latest"
}
if (-not $StatePath) {
    $StatePath = Join-Path $RepoRoot "docs\automation\loop_shared_state_audit_state.json"
}

$requiredLoops = @("improvement", "token", "cyber", "synthetic_data")
$optionalLoops = @("file_organizer")
$issues = New-Object 'System.Collections.Generic.List[object]'

if (-not (Test-Path $EventsFile)) {
    Add-ReportIssue -Bucket $issues -Severity "error" -Loop "shared_state" -Message "Missing events file: $EventsFile"
    $eventObjects = @()
} else {
    $eventObjects = @()
    $lines = Get-Content -Path $EventsFile | Where-Object { $_.Trim() -ne "" }
    foreach ($line in $lines) {
        try {
            $eventObjects += ($line | ConvertFrom-Json)
        } catch {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop "shared_state" -Message "Invalid JSON line in events file."
        }
    }
}

$latestEventsByLoop = @{}
foreach ($eventObj in $eventObjects) {
    if (-not $eventObj) {
        continue
    }
    if (($eventObj.PSObject.Properties.Name -contains "event") -and $eventObj.event -eq "finished" -and
        ($eventObj.PSObject.Properties.Name -contains "loop") -and $eventObj.loop) {
        $canonicalLoop = Resolve-LoopCanonicalName -Loop ([string]$eventObj.loop)
        $latestEventsByLoop[$canonicalLoop] = $eventObj

        if (($eventObj.PSObject.Properties.Name -contains "summary_path") -and $eventObj.summary_path) {
            $eventSummaryPath = Join-Path $RepoRoot ([string]$eventObj.summary_path)
            if (-not (Test-Path $eventSummaryPath)) {
                Add-ReportIssue -Bucket $issues -Severity "error" -Loop $canonicalLoop -Message "Finished event summary_path does not exist: $($eventObj.summary_path)"
            }
        }
    }
}

$latestFiles = @{}
if (-not (Test-Path $LatestDir)) {
    Add-ReportIssue -Bucket $issues -Severity "error" -Loop "shared_state" -Message "Missing latest-state directory: $LatestDir"
} else {
    foreach ($path in Get-ChildItem -Path $LatestDir -Filter *.json -File) {
        $loopName = [System.IO.Path]::GetFileNameWithoutExtension($path.Name)
        $latestFiles[$loopName] = $path.FullName
    }
}

foreach ($loop in $requiredLoops + $optionalLoops) {
    if (-not $latestFiles.ContainsKey($loop)) {
        $severity = if ($requiredLoops -contains $loop) { "error" } else { "warning" }
        Add-ReportIssue -Bucket $issues -Severity $severity -Loop $loop -Message "Missing latest-state file: $loop.json"
        continue
    }

    $latestPath = $latestFiles[$loop]
    try {
        $latestObj = Get-Content -Path $latestPath -Raw | ConvertFrom-Json
    } catch {
        Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state file is not valid JSON: $latestPath"
        continue
    }

    if (($latestObj.PSObject.Properties.Name -contains "loop") -and $latestObj.loop) {
        $recordedLoop = Resolve-LoopCanonicalName -Loop ([string]$latestObj.loop)
        if ($recordedLoop -ne $loop) {
            Add-ReportIssue -Bucket $issues -Severity "warning" -Loop $loop -Message "Latest-state loop field '$($latestObj.loop)' normalizes to '$recordedLoop' instead of '$loop'."
        }
    }

    $latestSummary = $null
    if (($latestObj.PSObject.Properties.Name -contains "summary_path") -and $latestObj.summary_path) {
        $latestSummary = [string]$latestObj.summary_path
        $latestSummaryPath = Join-Path $RepoRoot $latestSummary
        if (-not (Test-Path $latestSummaryPath)) {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state summary_path does not exist: $latestSummary"
        }
    }

    $latestRunId = Get-LatestRunIdValue -Obj $latestObj
    $eventObj = $latestEventsByLoop[$loop]
    if ($eventObj) {
        if (-not $latestRunId) {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state file is missing run_id/last_run_id while a finished event exists."
        } elseif ($latestRunId -ne [string]$eventObj.run_id) {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state run_id '$latestRunId' does not match latest finished event '$($eventObj.run_id)'."
        }

        if (-not $latestSummary) {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state summary_path is blank while a finished event exists."
        } elseif ($latestSummary -ne [string]$eventObj.summary_path) {
            Add-ReportIssue -Bucket $issues -Severity "error" -Loop $loop -Message "Latest-state summary_path '$latestSummary' does not match latest finished event '$($eventObj.summary_path)'."
        }
    } elseif ($latestRunId -or $latestSummary) {
        Add-ReportIssue -Bucket $issues -Severity "warning" -Loop $loop -Message "Latest-state file has run data but no matching finished event was found."
    }
}

$errorCount = @($issues | Where-Object { $_.severity -eq "error" }).Count
$warningCount = @($issues | Where-Object { $_.severity -eq "warning" }).Count

$reportLines = @(
    "Loop shared-state audit",
    "Errors: $errorCount",
    "Warnings: $warningCount"
)
foreach ($issue in $issues) {
    $reportLines += "- [$($issue.severity)] $($issue.loop): $($issue.message)"
}
if ($issues.Count -eq 0) {
    $reportLines += "No shared-state drift detected."
}

if ($ReportPath) {
    $reportDir = Split-Path -Parent $ReportPath
    if ($reportDir -and -not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }
    Set-Content -Path $ReportPath -Value ($reportLines -join [Environment]::NewLine) -Encoding UTF8
}

$stateDir = Split-Path -Parent $StatePath
if ($stateDir -and -not (Test-Path $stateDir)) {
    New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
}
$state = [ordered]@{
    last_run_ts = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    status = if ($errorCount -eq 0 -and $warningCount -eq 0) { "clean" } elseif ($errorCount -eq 0) { "warning" } else { "error" }
    error_count = $errorCount
    warning_count = $warningCount
    issues = $issues
}
$stateJson = $state | ConvertTo-Json -Depth 10
Set-Content -Path $StatePath -Value $stateJson -Encoding UTF8

foreach ($line in $reportLines) {
    Write-Host $line
}

if ($errorCount -gt 0) {
    exit 1
}

exit 0

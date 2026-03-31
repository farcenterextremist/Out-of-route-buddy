# Write Loop Shared State (dedupe-safe)
# Run from repo root:
# .\scripts\automation\write_loop_shared_state.ps1 -Loop token -RunId token-20260314-0010 -SummaryPath docs/automation/TOKEN_LOOP_RUN_LEDGER.md -NextSteps @("step1","step2")

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
    [string]$EventsFile = "",
    [string]$LatestFile = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "loop_run_contract.ps1")

function Test-HasProperty($Obj, [string]$Name) {
    return $Obj -and ($Obj.PSObject.Properties.Name -contains $Name)
}

$RepoRoot = Get-LoopAutomationRepoRoot -ScriptRoot $PSScriptRoot
$CanonicalLoop = Resolve-LoopCanonicalName -Loop $Loop

if (-not $EventsFile) {
    $EventsFile = Join-Path $RepoRoot "docs\automation\loop_shared_events.jsonl"
}

if (-not $LatestFile) {
    $LatestFile = Get-LoopLatestStatePath -RepoRoot $RepoRoot -Loop $CanonicalLoop
}

$eventsDir = Split-Path -Parent $EventsFile
$latestDir = Split-Path -Parent $LatestFile
if (-not (Test-Path $eventsDir)) { New-Item -ItemType Directory -Path $eventsDir -Force | Out-Null }
if (-not (Test-Path $latestDir)) { New-Item -ItemType Directory -Path $latestDir -Force | Out-Null }

$ts = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")

# 1) Dedupe-safe finished event append (loop|run_id)
$duplicate = $false
if (Test-Path $EventsFile) {
    $lines = Get-Content $EventsFile | Where-Object { $_.Trim() -ne "" }
    foreach ($line in $lines) {
        try {
            $obj = $line | ConvertFrom-Json
            if ((Test-HasProperty $obj "event") -and $obj.event -eq "finished" -and
                (Test-HasProperty $obj "loop") -and (Test-HasProperty $obj "run_id") -and
                (Resolve-LoopCanonicalName -Loop $obj.loop) -eq $CanonicalLoop -and $obj.run_id -eq $RunId) {
                $duplicate = $true
                break
            }
        } catch {
            # Ignore malformed historical lines; schema suite covers detection.
        }
    }
}

if ($duplicate) {
    Write-Host "SKIP: Duplicate finished event blocked for key '$CanonicalLoop|$RunId'"
} else {
    $eventObj = [PSCustomObject]@{
        ts           = $ts
        loop         = $CanonicalLoop
        event        = "finished"
        run_id       = $RunId
        summary_path = $SummaryPath
        next_steps   = @($NextSteps)
    }
    if ($Checkpoint) {
        $eventObj | Add-Member -NotePropertyName "checkpoint" -NotePropertyValue $Checkpoint -Force
    }
    $jsonLine = $eventObj | ConvertTo-Json -Compress
    Add-Content -Path $EventsFile -Value $jsonLine -NoNewline
    Add-Content -Path $EventsFile -Value ""
    Write-Host "OK: Appended finished event for '$CanonicalLoop|$RunId'"
}

# 2) Update latest file (always)
$latestObj = [ordered]@{}
if (Test-Path $LatestFile) {
    try {
        $existing = Get-Content $LatestFile -Raw | ConvertFrom-Json
        foreach ($prop in $existing.PSObject.Properties) {
            $latestObj[$prop.Name] = $prop.Value
        }
    } catch {
        # If existing latest file is malformed, replace with fresh object.
        $latestObj = [ordered]@{}
    }
}

$latestObj["loop"] = $CanonicalLoop
$latestObj["last_run_ts"] = $ts
$latestObj["last_run_id"] = $RunId
$latestObj["run_id"] = $RunId
$latestObj["summary_path"] = $SummaryPath
$latestObj["suggested_next_steps"] = @($NextSteps)
if ($Checkpoint) {
    $latestObj["checkpoint"] = $Checkpoint
}

$latestJson = $latestObj | ConvertTo-Json -Depth 10
Set-Content -Path $LatestFile -Value $latestJson -Encoding UTF8
Write-Host "OK: Updated latest state file -> $LatestFile"

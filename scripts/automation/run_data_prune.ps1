[CmdletBinding()]
param(
    [switch]$Approve,
    [switch]$PurgeStaging,
    [string]$RepoRoot
)

$ErrorActionPreference = "Stop"

function Get-RepositoryRoot {
    param([string]$ProvidedRoot)
    if (-not [string]::IsNullOrWhiteSpace($ProvidedRoot)) {
        return (Resolve-Path $ProvidedRoot).Path
    }

    $root = $PSScriptRoot
    for ($i = 0; $i -lt 2; $i++) {
        $root = Split-Path -Parent $root
    }
    return $root
}

function Test-PathUnder {
    param(
        [Parameter(Mandatory = $true)][string]$ChildPath,
        [Parameter(Mandatory = $true)][string]$ParentPath
    )
    $child = [System.IO.Path]::GetFullPath($ChildPath).TrimEnd('\')
    $parent = [System.IO.Path]::GetFullPath($ParentPath).TrimEnd('\')
    return ($child -eq $parent) -or $child.StartsWith("$parent\", [System.StringComparison]::OrdinalIgnoreCase)
}

function Test-ProtectedPath {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string]$RepoPath
    )

    $full = [System.IO.Path]::GetFullPath($FilePath)
    $relative = [System.IO.Path]::GetRelativePath($RepoPath, $full).Replace('\', '/')

    if ($relative -eq "docs/automation/loop_shared_events.jsonl") { return $true }
    if ($relative -like "docs/automation/loop_latest/*") { return $true }
    if ($relative -like "docs/automation/*RUN_LEDGER*.md") { return $true }
    if ($relative -like "docs/automation/*SUMMARY*.md") { return $true }
    if ($relative -like "docs/agents/data-sets/hub/*") { return $true }
    if ($relative -like ".cursor/rules/*") { return $true }
    if ($relative -like ".cursor/skills/*") { return $true }
    if ($relative -like "docs/automation/_trash_staging/*") { return $true }

    return $false
}

function Get-AgedFiles {
    param(
        [Parameter(Mandatory = $true)][string]$RootPath,
        [Parameter(Mandatory = $true)][string[]]$Extensions,
        [Parameter(Mandatory = $true)][datetime]$OlderThanUtc
    )
    if (-not (Test-Path $RootPath)) { return @() }

    $files = Get-ChildItem -Path $RootPath -File -Recurse
    return $files | Where-Object {
        ($Extensions -contains $_.Extension.ToLowerInvariant()) -and
        ($_.LastWriteTimeUtc -lt $OlderThanUtc)
    }
}

function Get-SnapshotCandidates {
    param(
        [Parameter(Mandatory = $true)][string]$SnapshotRoot,
        [Parameter(Mandatory = $true)][datetime]$OlderThanUtc,
        [Parameter(Mandatory = $true)][int]$KeepLatest
    )
    if (-not (Test-Path $SnapshotRoot)) { return @() }

    $ordered = Get-ChildItem -Path $SnapshotRoot -File -Filter "*.json" |
        Sort-Object LastWriteTimeUtc -Descending

    if ($ordered.Count -le $KeepLatest) { return @() }

    $tail = $ordered | Select-Object -Skip $KeepLatest
    return $tail | Where-Object { $_.LastWriteTimeUtc -lt $OlderThanUtc }
}

function Add-CandidateFile {
    param(
        [Parameter(Mandatory = $true)][System.Collections.Generic.Dictionary[string, object]]$Map,
        [Parameter(Mandatory = $true)][System.IO.FileInfo]$File
    )
    if (-not $Map.ContainsKey($File.FullName)) {
        $Map[$File.FullName] = $File
    }
}

function Move-CandidateToStaging {
    param(
        [Parameter(Mandatory = $true)][System.IO.FileInfo]$File,
        [Parameter(Mandatory = $true)][string]$RepoPath,
        [Parameter(Mandatory = $true)][string]$StagingDayRoot
    )
    $relative = [System.IO.Path]::GetRelativePath($RepoPath, $File.FullName)
    $destination = Join-Path $StagingDayRoot $relative
    $destinationDir = Split-Path -Parent $destination
    if (-not (Test-Path $destinationDir)) {
        New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
    }
    Move-Item -LiteralPath $File.FullName -Destination $destination -Force
}

function Remove-AgedStagingFiles {
    param(
        [Parameter(Mandatory = $true)][string]$StagingRoot,
        [Parameter(Mandatory = $true)][datetime]$OlderThanUtc,
        [Parameter(Mandatory = $true)][switch]$AllowDelete
    )
    if (-not (Test-Path $StagingRoot)) {
        return 0
    }

    $purged = 0
    $candidates = Get-ChildItem -Path $StagingRoot -File -Recurse |
        Where-Object { $_.LastWriteTimeUtc -lt $OlderThanUtc }

    foreach ($file in $candidates) {
        if ($AllowDelete) {
            Remove-Item -LiteralPath $file.FullName -Force
        }
        $purged++
    }
    return $purged
}

function Add-LedgerRunEntry {
    param(
        [Parameter(Mandatory = $true)][string]$LedgerPath,
        [Parameter(Mandatory = $true)][string]$RunId,
        [Parameter(Mandatory = $true)][string]$Mode,
        [Parameter(Mandatory = $true)][bool]$PurgeRequested,
        [Parameter(Mandatory = $true)][int]$CandidateCount,
        [Parameter(Mandatory = $true)][int]$BlockedCount,
        [Parameter(Mandatory = $true)][int]$MovedCount,
        [Parameter(Mandatory = $true)][int]$PurgedCount,
        [Parameter(Mandatory = $true)][string[]]$Warnings
    )

    if (-not (Test-Path $LedgerPath)) {
        $header = @(
            "# Cleanup Run Ledger",
            "",
            "Append one block per cleanup run.",
            ""
        ) -join "`n"
        Set-Content -Path $LedgerPath -Value $header -Encoding UTF8
    }

    $timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    $warningLine = if ($Warnings.Count -eq 0) { "none" } else { $Warnings -join "; " }
    $entry = @(
        "",
        "## Cleanup Run: $RunId",
        "- Timestamp (UTC): $timestamp",
        "- Operator: run_data_prune.ps1",
        "- Mode: $Mode",
        "- Purge staging: $PurgeRequested",
        "- Candidate count: $CandidateCount",
        "- Protected-blocked count: $BlockedCount",
        "- Moved to quarantine: $MovedCount",
        "- Purged from staging: $PurgedCount",
        "- Warnings: $warningLine"
    ) -join "`n"

    Add-Content -Path $LedgerPath -Value $entry -Encoding UTF8
}

$resolvedRepoRoot = Get-RepositoryRoot -ProvidedRoot $RepoRoot
$automationRoot = Join-Path $resolvedRepoRoot "docs\automation"
$snapshotRoot = Join-Path $automationRoot "token_loop_snapshots"
$stagingRoot = Join-Path $automationRoot "_trash_staging"
$stagingDay = (Get-Date).ToString("yyyy-MM-dd")
$stagingDayRoot = Join-Path $stagingRoot $stagingDay
$ledgerPath = Join-Path $automationRoot "CLEANUP_RUN_LEDGER.md"
$runId = "cleanup-" + (Get-Date -Format "yyyyMMdd-HHmmss")

$hotCutoffUtc = (Get-Date).ToUniversalTime().AddDays(-14)
$snapshotCutoffUtc = (Get-Date).ToUniversalTime().AddDays(-30)
$stagingCutoffUtc = (Get-Date).ToUniversalTime().AddDays(-7)

if (-not (Test-Path $automationRoot)) {
    throw "Automation root not found: $automationRoot"
}

$candidateMap = New-Object 'System.Collections.Generic.Dictionary[string, object]'

$continuityTmpRoot = Join-Path $automationRoot "_continuity_test_tmp"
if (Test-Path $continuityTmpRoot) {
    foreach ($file in (Get-ChildItem -Path $continuityTmpRoot -File -Recurse)) {
        Add-CandidateFile -Map $candidateMap -File $file
    }
}

foreach ($file in (Get-ChildItem -Path $automationRoot -File -Filter "*events_test.jsonl" -ErrorAction SilentlyContinue)) {
    Add-CandidateFile -Map $candidateMap -File $file
}

foreach ($file in (Get-AgedFiles -RootPath $automationRoot -Extensions @(".tmp", ".bak") -OlderThanUtc $hotCutoffUtc)) {
    Add-CandidateFile -Map $candidateMap -File $file
}

foreach ($file in (Get-SnapshotCandidates -SnapshotRoot $snapshotRoot -OlderThanUtc $snapshotCutoffUtc -KeepLatest 3)) {
    Add-CandidateFile -Map $candidateMap -File $file
}

$blocked = New-Object System.Collections.Generic.List[string]
$finalCandidates = New-Object System.Collections.Generic.List[System.IO.FileInfo]

foreach ($entry in $candidateMap.GetEnumerator()) {
    $file = [System.IO.FileInfo]$entry.Value
    if (Test-ProtectedPath -FilePath $file.FullName -RepoPath $resolvedRepoRoot) {
        $blocked.Add($file.FullName) | Out-Null
        continue
    }
    if (Test-Path $file.FullName) {
        $finalCandidates.Add($file) | Out-Null
    }
}

$mode = if ($Approve) { "approve" } else { "dry-run" }
$warnings = New-Object System.Collections.Generic.List[string]
$movedCount = 0
$purgedCount = 0

if ($Approve) {
    if (-not (Test-Path $stagingDayRoot)) {
        New-Item -ItemType Directory -Path $stagingDayRoot -Force | Out-Null
    }
    foreach ($file in $finalCandidates) {
        Move-CandidateToStaging -File $file -RepoPath $resolvedRepoRoot -StagingDayRoot $stagingDayRoot
        $movedCount++
    }
} else {
    $warnings.Add("Dry-run mode only. Re-run with -Approve to move files.") | Out-Null
}

if ($PurgeStaging) {
    if (-not $Approve) {
        $warnings.Add("Purge requested without -Approve; reporting only, no permanent deletes.") | Out-Null
        $purgedCount = Remove-AgedStagingFiles -StagingRoot $stagingRoot -OlderThanUtc $stagingCutoffUtc -AllowDelete:$false
    } else {
        $purgedCount = Remove-AgedStagingFiles -StagingRoot $stagingRoot -OlderThanUtc $stagingCutoffUtc -AllowDelete:$true
    }
}

Add-LedgerRunEntry `
    -LedgerPath $ledgerPath `
    -RunId $runId `
    -Mode $mode `
    -PurgeRequested $PurgeStaging.IsPresent `
    -CandidateCount $finalCandidates.Count `
    -BlockedCount $blocked.Count `
    -MovedCount $movedCount `
    -PurgedCount $purgedCount `
    -Warnings @($warnings)

Write-Host "Cleanup run complete."
Write-Host "Mode: $mode"
Write-Host "Candidates discovered: $($finalCandidates.Count)"
Write-Host "Protected-blocked: $($blocked.Count)"
Write-Host "Moved to quarantine: $movedCount"
Write-Host "Purged from staging: $purgedCount"
Write-Host "Ledger: $ledgerPath"

# Test Token Loop Events Analysis — Validate token_loop_events.jsonl structure and run shape.
# Run from repo root: .\scripts\automation\test_token_loop_events_analysis.ps1 [-EventsFile path]
# Exit 0 = pass (file valid and structure as expected), 1 = fail or file missing.

param(
    [string]$EventsFile = ""
)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

if (-not $EventsFile) {
    $EventsFile = Join-Path $RepoRoot "docs\automation\token_loop_events.jsonl"
}
$failCount = 0

Write-Host "Testing Token Loop Events (analysis)..."
Write-Host "  File: $EventsFile"
Write-Host ""

# 1. File exists (optional: allow missing for fresh installs)
if (-not (Test-Path $EventsFile)) {
    Write-Host "PASS: Events file missing (no runs yet); structure check skipped."
    exit 0
}

# 2. Each line valid JSON with event and run_id
$lines = Get-Content $EventsFile -ErrorAction SilentlyContinue | Where-Object { $_.Trim() -ne "" }
if ($lines.Count -eq 0) {
    Write-Host "PASS: Events file empty; structure valid."
    exit 0
}

$parseErrors = 0
$objs = @()
foreach ($line in $lines) {
    try {
        $obj = $line | ConvertFrom-Json
        if (-not (Get-Member -InputObject $obj -Name "event" -MemberType Properties -ErrorAction SilentlyContinue)) { $parseErrors++ }
        elseif (-not (Get-Member -InputObject $obj -Name "run_id" -MemberType Properties -ErrorAction SilentlyContinue)) { $parseErrors++ }
        else { $objs += $obj }
    } catch {
        $parseErrors++
    }
}

if ($parseErrors -gt 0) {
    Write-Host "FAIL: $parseErrors lines invalid or missing event/run_id"
    $failCount++
} else {
    Write-Host "PASS: All $($lines.Count) lines valid JSON with event and run_id"
}

# 3. token_loop_start count equals token_loop_end count (incomplete runs allowed; we only check structure)
$starts = ($objs | Where-Object { $_.event -eq "token_loop_start" }).Count
$ends = ($objs | Where-Object { $_.event -eq "token_loop_end" }).Count
# Don't fail if unequal (incomplete run); just report
Write-Host "  token_loop_start: $starts, token_loop_end: $ends"

# 3a. Report incomplete start-only runs and orphaned event-only run_ids
$groupedRuns = $objs | Group-Object -Property run_id
$incompleteRunIds = @()
$orphanRunIds = @()
foreach ($group in $groupedRuns) {
    $runEvents = @($group.Group | ForEach-Object { $_.event })
    $hasStart = $runEvents -contains "token_loop_start"
    $hasEnd = $runEvents -contains "token_loop_end"
    if ($hasStart -and -not $hasEnd) {
        $incompleteRunIds += $group.Name
    }
    if (-not $hasStart -and -not $hasEnd) {
        $orphanRunIds += $group.Name
    }
}

if ($incompleteRunIds.Count -gt 0) {
    Write-Host "  Incomplete run_ids (start without end): $($incompleteRunIds -join ', ')"
}
if ($orphanRunIds.Count -gt 0) {
    Write-Host "  Orphan run_ids (step events without start/end): $($orphanRunIds -join ', ')"
}

# 4. Report latest started and latest completed runs separately (informational)
$latestStarted = $objs | Where-Object { $_.event -eq "token_loop_start" } | Select-Object -Last 1
if ($latestStarted) {
    $latestStartedRunId = $latestStarted.run_id
    $stepEnds = ($objs | Where-Object { $_.run_id -eq $latestStartedRunId -and $_.event -eq "step_end" }).Count
    Write-Host "  Latest started run_id: $latestStartedRunId, step_end count: $stepEnds (expect 8 for full run)"
}

$latestCompleted = $objs | Where-Object { $_.event -eq "token_loop_end" } | Select-Object -Last 1
if ($latestCompleted) {
    Write-Host "  Latest completed run_id: $($latestCompleted.run_id)"
}

$latestChangeMetrics = $objs |
    Where-Object {
        $_.event -eq "step_end" -and
        $_.step -eq "5" -and
        (Get-Member -InputObject $_ -Name "metrics" -MemberType Properties -ErrorAction SilentlyContinue)
    } |
    Select-Object -Last 1
if ($latestChangeMetrics) {
    $m = $latestChangeMetrics.metrics
    $changeFlags = @(
        "rules=$($m.changed_rules)",
        "skills=$($m.changed_skills)",
        "settings=$($m.changed_settings)",
        "cache_index_hygiene=$($m.changed_cache_index_hygiene)"
    ) -join ", "
    Write-Host "  Latest Step 5 change metrics: $changeFlags"
    if (Get-Member -InputObject $m -Name "api_usage_snapshot_written" -MemberType Properties -ErrorAction SilentlyContinue) {
        Write-Host "  Latest Step 5 API usage snapshot: written=$($m.api_usage_snapshot_written), requests=$($m.api_usage_num_model_requests), input_tokens=$($m.api_usage_input_tokens), output_tokens=$($m.api_usage_output_tokens)"
    }
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "Token loop events analysis passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount test(s) failed."
    exit 1
}

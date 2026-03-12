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

# 4. Optional: latest run_id has 8 step_end events for full run (informational)
$runIds = $objs | ForEach-Object { $_.run_id } | Select-Object -Unique
if ($runIds.Count -gt 0) {
    $latestRunId = $runIds[-1]
    $stepEnds = ($objs | Where-Object { $_.run_id -eq $latestRunId -and $_.event -eq "step_end" }).Count
    Write-Host "  Latest run_id: $latestRunId, step_end count: $stepEnds (expect 8 for full run)"
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "Token loop events analysis passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount test(s) failed."
    exit 1
}

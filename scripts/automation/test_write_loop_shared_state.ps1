# Test write_loop_shared_state.ps1 dedupe behavior.
# Run from repo root: .\scripts\automation\test_write_loop_shared_state.ps1

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$failCount = 0

function Fail([string]$Message) {
    Write-Host "FAIL: $Message"
    $script:failCount++
}

function Pass([string]$Message) {
    Write-Host "PASS: $Message"
}

$Writer = Join-Path $RepoRoot "scripts\automation\write_loop_shared_state.ps1"
if (-not (Test-Path $Writer)) {
    Write-Host "FAIL: write_loop_shared_state.ps1 not found"
    exit 1
}

$TmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$EventsFile = Join-Path $TmpDir "loop_shared_events.test.jsonl"
$LatestFile = Join-Path $TmpDir "token.latest.test.json"
if (-not (Test-Path $TmpDir)) { New-Item -ItemType Directory -Path $TmpDir -Force | Out-Null }
Remove-Item $EventsFile -Force -ErrorAction SilentlyContinue
Remove-Item $LatestFile -Force -ErrorAction SilentlyContinue

$runId = "token-test-dedupe"
$summaryPath = "docs/automation/TOKEN_LOOP_RUN_LEDGER.md"
$steps = @("a", "b")

Write-Host "Testing write_loop_shared_state dedupe behavior..."
Write-Host ""

# First write should append one event.
& $Writer -Loop "token" -RunId $runId -SummaryPath $summaryPath -NextSteps $steps -EventsFile $EventsFile -LatestFile $LatestFile

# Duplicate write with same loop|run_id should not append second event.
& $Writer -Loop "token" -RunId $runId -SummaryPath $summaryPath -NextSteps $steps -EventsFile $EventsFile -LatestFile $LatestFile

if (-not (Test-Path $EventsFile)) {
    Fail "Events test file not created"
} else {
    $lines = Get-Content $EventsFile | Where-Object { $_.Trim() -ne "" }
    if ($lines.Count -ne 1) {
        Fail "Expected exactly 1 event line after duplicate write, got $($lines.Count)"
    } else {
        Pass "Duplicate event append blocked (line count = 1)"
    }
}

if (-not (Test-Path $LatestFile)) {
    Fail "Latest test file not created"
} else {
    try {
        $obj = Get-Content $LatestFile -Raw | ConvertFrom-Json
        if (-not $obj.run_id -or $obj.run_id -ne $runId) {
            Fail "Latest file run_id mismatch"
        } else {
            Pass "Latest file run_id updated"
        }
        if (-not $obj.summary_path -or $obj.summary_path -ne $summaryPath) {
            Fail "Latest file summary_path mismatch"
        } else {
            Pass "Latest file summary_path updated"
        }
    } catch {
        Fail "Latest file is not valid JSON"
    }
}

Remove-Item $EventsFile -Force -ErrorAction SilentlyContinue
Remove-Item $LatestFile -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All shared-state writer tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount writer test(s) failed."
    exit 1
}

# Test Shared State Contract
# Run from repo root: .\scripts\automation\test_shared_state_contract.ps1

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

Write-Host "Testing shared-state contract..."
Write-Host ""

$eventsPath = Join-Path $RepoRoot "docs\automation\loop_shared_events.jsonl"
if (-not (Test-Path $eventsPath)) {
    Fail "Missing events file: $eventsPath"
} else {
    Pass "Found events file"
    $lines = Get-Content $eventsPath | Where-Object { $_.Trim() -ne "" }
    if ($lines.Count -eq 0) {
        Fail "Events file is empty"
    } else {
        $lastLine = $lines[-1]
        try {
            $null = $lastLine | ConvertFrom-Json
            Pass "Latest event line is valid JSON"
        } catch {
            Fail "Latest event line is not valid JSON"
        }
    }
}

$latestDir = Join-Path $RepoRoot "docs\automation\loop_latest"
$requiredLoops = @("improvement", "token", "cyber", "synthetic_data")

if (-not (Test-Path $latestDir)) {
    Fail "Missing loop_latest directory: $latestDir"
} else {
    Pass "Found loop_latest directory"
    foreach ($loop in $requiredLoops) {
        $latestPath = Join-Path $latestDir "$loop.json"
        if (-not (Test-Path $latestPath)) {
            Fail "Missing latest state file: $latestPath"
            continue
        }

        try {
            $obj = Get-Content $latestPath -Raw | ConvertFrom-Json
            Pass "$loop latest state parses as JSON"

            if (-not ($obj.PSObject.Properties.Name -contains "summary_path")) {
                Fail "$loop missing 'summary_path'"
            } else {
                Pass "$loop has summary_path"
            }

            if (-not ($obj.PSObject.Properties.Name -contains "last_run_ts")) {
                Fail "$loop missing 'last_run_ts'"
            } else {
                Pass "$loop has last_run_ts"
            }

            if (-not ($obj.PSObject.Properties.Name -contains "suggested_next_steps")) {
                Fail "$loop missing 'suggested_next_steps'"
            } else {
                Pass "$loop has suggested_next_steps"
            }
        } catch {
            Fail "$loop latest state is not valid JSON"
        }
    }
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All shared-state contract tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount shared-state check(s) failed."
    exit 1
}

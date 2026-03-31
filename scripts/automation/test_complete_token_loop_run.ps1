# Test token loop completion helper with isolated temp files.
# Run from repo root: .\scripts\automation\test_complete_token_loop_run.ps1

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

$helperScript = Join-Path $RepoRoot "scripts\automation\complete_token_loop_run.ps1"
$tmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$summaryRelativePath = "docs/automation/_continuity_test_tmp/token-helper-summary.md"
$summaryFile = Join-Path $RepoRoot $summaryRelativePath
$sharedEventsFile = Join-Path $tmpDir "token.helper.shared_events.jsonl"
$latestFile = Join-Path $tmpDir "token.helper.latest.json"
$contextFile = Join-Path $tmpDir "token.helper.context.json"
$listenerEventsFile = Join-Path $tmpDir "token.helper.listener.jsonl"
$failureListenerEventsFile = Join-Path $tmpDir "token.helper.failure.listener.jsonl"

if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
}

Remove-Item $summaryFile, $sharedEventsFile, $latestFile, $contextFile, $listenerEventsFile, $failureListenerEventsFile -Force -ErrorAction SilentlyContinue
Set-Content -Path $summaryFile -Value "# token helper summary`n" -Encoding UTF8

Write-Host "Testing token loop completion helper..."
Write-Host ""

if (-not (Test-Path $helperScript)) {
    Fail "complete_token_loop_run.ps1 not found"
}

$runId = "token-helper-test"
if ($failCount -eq 0) {
    $result = & $helperScript `
        -RunId $runId `
        -SummaryPath $summaryRelativePath `
        -NextSteps @("next token a", "next token b") `
        -ContextFile $contextFile `
        -EventsFile $sharedEventsFile `
        -LatestFile $latestFile `
        -ListenerOutFile $listenerEventsFile `
        -Metrics '{"steps_completed":8}'
}

if (-not $result) {
    Fail "Helper did not return a result object"
} elseif (-not $result.shared_state_updated -or -not $result.token_loop_end_recorded) {
    Fail "Helper result missing shared-state or listener confirmation"
} else {
    Pass "Helper confirmed shared-state update and token_loop_end recording"
}

if (-not (Test-Path $sharedEventsFile)) {
    Fail "Helper did not write shared-state events"
} else {
    $sharedEvent = (Get-Content -Path $sharedEventsFile | Where-Object { $_.Trim() -ne "" } | Select-Object -First 1) | ConvertFrom-Json
    if ($sharedEvent.loop -ne "token" -or $sharedEvent.run_id -ne $runId) {
        Fail "Shared-state event mismatch"
    } else {
        Pass "Shared-state event written for token run"
    }
}

if (-not (Test-Path $latestFile)) {
    Fail "Helper did not refresh latest-state file"
} else {
    $latest = Get-Content -Path $latestFile -Raw | ConvertFrom-Json
    if ($latest.run_id -ne $runId -or $latest.last_run_id -ne $runId) {
        Fail "Latest-state identifiers mismatch"
    } else {
        Pass "Latest-state identifiers updated"
    }
    if ($latest.summary_path -ne $summaryRelativePath) {
        Fail "Latest-state summary_path mismatch"
    } else {
        Pass "Latest-state summary_path updated"
    }
}

if (-not (Test-Path $listenerEventsFile)) {
    Fail "Helper did not emit token_loop_end listener event"
} else {
    $listenerEvent = (Get-Content -Path $listenerEventsFile | Where-Object { $_.Trim() -ne "" } | Select-Object -Last 1) | ConvertFrom-Json
    if ($listenerEvent.event -ne "token_loop_end" -or $listenerEvent.run_id -ne $runId) {
        Fail "Listener event mismatch"
    } else {
        Pass "token_loop_end emitted after closeout"
    }
}

$failureCaught = $false
try {
    & $helperScript `
        -RunId "token-helper-failure" `
        -SummaryPath "docs/automation/_continuity_test_tmp/missing-token-helper-summary.md" `
        -ListenerOutFile $failureListenerEventsFile | Out-Null
} catch {
    $failureCaught = $true
}

if (-not $failureCaught) {
    Fail "Helper should fail when summary path is missing"
} else {
    Pass "Helper fails before declaring token loop complete when summary is missing"
}

if (Test-Path $failureListenerEventsFile) {
    $failureLines = Get-Content -Path $failureListenerEventsFile | Where-Object { $_.Trim() -ne "" }
    if ($failureLines.Count -gt 0) {
        Fail "Failure path should not emit token_loop_end before finish wrapper succeeds"
    } else {
        Pass "Failure path did not emit token_loop_end"
    }
} else {
    Pass "Failure path emitted no listener file"
}

Remove-Item $summaryFile, $sharedEventsFile, $latestFile, $contextFile, $listenerEventsFile, $failureListenerEventsFile -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token loop completion helper tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount token loop completion helper test(s) failed."
    exit 1
}

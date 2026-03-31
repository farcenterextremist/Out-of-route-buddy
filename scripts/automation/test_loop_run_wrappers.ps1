# Test loop start/finish wrappers with isolated temp files.
# Run from repo root: .\scripts\automation\test_loop_run_wrappers.ps1

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

$startScript = Join-Path $RepoRoot "scripts\automation\start_loop_run.ps1"
$finishScript = Join-Path $RepoRoot "scripts\automation\finish_loop_run.ps1"
$tmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$loopEventsFile = Join-Path $tmpDir "loop_wrapper_events.test.jsonl"
$tokenEventsFile = Join-Path $tmpDir "token_wrapper_events.test.jsonl"
$sharedEventsFile = Join-Path $tmpDir "loop_shared_events.wrapper.test.jsonl"
$latestFile = Join-Path $tmpDir "improvement.wrapper.latest.json"
$contextFile = Join-Path $tmpDir "improvement.wrapper.context.json"
$summaryRelativePath = "docs/automation/_continuity_test_tmp/wrapper-summary.md"
$summaryFile = Join-Path $RepoRoot $summaryRelativePath
$efficiencyMarker = "<!-- LOOP_EFFICIENCY_BLOCK_START:run-wrapper-test -->"

if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
}
Remove-Item $loopEventsFile, $tokenEventsFile, $sharedEventsFile, $latestFile, $contextFile, $summaryFile -Force -ErrorAction SilentlyContinue

Set-Content -Path $summaryFile -Value "# wrapper summary`n" -Encoding UTF8

Write-Host "Testing loop run wrappers..."
Write-Host ""

if (-not (Test-Path $startScript)) {
    Fail "start_loop_run.ps1 not found"
}

if (-not (Test-Path $finishScript)) {
    Fail "finish_loop_run.ps1 not found"
}

$runId = "run-wrapper-test"
$tokenRunId = "token-wrapper-test"

if ($failCount -eq 0) {
    & $startScript -Loop "improvement" -RunId $runId -EmitStartEvent -Note "wrapper test" -ContextFile $contextFile -ListenerOutFile $loopEventsFile | Out-Null
    & $startScript -Loop "token" -RunId $tokenRunId -EmitStartEvent -Note "token wrapper test" -ContextFile (Join-Path $tmpDir "token.wrapper.context.json") -ListenerOutFile $tokenEventsFile | Out-Null
    & $finishScript -Loop "improvement" -RunId $runId -SummaryPath $summaryRelativePath -NextSteps @("next a", "next b") -ContextFile $contextFile -EventsFile $sharedEventsFile -LatestFile $latestFile | Out-Null
}

if (-not (Test-Path $contextFile)) {
    Fail "Improvement wrapper context file was not created"
} else {
    $context = Get-Content -Path $contextFile -Raw | ConvertFrom-Json
    if ($context.run_id -ne $runId) {
        Fail "Context file run_id mismatch"
    } else {
        Pass "Context file run_id recorded"
    }
    if (-not $context.finished_at) {
        Fail "Context file missing finished_at after finish wrapper"
    } else {
        Pass "Context file updated on finish"
    }
}

if (-not (Test-Path $loopEventsFile)) {
    Fail "Improvement start listener output not created"
} else {
    $improvementStart = (Get-Content -Path $loopEventsFile | Where-Object { $_.Trim() -ne "" } | Select-Object -First 1) | ConvertFrom-Json
    if ($improvementStart.event -ne "loop_start" -or $improvementStart.run_id -ne $runId) {
        Fail "Improvement wrapper start event mismatch"
    } else {
        Pass "Improvement wrapper emitted loop_start with stable run_id"
    }
}

if (-not (Test-Path $tokenEventsFile)) {
    Fail "Token start listener output not created"
} else {
    $tokenStart = (Get-Content -Path $tokenEventsFile | Where-Object { $_.Trim() -ne "" } | Select-Object -First 1) | ConvertFrom-Json
    if ($tokenStart.event -ne "token_loop_start" -or $tokenStart.run_id -ne $tokenRunId) {
        Fail "Token wrapper start event mismatch"
    } else {
        Pass "Token wrapper emitted token_loop_start with stable run_id"
    }
}

if (-not (Test-Path $sharedEventsFile)) {
    Fail "Finish wrapper did not write shared-state event"
} else {
    $event = (Get-Content -Path $sharedEventsFile | Where-Object { $_.Trim() -ne "" } | Select-Object -First 1) | ConvertFrom-Json
    if ($event.loop -ne "improvement" -or $event.run_id -ne $runId) {
        Fail "Finish wrapper shared-state event mismatch"
    } else {
        Pass "Finish wrapper wrote shared-state event"
    }
}

if (-not (Test-Path $latestFile)) {
    Fail "Finish wrapper did not write latest-state file"
} else {
    $latest = Get-Content -Path $latestFile -Raw | ConvertFrom-Json
    if ($latest.run_id -ne $runId -or $latest.last_run_id -ne $runId) {
        Fail "Latest-state file missing run_id/last_run_id update"
    } else {
        Pass "Finish wrapper refreshed latest-state identifiers"
    }
    if ($latest.summary_path -ne $summaryRelativePath) {
        Fail "Latest-state summary_path mismatch"
    } else {
        Pass "Finish wrapper stored summary_path"
    }
}

if (-not (Test-Path $summaryFile)) {
    Fail "Summary file missing after finish wrapper"
} else {
    $summaryContent = Get-Content -Path $summaryFile -Raw
    if ($summaryContent -notmatch [Regex]::Escape("## Loop Efficiency Score")) {
        Fail "Finish wrapper did not persist loop efficiency block into summary"
    } else {
        Pass "Finish wrapper persisted loop efficiency block into summary"
    }
    $markerCount = ([regex]::Matches($summaryContent, [Regex]::Escape($efficiencyMarker))).Count
    if ($markerCount -ne 1) {
        Fail "Expected exactly one efficiency block marker after first write, got $markerCount"
    } else {
        Pass "Efficiency block marker present once after first write"
    }
}

& $finishScript -Loop "improvement" -RunId $runId -SummaryPath $summaryRelativePath -NextSteps @("next a", "next b") -ContextFile $contextFile -EventsFile $sharedEventsFile -LatestFile $latestFile | Out-Null

if (Test-Path $summaryFile) {
    $updatedSummary = Get-Content -Path $summaryFile -Raw
    $markerCount = ([regex]::Matches($updatedSummary, [Regex]::Escape($efficiencyMarker))).Count
    if ($markerCount -ne 1) {
        Fail "Efficiency block duplicated on rerun (marker count $markerCount)"
    } else {
        Pass "Efficiency block is idempotent on rerun"
    }
} else {
    Fail "Summary file missing after rerun"
}

Remove-Item $loopEventsFile, $tokenEventsFile, $sharedEventsFile, $latestFile, $contextFile, $summaryFile, (Join-Path $tmpDir "token.wrapper.context.json") -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All loop wrapper tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount loop wrapper test(s) failed."
    exit 1
}

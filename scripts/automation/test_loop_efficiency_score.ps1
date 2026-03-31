# Test loop efficiency scoring and progress-bar output.
# Run from repo root: .\scripts\automation\test_loop_efficiency_score.ps1

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

$scriptPath = Join-Path $RepoRoot "scripts\automation\measure_loop_efficiency.ps1"
$statePath = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp\loop_efficiency_state.test.json"

if (-not (Test-Path $scriptPath)) {
    Fail "measure_loop_efficiency.ps1 not found"
} else {
    $allPass = & $scriptPath `
        -RunContractStatus pass `
        -GateWrappersStatus pass `
        -SharedStateAuditStatus pass `
        -ContinuityStatus pass `
        -HealthSignalsStatus pass `
        -DocumentationStatus pass `
        -StatePath $statePath `
        -PassThru `
        -Quiet

    if ($allPass.score -ne 100) {
        Fail "Expected perfect score 100, got $($allPass.score)"
    } else {
        Pass "Perfect score returns 100/100"
    }

    if ($allPass.progress_bar -ne "[####################] 100%") {
        Fail "Unexpected perfect progress bar: $($allPass.progress_bar)"
    } else {
        Pass "Perfect progress bar rendered"
    }

    $mixed = & $scriptPath `
        -RunContractStatus pass `
        -GateWrappersStatus fail `
        -SharedStateAuditStatus pass `
        -ContinuityStatus fail `
        -HealthSignalsStatus pass `
        -DocumentationStatus fail `
        -StatePath $statePath `
        -PassThru `
        -Quiet

    if ($mixed.score -ne 55) {
        Fail "Expected mixed score 55, got $($mixed.score)"
    } else {
        Pass "Mixed score weights are correct"
    }

    if ($mixed.progress_bar -ne "[###########---------] 55%") {
        Fail "Unexpected mixed progress bar: $($mixed.progress_bar)"
    } else {
        Pass "Mixed progress bar rendered"
    }

    if (-not (Test-Path $statePath)) {
        Fail "Efficiency state file was not written"
    } else {
        Pass "Efficiency state file written"
    }
}

Remove-Item $statePath -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All loop efficiency score tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount loop efficiency score test(s) failed."
    exit 1
}

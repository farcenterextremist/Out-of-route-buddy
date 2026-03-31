# Run all loop continuity tests.
# Run from repo root: .\scripts\automation\run_loop_continuity_tests.ps1

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$statePath = Join-Path $RepoRoot "docs\automation\loop_continuity_state.json"

$scripts = @(
    "test_loop_gate_contract.ps1",
    "test_loop_role_topology_contract.ps1",
    "test_shared_state_contract.ps1",
    "test_shared_state_schema.ps1",
    "test_write_loop_shared_state.ps1",
    "test_write_loop_efficiency_block.ps1",
    "test_loop_run_wrappers.ps1",
    "test_loop_shared_state_audit.ps1",
    "test_loop_efficiency_score.ps1",
    "test_loop_desktop_guide_export.ps1"
)

$failed = 0
$failedSuites = @()
foreach ($scriptName in $scripts) {
    $path = Join-Path $RepoRoot "scripts\automation\$scriptName"
    if (-not (Test-Path $path)) {
        Write-Host "SKIP: $scriptName not found"
        continue
    }

    Write-Host "--- $scriptName ---"
    & $path
    if ($LASTEXITCODE -ne 0) {
        $failed++
        $failedSuites += $scriptName
    }
    Write-Host ""
}

$state = [ordered]@{
    last_run_ts = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    status = if ($failed -eq 0) { "passed" } else { "failed" }
    failed_count = $failed
    failed_suites = @($failedSuites)
}
$stateJson = $state | ConvertTo-Json -Depth 10
Set-Content -Path $statePath -Value $stateJson -Encoding UTF8

$efficiencyScript = Join-Path $RepoRoot "scripts\automation\measure_loop_efficiency.ps1"
if (Test-Path $efficiencyScript) {
    & $efficiencyScript -ContinuityStatus $(if ($failed -eq 0) { "pass" } else { "fail" })
    Write-Host ""
}

if ($failed -eq 0) {
    Write-Host "All loop continuity tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failed continuity suite(s) failed."
    exit 1
}

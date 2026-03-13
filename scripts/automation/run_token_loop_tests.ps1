# Run all Token Loop tests — listener, snapshot, events analysis.
# Run from repo root: .\scripts\automation\run_token_loop_tests.ps1
# Exit 0 if all pass, 1 if any fail.

param([switch]$KeepOutput)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$scripts = @(
    @{ Name = "Compile (parse)"; Script = "test_token_loop_compile.ps1"; HasKeep = $false },
    @{ Name = "Wiring"; Script = "test_token_loop_wiring.ps1"; HasKeep = $false },
    @{ Name = "Listener"; Script = "test_token_loop_listener.ps1"; HasKeep = $true },
    @{ Name = "Snapshot"; Script = "test_token_loop_snapshot.ps1"; HasKeep = $true },
    @{ Name = "Events analysis"; Script = "test_token_loop_events_analysis.ps1"; HasKeep = $false }
)

$failed = 0
foreach ($s in $scripts) {
    $path = Join-Path $RepoRoot "scripts\automation\$($s.Script)"
    if (-not (Test-Path $path)) {
        Write-Host "SKIP: $($s.Script) not found"
        continue
    }
    Write-Host "--- $($s.Name) ---"
    if ($KeepOutput -and $s.HasKeep) {
        & $path -KeepOutput
    } else {
        & $path
    }
    if ($LASTEXITCODE -ne 0) { $failed++ }
    Write-Host ""
}

if ($failed -eq 0) {
    Write-Host "All token loop tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failed test suite(s) failed."
    exit 1
}

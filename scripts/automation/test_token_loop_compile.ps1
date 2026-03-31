# Test Token Loop Scripts Parse — Verify all token loop PowerShell scripts parse without syntax errors.
# Run from repo root: .\scripts\automation\test_token_loop_compile.ps1
# Exit 0 = all parse OK, 1 = parse error(s).

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$scriptsDir = Join-Path $RepoRoot "scripts\automation"
$scripts = @(
    "complete_token_loop_run.ps1",
    "compare_token_rule_overhead.ps1",
    "monitor_openai_api_usage.ps1",
    "loop_run_contract.ps1",
    "start_loop_run.ps1",
    "measure_loop_efficiency.ps1",
    "run_llm_loop.ps1",
    "token_loop_state_snapshot.ps1",
    "token_loop_listener.ps1",
    "run_token_loop.ps1",
    "test_complete_token_loop_run.ps1",
    "test_compare_token_rule_overhead.ps1",
    "test_monitor_openai_api_usage.ps1",
    "test_token_loop_listener.ps1",
    "test_token_loop_snapshot.ps1",
    "test_token_loop_events_analysis.ps1",
    "test_token_loop_wiring.ps1",
    "run_token_loop_tests.ps1"
)

Write-Host "Testing Token Loop Scripts (parse/compile)..."
Write-Host ""

$failCount = 0
foreach ($name in $scripts) {
    $path = Join-Path $scriptsDir $name
    if (-not (Test-Path $path)) {
        Write-Host "SKIP: $name not found"
        continue
    }
    try {
        $content = Get-Content -Path $path -Raw -ErrorAction Stop
        $errors = $null
        $null = [System.Management.Automation.Language.Parser]::ParseInput($content, [ref]$null, [ref]$errors)
        if ($errors -and $errors.Count -gt 0) {
            Write-Host "FAIL: $name has parse error(s): $($errors[0].Message)"
            $failCount++
        } else {
            Write-Host "PASS: $name parses OK"
        }
    } catch {
        Write-Host "FAIL: $name - $($_.Exception.Message)"
        $failCount++
    }
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token loop scripts parse successfully."
    exit 0
} else {
    Write-Host "FAIL: $failCount script(s) have parse errors."
    exit 1
}

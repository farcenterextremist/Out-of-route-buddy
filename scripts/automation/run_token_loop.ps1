# Token Reduction Loop — Start a run and record it via the listener.
# When user says "start token loop," the listener starts alongside the loop: run this script (or invoke token_loop_start), then run the 6 steps with step_start/step_end, then token_loop_end. Listener data is used to improve the loop (save token spend, manage context squish).
# Run from repo root: .\scripts\automation\run_token_loop.ps1
# Optional: .\scripts\automation\run_token_loop.ps1 -Test  (run all token loop tests first)

param([switch]$Test)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ListenerScript = Join-Path $RepoRoot "scripts\automation\token_loop_listener.ps1"
$SnapshotScript = Join-Path $RepoRoot "scripts\automation\token_loop_state_snapshot.ps1"
$RunId = "token-$((Get-Date).ToString('yyyyMMdd-HHmm'))"

if ($Test) {
    Write-Host "Running all token loop tests..."
    & (Join-Path $RepoRoot "scripts\automation\run_token_loop_tests.ps1")
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Tests failed. Fix before running token loop."
        exit $LASTEXITCODE
    }
    Write-Host ""
}

if (-not (Test-Path $ListenerScript)) {
    Write-Host "FAIL: token_loop_listener.ps1 not found."
    exit 1
}

# 1. Record current state (rollback + progress tracking)
if (Test-Path $SnapshotScript) {
    & $SnapshotScript -RunId $RunId
} else {
    Write-Host "WARN: token_loop_state_snapshot.ps1 not found; skipping state snapshot."
}

# 2. Start listener (use same RunId for all events this run)
& $ListenerScript -Event "token_loop_start" -Note "run_token_loop.ps1" -RunId $RunId

Write-Host ""
Write-Host "Token Reduction Loop started. RunId: $RunId"
Write-Host "State snapshot: docs\automation\token_loop_snapshots\$RunId.json (rollback + progress tracking)."
Write-Host "Goals: save token spend, manage context squish. Listener data improves the loop."
Write-Host "Follow docs/automation/TOKEN_REDUCTION_LOOP.md steps 0-7 (0=deep research, 7=organize results and recommend next tasks in TOKEN_LOOP_NEXT_TASKS.md)."
Write-Host "Invoke token_loop_listener.ps1 at each step (step_start, step_end) and at end (token_loop_end). No human in the loop."
Write-Host "Example end: .\scripts\automation\token_loop_listener.ps1 -Event token_loop_end -Note steps=8 -RunId $RunId -Metrics '{`"steps_completed`":8}'"
Write-Host ""

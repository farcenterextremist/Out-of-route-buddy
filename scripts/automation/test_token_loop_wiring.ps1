# Test Token Loop Wiring — Verify required docs and scripts exist and loop is wired for agents.
# Run from repo root: .\scripts\automation\test_token_loop_wiring.ps1
# Exit 0 = pass, 1 = fail.

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$failCount = 0
$docsDir = Join-Path $RepoRoot "docs\automation"
$scriptsDir = Join-Path $RepoRoot "scripts\automation"

Write-Host "Testing Token Loop Wiring..."
Write-Host ""

# 1. Required docs exist
$requiredDocs = @(
    "TOKEN_REDUCTION_LOOP.md",
    "TOKEN_LOOP_NEXT_TASKS.md",
    "TOKEN_LOOP_IMPROVEMENT_PLAN.md",
    "TOKEN_SAVING_PRACTICES.md",
    "TOKEN_LOOP_RUN_LEDGER.md",
    "TOKEN_LOOP_MASTER_PLAN.md",
    "TOKEN_LOOP_LISTENER.md"
)
foreach ($doc in $requiredDocs) {
    $path = Join-Path $docsDir $doc
    if (-not (Test-Path $path)) {
        Write-Host "FAIL: Missing doc $doc"
        $failCount++
    } else {
        Write-Host "PASS: $doc exists"
    }
}

# 2. Required scripts exist
$requiredScripts = @(
    "token_loop_state_snapshot.ps1",
    "token_loop_listener.ps1",
    "run_token_loop.ps1",
    "test_token_loop_listener.ps1",
    "test_token_loop_snapshot.ps1",
    "test_token_loop_events_analysis.ps1",
    "test_token_loop_wiring.ps1",
    "test_token_loop_compile.ps1",
    "run_token_loop_tests.ps1"
)
foreach ($script in $requiredScripts) {
    $path = Join-Path $scriptsDir $script
    if (-not (Test-Path $path)) {
        Write-Host "FAIL: Missing script $script"
        $failCount++
    } else {
        Write-Host "PASS: $script exists"
    }
}

# 3. TOKEN_REDUCTION_LOOP contains key wiring (Step 0, Step 7, NEXT_TASKS, token_loop_start)
$loopPath = Join-Path $docsDir "TOKEN_REDUCTION_LOOP.md"
$loopContent = $null
if (Test-Path $loopPath) {
    $loopContent = Get-Content $loopPath -Raw -ErrorAction SilentlyContinue
}
$wiringStrings = @(
    "Step 0",
    "Step 7",
    "TOKEN_LOOP_NEXT_TASKS",
    "token_loop_start",
    "token_loop_end",
    "run_token_loop.ps1"
)
foreach ($s in $wiringStrings) {
    if (-not $loopContent -or $loopContent -notmatch [regex]::Escape($s)) {
        Write-Host "FAIL: TOKEN_REDUCTION_LOOP.md missing key wiring: $s"
        $failCount++
    } else {
        Write-Host "PASS: Loop doc contains '$s'"
    }
}

# 4. Snapshot output directory writable (exists or can be created)
$snapDir = Join-Path $RepoRoot "docs\automation\token_loop_snapshots"
if (-not (Test-Path $snapDir)) {
    try {
        New-Item -ItemType Directory -Path $snapDir -Force | Out-Null
        Write-Host "PASS: token_loop_snapshots dir created"
    } catch {
        Write-Host "FAIL: Cannot create token_loop_snapshots directory"
        $failCount++
    }
} else {
    Write-Host "PASS: token_loop_snapshots dir exists"
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token loop wiring tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount wiring test(s) failed."
    exit 1
}

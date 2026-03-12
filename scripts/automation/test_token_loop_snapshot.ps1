# Test Token Loop Snapshot — Verify snapshot script output schema and always_apply derivation.
# Run from repo root: .\scripts\automation\test_token_loop_snapshot.ps1
# Runs token_loop_state_snapshot.ps1 with a test RunId and validates output. Exit 0 = pass, 1 = fail.

param([switch]$KeepOutput)

$ErrorActionPreference = "Stop"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$SnapshotScript = Join-Path $RepoRoot "scripts\automation\token_loop_state_snapshot.ps1"
$SnapDir = Join-Path $RepoRoot "docs\automation\token_loop_snapshots"
$RunId = "token-snapshot-test-$((Get-Date).ToString('yyyyMMdd-HHmmss'))"
$OutFile = Join-Path $SnapDir "$RunId.json"
$failCount = 0

Write-Host "Testing Token Loop State Snapshot..."
Write-Host ""

# 1. Snapshot script exists
if (-not (Test-Path $SnapshotScript)) {
    Write-Host "FAIL: token_loop_state_snapshot.ps1 not found at $SnapshotScript"
    $failCount++
    if (-not $KeepOutput) { exit 1 }
} else {
    Write-Host "PASS: token_loop_state_snapshot.ps1 exists"
}

# 2. Run snapshot (creates docs/automation/token_loop_snapshots/<RunId>.json)
if (Test-Path $SnapshotScript) {
    & $SnapshotScript -RunId $RunId 2>&1 | Out-Null
}

# 3. Output file exists
if (-not (Test-Path $OutFile)) {
    Write-Host "FAIL: No snapshot file created at $OutFile"
    $failCount++
} else {
    Write-Host "PASS: Snapshot file created"
}

# 4. Valid JSON and required top-level keys
$json = $null
if (Test-Path $OutFile) {
    try {
        $json = Get-Content -Path $OutFile -Raw | ConvertFrom-Json
    } catch {
        Write-Host "FAIL: Snapshot file is not valid JSON"
        $failCount++
    }
}

$requiredKeys = @("run_id", "ts", "rules", "always_apply_count", "always_apply_lines")
if ($json) {
    $missing = @()
    foreach ($k in $requiredKeys) {
        if (-not (Get-Member -InputObject $json -Name $k -MemberType Properties -ErrorAction SilentlyContinue)) {
            $missing += $k
        }
    }
    if ($missing.Count -gt 0) {
        Write-Host "FAIL: Missing required keys: $($missing -join ', ')"
        $failCount++
    } else {
        Write-Host "PASS: All required keys present (run_id, ts, rules, always_apply_count, always_apply_lines)"
    }
    # git_head or settings_snippet at least one
    $hasOptional = (Get-Member -InputObject $json -Name "git_head" -MemberType Properties -ErrorAction SilentlyContinue) -or
                   (Get-Member -InputObject $json -Name "settings_snippet" -MemberType Properties -ErrorAction SilentlyContinue)
    if (-not $hasOptional) {
        Write-Host "FAIL: Missing git_head or settings_snippet"
        $failCount++
    } else {
        Write-Host "PASS: git_head or settings_snippet present"
    }
}

# 5. always_apply_count equals count of rules where always_apply is true
if ($json -and $json.rules) {
    $rulesArray = @($json.rules)
    $alwaysApplyRules = @($rulesArray | Where-Object { $_.always_apply -eq $true })
    $expectedCount = $alwaysApplyRules.Count
    $actualCount = $json.always_apply_count
    if ($actualCount -ne $expectedCount) {
        Write-Host "FAIL: always_apply_count = $actualCount but rules with always_apply=true = $expectedCount"
        $failCount++
    } else {
        Write-Host "PASS: always_apply_count ($actualCount) equals count of rules with always_apply=true"
    }

    # 6. always_apply_lines equals sum of line_count of those rules
    $expectedLines = 0
    foreach ($r in $alwaysApplyRules) {
        if ($null -ne $r.line_count) { $expectedLines += $r.line_count }
    }
    $actualLines = $json.always_apply_lines
    if ($null -eq $actualLines) { $actualLines = 0 }
    if ($actualLines -ne $expectedLines) {
        Write-Host "FAIL: always_apply_lines = $actualLines but sum of line_count for always-apply rules = $expectedLines"
        $failCount++
    } else {
        Write-Host "PASS: always_apply_lines ($actualLines) equals sum of line_count for always-apply rules"
    }
} elseif ($json) {
    if ($json.always_apply_count -ne 0 -or $json.always_apply_lines -ne 0) {
        Write-Host "FAIL: rules missing or empty but always_apply_count/always_apply_lines non-zero"
        $failCount++
    }
}

# Cleanup
if (-not $KeepOutput -and (Test-Path $OutFile)) {
    Remove-Item $OutFile -Force -ErrorAction SilentlyContinue
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token loop snapshot tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount test(s) failed."
    exit 1
}

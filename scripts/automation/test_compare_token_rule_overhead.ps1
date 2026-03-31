# Test token rule overhead comparison script with isolated fixtures.
# Run from repo root: .\scripts\automation\test_compare_token_rule_overhead.ps1

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

$compareScript = Join-Path $RepoRoot "scripts\automation\compare_token_rule_overhead.ps1"
$tmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$rulesDir = Join-Path $RepoRoot ".cursor\rules"
$backupDir = Join-Path $tmpDir "rules_backup_compare"
$tempRuleA = Join-Path $rulesDir "z_token_compare_rule_a.mdc"
$tempRuleB = Join-Path $rulesDir "z_token_compare_rule_b.mdc"
$snapshotFile = Join-Path $tmpDir "token.compare.snapshot.json"
$reportFile = Join-Path $tmpDir "token.compare.report.json"

function Get-RuleSnapshotFromWorkspace([string]$RulesDir) {
    $rulesRaw = @()
    if (Test-Path $RulesDir) {
        Get-ChildItem -Path $RulesDir -Filter "*.mdc" -File | ForEach-Object {
            $lines = (Get-Content $_.FullName -ErrorAction SilentlyContinue | Measure-Object -Line).Lines
            $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
            $alwaysApply = $false
            if ($content -match 'alwaysApply:\s*true') { $alwaysApply = $true }
            $rulesRaw += @{
                name = $_.Name
                line_count = $lines
                always_apply = $alwaysApply
            }
        }
    }

    $seen = @{}
    $rules = @()
    foreach ($r in $rulesRaw) {
        if (-not $seen[$r.name]) {
            $seen[$r.name] = $true
            $rules += $r
        }
    }

    $alwaysApplyRules = @($rules | Where-Object { $_.always_apply })
    $alwaysApplyLines = ($alwaysApplyRules | ForEach-Object { $_.line_count } | Measure-Object -Sum).Sum
    if ($null -eq $alwaysApplyLines) { $alwaysApplyLines = 0 }

    return @{
        rules = $rules
        always_apply_count = $alwaysApplyRules.Count
        always_apply_lines = $alwaysApplyLines
    }
}

if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
}

Write-Host "Testing token rule overhead comparison..."
Write-Host ""

if (-not (Test-Path $compareScript)) {
    Fail "compare_token_rule_overhead.ps1 not found"
}

if (Test-Path $backupDir) {
    Remove-Item $backupDir -Recurse -Force -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

$restored = $false
try {
    if (Test-Path $tempRuleA) { Move-Item -Path $tempRuleA -Destination (Join-Path $backupDir "z_token_compare_rule_a.mdc") -Force }
    if (Test-Path $tempRuleB) { Move-Item -Path $tempRuleB -Destination (Join-Path $backupDir "z_token_compare_rule_b.mdc") -Force }
    Remove-Item $snapshotFile, $reportFile -Force -ErrorAction SilentlyContinue

    $baseline = Get-RuleSnapshotFromWorkspace -RulesDir $rulesDir
    $baselineRules = @($baseline.rules)
    $baselineRules += @(
        @{ name = "z_token_compare_rule_a.mdc"; line_count = 4; always_apply = $false },
        @{ name = "z_token_compare_rule_b.mdc"; line_count = 5; always_apply = $false }
    )

    $snapshotObject = @{
        run_id = "token-compare-fixture"
        ts = "2026-03-16T00:00:00Z"
        rules = $baselineRules
        always_apply_count = $baseline.always_apply_count
        always_apply_lines = $baseline.always_apply_lines
    }
    $snapshotObject | ConvertTo-Json -Depth 10 | Set-Content -Path $snapshotFile -Encoding UTF8

    @(
        "---",
        "description: compare fixture A",
        "alwaysApply: true",
        "---"
    ) | Set-Content -Path $tempRuleA -Encoding UTF8

    @(
        "---",
        "description: compare fixture B",
        "alwaysApply: false",
        "---",
        "extra line"
    ) | Set-Content -Path $tempRuleB -Encoding UTF8

    $result = & $compareScript -SnapshotPath $snapshotFile -OutPath $reportFile -PassThru

    if (-not $result) {
        Fail "Comparison script did not return a result"
    } else {
        if ($result.snapshot_run_id -ne "token-compare-fixture") {
            Fail "Snapshot run_id mismatch"
        } else {
            Pass "Snapshot run_id preserved"
        }
        if ($result.baseline_always_apply_count -ne $baseline.always_apply_count -or $result.current_always_apply_count -ne ($baseline.always_apply_count + 1)) {
            Fail "Always-apply count comparison mismatch"
        } else {
            Pass "Always-apply count compared correctly"
        }
        if ($result.delta_always_apply_count -ne 1) {
            Fail "Always-apply count delta mismatch"
        } else {
            Pass "Always-apply count delta computed"
        }
        $fixtureRule = @($result.changed_rules | Where-Object { $_.name -eq "z_token_compare_rule_a.mdc" })
        if ($fixtureRule.Count -ne 1) {
            Fail "Expected changed_rules to include fixture rule A"
        } else {
            Pass "Fixture rule A captured in changed rules"
        }
        if ($result.changed_rules.Count -lt 1) {
            Fail "Expected changed_rules to include fixture differences"
        } else {
            Pass "Changed rules captured"
        }
    }

    if (-not (Test-Path $reportFile)) {
        Fail "Comparison report file not written"
    } else {
        try {
            $reportJson = Get-Content -Path $reportFile -Raw | ConvertFrom-Json
            if ($reportJson.snapshot_run_id -ne "token-compare-fixture") {
                Fail "Report file snapshot_run_id mismatch"
            } else {
                Pass "Report file written as valid JSON"
            }
        } catch {
            Fail "Comparison report file is not valid JSON"
        }
    }
}
finally {
    Remove-Item $tempRuleA, $tempRuleB, $snapshotFile, $reportFile -Force -ErrorAction SilentlyContinue
    if (Test-Path (Join-Path $backupDir "z_token_compare_rule_a.mdc")) {
        Move-Item -Path (Join-Path $backupDir "z_token_compare_rule_a.mdc") -Destination $tempRuleA -Force
    }
    if (Test-Path (Join-Path $backupDir "z_token_compare_rule_b.mdc")) {
        Move-Item -Path (Join-Path $backupDir "z_token_compare_rule_b.mdc") -Destination $tempRuleB -Force
    }
    Remove-Item $backupDir -Recurse -Force -ErrorAction SilentlyContinue
    $restored = $true
}

if (-not $restored) {
    Fail "Fixture cleanup did not complete"
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All token rule overhead comparison tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount token rule overhead comparison test(s) failed."
    exit 1
}

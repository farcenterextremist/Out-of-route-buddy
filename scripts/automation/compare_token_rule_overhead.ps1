# Compare current token rule overhead against a token snapshot.
# Run from repo root:
# .\scripts\automation\compare_token_rule_overhead.ps1
# .\scripts\automation\compare_token_rule_overhead.ps1 -SnapshotPath docs/automation/token_loop_snapshots/token-20260316-0147.json

param(
    [string]$SnapshotPath = "",
    [string]$OutPath = "",
    [switch]$PassThru
)

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

function Resolve-SnapshotPath {
    param(
        [string]$RepoRoot,
        [string]$RequestedPath
    )

    if ($RequestedPath) {
        if ([System.IO.Path]::IsPathRooted($RequestedPath)) {
            return $RequestedPath
        }
        return (Join-Path $RepoRoot $RequestedPath)
    }

    $snapDir = Join-Path $RepoRoot "docs\automation\token_loop_snapshots"
    if (-not (Test-Path $snapDir)) {
        throw "Token snapshot directory not found: $snapDir"
    }

    $latest = Get-ChildItem -Path $snapDir -Filter "token-*.json" -File |
        Where-Object { $_.BaseName -notlike "token-snapshot-test-*" } |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1

    if (-not $latest) {
        throw "No token snapshot files found in $snapDir"
    }

    return $latest.FullName
}

function Get-CurrentRuleSnapshot {
    param([string]$RepoRoot)

    $rulesDir = Join-Path $RepoRoot ".cursor\rules"
    $rulesRaw = @()
    if (Test-Path $rulesDir) {
        Get-ChildItem -Path $rulesDir -Filter "*.mdc" -File | ForEach-Object {
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

$snapshotFile = Resolve-SnapshotPath -RepoRoot $RepoRoot -RequestedPath $SnapshotPath
if (-not (Test-Path $snapshotFile)) {
    throw "Snapshot file does not exist: $snapshotFile"
}

$snapshot = Get-Content -Path $snapshotFile -Raw | ConvertFrom-Json
$baselineRules = @{}
foreach ($rule in @($snapshot.rules)) {
    $baselineRules[$rule.name] = $rule
}

$current = Get-CurrentRuleSnapshot -RepoRoot $RepoRoot
$currentRules = @{}
foreach ($rule in @($current.rules)) {
    $currentRules[$rule.name] = $rule
}

$allRuleNames = @($baselineRules.Keys + $currentRules.Keys | Select-Object -Unique | Sort-Object)
$changedRules = @()
foreach ($name in $allRuleNames) {
    $baselineRule = $baselineRules[$name]
    $currentRule = $currentRules[$name]

    $baselineAlwaysApply = if ($baselineRule) { [bool]$baselineRule.always_apply } else { $null }
    $currentAlwaysApply = if ($currentRule) { [bool]$currentRule.always_apply } else { $null }
    $baselineLines = if ($baselineRule) { [int]$baselineRule.line_count } else { $null }
    $currentLines = if ($currentRule) { [int]$currentRule.line_count } else { $null }

    if ($baselineAlwaysApply -ne $currentAlwaysApply -or $baselineLines -ne $currentLines) {
        $changedRules += [PSCustomObject]@{
            name = $name
            baseline_always_apply = $baselineAlwaysApply
            current_always_apply = $currentAlwaysApply
            baseline_line_count = $baselineLines
            current_line_count = $currentLines
        }
    }
}

$result = [PSCustomObject]@{
    snapshot_path = $snapshotFile
    snapshot_run_id = $snapshot.run_id
    baseline_always_apply_count = [int]$snapshot.always_apply_count
    current_always_apply_count = [int]$current.always_apply_count
    delta_always_apply_count = ([int]$current.always_apply_count - [int]$snapshot.always_apply_count)
    baseline_always_apply_lines = [int]$snapshot.always_apply_lines
    current_always_apply_lines = [int]$current.always_apply_lines
    delta_always_apply_lines = ([int]$current.always_apply_lines - [int]$snapshot.always_apply_lines)
    changed_rules = @($changedRules)
}

if ($OutPath) {
    $targetPath = if ([System.IO.Path]::IsPathRooted($OutPath)) {
        $OutPath
    } else {
        Join-Path $RepoRoot $OutPath
    }
    $targetDir = Split-Path -Parent $targetPath
    if ($targetDir -and -not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    $result | ConvertTo-Json -Depth 10 | Set-Content -Path $targetPath -Encoding UTF8
}

Write-Host "Token rule overhead comparison"
Write-Host "  Snapshot: $snapshotFile"
Write-Host "  Always-apply count: $($snapshot.always_apply_count) -> $($current.always_apply_count) (delta $($result.delta_always_apply_count))"
Write-Host "  Always-apply lines: $($snapshot.always_apply_lines) -> $($current.always_apply_lines) (delta $($result.delta_always_apply_lines))"
if ($changedRules.Count -eq 0) {
    Write-Host "  Changed rules: none"
} else {
    Write-Host "  Changed rules:"
    foreach ($rule in $changedRules) {
        Write-Host "    - $($rule.name): always_apply $($rule.baseline_always_apply) -> $($rule.current_always_apply); lines $($rule.baseline_line_count) -> $($rule.current_line_count)"
    }
}

if ($PassThru) {
    $result
}

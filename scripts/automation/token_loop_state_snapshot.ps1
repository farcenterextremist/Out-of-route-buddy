# Token Loop State Snapshot — Record current state at loop start for rollback and progress tracking.
# Run from repo root: .\scripts\automation\token_loop_state_snapshot.ps1 -RunId <run_id>
# Output: docs/automation/token_loop_snapshots\<run_id>.json
# Use: compare before/after, revert if needed, track what works / what doesn't.

param(
    [Parameter(Mandatory = $true)]
    [string]$RunId
)

$ErrorActionPreference = "Continue"
$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ts = Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ"
$SnapDir = Join-Path $RepoRoot "docs\automation\token_loop_snapshots"
$OutFile = Join-Path $SnapDir "$RunId.json"

if (-not (Test-Path $SnapDir)) { New-Item -ItemType Directory -Path $SnapDir -Force | Out-Null }

# 1. Rules snapshot: .cursor/rules/*.mdc — name, line count, alwaysApply (dedupe by name)
$rulesDir = Join-Path $RepoRoot ".cursor\rules"
$rulesRaw = @()
if (Test-Path $rulesDir) {
    Get-ChildItem -Path $rulesDir -Filter "*.mdc" -File | ForEach-Object {
        $lines = (Get-Content $_.FullName -ErrorAction SilentlyContinue | Measure-Object -Line).Lines
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        $alwaysApply = $false
        if ($content -match 'alwaysApply:\s*true') { $alwaysApply = $true }
        $rulesRaw += @{ name = $_.Name; line_count = $lines; always_apply = $alwaysApply }
    }
}
# Dedupe by name so same file from different path normalization is counted once
$seen = @{}
$rules = @()
foreach ($r in $rulesRaw) {
    if (-not $seen[$r.name]) {
        $seen[$r.name] = $true
        $rules += $r
    }
}
# Derive counts only from the rules array (force array so .Count is element count, not hashtable key count)
$alwaysApplyRules = @($rules | Where-Object { $_.always_apply })
$always_apply_count = $alwaysApplyRules.Count
$always_apply_lines = ($alwaysApplyRules | ForEach-Object { $_.line_count } | Measure-Object -Sum).Sum
if ($null -eq $always_apply_lines) { $always_apply_lines = 0 }

# 2. Settings snippet (token-relevant)
$settingsPath = Join-Path $RepoRoot ".vscode\settings.json"
$settingsSnippet = $null
if (Test-Path $settingsPath) {
    try {
        $settings = Get-Content $settingsPath -Raw | ConvertFrom-Json
        $settingsSnippet = @{
            files_watcher_exclude = $settings."files.watcherExclude"
            search_exclude = $settings."search.exclude"
        }
    } catch {
        $settingsSnippet = @{ note = "could not parse settings.json" }
    }
}

# 3. Git HEAD (for revert)
$gitHead = $null
try {
    $gitHead = & git rev-parse HEAD 2>$null
    if (-not $gitHead) { $gitHead = $null }
} catch { }

# 4. Build snapshot object (always_apply_count and always_apply_lines derived only from rules array)
$snapshot = @{
    run_id = $RunId
    ts = $ts
    purpose = "rollback and progress tracking; what works / what doesn't"
    rules = $rules
    always_apply_count = $always_apply_count
    always_apply_lines = $always_apply_lines
    settings_snippet = $settingsSnippet
    git_head = $gitHead
}

$json = $snapshot | ConvertTo-Json -Depth 5
Set-Content -Path $OutFile -Value $json -Encoding UTF8

Write-Host "[$ts] Token loop state snapshot -> $OutFile"
Write-Host "  rules: $($rules.Count) files, always_apply: $($snapshot.always_apply_count), lines: $($snapshot.always_apply_lines)"

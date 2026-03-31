# Test Loop Role Topology Contract
# Run from repo root: .\scripts\automation\test_loop_role_topology_contract.ps1

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$failCount = 0

function Assert-FileContains {
    param(
        [string]$Path,
        [string[]]$Needles
    )

    if (-not (Test-Path $Path)) {
        Write-Host "FAIL: Missing required file: $Path"
        $script:failCount++
        return
    }

    $content = Get-Content $Path -Raw
    foreach ($needle in $Needles) {
        if ($content -notmatch [Regex]::Escape($needle)) {
            Write-Host "FAIL: '$needle' not found in $Path"
            $script:failCount++
        } else {
            Write-Host "PASS: '$needle' present in $Path"
        }
    }
}

Write-Host "Testing loop role topology contract..."
Write-Host ""

$tabModelPath = Join-Path $RepoRoot "docs\automation\LOOPMASTER_TAB_AND_SPAWN_MODEL.md"
$blueprintPath = Join-Path $RepoRoot "docs\automation\LOOPMASTER_SUPER_LOOP_BLUEPRINT.md"
$masterRolePath = Join-Path $RepoRoot "docs\automation\LOOP_MASTER_ROLE.md"

Assert-FileContains -Path $tabModelPath -Needles @(
    "ArchitectTab",
    "Builder",
    "Optimizer",
    "Guard",
    "Watcher",
    "One writer per file family",
    "should validate, not lead implementation."
)

Assert-FileContains -Path $blueprintPath -Needles @(
    "Default tab operating model",
    "ArchitectTab",
    "Watcher"
)

Assert-FileContains -Path $masterRolePath -Needles @(
    "Use role-named worker tabs when splitting work.",
    "ArchitectTab",
    "Watcher"
)

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All loop role topology contract tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount topology contract check(s) failed."
    exit 1
}

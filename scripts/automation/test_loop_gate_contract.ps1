# Test Loop Gate Contract
# Run from repo root: .\scripts\automation\test_loop_gate_contract.ps1

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

Write-Host "Testing loop gate contract..."
Write-Host ""

$loopGatesPath = Join-Path $RepoRoot "docs\automation\LOOP_GATES.md"
$improvementPath = Join-Path $RepoRoot "docs\automation\IMPROVEMENT_LOOP_ROUTINE.md"
$consistencyPath = Join-Path $RepoRoot "docs\automation\LOOP_CONSISTENCY_STANDARD.md"
$qualityPath = Join-Path $RepoRoot "docs\automation\QUALITY_STANDARDS_AND_PROOF.md"
$frontendGatePath = Join-Path $RepoRoot "docs\automation\FRONTEND_CHANGE_AUTOMATION_GATE.md"

Assert-FileContains -Path $loopGatesPath -Needles @(
    "At loop start (before execution)",
    "At loop end (after execution)",
    "LOOP_CONSISTENCY_STANDARD.md"
)

Assert-FileContains -Path $improvementPath -Needles @(
    "Consistency contract (required)",
    "Sandboxing verification"
)

Assert-FileContains -Path $consistencyPath -Needles @(
    "Universal loop contract"
)

Assert-FileContains -Path $qualityPath -Needles @(
    "Proof of Quality"
)

Assert-FileContains -Path $frontendGatePath -Needles @(
    "Obvious Benefit Score",
    "Subtlety Safety Score"
)

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All loop gate contract tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount contract check(s) failed."
    exit 1
}

# Test write_loop_efficiency_block.ps1 idempotent markdown persistence.
# Run from repo root: .\scripts\automation\test_write_loop_efficiency_block.ps1

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

$scriptPath = Join-Path $RepoRoot "scripts\automation\write_loop_efficiency_block.ps1"
$tmpDir = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp"
$targetPath = Join-Path $tmpDir "efficiency_block_test.md"
$marker = "<!-- LOOP_EFFICIENCY_BLOCK_START:run-efficiency-test -->"

if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
}

Set-Content -Path $targetPath -Value "# test summary`n" -Encoding UTF8

if (-not (Test-Path $scriptPath)) {
    Fail "write_loop_efficiency_block.ps1 not found"
} else {
    & $scriptPath -TargetPath $targetPath -RunId "run-efficiency-test" -Score 90 -Grade "A" -ProgressBar "[##################--] 90%" -WhyItMoved "First write." | Out-Null
    $first = Get-Content -Path $targetPath -Raw
    if ($first -notmatch [Regex]::Escape("## Loop Efficiency Score")) {
        Fail "Efficiency block heading missing after first write"
    } else {
        Pass "Efficiency block heading written"
    }

    & $scriptPath -TargetPath $targetPath -RunId "run-efficiency-test" -Score 95 -Grade "A" -ProgressBar "[###################-] 95%" -WhyItMoved "Replacement write." | Out-Null
    $second = Get-Content -Path $targetPath -Raw
    $markerCount = ([regex]::Matches($second, [Regex]::Escape($marker))).Count
    if ($markerCount -ne 1) {
        Fail "Efficiency block marker duplicated ($markerCount)"
    } else {
        Pass "Efficiency block marker remains unique"
    }

    if ($second -notmatch [Regex]::Escape("95/100")) {
        Fail "Efficiency block did not update score on replacement"
    } else {
        Pass "Efficiency block replaces prior content"
    }
}

Remove-Item $targetPath -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All loop efficiency block writer tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount loop efficiency block writer test(s) failed."
    exit 1
}

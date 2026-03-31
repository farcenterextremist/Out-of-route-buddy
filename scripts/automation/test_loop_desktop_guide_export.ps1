# Test Loopmaster desktop guide export.
# Run from repo root: .\scripts\automation\test_loop_desktop_guide_export.ps1

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$ExporterScript = Join-Path $RepoRoot "scripts\automation\export_loopmaster_desktop_guide.ps1"
$TestTarget = Join-Path $RepoRoot "docs\automation\_continuity_test_tmp\loopmaster_desktop_guide_test.txt"
$failCount = 0

Write-Host "Testing Loopmaster desktop guide export..."
Write-Host ""

if (-not (Test-Path $ExporterScript)) {
    Write-Host "FAIL: export_loopmaster_desktop_guide.ps1 not found at $ExporterScript"
    $failCount++
} else {
    Write-Host "PASS: export_loopmaster_desktop_guide.ps1 exists"
}

Remove-Item $TestTarget -Force -ErrorAction SilentlyContinue

& $ExporterScript -TargetPath $TestTarget 2>&1 | Out-Null

if (-not (Test-Path $TestTarget)) {
    Write-Host "FAIL: No exported guide created at $TestTarget"
    $failCount++
} else {
    Write-Host "PASS: Exported guide created"
    $content = Get-Content $TestTarget -Raw
    foreach ($needle in @(
        "Loopmaster",
        "ArchitectTab",
        "Watcher",
        "CROSS-PROJECT FLYWHEEL KIT"
    )) {
        if ($content -notmatch [Regex]::Escape($needle)) {
            Write-Host "FAIL: '$needle' not found in exported guide"
            $failCount++
        } else {
            Write-Host "PASS: '$needle' present in exported guide"
        }
    }
}

Remove-Item $TestTarget -Force -ErrorAction SilentlyContinue

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "All desktop guide export tests passed."
    exit 0
} else {
    Write-Host "FAIL: $failCount desktop guide export test(s) failed."
    exit 1
}

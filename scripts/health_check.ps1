# Health check: build + unit tests
# Usage: .\scripts\health_check.ps1
# Related: 25-point #16, docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }
Set-Location $root

Write-Host "Health check: build + unit tests" -ForegroundColor Cyan
Write-Host "Root: $root" -ForegroundColor Gray

# Build
Write-Host "`nRunning: .\gradlew.bat assembleDebug ..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[OK] Build succeeded" -ForegroundColor Green

# Unit tests (app unit tests + coverage report + verification; root has no "test" task)
Write-Host "`nRunning: .\gradlew.bat jacocoSuite ..." -ForegroundColor Cyan
& .\gradlew.bat jacocoSuite --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[OK] Unit tests passed" -ForegroundColor Green

Write-Host "`nHealth check complete." -ForegroundColor Green

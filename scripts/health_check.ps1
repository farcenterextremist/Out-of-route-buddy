# Health check: build + unit tests (optional: .env present for coordinator email)
# Usage: .\scripts\health_check.ps1  [ -CheckEnv ]
# Related: 25-point #16, docs/agents/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md

param(
    [switch]$CheckEnv
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }
Set-Location $root

Write-Host "Health check: build + unit tests" -ForegroundColor Cyan
Write-Host "Root: $root" -ForegroundColor Gray

# Optional: verify coordinator .env exists (for email send/read)
if ($CheckEnv) {
    $envPath = Join-Path $root "scripts\coordinator-email\.env"
    if (Test-Path $envPath) {
        Write-Host "[OK] coordinator-email .env present" -ForegroundColor Green
    } else {
        Write-Host "[SKIP] coordinator-email .env not found (optional for email)" -ForegroundColor Yellow
    }
}

# Build
Write-Host "`nRunning: .\gradlew.bat assembleDebug ..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[OK] Build succeeded" -ForegroundColor Green

# Unit tests
Write-Host "`nRunning: .\gradlew.bat test ..." -ForegroundColor Cyan
& .\gradlew.bat test --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[OK] Unit tests passed" -ForegroundColor Green

Write-Host "`nHealth check complete." -ForegroundColor Green

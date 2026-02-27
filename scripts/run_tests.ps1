# ==================== RUN TESTS (PowerShell) ====================
# Runs unit tests for OutOfRouteBuddy. Use for local verification before CI.
# CI runs: .github/workflows/android-tests.yml

param(
    [switch]$WithCoverage,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

Write-Host "Running OutOfRouteBuddy unit tests..." -ForegroundColor Blue

# Run from repo root (script lives in scripts/)
$repoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repoRoot

try {
    if ($WithCoverage) {
        Write-Host "Running tests with coverage report..." -ForegroundColor Cyan
        ./gradlew.bat testDebugUnitTest jacocoTestReport
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        Write-Host "Coverage report: app/build/reports/jacoco/jacocoTestReport/html/index.html" -ForegroundColor Green
        Write-Host "Run scripts/coverage-analysis.ps1 for analysis" -ForegroundColor Yellow
    } else {
        ./gradlew.bat testDebugUnitTest
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    Write-Host "Tests passed." -ForegroundColor Green
} finally {
    Pop-Location
}

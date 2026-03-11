# OutOfRouteBuddy app debugging script
# Run from repo root: .\scripts\debug_app.ps1
# Options: -Clean (run clean first), -SkipTests, -SkipLint

param(
    [switch]$Clean,
    [switch]$SkipTests,
    [switch]$SkipLint
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot + "\.."
if (-not (Test-Path "$root\gradlew.bat")) {
    Write-Error "Run from repo root or ensure gradlew.bat exists in parent of scripts."
}
Set-Location $root

Write-Host "=== OutOfRouteBuddy debug run ===" -ForegroundColor Cyan
Write-Host ""

if ($Clean) {
    Write-Host "[1/4] Clean..." -ForegroundColor Yellow
    & .\gradlew.bat clean --no-daemon
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host ""
} else {
    Write-Host "[1/4] Clean (skipped). Use -Clean to run clean first." -ForegroundColor Gray
}

Write-Host "[2/4] assembleDebug..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "assembleDebug FAILED." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "assembleDebug OK." -ForegroundColor Green
Write-Host ""

if (-not $SkipTests) {
    Write-Host "[3/4] testDebugUnitTest..." -ForegroundColor Yellow
    & .\gradlew.bat :app:testDebugUnitTest --no-daemon
    if ($LASTEXITCODE -ne 0) {
        Write-Host "testDebugUnitTest FAILED. If lock on test-results, try: .\scripts\debug_app.ps1 -Clean" -ForegroundColor Red
        exit $LASTEXITCODE
    }
    Write-Host "testDebugUnitTest OK." -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "[3/4] testDebugUnitTest (skipped)." -ForegroundColor Gray
}

if (-not $SkipLint) {
    Write-Host "[4/4] lintDebug..." -ForegroundColor Yellow
    & .\gradlew.bat :app:lintDebug --no-daemon
    if ($LASTEXITCODE -ne 0) {
        Write-Host "lintDebug reported issues. Check app\build\reports\ for lint results." -ForegroundColor Yellow
    } else {
        Write-Host "lintDebug OK." -ForegroundColor Green
    }
    Write-Host "Lint report: app\build\reports\lint-results-debug.html (or similar)" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "[4/4] lintDebug (skipped)." -ForegroundColor Gray
}

Write-Host "=== Done ===" -ForegroundColor Cyan
Write-Host "APK: app\build\outputs\apk\debug\app-debug.apk"
Write-Host "Docs: docs\DEBUGGING.md"

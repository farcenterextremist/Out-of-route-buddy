# Run instrumented tests for Statistics and Stat Card (stats + calendar trip cards).
# Clears app data first so TripInputFragment is the first screen, then runs the tests.
# Usage: .\scripts\run-stats-stat-card-instrumented-tests.ps1
# Requires: device/emulator connected, adb in PATH.

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$PackageName = "com.example.outofroutebuddy"
$TestClass = "com.example.outofroutebuddy.StatisticsAndStatCardInstrumentedTest"

Write-Host "Clearing app data for $PackageName so trip screen shows first..."
& adb shell pm clear $PackageName
if ($LASTEXITCODE -ne 0) {
    Write-Warning "adb pm clear failed (device connected?). Continuing anyway..."
}

Write-Host "Running instrumented tests: $TestClass"
Push-Location $ProjectRoot
try {
    & .\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments=class=$TestClass" --no-daemon
    exit $LASTEXITCODE
} finally {
    Pop-Location
}

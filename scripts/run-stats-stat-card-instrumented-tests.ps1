# Run instrumented tests for Statistics and Stat Card (stats + calendar trip cards).
# Clears app data first so TripInputFragment is the first screen, then runs the tests.
# Usage: .\scripts\run-stats-stat-card-instrumented-tests.ps1
# Requires: device/emulator connected, adb in PATH.

$ErrorActionPreference = "Stop"
$PackageName = "com.example.outofroutebuddy"
$TestClass = "com.example.outofroutebuddy.StatisticsAndStatCardInstrumentedTest"

& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "run-instrumented-tests-with-shared-pool-sync.ps1") -PackageName $PackageName -TestClass $TestClass
exit $LASTEXITCODE

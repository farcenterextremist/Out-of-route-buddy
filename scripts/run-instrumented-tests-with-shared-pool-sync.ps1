# Run instrumented tests and then sync the local shared pool.
# Usage:
#   .\scripts\run-instrumented-tests-with-shared-pool-sync.ps1
#   .\scripts\run-instrumented-tests-with-shared-pool-sync.ps1 -TestClass "com.example.FooTest"

[CmdletBinding()]
param(
    [string]$PackageName = "com.example.outofroutebuddy",
    [string]$TestClass = "",
    [string]$MyTruckingBotRoot = ""
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$AutomationScripts = Join-Path $ProjectRoot "scripts\automation"
$testExitCode = 0
$syncExitCode = 0
$frontendAnalyzerExitCode = 0

Write-Host "Clearing app data for $PackageName so the test run starts clean..."
& adb shell pm clear $PackageName
if ($LASTEXITCODE -ne 0) {
    Write-Warning "adb pm clear failed (device connected?). Continuing anyway..."
}

Push-Location $ProjectRoot
try {
    $gradleArgs = @(":app:connectedDebugAndroidTest", "--no-daemon")
    if (-not [string]::IsNullOrWhiteSpace($TestClass)) {
        Write-Host "Running instrumented tests for class: $TestClass"
        $gradleArgs += "-Pandroid.testInstrumentationRunnerArguments=class=$TestClass"
    } else {
        Write-Host "Running instrumented tests: full connectedDebugAndroidTest suite"
    }

    & .\gradlew.bat @gradleArgs
    $testExitCode = $LASTEXITCODE

    Write-Host "Syncing shared pool after instrumented tests..."
    $syncArgs = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $AutomationScripts "sync_shared_pool_after_instrumented_tests.ps1"),
        "-PackageName", $PackageName
    )
    if (-not [string]::IsNullOrWhiteSpace($MyTruckingBotRoot)) {
        $syncArgs += @("-MyTruckingBotRoot", $MyTruckingBotRoot)
    }

    & powershell @syncArgs
    $syncExitCode = $LASTEXITCODE

    Write-Host "Running frontend analyzer after instrumented tests..."
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $AutomationScripts "run_frontend_analyzer_after_instrumented_tests.ps1") -PackageName $PackageName -ReviewName "instrumented_frontend_review"
    $frontendAnalyzerExitCode = $LASTEXITCODE
} finally {
    Pop-Location
}

if ($testExitCode -ne 0) {
    exit $testExitCode
}

if ($syncExitCode -ne 0) {
    exit $syncExitCode
}

exit $frontendAnalyzerExitCode

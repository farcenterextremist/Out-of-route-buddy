# OutOfRouteBuddy Automated Testing Script
# This script runs all automated tests with detailed reporting

param(
    [switch]$UnitTests,
    [switch]$InstrumentationTests,
    [switch]$AllTests,
    
    [switch]$AccessibilityTests,
    [switch]$Verbose
)

Write-Host "OutOfRouteBuddy Automated Testing Suite" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Set default behavior
if (-not ($UnitTests -or $InstrumentationTests -or $AllTests -or $AccessibilityTests)) {
    $AllTests = $true
}

# Function to run tests with proper formatting
function Run-TestCommand {
    param(
        [string]$Command,
        [string]$TestName
    )
    
    Write-Host "\nRunning $TestName..." -ForegroundColor Yellow
    Write-Host "Command: $Command" -ForegroundColor Gray
    
    $startTime = Get-Date
    $result = Invoke-Expression $Command
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "$TestName completed successfully in $($duration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Green
    } else {
        Write-Host "$TestName failed after $($duration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Red
        if ($Verbose) {
            Write-Host "Error output: $result" -ForegroundColor Red
        }
    }
    
    return $LASTEXITCODE -eq 0
}

# Function to check if device/emulator is connected
function Test-DeviceConnected {
    $devices = adb devices
    $connectedDevices = ($devices | Select-String "device$").Count
    
    if ($connectedDevices -eq 0) {
        Write-Host "No Android devices/emulators connected!" -ForegroundColor Red
        Write-Host "Please connect a device or start an emulator before running instrumentation tests." -ForegroundColor Yellow
        return $false
    }
    
    Write-Host "Found $connectedDevices connected device(s)" -ForegroundColor Green
    return $true
}

# Main test execution
$overallSuccess = $true

try {
    # Clean and build project first
    Write-Host "\nBuilding project..." -ForegroundColor Blue
    $buildSuccess = Run-TestCommand ".\gradlew clean build" "Project Build"
    if (-not $buildSuccess) {
        Write-Host "Build failed! Cannot proceed with tests." -ForegroundColor Red
        exit 1
    }
    
    # Run unit tests
    if ($UnitTests -or $AllTests) {
        Write-Host "\nRunning Unit Tests..." -ForegroundColor Cyan
        $unitSuccess = Run-TestCommand ".\gradlew test" "Unit Tests"
        $overallSuccess = $overallSuccess -and $unitSuccess
    }
    
    # Check for connected devices before running instrumentation tests
    if ($InstrumentationTests -or $AllTests -or $AccessibilityTests) {
        if (-not (Test-DeviceConnected)) {
            Write-Host "\nSkipping instrumentation tests - no device connected" -ForegroundColor Yellow
        } else {
            # Run accessibility tests
            if ($AccessibilityTests -or $AllTests) {
                Write-Host "\nRunning Accessibility Tests..." -ForegroundColor Cyan
                $accessibilitySuccess = Run-TestCommand ".\gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.outofroutebuddy.accessibility.AccessibilityTest" "Accessibility Tests"
                $overallSuccess = $overallSuccess -and $accessibilitySuccess
            }
            
            # Run complete test suite
            if ($AllTests) {
                Write-Host "\nRunning Complete Test Suite..." -ForegroundColor Cyan
                $completeSuiteSuccess = Run-TestCommand ".\gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.outofroutebuddy.CompleteTestSuite" "Complete Test Suite"
                $overallSuccess = $overallSuccess -and $completeSuiteSuccess
            }
        }
    }
    
    # Generate test report
    Write-Host "\nTest Execution Summary" -ForegroundColor Green
    Write-Host "=========================" -ForegroundColor Green
    
    if ($overallSuccess) {
        Write-Host "All tests completed successfully!" -ForegroundColor Green
        Write-Host "\nYour OutOfRouteBuddy app is ready for deployment!" -ForegroundColor Green
    } else {
        Write-Host "Some tests failed. Please review the output above." -ForegroundColor Red
        Write-Host "\nCommon troubleshooting steps:" -ForegroundColor Yellow
        Write-Host "  1. Check that all dependencies are properly configured" -ForegroundColor Gray
        Write-Host "  2. Ensure device/emulator is connected for instrumentation tests" -ForegroundColor Gray
        Write-Host "  3. Verify that the app builds successfully" -ForegroundColor Gray
        Write-Host "  4. Check test logs for specific failure details" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "\nUnexpected error occurred: $($_.Exception.Message)" -ForegroundColor Red
    if ($Verbose) {
        Write-Host "Stack trace: $($_.Exception.StackTrace)" -ForegroundColor Red
    }
    exit 1
}

# Exit with appropriate code
if ($overallSuccess) {
    exit 0
} else {
    exit 1
} 
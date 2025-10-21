# 🚀 Build Performance Monitoring Script
# =====================================
# This script monitors build performance and provides insights
# into the effectiveness of build optimizations for OutOfRouteBuddy.

param(
    [string]$Task = "build",
    [switch]$Clean = $false,
    [switch]$Profile = $false,
    [switch]$Verbose = $false
)

Write-Host "OutOfRouteBuddy Build Performance Monitor" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Configuration
$GRADLE_CMD = "./gradlew"
$PROJECT_DIR = Get-Location
$BUILD_LOG_DIR = "$PROJECT_DIR/build-logs"
$PERFORMANCE_LOG = "$BUILD_LOG_DIR/performance.log"

# Create build logs directory if it doesn't exist
if (!(Test-Path $BUILD_LOG_DIR)) {
    New-Item -ItemType Directory -Path $BUILD_LOG_DIR | Out-Null
}

# Function to get system information
function Get-SystemInfo {
    Write-Host "System Information:" -ForegroundColor Yellow
    Write-Host "  CPU Cores: $((Get-WmiObject -Class Win32_Processor).NumberOfCores)" -ForegroundColor Gray
    Write-Host "  Total Memory: $([math]::Round((Get-WmiObject -Class Win32_ComputerSystem).TotalPhysicalMemory / 1GB, 2)) GB" -ForegroundColor Gray
    Write-Host "  Available Memory: $([math]::Round((Get-WmiObject -Class Win32_OperatingSystem).FreePhysicalMemory / 1MB, 2)) GB" -ForegroundColor Gray
    Write-Host "  Java Version: $(java -version 2>&1 | Select-String 'version' | Select-Object -First 1)" -ForegroundColor Gray
    Write-Host ""
}

# Function to measure build time
function Measure-BuildTime {
    param([string]$TaskName)
    
    $startTime = Get-Date
    $logFile = "$BUILD_LOG_DIR/${TaskName}_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
    
    Write-Host "Starting $TaskName..." -ForegroundColor Green
    
    if ($Verbose) {
        & $GRADLE_CMD $TaskName --no-daemon --info 2>&1 | Tee-Object -FilePath $logFile
    } else {
        & $GRADLE_CMD $TaskName --no-daemon 2>&1 | Tee-Object -FilePath $logFile
    }
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    return @{
        Task = $TaskName
        StartTime = $startTime
        EndTime = $endTime
        Duration = $duration
        LogFile = $logFile
        Success = $LASTEXITCODE -eq 0
    }
}

# Function to analyze build performance
function Analyze-BuildPerformance {
    param([array]$BuildResults)
    
    Write-Host "Build Performance Analysis:" -ForegroundColor Yellow
    Write-Host "===============================" -ForegroundColor Yellow
    
    foreach ($result in $BuildResults) {
        $status = if ($result.Success) { "SUCCESS" } else { "FAIL" }
        $duration = $result.Duration.TotalSeconds
        
        Write-Host "  $status $($result.Task): $([math]::Round($duration, 2)) seconds" -ForegroundColor $(if ($result.Success) { "Green" } else { "Red" })
        
        # Log performance data
        $performanceData = @{
            Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Task = $result.Task
            Duration = $duration
            Success = $result.Success
            LogFile = $result.LogFile
        }
        
        $performanceData | ConvertTo-Json | Add-Content -Path $PERFORMANCE_LOG
    }
    
    # Calculate statistics
    $successfulBuilds = $BuildResults | Where-Object { $_.Success }
    if ($successfulBuilds.Count -gt 0) {
        $avgDuration = ($successfulBuilds | ForEach-Object { $_.Duration.TotalSeconds } | Measure-Object -Average).Average
        $minDuration = ($successfulBuilds | ForEach-Object { $_.Duration.TotalSeconds } | Measure-Object -Minimum).Minimum
        $maxDuration = ($successfulBuilds | ForEach-Object { $_.Duration.TotalSeconds } | Measure-Object -Maximum).Maximum
        
        Write-Host ""
        Write-Host "Performance Statistics:" -ForegroundColor Cyan
        Write-Host "  Average Duration: $([math]::Round($avgDuration, 2)) seconds" -ForegroundColor Gray
        Write-Host "  Minimum Duration: $([math]::Round($minDuration, 2)) seconds" -ForegroundColor Gray
        Write-Host "  Maximum Duration: $([math]::Round($maxDuration, 2)) seconds" -ForegroundColor Gray
        $successRate = [math]::Round(($successfulBuilds.Count / $BuildResults.Count) * 100, 1)
        Write-Host ("  Success Rate: " + $successfulBuilds.Count + "/" + $BuildResults.Count + " (" + $successRate + "%)") -ForegroundColor Gray
    }
}

# Function to check build optimizations
function Test-BuildOptimizations {
    Write-Host "Build Optimization Status:" -ForegroundColor Yellow
    Write-Host "=============================" -ForegroundColor Yellow
    
    # Check Gradle properties
    $gradleProps = Get-Content "gradle.properties" -ErrorAction SilentlyContinue
    $optimizations = @{
        "Parallel Execution" = $gradleProps -match "org\.gradle\.parallel=true"
        "Build Caching" = $gradleProps -match "org\.gradle\.caching=true"
        "Configuration Cache" = $gradleProps -match "org\.gradle\.unsafe\.configuration-cache=true"
        "Configure on Demand" = $gradleProps -match "org\.gradle\.configureondemand=true"
        "Enhanced Memory" = $gradleProps -match "org\.gradle\.jvmargs.*-Xmx4096m"
        "Kotlin Incremental" = $gradleProps -match "kotlin\.incremental=true"
        "Kapt Incremental" = $gradleProps -match "kapt\.incremental\.apt=true"
    }
    
    foreach ($opt in $optimizations.GetEnumerator()) {
        $status = if ($opt.Value) { "ENABLED" } else { "DISABLED" }
        Write-Host "  $status $($opt.Key)" -ForegroundColor $(if ($opt.Value) { "Green" } else { "Red" })
    }
    
    Write-Host ""
}

# Function to provide optimization recommendations
function Get-OptimizationRecommendations {
    Write-Host "Optimization Recommendations:" -ForegroundColor Yellow
    Write-Host "=================================" -ForegroundColor Yellow
    
    $recommendations = @(
        "Enable Gradle Enterprise for advanced build analytics",
        "Use Gradle Build Cache for faster incremental builds",
        "Consider using Gradle Build Scans for detailed insights",
        "Optimize dependency versions to reduce resolution time",
        "Use parallel test execution for faster test runs",
        "Enable R8 optimization for smaller APK sizes",
        "Consider using Gradle Configuration Cache for faster project setup"
    )
    
    foreach ($rec in $recommendations) {
        Write-Host "  - $rec" -ForegroundColor Gray
    }
    
    Write-Host ""
}

# Main execution
try {
    # Display system information
    Get-SystemInfo
    
    # Check build optimizations
    Test-BuildOptimizations
    
    # Clean build if requested
    if ($Clean) {
        Write-Host "Performing clean build..." -ForegroundColor Yellow
        & $GRADLE_CMD clean --no-daemon | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Clean failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "Clean completed successfully" -ForegroundColor Green
        Write-Host ""
    }
    
    # Build tasks to measure
    $buildTasks = @($Task)
    if ($Profile) {
        $buildTasks = @("clean", "build", "testDebugUnitTest")
    }
    
    $buildResults = @()
    
    # Measure each build task
    foreach ($buildTask in $buildTasks) {
        $result = Measure-BuildTime -TaskName $buildTask
        $buildResults += $result
        
        if (!$result.Success) {
            Write-Host "$buildTask failed! Check log: $($result.LogFile)" -ForegroundColor Red
        }
    }
    
    # Analyze performance
    Analyze-BuildPerformance -BuildResults $buildResults
    
    # Provide recommendations
    Get-OptimizationRecommendations
    
    # Summary
    $failedTasks = $buildResults | Where-Object { !$_.Success }
    if ($failedTasks.Count -eq 0) {
        Write-Host "All build tasks completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Some build tasks failed. Check logs for details." -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Build logs saved to: $BUILD_LOG_DIR" -ForegroundColor Cyan
    Write-Host "Performance data saved to: $PERFORMANCE_LOG" -ForegroundColor Cyan
    
} catch {
    Write-Host "Error during build performance monitoring: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} 
# ==================== JACOCO COVERAGE ANALYSIS SCRIPT (PowerShell) ====================
# This script provides comprehensive coverage analysis and insights

param(
    [switch]$OpenReport,
    [switch]$Verbose
)

Write-Host "📊 JaCoCo Coverage Analysis Suite" -ForegroundColor Blue
Write-Host "=================================" -ForegroundColor Blue

# Function to print colored output
function Write-Status {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Check if reports exist
function Test-Reports {
    $reportDir = "app\build\reports\jacoco\jacocoTestReport"
    
    if (-not (Test-Path $reportDir)) {
        Write-Status "❌ Coverage reports not found!" "Red"
        Write-Status "Run './gradlew jacocoTestReport' first" "Yellow"
        exit 1
    }
    
    Write-Status "✅ Coverage reports found at: $reportDir" "Green"
}

# Analyze coverage metrics
function Get-CoverageMetrics {
    $xmlReport = "app\build\reports\jacoco\jacocoTestReport\jacocoTestReport.xml"
    
    if (-not (Test-Path $xmlReport)) {
        Write-Status "❌ XML report not found: $xmlReport" "Red"
        return
    }
    
    Write-Status "📈 Analyzing coverage metrics..." "Blue"
    
    # Read XML content
    $xmlContent = Get-Content $xmlReport -Raw
    
    # Extract coverage percentages using regex
    $lineRateMatch = [regex]::Match($xmlContent, 'line-rate="([0-9.]+)"')
    $branchRateMatch = [regex]::Match($xmlContent, 'branch-rate="([0-9.]+)"')
    $instructionRateMatch = [regex]::Match($xmlContent, 'instruction-rate="([0-9.]+)"')
    
    $overallCoverage = if ($lineRateMatch.Success) { [math]::Round([double]$lineRateMatch.Groups[1].Value * 100, 1) } else { 0 }
    $branchCoverage = if ($branchRateMatch.Success) { [math]::Round([double]$branchRateMatch.Groups[1].Value * 100, 1) } else { 0 }
    $instructionCoverage = if ($instructionRateMatch.Success) { [math]::Round([double]$instructionRateMatch.Groups[1].Value * 100, 1) } else { 0 }
    
    Write-Host ""
    Write-Status "📊 COVERAGE METRICS:" "Blue"
    Write-Host "  Overall Coverage: $overallCoverage%"
    Write-Host "  Branch Coverage:   $branchCoverage%"
    Write-Host "  Instruction Coverage: $instructionCoverage%"
    Write-Host ""
    
    # Coverage thresholds
    $overallThreshold = 70.0
    $branchThreshold = 60.0
    $instructionThreshold = 75.0
    
    Write-Status "🎯 COVERAGE THRESHOLDS:" "Blue"
    Write-Host "  Overall:    $overallThreshold% (Current: $overallCoverage%)"
    Write-Host "  Branch:     $branchThreshold% (Current: $branchCoverage%)"
    Write-Host "  Instruction: $instructionThreshold% (Current: $instructionCoverage%)"
    Write-Host ""
    
    # Check thresholds
    $overallPass = $overallCoverage -ge $overallThreshold
    $branchPass = $branchCoverage -ge $branchThreshold
    $instructionPass = $instructionCoverage -ge $instructionThreshold
    
    if ($overallPass -and $branchPass -and $instructionPass) {
        Write-Status "✅ All coverage thresholds met!" "Green"
    } else {
        Write-Status "❌ Some coverage thresholds not met:" "Red"
        if (-not $overallPass) { Write-Host "  - Overall coverage below $overallThreshold%" }
        if (-not $branchPass) { Write-Host "  - Branch coverage below $branchThreshold%" }
        if (-not $instructionPass) { Write-Host "  - Instruction coverage below $instructionThreshold%" }
    }
    
    return @{
        Overall = $overallCoverage
        Branch = $branchCoverage
        Instruction = $instructionCoverage
        OverallPass = $overallPass
        BranchPass = $branchPass
        InstructionPass = $instructionPass
    }
}

# Find low coverage classes
function Find-LowCoverageClasses {
    $xmlReport = "app\build\reports\jacoco\jacocoTestReport\jacocoTestReport.xml"
    
    Write-Status "🔍 Classes with low coverage (< 60%):" "Yellow"
    
    # Read XML and find classes with low coverage
    $xmlContent = Get-Content $xmlReport -Raw
    $classMatches = [regex]::Matches($xmlContent, '<class name="([^"]+)"[^>]*line-rate="0\.[0-5]')
    
    $lowCoverageClasses = @()
    foreach ($match in $classMatches) {
        $className = $match.Groups[1].Value
        $lowCoverageClasses += $className
    }
    
    if ($lowCoverageClasses.Count -gt 0) {
        $lowCoverageClasses | Select-Object -First 10 | ForEach-Object {
            Write-Host "  - $_"
        }
    } else {
        Write-Host "  No classes found with coverage < 60%"
    }
    
    Write-Host ""
    Write-Status "💡 Consider adding tests for these classes" "Yellow"
}

# Generate coverage summary
function Show-CoverageSummary {
    $htmlReport = "app\build\reports\jacoco\jacocoTestReport\html\index.html"
    
    Write-Status "📋 COVERAGE SUMMARY:" "Blue"
    Write-Host ""
    Write-Host "📁 HTML Report: $htmlReport"
    Write-Host "📄 XML Report:  app\build\reports\jacoco\jacocoTestReport\jacocoTestReport.xml"
    Write-Host "📊 CSV Report:  app\build\reports\jacoco\jacocoTestReport\jacocoTestReport.csv"
    Write-Host ""
    Write-Host "🌐 Open HTML report in browser:"
    Write-Host "   file:///$((Get-Location).Path.Replace('\', '/'))/$($htmlReport.Replace('\', '/'))"
    Write-Host ""
    
    if ($OpenReport) {
        Write-Status "🌐 Opening HTML report..." "Green"
        Start-Process $htmlReport
    }
}

# Generate coverage badge
function New-CoverageBadge {
    param($CoverageData)
    
    $coverage = $CoverageData.Overall
    $color = if ($coverage -ge 80) { "brightgreen" } 
             elseif ($coverage -ge 70) { "green" }
             elseif ($coverage -ge 60) { "yellow" }
             else { "red" }
    
    $badgeUrl = "https://img.shields.io/badge/coverage-${coverage}%25-${color}"
    
    Write-Host ""
    Write-Status "🏆 COVERAGE BADGE:" "Blue"
    Write-Host "   $badgeUrl"
    Write-Host ""
    Write-Host "   Add to README.md:"
    Write-Host "   ![Coverage]($badgeUrl)" -ForegroundColor Gray
}

# Main execution
function Main {
    Write-Host "Starting coverage analysis..." -ForegroundColor Blue
    Write-Host ""
    
    Test-Reports
    $coverageData = Get-CoverageMetrics
    Find-LowCoverageClasses
    Show-CoverageSummary
    New-CoverageBadge -CoverageData $coverageData
    
    Write-Status "🎉 Coverage analysis complete!" "Green"
    Write-Host ""
    Write-Status "Next steps:" "Blue"
    Write-Host "  1. Review HTML report for detailed coverage"
    Write-Host "  2. Add tests for low-coverage classes"
    Write-Host "  3. Run './gradlew jacocoCoverageVerification' to verify thresholds"
    Write-Host "  4. Integrate with CI/CD pipeline"
    Write-Host "  5. Use -OpenReport flag to open HTML report automatically"
}

# Run main function
Main

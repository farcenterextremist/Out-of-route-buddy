#!/bin/bash

# ==================== JACOCO COVERAGE ANALYSIS SCRIPT ====================
# This script provides comprehensive coverage analysis and insights

set -e

echo "📊 JaCoCo Coverage Analysis Suite"
echo "================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Check if reports exist
check_reports() {
    local report_dir="app/build/reports/jacoco/jacocoTestReport"
    
    if [ ! -d "$report_dir" ]; then
        print_status $RED "❌ Coverage reports not found!"
        print_status $YELLOW "Run './gradlew jacocoTestReport' first"
        exit 1
    fi
    
    print_status $GREEN "✅ Coverage reports found at: $report_dir"
}

# Analyze coverage metrics
analyze_coverage() {
    local xml_report="app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
    
    if [ ! -f "$xml_report" ]; then
        print_status $RED "❌ XML report not found: $xml_report"
        return 1
    fi
    
    print_status $BLUE "📈 Analyzing coverage metrics..."
    
    # Extract coverage percentages using grep and awk
    local overall_coverage=$(grep -o 'line-rate="[0-9.]*"' "$xml_report" | head -1 | grep -o '[0-9.]*' | awk '{printf "%.1f", $1 * 100}')
    local branch_coverage=$(grep -o 'branch-rate="[0-9.]*"' "$xml_report" | head -1 | grep -o '[0-9.]*' | awk '{printf "%.1f", $1 * 100}')
    local instruction_coverage=$(grep -o 'instruction-rate="[0-9.]*"' "$xml_report" | head -1 | grep -o '[0-9.]*' | awk '{printf "%.1f", $1 * 100}')
    
    echo ""
    print_status $BLUE "📊 COVERAGE METRICS:"
    echo "  Overall Coverage: ${overall_coverage}%"
    echo "  Branch Coverage:   ${branch_coverage}%"
    echo "  Instruction Coverage: ${instruction_coverage}%"
    echo ""
    
    # Coverage thresholds
    local overall_threshold=70.0
    local branch_threshold=60.0
    local instruction_threshold=75.0
    
    print_status $BLUE "🎯 COVERAGE THRESHOLDS:"
    echo "  Overall:    ${overall_threshold}% (Current: ${overall_coverage}%)"
    echo "  Branch:     ${branch_threshold}% (Current: ${branch_coverage}%)"
    echo "  Instruction: ${instruction_threshold}% (Current: ${instruction_coverage}%)"
    echo ""
    
    # Check thresholds
    local overall_pass=$(echo "$overall_coverage >= $overall_threshold" | bc -l)
    local branch_pass=$(echo "$branch_coverage >= $branch_threshold" | bc -l)
    local instruction_pass=$(echo "$instruction_coverage >= $instruction_threshold" | bc -l)
    
    if [ "$overall_pass" -eq 1 ] && [ "$branch_pass" -eq 1 ] && [ "$instruction_pass" -eq 1 ]; then
        print_status $GREEN "✅ All coverage thresholds met!"
    else
        print_status $RED "❌ Some coverage thresholds not met:"
        [ "$overall_pass" -eq 0 ] && echo "  - Overall coverage below ${overall_threshold}%"
        [ "$branch_pass" -eq 0 ] && echo "  - Branch coverage below ${branch_threshold}%"
        [ "$instruction_pass" -eq 0 ] && echo "  - Instruction coverage below ${instruction_threshold}%"
    fi
}

# Find low coverage classes
find_low_coverage() {
    local xml_report="app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
    
    print_status $YELLOW "🔍 Classes with low coverage (< 60%):"
    
    # Extract classes with low coverage
    grep -A 5 -B 5 'line-rate="0\.[0-5]' "$xml_report" | grep 'name=' | sed 's/.*name="\([^"]*\)".*/\1/' | head -10
    
    echo ""
    print_status $YELLOW "💡 Consider adding tests for these classes"
}

# Generate coverage summary
generate_summary() {
    local html_report="app/build/reports/jacoco/jacocoTestReport/html/index.html"
    
    print_status $BLUE "📋 COVERAGE SUMMARY:"
    echo ""
    echo "📁 HTML Report: $html_report"
    echo "📄 XML Report:  app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
    echo "📊 CSV Report:  app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.csv"
    echo ""
    echo "🌐 Open HTML report in browser:"
    echo "   file://$(pwd)/$html_report"
    echo ""
}

# Main execution
main() {
    echo "Starting coverage analysis..."
    echo ""
    
    check_reports
    analyze_coverage
    find_low_coverage
    generate_summary
    
    print_status $GREEN "🎉 Coverage analysis complete!"
    echo ""
    print_status $BLUE "Next steps:"
    echo "  1. Review HTML report for detailed coverage"
    echo "  2. Add tests for low-coverage classes"
    echo "  3. Run './gradlew coverageCheck' to verify thresholds"
    echo "  4. Integrate with CI/CD pipeline"
}

# Run main function
main "$@"

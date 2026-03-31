# Simple debugging + code cleanup automation for OutOfRouteBuddy
# Usage:
#   .\scripts\automation\run_simple_debug_cleanup.ps1
#   .\scripts\automation\run_simple_debug_cleanup.ps1 -Clean
#   .\scripts\automation\run_simple_debug_cleanup.ps1 -SkipDetekt
#   .\scripts\automation\run_simple_debug_cleanup.ps1 -DryRun

[CmdletBinding()]
param(
    [switch]$Clean,
    [switch]$SkipTests,
    [switch]$SkipLint,
    [switch]$SkipDetekt,
    [switch]$Assemble,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not (Test-Path (Join-Path $repoRoot "gradlew.bat"))) {
    throw "Could not find gradlew.bat. Run this script from the OutOfRouteBuddy workspace."
}

function Invoke-GradleStep {
    param(
        [string]$Label,
        [string[]]$Arguments
    )

    $commandText = ".\gradlew.bat " + ($Arguments -join " ")
    Write-Host ""
    Write-Host "[$Label] $commandText" -ForegroundColor Yellow

    if ($DryRun) {
        return [pscustomobject]@{
            Step = $Label
            Command = $commandText
            ExitCode = 0
            DurationSeconds = 0
            Status = "DRY_RUN"
        }
    }

    $start = Get-Date
    & .\gradlew.bat @Arguments
    $exitCode = $LASTEXITCODE
    $duration = [math]::Round(((Get-Date) - $start).TotalSeconds, 1)

    return [pscustomobject]@{
        Step = $Label
        Command = $commandText
        ExitCode = $exitCode
        DurationSeconds = $duration
        Status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }
    }
}

Push-Location $repoRoot
try {
    Write-Host "=== Simple Debug + Cleanup Automation ===" -ForegroundColor Cyan
    Write-Host "Repo: $repoRoot" -ForegroundColor Gray
    if ($DryRun) {
        Write-Host "Dry run enabled: commands will be printed but not executed." -ForegroundColor Gray
    }

    $results = New-Object System.Collections.Generic.List[object]

    if ($Clean) {
        $results.Add((Invoke-GradleStep -Label "Clean" -Arguments @("clean", "--no-daemon")))
    }

    if ($Assemble) {
        $results.Add((Invoke-GradleStep -Label "Assemble Debug" -Arguments @(":app:assembleDebug", "--no-daemon")))
    }

    if (-not $SkipTests) {
        $results.Add((Invoke-GradleStep -Label "Unit Tests" -Arguments @(":app:testDebugUnitTest", "--no-daemon")))
    }

    if (-not $SkipLint) {
        $results.Add((Invoke-GradleStep -Label "Android Lint" -Arguments @(":app:lintDebug", "--no-daemon")))
    }

    if (-not $SkipDetekt) {
        $results.Add((Invoke-GradleStep -Label "Detekt" -Arguments @(":app:detekt", "--no-daemon")))
    }

    Write-Host ""
    Write-Host "=== Summary ===" -ForegroundColor Cyan
    $results | Format-Table Step, Status, ExitCode, DurationSeconds -AutoSize

    if (-not $DryRun) {
        Write-Host ""
        Write-Host "Useful reports:" -ForegroundColor Cyan
        Write-Host "- Unit tests: app\build\reports\tests\testDebugUnitTest\index.html" -ForegroundColor Gray
        Write-Host "- Lint: app\build\reports\lint-results-debug.html" -ForegroundColor Gray
        Write-Host "- Detekt: app\build\reports\detekt\detekt.html" -ForegroundColor Gray
    }

    $failed = $results | Where-Object { $_.ExitCode -ne 0 }
    if ($failed.Count -gt 0) {
        Write-Host ""
        Write-Host "One or more debug/cleanup checks failed." -ForegroundColor Red
        exit 1
    }

    Write-Host ""
    Write-Host "Simple debug + cleanup automation finished successfully." -ForegroundColor Green
} finally {
    Pop-Location
}

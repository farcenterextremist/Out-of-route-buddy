# Loop diagnostic baseline for repeatable problem-hunt evidence.
# Usage:
#   .\scripts\automation\run_loop_diagnostic_baseline.ps1
#   .\scripts\automation\run_loop_diagnostic_baseline.ps1 -FocusPaths app/src,scripts/automation
#   .\scripts\automation\run_loop_diagnostic_baseline.ps1 -ResidualRisk "Recovery flow still needs emulator proof"

[CmdletBinding()]
param(
    [string[]]$FocusPaths = @("app/src", "scripts/automation", "docs/automation"),
    [string]$HotspotPattern = "TODO|FIXME|fallback|\bdeprecated\b|\bwarning\b",
    [int]$HotspotLimit = 12,
    [int]$IgnoredTestLimit = 8,
    [string]$ResidualRisk = "",
    [string]$NextDiagnosticStep = "",
    [string]$OutputPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$automationDir = Join-Path $repoRoot "docs\automation"
$healthStatePath = Join-Path $automationDir "loop_health_state.json"
$ignoredTestsPath = Join-Path $repoRoot "docs\qa\FAILING_OR_IGNORED_TESTS.md"

function Resolve-FocusPath {
    param([string]$PathValue)

    if ([string]::IsNullOrWhiteSpace($PathValue)) {
        return $null
    }

    $candidate = if ([System.IO.Path]::IsPathRooted($PathValue)) {
        $PathValue
    } else {
        Join-Path $repoRoot $PathValue
    }

    if (Test-Path $candidate) {
        return (Resolve-Path $candidate).Path
    }

    return $null
}

function Expand-FocusPaths {
    param([string[]]$PathValues)

    $expanded = New-Object System.Collections.Generic.List[string]
    foreach ($pathValue in $PathValues) {
        if ([string]::IsNullOrWhiteSpace($pathValue)) {
            continue
        }

        $segments = $pathValue.Split(',') | ForEach-Object { $_.Trim() } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
        foreach ($segment in $segments) {
            $expanded.Add($segment)
        }
    }

    return $expanded
}

function Get-EnvironmentSummary {
    if (-not (Test-Path $healthStatePath)) {
        return "unknown (no loop health state file found)"
    }

    try {
        $state = Get-Content -Raw -Path $healthStatePath | ConvertFrom-Json
        $status = if ($state.status) { $state.status } else { "unknown" }
        $checkParts = @()
        if ($state.checks) {
            foreach ($property in $state.checks.PSObject.Properties) {
                $checkParts += "$($property.Name)=$($property.Value)"
            }
        }

        $checksText = if ($checkParts.Count -gt 0) {
            $checkParts -join ", "
        } else {
            "no detailed checks"
        }

        return "$status (lastCheck=$($state.lastCheck); $checksText)"
    } catch {
        return "degraded (could not parse loop health state: $($_.Exception.Message))"
    }
}

function Get-IgnoredTestFindings {
    if (-not (Test-Path $ignoredTestsPath)) {
        return @("Ignored/failing tests doc missing: docs/qa/FAILING_OR_IGNORED_TESTS.md")
    }

    $results = New-Object System.Collections.Generic.List[string]
    $lines = Get-Content -Path $ignoredTestsPath

    foreach ($line in $lines) {
        if ($line -notmatch '^\|\s+\*\*') {
            continue
        }

        $columns = $line.Split('|')
        if ($columns.Count -lt 4) {
            continue
        }

        $testClass = $columns[1].Trim()
        $reason = $columns[2].Trim()
        $owner = $columns[3].Trim()
        $fixBy = $columns[4].Trim()

        if ($reason -match 'resolved' -or $fixBy -match 'N/A \(resolved\)' -or $fixBy -eq 'N/A (resolved).') {
            continue
        }

        $results.Add("$testClass - $reason (owner: $owner; fix/defer: $fixBy)")
        if ($results.Count -ge $IgnoredTestLimit) {
            break
        }
    }

    if ($results.Count -eq 0) {
        $results.Add("No active ignored/failing test rows found in FAILING_OR_IGNORED_TESTS.md")
    }

    return $results
}

function Get-HotspotFindings {
    param(
        [string[]]$ResolvedPaths
    )

    $results = New-Object System.Collections.Generic.List[string]
    if ($ResolvedPaths.Count -eq 0) {
        $results.Add("No valid focus paths were found for hotspot search.")
        return $results
    }

    $rgCommand = Get-Command rg -ErrorAction SilentlyContinue
    if ($rgCommand) {
        try {
            $rgArgs = @(
                "--line-number",
                "--no-heading",
                "-i",
                "--color", "never",
                "--glob", "*.{kt,kts,md,xml,ps1}"
                $HotspotPattern
            ) + $ResolvedPaths

            $rawMatches = & $rgCommand.Source @rgArgs 2>$null
            foreach ($match in $rawMatches | Select-Object -First $HotspotLimit) {
                $results.Add($match)
            }
        } catch {
            $results.Add("Hotspot search failed with rg: $($_.Exception.Message)")
        }
    } else {
        foreach ($path in $ResolvedPaths) {
            try {
                $candidateFiles = Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue |
                    Where-Object { $_.Extension -in @(".kt", ".kts", ".md", ".xml", ".ps1") }
                $matches = $candidateFiles | Select-String -Pattern $HotspotPattern -ErrorAction SilentlyContinue
                foreach ($match in $matches | Select-Object -First $HotspotLimit) {
                    $results.Add("$($match.Path):$($match.LineNumber): $($match.Line.Trim())")
                }
                if ($results.Count -ge $HotspotLimit) {
                    break
                }
            } catch {
                $results.Add("Hotspot search fallback failed for ${path}: $($_.Exception.Message)")
            }
        }
    }

    if ($results.Count -eq 0) {
        $results.Add("No hotspot matches found for pattern '$HotspotPattern'.")
    }

    return $results
}

function Get-ReportAvailability {
    $reportPaths = @(
        "app\build\reports\tests\testDebugUnitTest\index.html",
        "app\build\reports\lint-results-debug.html",
        "app\build\reports\detekt\detekt.html"
    )

    $results = New-Object System.Collections.Generic.List[string]
    foreach ($relativePath in $reportPaths) {
        $fullPath = Join-Path $repoRoot $relativePath
        if (Test-Path $fullPath) {
            $results.Add("available: $relativePath")
        } else {
            $results.Add("missing: $relativePath")
        }
    }

    return $results
}

$normalizedFocusPaths = Expand-FocusPaths -PathValues $FocusPaths
$resolvedFocusPaths = @()
foreach ($focusPath in $normalizedFocusPaths) {
    $resolvedPath = Resolve-FocusPath -PathValue $focusPath
    if ($resolvedPath) {
        $resolvedFocusPaths += $resolvedPath
    }
}

$environmentSummary = Get-EnvironmentSummary
$ignoredTests = Get-IgnoredTestFindings
$hotspots = Get-HotspotFindings -ResolvedPaths $resolvedFocusPaths
$reports = Get-ReportAvailability
$focusPathsText = if ($normalizedFocusPaths.Count -gt 0) {
    $normalizedFocusPaths -join ", "
} else {
    "none"
}

$problemSearchSummary = @(
    "ignored/failing tests from docs/qa/FAILING_OR_IGNORED_TESTS.md",
    "hotspot regex '$HotspotPattern' across: $focusPathsText",
    "report presence for unit tests, lint, and detekt"
)

$findingLines = New-Object System.Collections.Generic.List[string]
if ($ignoredTests.Count -gt 0) {
    $findingLines.Add("Ignored/failing tests:")
    foreach ($item in $ignoredTests) {
        $findingLines.Add("- $item")
    }
}

if ($hotspots.Count -gt 0) {
    $findingLines.Add("Hotspots:")
    foreach ($item in $hotspots) {
        $findingLines.Add("- $item")
    }
}

if ($reports.Count -gt 0) {
    $findingLines.Add("Reports:")
    foreach ($item in $reports) {
        $findingLines.Add("- $item")
    }
}

if ([string]::IsNullOrWhiteSpace($ResidualRisk)) {
    $ResidualRisk = "Review the listed hotspots and ignored tests; at least one may still hide a regression path if the current loop touches that area."
}

if ([string]::IsNullOrWhiteSpace($NextDiagnosticStep)) {
    $NextDiagnosticStep = "Pick the highest-risk hotspot or ignored test above and validate it with a focused code read, targeted test run, or manual repro plan."
}

$outputLines = New-Object System.Collections.Generic.List[string]
$outputLines.Add("## Diagnostic Sweep")
$outputLines.Add("")
$outputLines.Add("- Environment: $environmentSummary")
$outputLines.Add("- Problem search: $($problemSearchSummary -join '; ')")
$outputLines.Add("- Findings:")
foreach ($line in $findingLines) {
    $outputLines.Add("  $line")
}
$outputLines.Add("- Residual risk: $ResidualRisk")
$outputLines.Add("- Next diagnostic step: $NextDiagnosticStep")

$outputText = $outputLines -join [Environment]::NewLine
Write-Host $outputText

if (-not [string]::IsNullOrWhiteSpace($OutputPath)) {
    $destination = if ([System.IO.Path]::IsPathRooted($OutputPath)) {
        $OutputPath
    } else {
        Join-Path $repoRoot $OutputPath
    }

    $destinationDir = Split-Path -Parent $destination
    if ($destinationDir -and -not (Test-Path $destinationDir)) {
        New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
    }

    $outputText | Set-Content -Path $destination -Encoding UTF8
    Write-Host ""
    Write-Host "Saved diagnostic sweep to $destination"
}

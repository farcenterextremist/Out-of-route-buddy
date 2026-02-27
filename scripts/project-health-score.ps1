# Project Health Score — parses project-examination doc and computes overall health
# Usage: .\scripts\project-health-score.ps1 [ -DocPath path ]
# Output: Overall health WITH and WITHOUT Feature Completeness

param(
    [string]$DocPath = "docs\agents\data-sets\project-examination-and-agent-assignment.md"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }
$fullPath = Join-Path $root $DocPath

if (-not (Test-Path $fullPath)) {
    Write-Host "Doc not found: $fullPath" -ForegroundColor Red
    exit 1
}

$content = Get-Content $fullPath -Raw

# Parse Summary table: | Category | Score | Priority |
$scores = @{}
$inSummary = $false
foreach ($line in ($content -split "`n")) {
    if ($line -match "^\|\s*Category\s*\|\s*Score\s*\|") { $inSummary = $true; continue }
    if ($inSummary -and $line -match "^\|\s*[-]+\s*\|") { continue }
    if ($inSummary -and $line -match "^\|\s*(\w[\w\s/]*?)\s*\|\s*(\d+)%?\s*\|") {
        $cat = $matches[1].Trim()
        $pct = [int]$matches[2]
        $scores[$cat] = $pct
    }
    if ($inSummary -and $line -match "^\|\s*Overall project health") { break }
}

if ($scores.Count -eq 0) {
    Write-Host "Could not parse scores from doc." -ForegroundColor Red
    exit 1
}

# Compute
$allCats = $scores.Keys | Sort-Object
$withFC = $allCats
$withoutFC = $allCats | Where-Object { $_ -ne "Feature Completeness" }

$avgWith = if ($withFC.Count -gt 0) {
    [math]::Round(($withFC | ForEach-Object { $scores[$_] } | Measure-Object -Average).Average, 1)
} else { 0 }
$avgWithout = if ($withoutFC.Count -gt 0) {
    [math]::Round(($withoutFC | ForEach-Object { $scores[$_] } | Measure-Object -Average).Average, 1)
} else { 0 }

# Output
Write-Host ""
Write-Host "Project Health Score (from $DocPath)" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
foreach ($c in $allCats) {
    $pct = $scores[$c]
    $bar = "=" * [math]::Min(20, [math]::Floor($pct / 5)) + " " * (20 - [math]::Min(20, [math]::Floor($pct / 5)))
    Write-Host ("  {0,-25} {1,3}%  [{2}]" -f $c, $pct, $bar)
}
Write-Host ""
Write-Host "------------------------------------" -ForegroundColor Gray
Write-Host ""
Write-Host "  WITH Feature Completeness:    $avgWith%" -ForegroundColor White
Write-Host "  WITHOUT Feature Completeness: $avgWithout%" -ForegroundColor White
Write-Host ""

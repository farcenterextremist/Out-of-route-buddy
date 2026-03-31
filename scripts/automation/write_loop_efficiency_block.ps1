# Persist a loop-efficiency markdown block into a summary or ledger file.
# Run from repo root:
# .\scripts\automation\write_loop_efficiency_block.ps1 -TargetPath docs/automation/IMPROVEMENT_LOOP_SUMMARY_2026-03-16.md -RunId run-20260316-123000 -Score 100 -Grade A -ProgressBar "[####################] 100%"

param(
    [Parameter(Mandatory = $true)]
    [string]$TargetPath,

    [Parameter(Mandatory = $true)]
    [string]$RunId,

    [Parameter(Mandatory = $true)]
    [int]$Score,

    [Parameter(Mandatory = $true)]
    [string]$Grade,

    [Parameter(Mandatory = $true)]
    [string]$ProgressBar,

    [string]$WhyItMoved = "Loop-design hardening state refreshed from current automation evidence."
)

$ErrorActionPreference = "Stop"

$markerStart = "<!-- LOOP_EFFICIENCY_BLOCK_START:$RunId -->"
$markerEnd = "<!-- LOOP_EFFICIENCY_BLOCK_END:$RunId -->"

$block = @"
$markerStart
## Loop Efficiency Score

- Efficiency score: $Score/100
- Progress bar: $ProgressBar
- Grade: $Grade
- Why it moved: $WhyItMoved
$markerEnd
"@

if (-not (Test-Path $TargetPath)) {
    throw "Target file does not exist: $TargetPath"
}

$content = Get-Content -Path $TargetPath -Raw
$escapedStart = [Regex]::Escape($markerStart)
$escapedEnd = [Regex]::Escape($markerEnd)
$pattern = "(?s)$escapedStart.*?$escapedEnd"

if ($content -match $pattern) {
    $updated = [Regex]::Replace($content, $pattern, $block)
} else {
    $trimmed = $content.TrimEnd("`r", "`n")
    if ([string]::IsNullOrWhiteSpace($trimmed)) {
        $updated = $block + [Environment]::NewLine
    } else {
        $updated = $trimmed + [Environment]::NewLine + [Environment]::NewLine + $block + [Environment]::NewLine
    }
}

Set-Content -Path $TargetPath -Value $updated -Encoding UTF8
Write-Host "Loop efficiency block written -> $TargetPath"

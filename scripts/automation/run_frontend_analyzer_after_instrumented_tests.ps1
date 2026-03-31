# Capture frontend evidence after instrumented tests and create a review stub
# that follows the screenshot review and pleasantness standards.

[CmdletBinding()]
param(
    [string]$ReviewName = "post_instrumented_frontend",
    [string]$PackageName = "com.example.outofroutebuddy",
    [ValidateSet("", "settings")]
    [string]$OpenDestination = ""
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$ReviewRoot = Join-Path $RepoRoot "docs\ux\reviews\instrumented"
New-Item -ItemType Directory -Force -Path $ReviewRoot | Out-Null

$Timestamp = Get-Date -Format "yyyy-MM-dd_HHmmss"
$SafeReviewName = ($ReviewName -replace "[^A-Za-z0-9_-]", "_")
$ScreenshotPath = Join-Path $ReviewRoot "${Timestamp}_${SafeReviewName}.png"
$ReviewPath = Join-Path $ReviewRoot "${Timestamp}_${SafeReviewName}.md"

$ScreenshotCaptured = $false
$LaunchActivity = "$PackageName.SplashActivity"

if ($OpenDestination) {
    Write-Host "Opening $OpenDestination before screenshot capture..."
    try {
        & adb shell am start -S -n "$PackageName/$LaunchActivity" --es "open_destination" "$OpenDestination" | Out-Null
        Start-Sleep -Seconds 4
    } catch {
        Write-Warning "Could not open destination '$OpenDestination' before capture: $($_.Exception.Message)"
    }
}

Write-Host "Capturing frontend screenshot for analyzer review..."
try {
    & cmd /c "adb exec-out screencap -p > `"$ScreenshotPath`""
    if ($LASTEXITCODE -eq 0 -and (Test-Path $ScreenshotPath) -and ((Get-Item $ScreenshotPath).Length -gt 0)) {
        $ScreenshotCaptured = $true
    } else {
        Write-Warning "Screenshot capture did not produce a usable PNG."
    }
} catch {
    Write-Warning "Frontend analyzer screenshot capture failed: $($_.Exception.Message)"
}

$EvidenceLine = if ($ScreenshotCaptured) {
    "Current-state screenshot captured at ``$ScreenshotPath``."
} else {
    "No screenshot captured. Re-run on a connected device/emulator and capture evidence with ``adb exec-out screencap -p``."
}

$ReviewContent = @"
# Instrumented Frontend Analyzer Review Stub

**Created:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Package:** $PackageName
**Review name:** $ReviewName
**Evidence:** $EvidenceLine
**References:** `docs/ux/SCREENSHOT_REVIEW_WORKFLOW.md`, `docs/ux/PLEASANTNESS_AND_FLOW_STANDARD.md`

## Screenshot Review

- Screen: [fill in current screen/flow name]
- Evidence set: [current-state capture, light/dark if available, interaction state]
- Pleasantness score: X/20
- Strongest visual trait: [one line]
- Weakest visual trait: [one line]
- Flow clarity: [clear | mostly clear | confusing]
- Recommendation: [acceptable | polish next | structural proposal only]

## Pleasantness and Flow Review

- Screen or flow: [name]
- Pleasantness score: X/20
- Strongest area: [one line]
- Weakest area: [one line]
- Evidence: [docs, screenshots, code, or checks]
- Decision: [acceptable | improve before shipping | proposal only]

## Notes

- Use `claim -> evidence -> residual weakness -> next step`.
- Do not approve structural UI changes without user approval.
- If instrumented tests changed the visible UI state, compare this capture to the previous capture when available.
"@

Set-Content -Path $ReviewPath -Value $ReviewContent -Encoding UTF8

Write-Host "Frontend analyzer review stub written to: $ReviewPath"
if ($ScreenshotCaptured) {
    Write-Host "Frontend analyzer screenshot written to: $ScreenshotPath"
}

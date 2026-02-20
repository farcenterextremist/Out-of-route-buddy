# Send a preset "big change" email via PowerShell (.NET SMTP). No Python required.
# Usage: .\send_phase_completion_email.ps1 -Preset phase_abc
param(
    [Parameter(Mandatory = $true)][string]$Preset
)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$presets = @{
    "phase_abc" = @{
        Subject = "OutOfRouteBuddy: Emulator Phase A/B/C complete - summary"
        BodyFile = "phase_abc_completion_body.txt"
    }
}
$p = $presets[$Preset.ToLower()]
if (-not $p) {
    Write-Error "Unknown preset: $Preset. Available: $($presets.Keys -join ', ')"
    exit 1
}
$bodyPath = Join-Path $ScriptDir $p.BodyFile
if (-not (Test-Path $bodyPath)) {
    Write-Error "Body file not found: $bodyPath"
    exit 1
}
& (Join-Path $ScriptDir "send_email.ps1") -Subject $p.Subject -BodyFile $p.BodyFile

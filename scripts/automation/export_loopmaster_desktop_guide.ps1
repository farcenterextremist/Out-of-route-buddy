# Export Loopmaster desktop guide from repo source of truth.
# Run from repo root: .\scripts\automation\export_loopmaster_desktop_guide.ps1

param(
    [string]$SourcePath = "docs\automation\LOOPMASTER_DESKTOP_GUIDE_SOURCE.txt",
    [string]$TargetPath = "$env:USERPROFILE\Desktop\Loopmaster_Best_Practices_and_Design_Flow.txt"
)

$ErrorActionPreference = "Stop"

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
Set-Location $RepoRoot

$resolvedSource = Join-Path $RepoRoot $SourcePath
if (-not (Test-Path $resolvedSource)) {
    Write-Error "Source guide not found: $resolvedSource"
}

$targetDir = Split-Path -Parent $TargetPath
if (-not [string]::IsNullOrWhiteSpace($targetDir) -and -not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
}

$content = Get-Content $resolvedSource -Raw
Set-Content -Path $TargetPath -Value $content -Encoding ascii

Write-Host "Exported Loopmaster desktop guide:"
Write-Host "  Source: $resolvedSource"
Write-Host "  Target: $TargetPath"

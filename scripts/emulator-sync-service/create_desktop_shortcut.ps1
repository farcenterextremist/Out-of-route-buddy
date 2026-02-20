# Create a desktop shortcut that opens the phone emulator in one click.
# Run from repo root: powershell -ExecutionPolicy Bypass -File scripts\emulator-sync-service\create_desktop_shortcut.ps1

$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }

# Repo root = two levels up from scripts\emulator-sync-service
$RepoRoot = (Resolve-Path (Join-Path $ScriptDir "..\..")).Path
$LaunchBat = Join-Path $RepoRoot "scripts\launch_emulator.bat"

$Desktop = [Environment]::GetFolderPath("Desktop")
$ShortcutPath = Join-Path $Desktop "OutOfRouteBuddy Emulator.lnk"

$ws = New-Object -ComObject WScript.Shell
$s = $ws.CreateShortcut($ShortcutPath)
$s.TargetPath = $LaunchBat
$s.WorkingDirectory = $RepoRoot
$s.Description = "Open the OutOfRouteBuddy phone emulator. Edits sync to the project so Cursor sees updates; keep the other two windows open while you work."

# Stylized icon
$CustomIcon = Join-Path $ScriptDir "icon.ico"
if (Test-Path $CustomIcon) {
    $s.IconLocation = $CustomIcon
} else {
    $s.IconLocation = "${env:SystemRoot}\System32\shell32.dll,21"
}
$s.Save()
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($ws) | Out-Null

Write-Host "Desktop shortcut created: $ShortcutPath"
Write-Host "Double-click it to open the emulator in your browser. Two other windows (sync + server) will stay open; close them when done."

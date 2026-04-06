# Install debug APK on a physical device only — ignores emulators (see project preference).
# Run from repo root: .\scripts\install-debug-physical-device.ps1
# Optional: -Serial <adb_serial> to pick a specific physical device when several are connected.

param(
    [string]$Serial = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Get-PhysicalAdbSerials {
    $serials = [System.Collections.Generic.List[string]]::new()
    adb devices 2>$null | ForEach-Object {
        if ($_ -match '^\s*(\S+)\s+device\s*$') {
            $s = $Matches[1]
            if ($s -notmatch '^emulator-') {
                $serials.Add($s) | Out-Null
            }
        }
    }
    return $serials
}

$target = $Serial
if ([string]::IsNullOrWhiteSpace($target)) {
    $physical = Get-PhysicalAdbSerials
    if ($physical.Count -eq 0) {
        Write-Error "No physical device connected (emulators are skipped). Connect USB or wireless debugging and confirm `adb devices` shows `device`."
    }
    $target = $physical[0]
    if ($physical.Count -gt 1) {
        Write-Warning "Multiple physical devices; using $target. Pass -Serial to choose another."
    }
} else {
    $physical = Get-PhysicalAdbSerials
    if ($physical -notcontains $target) {
        Write-Error "Serial '$target' is not a connected physical device (or is an emulator)."
    }
}

Write-Host "Installing to physical device: $target" -ForegroundColor Cyan

& .\gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) {
    Write-Error "APK not found: $apk"
}

adb -s $target install -r $apk
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Installed: $apk -> $target" -ForegroundColor Green

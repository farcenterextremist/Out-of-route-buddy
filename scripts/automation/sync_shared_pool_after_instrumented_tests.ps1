# Sync OutOfRouteBuddy shared-pool exports after instrumented tests.
# Pulls export bundles from the connected device/emulator, then updates the
# sibling MyTruckingBot shared pool so GOLD and synthetic exports stay visible.

[CmdletBinding()]
param(
    [string]$PackageName = "com.example.outofroutebuddy",
    [string]$MyTruckingBotRoot = "",
    [switch]$SkipOpenRoadPublish
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if ([string]::IsNullOrWhiteSpace($MyTruckingBotRoot)) {
    $ParentRoot = Split-Path -Parent $RepoRoot
    $MyTruckingBotRoot = Join-Path $ParentRoot "MyTruckingBot"
}

if (-not (Test-Path $MyTruckingBotRoot)) {
    throw "MyTruckingBot repo not found at '$MyTruckingBotRoot'."
}

$ImportDir = Join-Path $MyTruckingBotRoot "shared_pool_imports\oorb"
New-Item -ItemType Directory -Force -Path $ImportDir | Out-Null

$DeviceExportDir = "/sdcard/Android/data/$PackageName/files/shared_pool_exports"

Write-Host "Preparing shared-pool import directory: $ImportDir"
Get-ChildItem -Path $ImportDir -Filter "*.json" -ErrorAction SilentlyContinue | Remove-Item -Force

Write-Host "Pulling export bundles from device/emulator: $DeviceExportDir"
& adb shell "ls $DeviceExportDir" *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Warning "No shared-pool exports found on device/emulator at $DeviceExportDir."
} else {
    & adb pull "$DeviceExportDir/." "$ImportDir"
    if ($LASTEXITCODE -ne 0) {
        throw "adb pull failed for shared-pool export directory."
    }
}

Push-Location $MyTruckingBotRoot
try {
    Write-Host "Registering reference datasets in shared pool..."
    & python scripts/shared_pool/register_reference_datasets.py
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to register reference datasets."
    }

    if (-not $SkipOpenRoadPublish) {
        Write-Host "Publishing MyTruckingBot/OpenRoad data into shared pool..."
        & python scripts/shared_pool/sync_local_shared_pool.py --publish-openroad
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to publish OpenRoad data to shared pool."
        }
    }

    Write-Host "Importing OutOfRouteBuddy GOLD and virtual-fleet bundles..."
    & python scripts/shared_pool/sync_local_shared_pool.py --import-oorb --import-virtual-fleet --oorb-export-dir "$ImportDir"
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to import OutOfRouteBuddy bundles into shared pool."
    }
} finally {
    Pop-Location
}

Write-Host "Shared-pool sync complete."

# Generates OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt (default: user's Desktop).
# Run from repo root: .\scripts\automation\write_ship_instructions.ps1
# Optional: -OutFile "C:\path\to\file.txt"

param(
    [string]$OutFile = ""
)

$RepoRoot = $PSScriptRoot
for ($i = 0; $i -lt 2; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
if (-not $OutFile) { $OutFile = Join-Path ([Environment]::GetFolderPath("Desktop")) "OUTOFROUTEBUDDY_SHIP_INSTRUCTIONS.txt" }
$outDir = Split-Path -Parent $OutFile
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }

# Read version from app/build.gradle.kts
$gradlePath = Join-Path $RepoRoot "app\build.gradle.kts"
$versionCode = "?"
$versionName = "?"
if (Test-Path $gradlePath) {
    $content = Get-Content $gradlePath -Raw
    if ($content -match "versionCode\s*=\s*(\d+)") { $versionCode = $Matches[1] }
    if ($content -match 'versionName\s*=\s*"([^"]+)"') { $versionName = $Matches[1] }
}

# Test status from last pulse if available
$pulseLog = Join-Path $RepoRoot "docs\automation\pulse_log.txt"
$lastTestStatus = "unknown"
if (Test-Path $pulseLog) {
    $lines = Get-Content $pulseLog -Tail 20
    $t = $lines | Where-Object { $_ -match "tests:\s*(.+)" } | Select-Object -Last 1
    if ($t) { $lastTestStatus = ($t -replace ".*tests:\s*", "").Trim() }
}

$generated = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$body = @"
================================================================================
OUTOFROUTEBUDDY — SHIP INSTRUCTIONS
Generated: $generated
================================================================================
Objective: Shippable product. Use this checklist and commands to build, sign,
and ship the app (e.g. Google Play). Ref: docs/DEPLOYMENT.md, docs/STORE_CHECKLIST.md.

Current app version (app/build.gradle.kts): versionCode $versionCode, versionName $versionName
Last pulse test status: $lastTestStatus

--------------------------------------------------------------------------------
BEFORE YOU SHIP
--------------------------------------------------------------------------------
1. Version and changelog
   - Confirm versionCode and versionName in app/build.gradle.kts (current: $versionCode / $versionName).
   - Update CHANGELOG.md with release date and user-facing changes.

2. Quality gates
   - Run unit tests:     .\gradlew.bat :app:testDebugUnitTest
   - Run lint:          .\gradlew.bat :app:lintDebug
   - Fix or document any failures; do not ship with failing tests or new lint errors.

3. Manual smoke test
   - Install debug on device: .\gradlew.bat :app:installDebug
   - Test: start trip, add miles, end trip, view history, open calendar/stats, settings.
   - Test trip recovery (force-stop app during trip, reopen).

4. Privacy and store listing
   - Privacy policy URL set in Play Console and (if required) in-app.
   - Data safety section completed (on-device data; crash/diagnostics if using Firebase).
   - Release notes drafted for store.

--------------------------------------------------------------------------------
BUILD RELEASE
--------------------------------------------------------------------------------
From repo root (PowerShell):

  .\gradlew.bat clean
  .\gradlew.bat assembleRelease

Output (unsigned): app\build\outputs\apk\release\app-release-unsigned.apk

--------------------------------------------------------------------------------
SIGN THE RELEASE
--------------------------------------------------------------------------------
Use your release keystore (see docs/STORE_CHECKLIST.md §3). Do not commit the keystore.

  jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore <path-to-keystore> app\build\outputs\apk\release\app-release-unsigned.apk <alias>
  # Then zipalign if needed (Android Studio / build tools).

Or use Android Studio: Build > Generate Signed Bundle / APK and follow the wizard.

--------------------------------------------------------------------------------
UPLOAD AND RELEASE
--------------------------------------------------------------------------------
- Google Play: Upload AAB or APK to Play Console. Use the release track (internal / closed / production).
- Add release notes for this version.
- Staged rollout recommended for first production release.

--------------------------------------------------------------------------------
AFTER SHIP
--------------------------------------------------------------------------------
- Monitor Firebase Crashlytics and Analytics for the new version.
- Have a rollback plan (e.g. halt rollout in Play Console if critical issues appear).
- Plan first patch (e.g. 1.0.3) for any follow-up fixes or store feedback.

================================================================================
Ref: docs/DEPLOYMENT.md, docs/STORE_CHECKLIST.md, docs/automation/8_HOUR_IMPROVEMENT_PLAN.md
================================================================================
"@

Set-Content -Path $OutFile -Value $body -Encoding UTF8
Write-Host "Ship instructions written to: $OutFile"

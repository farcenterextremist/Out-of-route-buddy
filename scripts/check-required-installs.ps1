# OutOfRouteBuddy - Check required installs for build and unit tests
# Run from project root: .\scripts\check-required-installs.ps1

$ErrorActionPreference = "SilentlyContinue"
$required = @()
$optional = @()

Write-Host "`n=== OutOfRouteBuddy - Required Installs Check ===" -ForegroundColor Cyan
Write-Host ""

# --- 1. Java (JDK 17) ---
Write-Host "[1] Java (JDK 17)" -ForegroundColor Yellow
$javaHome = $env:JAVA_HOME
$javaExe = $null
if ($javaHome) {
    $javaExe = Get-Command "$javaHome\bin\javac.exe" -ErrorAction SilentlyContinue
    if (-not $javaExe) { $javaExe = Get-Command "$javaHome\bin\java.exe" -ErrorAction SilentlyContinue }
}
if (-not $javaExe) { $javaExe = Get-Command javac -ErrorAction SilentlyContinue }
if (-not $javaExe) { $javaExe = Get-Command java -ErrorAction SilentlyContinue }

if ($javaExe) {
    try {
        $versionOutput = & $javaExe.Source -version 2>&1 | Out-String
        # Parse "17.x.x" or "openjdk version `"17.x.x`""
        if ($versionOutput -match "(\d+)\.(\d+)") { $major = [int]$Matches[1] } else { $major = 0 }
        if ($major -eq 17) {
            Write-Host "    OK - Java 17 found: $($javaExe.Source)" -ForegroundColor Green
        } elseif ($major -ge 11 -and $major -le 21) {
            Write-Host "    WARN - Java $major found. Project recommends Java 17." -ForegroundColor DarkYellow
            Write-Host "    Path: $($javaExe.Source)" -ForegroundColor Gray
        } else {
            Write-Host "    WARN - Java $major detected. Project expects Java 17." -ForegroundColor DarkYellow
        }
    } catch {
        Write-Host "    FAIL - Could not get Java version" -ForegroundColor Red
        $required += "Java 17 (JDK)"
    }
} else {
    Write-Host "    MISSING - JAVA_HOME not set and no java/javac in PATH" -ForegroundColor Red
    $required += "Java 17 (JDK)"
}

# --- 2. Android SDK ---
Write-Host "`n[2] Android SDK" -ForegroundColor Yellow
$sdkRoot = $env:ANDROID_HOME
if (-not $sdkRoot) { $sdkRoot = $env:ANDROID_SDK_ROOT }
if (-not $sdkRoot -and $env:LOCALAPPDATA) {
    $defaultSdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (Test-Path $defaultSdk) { $sdkRoot = $defaultSdk }
}
if ($sdkRoot -and (Test-Path $sdkRoot)) {
    $platforms = Join-Path $sdkRoot "platforms"
    $has34 = Test-Path (Join-Path $platforms "android-34")
    if ($has34) {
        Write-Host "    OK - Android SDK found (API 34): $sdkRoot" -ForegroundColor Green
    } else {
        Write-Host "    WARN - SDK found but android-34 may be missing. Install Android SDK Platform 34." -ForegroundColor DarkYellow
        Write-Host "    SDK: $sdkRoot" -ForegroundColor Gray
    }
} else {
    Write-Host "    MISSING - ANDROID_HOME not set or SDK path not found" -ForegroundColor Red
    $required += "Android SDK (API 34)"
}

# --- 3. Gradle (wrapper) ---
Write-Host "`n[3] Gradle" -ForegroundColor Yellow
$gradlew = Join-Path $PSScriptRoot "..\gradlew.bat"
if (Test-Path $gradlew) {
    Write-Host "    OK - Gradle wrapper present (gradlew.bat); Gradle will download on first run." -ForegroundColor Green
} else {
    Write-Host "    WARN - gradlew.bat not found. Run from repo root." -ForegroundColor DarkYellow
}

# --- 4. Git ---
Write-Host "`n[4] Git" -ForegroundColor Yellow
$gitExe = Get-Command git -ErrorAction SilentlyContinue
if ($gitExe) {
    $gitVer = (git --version 2>&1)
    Write-Host "    OK - $gitVer" -ForegroundColor Green
} else {
    Write-Host "    MISSING - Git not in PATH (needed for version control, branching, CI)" -ForegroundColor Red
    $required += "Git"
}

# --- 5. Python (for coordinator-email scripts) ---
Write-Host "`n[5] Python (optional - for scripts/coordinator-email)" -ForegroundColor Yellow
$pyExe = Get-Command python -ErrorAction SilentlyContinue
if (-not $pyExe) { $pyExe = Get-Command py -ErrorAction SilentlyContinue }
if (-not $pyExe -and $env:USERPROFILE) {
    $py312 = Join-Path $env:USERPROFILE "AppData\Local\Programs\Python\Python312\python.exe"
    if (Test-Path $py312) { $pyExe = Get-Command $py312 -ErrorAction SilentlyContinue }
}
if ($pyExe) {
    try {
        $pv = (& $pyExe.Source --version 2>&1) -join " "
        Write-Host "    OK - $pv" -ForegroundColor Green
    } catch { Write-Host "    OK - Python found at $($pyExe.Source)" -ForegroundColor Green }
} else {
    Write-Host "    Not found in PATH - Install if you use scripts/coordinator-email (Python 3.6+)." -ForegroundColor DarkYellow
}

# --- Summary and download links ---
Write-Host "`n--- Summary ---" -ForegroundColor Cyan
if ($required.Count -eq 0) {
    Write-Host "All required tools appear to be installed. You can run:" -ForegroundColor Green
    Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor White
    Write-Host "  .\gradlew.bat :app:testDebugUnitTest" -ForegroundColor White
} else {
    Write-Host "Missing or incomplete:" -ForegroundColor Red
    $required | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "`n--- How to install ---" -ForegroundColor Cyan
    Write-Host "Option A (recommended): Install Android Studio (includes JDK 17 + Android SDK)" -ForegroundColor White
    Write-Host "  https://developer.android.com/studio" -ForegroundColor Gray
    Write-Host "  After install: set JAVA_HOME to Android Studio's JBR, e.g." -ForegroundColor Gray
    Write-Host "  [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Android\Android Studio\jbr', 'User')" -ForegroundColor Gray
    Write-Host "  Set ANDROID_HOME to your SDK (e.g. %LOCALAPPDATA%\Android\Sdk)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Option B: Install separately" -ForegroundColor White
    Write-Host "  JDK 17 (Eclipse Temurin): https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=msi" -ForegroundColor Gray
    Write-Host "  Android command-line tools: https://developer.android.com/studio#command-line-tools-only" -ForegroundColor Gray
    if ($required -contains "Git") {
        Write-Host ""
        Write-Host "  Git: winget install --id Git.Git -e --source winget" -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "See scripts\SETUP_INSTALLS.md for step-by-step instructions." -ForegroundColor Cyan
}
Write-Host "--- Recommended Cursor/VS Code extensions ---" -ForegroundColor Cyan
Write-Host "  Kotlin (fwcd.kotlin), Gradle for Java (vscjava.vscode-gradle), Python (ms-python.python)." -ForegroundColor Gray
Write-Host "  Install from Extensions view (Ctrl+Shift+X) or: code --install-extension <id>" -ForegroundColor Gray
Write-Host ""

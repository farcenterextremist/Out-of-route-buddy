# Required installs for OutOfRouteBuddy

This project needs **Java 17** and the **Android SDK** to build and run unit tests. Gradle is provided by the project (Gradle wrapper).

---

## Quick check

From the project root in PowerShell:

```powershell
.\scripts\check-required-installs.ps1
```

This script reports what’s installed and what’s missing.

---

## Option A: Install Android Studio (recommended)

One installer gives you **JDK 17** and the **Android SDK**.

1. **Download**
   - **Official page:** https://developer.android.com/studio  
   - **Direct Windows 64-bit .exe:** https://redirector.gvt1.com/edgedl/android/studio/install/2024.2.1.12/android-studio-2024.2.1.12-windows.exe  
   - (If the direct link is outdated, use the official page and click “Download Android Studio”.)

2. **Install**
   - Run the `.exe`, accept the defaults (or choose “Standard”).
   - Let it install the Android SDK (including SDK Platform 34).

3. **Set environment variables** (required for command-line build and tests)

   In **PowerShell (Run as Administrator**, or use “Edit environment variables” in Windows):

   ```powershell
   # Use Android Studio’s bundled JDK (Java 17)
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "User")

   # SDK is usually here after Android Studio install
   [System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
   ```

   If you installed the SDK somewhere else, set `ANDROID_HOME` to that folder.

4. **Restart your terminal** (and Cursor/IDE) so the new variables are picked up.

5. **Install SDK Platform 34** (if the script says it’s missing)
   - Open Android Studio → **Settings** → **Languages & Frameworks** → **Android SDK**.
   - Open the **SDK Platforms** tab, check **Android 14.0 (API 34)**, apply.

---

## Option B: Install JDK and Android SDK separately

Use this if you don’t want the full Android Studio IDE.

### 1. JDK 17

1. **Download**
   - **Eclipse Temurin (Adoptium) JDK 17 for Windows x64 (MSI):**  
     https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=msi  

2. **Install**
   - Run the `.msi`, use default options (e.g. “Set JAVA_HOME” and “Add to PATH” if offered).

3. **Confirm**
   - Open a **new** PowerShell:
     ```powershell
     $env:JAVA_HOME
     java -version
     ```
   - You should see something like `openjdk version "17.x.x"`.

### 2. Android SDK (command-line only)

1. **Download**
   - **Command-line tools only:**  
     https://developer.android.com/studio#command-line-tools-only  
   - Under “Command line tools only”, download the **Windows** zip.

2. **Unzip**
   - Example: `C:\Android\cmdline-tools\latest\`
   - So that `sdkmanager.bat` is at e.g. `C:\Android\cmdline-tools\latest\bin\sdkmanager.bat`.

3. **Install SDK Platform 34 and build-tools**
   - In PowerShell:
     ```powershell
     cd "C:\Android\cmdline-tools\latest\bin"
     .\sdkmanager.bat --install "platforms;android-34" "build-tools;34.0.0"
     ```

4. **Set ANDROID_HOME**
   - Point it to the SDK root (the folder that contains `platforms` and `build-tools`).  
   - If you put cmdline-tools in `C:\Android\cmdline-tools\latest`, the SDK root is often `C:\Android` after you run the installs above:
     ```powershell
     [System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android", "User")
     ```
   - Open a new terminal and verify:
     ```powershell
     $env:ANDROID_HOME
     dir $env:ANDROID_HOME\platforms
     ```

---

## After everything is installed

1. **Close and reopen** your terminal (and Cursor).
2. **Run the check again:**
   ```powershell
   cd "c:\Users\brand\OneDrive\Out-of-route-buddy"
   .\scripts\check-required-installs.ps1
   ```
3. **Build and run unit tests:**
   ```powershell
   .\gradlew.bat assembleDebug
   .\gradlew.bat :app:testDebugUnitTest
   ```

---

## Reference

| Requirement   | Purpose                    | This project        |
|---------------|----------------------------|----------------------|
| **Java 17**   | Compile Kotlin/Java, run Gradle | JDK 17 (not 21+) |
| **Android SDK** | Build Android app, run tests    | API 34 (android-34)  |
| **Gradle**    | Build and test runner      | 9.0.0 via `gradlew.bat` (no install) |

More detail: `docs/DEPLOYMENT.md`.

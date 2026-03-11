# Gradle 9 migration notes

**Owner:** DevOps  
**Purpose:** Capture deprecations and warnings when moving to or running with Gradle 9.  
**Related:** 25-point #14, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## How to capture warnings

From project root:

```bash
./gradlew tasks --warning-mode all
# or
./gradlew build --warning-mode all
```

On Windows (PowerShell):

```powershell
.\gradlew.bat tasks --warning-mode all
```

Record any deprecation or configuration warnings here so we can fix them before or during a Gradle 9 upgrade.

---

## Current project config (reference)

- **JDK:** 17 (see `build.gradle.kts` comment and `docs/DEPLOYMENT.md`)
- **Kotlin:** 1.9.22 (forced in root `build.gradle.kts`)
- **Android:** minSdk 24, compileSdk 34, targetSdk 34
- **AGP:** See `libs.versions.toml` / plugin block in root and app

---

## Deprecations / warnings (to fill after running)

| Warning / deprecation | Location / fix |
|-----------------------|----------------|
| *(Run `--warning-mode all` and paste here)* | |

---

*Update this file after each `--warning-mode all` run. Target: clean run with Gradle 9 when ready.*

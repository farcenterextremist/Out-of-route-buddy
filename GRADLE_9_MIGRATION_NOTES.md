# Gradle 9 migration notes

**Owner:** DevOps  
**Purpose:** Capture deprecations and warnings when moving to or running with Gradle 9; assess migration difficulty.  
**Related:** 25-point #14, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md`.

---

## Difficulty assessment (summary)

| Factor | Current | Gradle 9 requirement | Difficulty |
|--------|---------|----------------------|------------|
| **Gradle** | 8.13 | 9.0+ (AGP 9 needs 9.1+) | Medium — follow upgrade guide |
| **JDK** | 17 | 17+ (required for daemon) | **None** — already on 17 |
| **AGP** | 8.13.0 | **9.0+** for Gradle 9 (min 8.4.0) | **High** — AGP 9 has breaking changes |
| **Kotlin (app)** | 1.9.22 | Gradle embeds Kotlin 2.2; KGP min 2.0 | **Medium** — bump Kotlin + Compose compiler |
| **Kotlin DSL** | Yes | No `this@Build_gradle` etc.; use `project`/`settings` | Low — check scripts |
| **Deprecations** | 1 from plugins | Fix or update plugins | Low if AGP/plugins updated |

**Overall:** **Medium–high**. The main effort is **AGP 9 migration** (Kotlin plugin removal, new DSL, possible Hilt/KSP/Detekt compatibility), not raw Gradle 9. Doing “Gradle 9 only” with AGP 8.x is possible (Gradle 9 supports AGP ≥8.4.0) and **easier**; going to **AGP 9 + Gradle 9** is a larger, coordinated upgrade.

---

## Gradle 9 requirements (reference)

- **JVM:** Gradle daemon requires **JVM 17+** (project already uses JDK 17).
- **Kotlin DSL:** Script labels `this@Build_gradle` / `this@Settings_gradle` removed → use `project`, `settings`, `gradle`.
- **Kotlin:** Gradle 9 embeds **Kotlin 2.2.0**; minimum Kotlin Gradle Plugin **2.0.0** (project uses 1.9.22 → must upgrade).
- **AGP:** Minimum **8.4.0** for Gradle 9. For **AGP 9.0**: requires **Gradle 9.1.0**; AGP 9 drops standalone `org.jetbrains.kotlin.android` (Kotlin built-in), new DSL; may require Hilt/KSP/Compose compiler updates.
- **Configuration Cache:** Becomes preferred; ensure plugins and build logic are compatible.

---

## AGP 9 vs “Gradle 9 only” (recommendation)

1. **Option A — Gradle 9 + AGP 8.x (easier)**  
   - Upgrade to Gradle 9.0 (or 9.1), keep AGP 8.13.  
   - Fix Gradle deprecations (e.g. `Configuration.fileCollection(Spec)` → plugin/AGP update).  
   - Bump Kotlin to **2.0+** (and Compose compiler to match) so KGP is supported.  
   - **Difficulty: Medium.**

2. **Option B — Gradle 9 + AGP 9 (full upgrade)**  
   - Upgrade to Gradle 9.1+ and AGP 9.0+.  
   - Remove `org.jetbrains.kotlin.android` (AGP 9 has built-in Kotlin).  
   - Migrate to new AGP DSL if needed; verify Hilt, KSP, Detekt, JaCoCo, Navigation, Google Services.  
   - **Difficulty: High** — use [AGP Upgrade Assistant](https://developer.android.com/build/agp-upgrade-assistant) and release notes.

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

Record any deprecation or configuration warnings below. Re-run after upgrading Gradle/AGP/plugins.

---

## Migration completed (2026-03-13)

- **Gradle:** Upgraded to **9.0.0** (wrapper). Build and unit tests run successfully.
- **Kotlin:** Upgraded to **2.0.21** (Gradle 9 min KGP 2.0). Compose via **org.jetbrains.kotlin.plugin.compose** (no manual kotlinCompilerExtensionVersion).
- **KSP:** 2.0.21-1.0.27 (for Kotlin 2.0). Root `buildDir` replaced with `layout.buildDirectory` (clean task).
- **AGP:** 8.13.0 retained (Option A — Gradle 9 + AGP 8.x). Remaining deprecation `Configuration.fileCollection(Spec)` comes from plugins; non-blocking.

---

## Current project config (reference)

- **Gradle:** 9.0.0 (wrapper)
- **JDK:** 17 (see `build.gradle.kts` comment and `docs/DEPLOYMENT.md`)
- **Kotlin:** 2.0.21 (forced in root `build.gradle.kts`)
- **AGP:** 8.13.0 (`libs.versions.toml`)
- **Android:** minSdk 24, compileSdk 34, targetSdk 34
- **Plugins:** android.application, kotlin.android, kotlin.compose, navigation.safeargs, detekt, firebase.crashlytics, hilt, ksp, google.services; app also uses kapt, jacoco

---

## Deprecations / warnings (from `--warning-mode all`)

| Warning / deprecation | Location / fix |
|-----------------------|----------------|
| **Configuration.fileCollection(Spec)** is deprecated, removed in Gradle 9.0. Use `Configuration.getIncoming().artifactView(Action)` with componentFilter. | Emitted when configuring **:app**; likely from **AGP** or another plugin (not from project’s own build script). **Fix:** Upgrade AGP/plugins to versions that use the new API; or suppress until plugin update. |
| Configuration on demand is an incubating feature. | Informational; no change required for migration. |

---

## Suggested migration order

1. Run `gradle help --warning-mode all` (or `--scan`) and fix any **project-owned** deprecations.
2. **Option A:** Upgrade to **Gradle 9.0** (or 9.1), then Kotlin to **2.0+** and matching Compose compiler; keep AGP 8.13. Fix remaining warnings (plugin updates).
3. **Option B:** Plan **AGP 9 + Gradle 9.1**; use AGP Upgrade Assistant; remove `kotlin.android` plugin; update Hilt/KSP/Detekt/Compose; test thoroughly.
4. Re-run `--warning-mode all` and ensure no Gradle 9 removals are hit.

---

*Update this file after each `--warning-mode all` run. Target: clean run with Gradle 9 when ready.*

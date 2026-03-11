# Gradle 9 migration notes

**Purpose:** Capture deprecated usages and planned plugin/version updates for Gradle 9 compatibility.  
**Reference:** CRUCIAL_IMPROVEMENTS_TODO §1, PROJECT_AUDIT CFG1–CFG4.

## Implementation status

- **Gradle 9 migration:** Planned; not yet executed. Run `./gradlew assembleDebug --warning-mode all` and resolve each deprecation listed in console or `build_warnings.txt` before upgrading to Gradle 9. See "Planned updates" below.

## Current status

- **Gradle:** 8.13 (see gradle/wrapper/gradle-wrapper.properties).
- **Build:** `./gradlew assembleDebug --warning-mode all` reports: "Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0."
- **Capture:** `./gradlew --warning-mode all` (2025-03-11) reports:
  - `Configuration.fileCollection(Spec)` deprecated; use `Configuration.getIncoming().artifactView(Action)` with `componentFilter` instead. See [Gradle 8 upgrading guide](https://docs.gradle.org/8.13/userguide/upgrading_version_8.html#deprecate_filtered_configuration_file_and_filecollection_methods).
  - `Configuration on demand` is still incubating.
- **Release status:** `assembleRelease` succeeds on the current toolchain after fixing an invalid drawable asset; Gradle 9 cleanup remains a follow-up, not a first-release blocker.

## Planned updates (no code change in this phase)

1. **Gradle wrapper:** Upgrade to Gradle 9.x when stable and AGP supports it; re-run `--warning-mode all` and fix each deprecation.
2. **Android Gradle Plugin:** Align with Gradle 9–compatible AGP version per [AGP release notes](https://developer.android.com/build/releases/gradle-plugin).
3. **Plugins:** Update Hilt, Kotlin, Navigation, Detekt, Google Services to versions that support Gradle 9 if needed.
4. **Configuration on demand:** Either enable explicitly in settings.gradle.kts or migrate to stable API when available.

## Java version

- **Confirmed:** Java 17 everywhere (`sourceCompatibility`/`targetCompatibility`/`jvmTarget` in app/build.gradle.kts). No VERSION_1_8 references.

## Release minification (optional)

- **Current:** `isMinifyEnabled = true` and `isShrinkResources = true` for release (CFG2). ProGuard rules in `app/proguard-rules.pro`.
- **Decision:** Enabled per Advanced Improvement Plan. Test release build and instrumented tests on release variant.

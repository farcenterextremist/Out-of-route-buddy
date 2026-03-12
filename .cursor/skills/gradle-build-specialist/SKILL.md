---
name: gradle-build-specialist
description: >-
  Specializes in Gradle, build config, and dependency management for OutOfRouteBuddy.
  Use when fixing build warnings, Gradle 9 migration, dependencies, or when the user
  asks for Gradle/build help.
---

# Gradle/Build Specialist

## Quick Reference

Read `GRADLE_9_MIGRATION_NOTES.md` and `docs/CRUCIAL_IMPROVEMENTS_TODO.md` §1 for build work.

---

## Current Config

| Item | Value |
|------|-------|
| JDK | 17 |
| Kotlin | 1.9.22 |
| minSdk | 24 |
| compileSdk / targetSdk | 34 |
| AGP | libs.versions.toml |

---

## Capturing Warnings

```bash
./gradlew tasks --warning-mode all
# or
./gradlew build --warning-mode all
```

Windows: `.\gradlew.bat tasks --warning-mode all`

Record deprecations in `GRADLE_9_MIGRATION_NOTES.md` § Deprecations/warnings.

---

## Gradle 9 Readiness

- **Goal:** Clean run with Gradle 9
- **Process:** Run `--warning-mode all`; document each warning; fix or plan fix
- **Related:** CRUCIAL #1, `docs/archive/APP_IMPROVEMENT_25_POINT_BRAINSTORM.md` §14

---

## Key Files

| File | Purpose |
|------|---------|
| build.gradle.kts (root) | Plugins, Kotlin version |
| app/build.gradle.kts | App config, Room schema, Hilt, JaCoCo |
| libs.versions.toml | Version catalog |
| settings.gradle.kts | Project structure |

---

## Common Tasks

- **Dependency update:** Check libs.versions.toml; update; run `./gradlew dependencyCheckAnalyze` for CVE
- **Plugin deprecation:** Check plugin docs; update to recommended version
- **Build failure:** `./gradlew clean assembleDebug`; check DEPLOYMENT.md for env

---

## Additional Resources

- Migration notes: [GRADLE_9_MIGRATION_NOTES.md](../../../GRADLE_9_MIGRATION_NOTES.md)
- Deployment: [docs/DEPLOYMENT.md](../../../docs/DEPLOYMENT.md)

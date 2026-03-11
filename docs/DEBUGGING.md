# OutOfRouteBuddy — Debugging checklist

Quick reference for building, testing, and linting the Android app. See also `scripts/debug_app.ps1`.

---

## 1. Build (debug)

```powershell
cd c:\Users\brand\OutofRoutebuddy
.\gradlew.bat assembleDebug --no-daemon
```

- **Success:** `BUILD SUCCESSFUL`; APK at `app/build/outputs/apk/debug/`.
- **Failure:** Check Kotlin/Java compile errors or missing dependencies.

---

## 2. Unit tests (debug only)

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

- **"Unable to delete directory ... test-results":** Another process (IDE or previous Gradle) has the dir open. Fix: run `.\gradlew.bat clean` then retry, or close Android Studio/other Gradle processes.
- **To avoid release path:** Use `:app:testDebugUnitTest` (not `test`). The full `test` task also builds release and runs `mergeReleaseResources`.

---

## 3. Release build and mergeReleaseResources

```powershell
.\gradlew.bat :app:assembleRelease --no-daemon
```

- **Configuration cache lock:** "Timeout waiting to lock Configuration Cache" — another Gradle is running. Stop other Gradle/IDE sync, or run with `--no-configuration-cache`.
- **mergeReleaseResources FAILED:** Often due to:
  - **Duplicate resources:** Same resource name in multiple source sets (e.g. `src/main/res` and a flavor). Search for duplicate file names under `app/src`.
  - **AAPT2 / resource conflict:** Check build output for the exact AAPT2 error (e.g. duplicate value, invalid XML).
  - **Firebase/Crashlytics:** Release uses `releaseImplementation` for Crashlytics; ensure `google-services.json` is present and valid if you use release builds.

If you only need debug builds and unit tests, you can ignore release; use `assembleDebug` and `testDebugUnitTest`.

### How to get the exact mergeReleaseResources error

1. Ensure no other Gradle or Android Studio sync is running (avoid configuration-cache lock).
2. Run: `.\gradlew.bat :app:assembleRelease --no-daemon --stacktrace`
3. If it fails, read the "What went wrong" section and any AAPT2 or "duplicate resource" lines.
4. For duplicate resources: search under `app/src` for the same resource name in different folders (e.g. two `values/strings.xml` with the same key, or two drawables with the same name).

---

## 4. Lint

```powershell
.\gradlew.bat :app:lintDebug --no-daemon
```

- **Report:** `app/build/reports/lint-results-debug.html` (or path shown when lint finishes).
- **abortOnError:** Currently `false` in `app/build.gradle.kts`, so lint issues do not fail the build.

---

## 5. One-shot debug run (script)

From repo root:

```powershell
.\scripts\debug_app.ps1
```

Options:

- `.\scripts\debug_app.ps1 -SkipTests` — build + lint only.
- `.\scripts\debug_app.ps1 -SkipLint` — build + test only.
- `.\scripts\debug_app.ps1 -Clean` — run `clean` before build (helps if test-results dir is locked).

---

## 6. JaCoCo coverage

```powershell
.\gradlew.bat jacocoTestReport --no-daemon
```

- **Report:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

To run unit tests and generate the report without threshold verification (e.g. when verification fails): `.\gradlew.bat jacocoSuiteTestsOnly --no-daemon`. See `docs/qa/JACOCO_SUITE.md` for "Run jacocoSuite reliably" (stop other Gradle/IDE first, then `.\gradlew.bat jacocoSuite`).

---

## Summary

| Goal              | Command                                              |
|-------------------|------------------------------------------------------|
| Build debug APK   | `.\gradlew.bat assembleDebug --no-daemon`           |
| Unit tests only   | `.\gradlew.bat :app:testDebugUnitTest --no-daemon`  |
| Lint              | `.\gradlew.bat :app:lintDebug --no-daemon`          |
| Full debug suite  | `.\scripts\debug_app.ps1`                            |
| Clean + test      | `.\gradlew.bat clean :app:testDebugUnitTest --no-daemon` |

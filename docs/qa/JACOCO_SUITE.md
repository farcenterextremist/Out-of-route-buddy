# JaCoCo Testing Suite

**Purpose:** Single reference for the OutOfRouteBuddy JaCoCo (Java Code Coverage) setup: tasks, thresholds, and how to run the suite.

**Related:** [TEST_STRATEGY.md](./TEST_STRATEGY.md), `app/build.gradle.kts` (JaCoCo section).

---

## Quick start

From project root:

```bash
# Full suite: unit tests + coverage report + threshold verification
./gradlew jacocoSuite
```

Or on Windows:

```powershell
.\gradlew.bat jacocoSuite
```

After it passes, open the HTML report:

- **HTML:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **XML:** `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` (for CI/parsing)
- **CSV:** `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.csv`

---

## Suite tasks

| Task | Description | When to use |
|------|-------------|-------------|
| **jacocoSuite** | Unit tests → report → verification (single entry point) | CI and local “run everything” |
| **testDebugUnitTest** | Run unit tests only (with coverage data) | Quick test run |
| **jacocoTestReport** | Generate unit-test coverage report (XML, HTML, CSV) | After tests, to inspect coverage |
| **jacocoCoverageVerification** | Fail if coverage below thresholds | Enforce quality gate |
| **coverageCheck** | Same as verification + short success message | Alias for verification |
| **coverageAnalysis** | Generate report + print report path and thresholds | Reminder where reports live |
| **jacocoAndroidTestReport** | Coverage from instrumented tests (device/emulator) | After `connectedAndroidTest` |
| **jacocoCombinedReport** | Unit + instrumented coverage in one report | Full coverage picture |

---

## Thresholds (quality gate)

Configured in `app/build.gradle.kts`; enforced by `jacocoCoverageVerification` and `jacocoSuite`:

| Metric | Minimum | Meaning |
|--------|---------|--------|
| **Overall** | 70% | Line coverage across included classes |
| **Branch** | 60% | Branch (if/else) coverage per class |
| **Line** | 75% | Line coverage per class |

If any threshold is not met, `jacocoCoverageVerification` (and thus `jacocoSuite`) fails the build.

---

## What is excluded from coverage

The suite uses a shared **file filter** so that generated and framework code do not lower the reported coverage. Excluded patterns include:

- Android: `R`, `BuildConfig`, `Manifest`, `android.*`, `androidx.*`
- Tests: `*Test*`, `*Tests*`, `test/**`, `androidTest/**`
- DI: `di/**`, `*_Factory*`, `*_MembersInjector*`, `*Module*`, `*Dagger*`, `*Hilt*`
- Room: `*Dao_Impl*`, `*Database_Impl*`, `*RoomDatabase*`
- WorkManager: `*Worker*`, `*WorkManager*`
- Generated: `generated/**`, `build/**`, `tmp/**`, databinding

Defined once as `jacocoFileFilter` in `app/build.gradle.kts` and reused by all report and verification tasks.

---

## CI usage

- **coverage-analysis.yml:** Runs `testDebugUnitTest`, then `jacocoTestReport`, then threshold checks and PR comment.
- **coverage-check.yml:** Runs `testDebugUnitTest jacocoTestReport`, badge generation, and PR comment.
- **android-tests.yml:** Can run `jacocoTestReport` and `jacocoCoverageVerification`.

To align with local “one command” behavior, CI can run:

```bash
./gradlew jacocoSuite
```

instead of separate `testDebugUnitTest` + `jacocoTestReport` + `jacocoCoverageVerification` (since `jacocoSuite` depends on `jacocoCoverageVerification`, which pulls in the rest).

---

## Troubleshooting

- **Report not found:** Run `./gradlew jacocoSuite` (or at least `testDebugUnitTest jacocoTestReport`) first.
- **Verification fails:** Open the HTML report, find red packages/classes, add or fix unit tests for those lines/branches.
- **Instrumented report empty:** Ensure `connectedAndroidTest` has run and that `enableUnitTestCoverage` is set for the debug build type (already enabled in `app/build.gradle.kts`).

### Run jacocoSuite reliably

To avoid file locks and configuration-cache conflicts:

1. Stop other Gradle/IDE use: run `./gradlew --stop` (or `.\gradlew.bat --stop` on Windows), and close Android Studio or any other process that might lock the `app/build` directory.
2. From the project root, run the full gate: `./gradlew jacocoSuite` (Windows: `.\gradlew.bat jacocoSuite`).
3. If coverage verification fails (per-class thresholds), run tests and report only: `./gradlew jacocoSuiteTestsOnly`. Use this to confirm all tests pass and to view the report while working toward threshold compliance.

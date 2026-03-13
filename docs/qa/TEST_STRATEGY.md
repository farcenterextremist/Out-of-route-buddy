# Test strategy

**Owner:** QA Engineer  
**Purpose:** Define what we test, when, and quality gates. Single source for test approach.  
**Related:** [SSOT_TEST_SCENARIOS.md](./SSOT_TEST_SCENARIOS.md), [JACOCO_SUITE.md](./JACOCO_SUITE.md), [TEST_PLAN_TEMPLATE.md](./TEST_PLAN_TEMPLATE.md), [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md), [TEST_FAILURES_DOCUMENTATION.md](./TEST_FAILURES_DOCUMENTATION.md).

---

## What we test

| Layer | Type | Location | When |
|-------|------|----------|------|
| **Unit** | ViewModels, services, repositories, utils | `app/src/test/` | Every commit; CI |
| **Robolectric** | Fragments, dialogs, calendar, history, settings | `app/src/test/` | Every commit; CI |
| **Integration** | TripInputViewModelIntegrationTest, TripPersistenceRecoveryTest | `app/src/test/` | Every commit; CI |
| **Performance** | PerformanceTestSuite (relaxed thresholds for CI) | `app/src/test/` | CI; may be environment-dependent |
| **Instrumented** | Device/emulator tests | `app/src/androidTest/` | Pre-release; CI when configured |

### MainActivity coverage (minimum behaviors)

[MainActivityRobolectricTest](../../app/src/test/java/com/example/outofroutebuddy/ui/MainActivityRobolectricTest.kt) covers: activity launch; toolbar and trip input UI (loaded/bounce miles, start button, statistics, today's info card, OOR/total miles output); optional recovery/permission dialogs dismissed so test can assert views. Recovery flow (recoveredTripState != null) and permission handling are exercised when dialogs appear; navigation and full permission paths are covered by instrumented tests or integration where needed. **P1 (COVERAGE_SCORE_AND_CRITICAL_AREAS):** MainActivity has no dedicated fast unit tests beyond Robolectric; coverage is documented here and via instrumented tests. A minimal unit test for Application/MainActivity can be added in a later coverage ramp phase.

**Recovery / overlay detector:** Detector reset when user ends trip in-app is implemented via `ACTION_TRIP_ENDED_FROM_APP` (TripEndedOverlayService); TripInputViewModel calls `notifyTripEndedFromInApp` in endTrip(). Covered by TripEndedOverlayScenarioTest and overlay instrumented tests.

---

## Quality gates

- **Unit tests:** `./gradlew testDebugUnitTest` must pass.
- **Lint:** `./gradlew lint` (CFG3: **abortOnError = true** so lint is a hard gate). Fix or suppress remaining issues; see app/build.gradle.kts and docs/technical/CODE_QUALITY_NOTES.md. CI runs `lintDebug`; see android-tests.yml.
- **Coverage:** JaCoCo 70% threshold; combined reports. See [JACOCO_SUITE.md](./JACOCO_SUITE.md) for tasks.
- **CI coverage gate (accepted pass):** The accepted passing gate for coverage is **`jacocoSuiteTestsOnly`** (unit tests + coverage report; no threshold verification). Use this until per-class coverage is raised. Full **`jacocoSuite`** (including `jacocoCoverageVerification`) is the target once coverage ramp is complete; see [COVERAGE_SCORE_AND_CRITICAL_AREAS.md](./COVERAGE_SCORE_AND_CRITICAL_AREAS.md). CI is configured to run `jacocoSuiteTestsOnly`; see `.github/workflows/android-tests.yml`.
- **SSOT scenarios:** Tests must verify Known Truths per [SSOT_TEST_SCENARIOS.md](./SSOT_TEST_SCENARIOS.md).

---

## Known issues and workarounds

See [TEST_FAILURES_DOCUMENTATION.md](./TEST_FAILURES_DOCUMENTATION.md) for:

- PerformanceTestSuite: environment-dependent thresholds (relaxed for CI).
- Dispatcher-related flakiness: MockGpsSynchronizationServiceTest, TripInputViewModelIntegrationTest (ioDispatcher injection applied where fixed).

See [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md) for tracking of failing/ignored tests and planned actions.

**Deferred tests (documented, not blocking):**

- See [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md) for the full list: TripInputViewModelIntegrationTest, TripHistoryByDateViewModelTest, LocationValidationServiceTest, ThemeScreenshotTest, OfflineDataManagerPersistenceTest (one test). ThemeScreenshotTest: deferred until Paparazzi is configured.

---

## Per-feature test plans

For major features (Auto drive, Export, Offline persistence), create `TEST_PLAN_<feature>.md` using [TEST_PLAN_TEMPLATE.md](./TEST_PLAN_TEMPLATE.md). QA owns scenarios; hand off failures to Front-end or Back-end with steps.

---

## CI

`.github/workflows/android-tests.yml` runs unit tests, coverage report (`jacocoSuiteTestsOnly`), lint, and instrumented tests. The accepted passing gate for coverage is **jacocoSuiteTestsOnly** (tests + report); full **jacocoSuite** (with jacocoCoverageVerification) is planned when per-class coverage is improved. DevOps owns YAML; QA defines what "right" means.

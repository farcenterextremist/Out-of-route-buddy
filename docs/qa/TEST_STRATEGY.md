# Test strategy

**Owner:** QA Engineer  
**Purpose:** Define what we test, when, and quality gates. Single source for test approach.  
**Related:** [SSOT_TEST_SCENARIOS.md](./SSOT_TEST_SCENARIOS.md), [TEST_PLAN_TEMPLATE.md](./TEST_PLAN_TEMPLATE.md), [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md), [TEST_FAILURES_DOCUMENTATION.md](../../TEST_FAILURES_DOCUMENTATION.md).

---

## What we test

| Layer | Type | Location | When |
|-------|------|----------|------|
| **Unit** | ViewModels, services, repositories, utils | `app/src/test/` | Every commit; CI |
| **Robolectric** | Fragments, dialogs, calendar, history, settings | `app/src/test/` | Every commit; CI |
| **Integration** | TripInputViewModelIntegrationTest, TripPersistenceRecoveryTest | `app/src/test/` | Every commit; CI |
| **Performance** | PerformanceTestSuite (relaxed thresholds for CI) | `app/src/test/` | CI; may be environment-dependent |
| **Instrumented** | Device/emulator tests | `app/src/androidTest/` | Pre-release; CI when configured |

---

## Quality gates

- **Unit tests:** `./gradlew testDebugUnitTest` must pass.
- **Lint:** `./gradlew lint` (abortOnError configurable; see app/build.gradle.kts).
- **Coverage:** JaCoCo 70% threshold; combined reports.
- **SSOT scenarios:** Tests must verify Known Truths per [SSOT_TEST_SCENARIOS.md](./SSOT_TEST_SCENARIOS.md).

---

## Known issues and workarounds

See [TEST_FAILURES_DOCUMENTATION.md](../../TEST_FAILURES_DOCUMENTATION.md) for:

- PerformanceTestSuite: environment-dependent thresholds (relaxed for CI).
- Dispatcher-related flakiness: MockGpsSynchronizationServiceTest, TripInputViewModelIntegrationTest (ioDispatcher injection applied where fixed).

See [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md) for tracking of failing/ignored tests and planned actions.

**Deferred tests (documented, not blocking):**

- **ThemeScreenshotTest:** Screenshot tests deferred until Paparazzi (`com.squareup.paparazzi`) is configured. Test has `@Ignore` with reason; see class Javadoc.

---

## Per-feature test plans

For major features (Auto drive, Export, Offline persistence), create `TEST_PLAN_<feature>.md` using [TEST_PLAN_TEMPLATE.md](./TEST_PLAN_TEMPLATE.md). QA owns scenarios; hand off failures to Front-end or Back-end with steps.

---

## CI

`.github/workflows/android-tests.yml` runs unit tests, coverage, lint, and instrumented tests. DevOps owns YAML; QA defines what "right" means.

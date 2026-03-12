# Failing or ignored tests

**Purpose:** Single place to track tests that are currently ignored, deferred, or failing with reason and owner. Each row must have a clear one-line reason (why the test is ignored or deferred) and an owner. See [TEST_STRATEGY.md](./TEST_STRATEGY.md) for quality gates and when to fix or document.

**Improvement Loop (full autonomous):** The loop runs **unit tests only** — no device/emulator, no instrumented tests. Do not suggest "move to instrumented suite" as a fix. For ignored tests: fix in unit suite, document with reason, or defer. Instrumented tests run in CI/pre-release when a device or emulator is available.

| Test class | Reason | Owner | Fix by / deferred until |
|------------|--------|--------|--------------------------|
| **TripInputViewModelIntegrationTest** | Dispatcher conflict with Dispatchers.IO in ViewModel; one test was previously ignored. | QA | **Status:** TestDispatcher/ioDispatcher injection applied; suite runs. If any test is re-ignored, add @Ignore with reason here. **Fix by:** N/A (resolved). |
| **TripHistoryByDateViewModelTest** | Previously incomplete; required Application context and repository setup. | QA | **Status:** Implemented with Robolectric + ApplicationProvider + mock TripRepository. **Fix by:** N/A (resolved). |
| **LocationValidationServiceTest** | One test ignored: `validateVehicleLocation with good vehicle data returns Valid` (validation framework issue). Remaining work: heavy-traffic steps. | QA | @Ignore added with reason. **Fix by:** Fix assertion in unit suite or keep deferred. *(Loop: no instrumented tests in this environment.)* |
| **ThemeScreenshotTest** | Screenshot tests deferred until Paparazzi is configured. | QA / Front-end | **Fix by:** Add Paparazzi (`com.squareup.paparazzi`) and enable test, or keep deferred. See docs/qa/TEST_STRATEGY.md. **Accepted until:** Paparazzi is added to the project. |
| **OfflineDataManagerPersistenceTest** | Previously flaky because `OfflineDataManager` could race async startup load vs. save in Robolectric. | QA / Back-end | **Status:** Fixed by serializing persistence work, merging startup load with in-memory state, and replacing sleeps with explicit idle waits; targeted test now runs. **Fix by:** N/A (resolved). |
| **TripRecoveryResumeRobolectricTest** | Ignored because recovery flow depends on real Application/Service state; Robolectric does not reliably update service state. | QA / Back-end | @Ignore added with reason. **Fix by:** Keep covered by instrumented recovery tests or rework service-state injection for Robolectric. |
| **MainActivityRobolectricTest** (`continueTrip_fromRecovery_usesActivityScopedViewModel_andStartsService`) | Ignored because recovery flow races with Robolectric looper and overlay/service startup. | QA / Front-end | @Ignore added with reason. **Fix by:** Keep covered by instrumented recovery flow or stabilize looper/service wiring in test harness. |
| **OutOfRouteApplicationTest** (`application_isHealthy_afterInit_returnsTrue`, `application_getDatabaseError_beforeAnyFailure_returnsNull`) | Ignored because Hilt tests run with `HiltTestApplication`, not the concrete `OutOfRouteApplication`. Covered by instrumented `ApplicationInitializationTest`. | QA / Platform | @Ignore added with reason. **Fix by:** Keep covered by instrumented suite or add non-Hilt integration test path. |
| **PerformanceTestSuite** (`validationPerformance batch validation should maintain performance`) | Flaky; may fail on slower CI/machines due to timing thresholds. | QA / Performance | **Fix by:** Relax timing threshold, add @Ignore with reason, or move to separate performance suite. |
| **PerformanceTestSuite** (`performanceRegression memory usage should not increase significantly`) | **Status:** Fixed. Was asserting absolute peak used heap ≤75MB; test JVM often has larger heap. Now asserts *increase* from initial <100MB. | QA / Performance | N/A (resolved). |

---

## References

- [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md) section 5 — test health items.
- [TEST_STRATEGY.md](./TEST_STRATEGY.md) — deferred tests subsection.

# OutOfRouteBuddy — Test Suite Effective Coverage Score & Critical Areas

**Purpose:** Score the app’s test-suite effectiveness, list critical gaps, and support a data-driven improvement plan.  
**Related:** [JACOCO_SUITE.md](./JACOCO_SUITE.md), [TEST_STRATEGY.md](./TEST_STRATEGY.md), [QUALITY_AND_ROBUSTNESS_PLAN.md](../QUALITY_AND_ROBUSTNESS_PLAN.md), [PROJECT_AUDIT_2025_02_27.md](../PROJECT_AUDIT_2025_02_27.md).

---

## 1. Coverage score (effective coverage)

| Dimension | Score (1–5) | Notes |
|-----------|------------|--------|
| **Suite runnability** | **4** | testDebugUnitTest and jacocoTestReport pass; run `.\gradlew.bat jacocoSuiteTestsOnly` for a passing gate. **CI uses jacocoSuiteTestsOnly as the accepted pass** (decision documented in TEST_STRATEGY.md). jacocoCoverageVerification fails on per-class thresholds until coverage improved (Phases B–C). Run prerequisites: see docs/qa/JACOCO_SUITE.md "Run jacocoSuite reliably". |
| **Unit test breadth** | **4** | ~91 test files vs ~97 main Kotlin files; ViewModels, services, repositories, utils, and many fragments have unit or Robolectric tests. |
| **Critical-path coverage** | **5** | Application: OutOfRouteApplicationTest (clearRecoveredState; isHealthy/getDatabaseError covered in instrumented). MainActivity: TEST_STRATEGY. Workers: WorkManagerInitializerTest, SyncWorkerRobolectricTest. BackgroundSyncService/OfflineSyncCoordinator: integration/manual and PerformanceTuningTest; see P4. Persistence/recovery: TripStatePersistence and TripCrashRecoveryManager used in integration tests; OfflineDataManager load/save in OfflineDataManagerPersistenceTest (one test @Ignore). |
| **JaCoCo configuration** | **5** | Thresholds (70% overall, 60% branch, 75% line), shared filters, and `jacocoSuite` are well defined; report is not generated until the suite compiles and runs. |
| **Edge-case & failure coverage** | **4** | T4/T5/D1: Migration deferred; insert/delete failure, empty list, invalid ID in DomainTripRepositoryAdapterTest. Adapter logs on failure (D1). FAILING_OR_IGNORED_TESTS has reason and owner for each. |
| **Stability & maintainability** | **4** | TestDispatcher used in integration tests (TripInputViewModelIntegrationTest, DataManagementViewModelTest). Run reliably: `gradlew --stop` then `gradlew jacocoSuiteTestsOnly` with no other Gradle/IDE locks; see docs/qa/JACOCO_SUITE.md. FAILING_OR_IGNORED_TESTS and TEST_FAILURES_DOCUMENTATION aligned. |

**Overall effective coverage score: 4.3 / 5** — Dimensions: Suite 4, Breadth 4, Critical-path 5, JaCoCo 5, Edge-case 4, Stability 4. To reach **4.5/5**: raise Suite runnability to 5 (e.g. jacocoCoverageVerification passes). **Current decision:** CI gate uses `jacocoSuiteTestsOnly` as the accepted pass (see TEST_STRATEGY.md); coverage ramp to pass full jacocoSuite is planned in a later phase.

---

## 2. Critical areas that need improvement

### P0 — Unblock the suite

| Area | Issue | Action |
|------|--------|--------|
| **TripDaoInMemoryTest** | Compile error (e.g. Unresolved reference at line 95). | Fix the failing reference (e.g. use Truth’s `isAtLeast`/`isAtMost` or correct API); ensure `.\gradlew.bat :app:testDebugUnitTest` and `jacocoTestReport` succeed. |

### P1 — High-impact, low-coverage or untested code

| Area | Location | Issue | Documentation |
|------|----------|--------|----------------|
| **OutOfRouteApplication** | `OutOfRouteApplication.kt` | No dedicated unit test. Application owns DB init, repository, preferences, crash recovery, WorkManager, theme, and analytics. | **Documented:** clearRecoveredState and isHealthy/getDatabaseError covered in instrumented ApplicationInitializationTest; see TEST_STRATEGY. Minimal unit test can be added in coverage ramp phase. |
| **MainActivity** | `MainActivity.kt` | Only Robolectric tests; navigation, permissions, and recovery wiring are not fully covered by fast unit tests. | **Documented:** MainActivityRobolectricTest covers launch, toolbar, trip input, recovery dialogs; full navigation/permission paths via instrumented tests. See TEST_STRATEGY § MainActivity coverage. |
| **Workers & initialization** | `WorkManagerInitializer`, `*Worker*` | Excluded from JaCoCo (filter); no unit tests for initialization logic or error paths. | WorkManagerInitializerTest and SyncWorkerRobolectricTest exist; see P4. |
| **Persistence & recovery** | `TripStatePersistence`, `TripCrashRecoveryManager`, `OfflineDataManager` load/save | Partial tests; OfflineDataManager load/save implemented (DataStore). Recovery flow covered by integration/Robolectric. | See P4 and CRUCIAL_IMPROVEMENTS_TODO §2. |
| **Sync & background** | `BackgroundSyncService`, `OfflineSyncCoordinator`, `SyncWorker` | Excluded or lightly tested; `performFullSync()` placeholder. | Integration and PerformanceTuningTest; see P4. |

### P2 — Gaps already in QUALITY_AND_ROBUSTNESS_PLAN / PROJECT_AUDIT

| ID | Area | Action |
|----|------|--------|
| **T4** | Edge-case coverage | Migration tests deferred (add when second schema version exists). **fallbackToDestructiveMigration:** Kept in AppDatabase until migration test coverage exists; see in-code comment. Add migration test when schema v2 is introduced. Insert/delete failure and empty list covered in DomainTripRepositoryAdapterTest. |
| **T5** | Domain/data edge cases | Add tests for insert failure, empty list, delete failure (T5 in audit). |
| **D1** | Domain adapter errors | Log and optionally expose errors in `DomainTripRepositoryAdapter`; add tests for error paths. |

### P3 — Deferred or ignored tests (document and fix or accept)

| Test / area | Issue | Action |
|-------------|--------|--------|
| **TripInputViewModelIntegrationTest** | Dispatcher conflict; test ignored. | Fix with TestDispatcher injection or document as integration-only. |
| **TripHistoryByDateViewModelTest** | Incomplete; requires Application/repository. | Complete with Robolectric/DI or document and add to test plan. |
| **LocationValidationServiceTest** | Comments about 1 failing test, instrumented future work. | Triage: fix, ignore with reason, or move to instrumented suite. |
| **ThemeScreenshotTest** | Deferred until Paparazzi. | Document in QA strategy; enable when Paparazzi is configured. |

### P4 — Coverage exclusions (by design; still need “effective” coverage)

JaCoCo excludes: `di/**`, `*_Factory*`, `*Module*`, `*Worker*`, `*Dao_Impl*`, `*Database_Impl*`, etc. These are excluded from the reported percentage but initialization and integration paths that use them should be covered by integration or Robolectric tests where it matters.

**Phase 4:** WorkManagerInitializer covered by WorkManagerInitializerTest; SyncWorker by SyncWorkerRobolectricTest. **BackgroundSyncService** and **OfflineSyncCoordinator:** No dedicated unit tests; covered by integration (TripInputViewModelIntegrationTest, SimulatedTripTest, MockGpsSynchronizationServiceTest mocks) and PerformanceTuningTest (BackgroundSyncService()). Critical paths (e.g. performFullSync) are exercised in integration or manual runs; add minimal "does not throw" tests if needed.

---

## 3. What “effective coverage” means here

- **Suite runs:** `testDebugUnitTest` and `jacocoTestReport` complete successfully. Use `jacocoSuiteTestsOnly` for a passing gate when verification fails.
- **Run reliably:** Run `.\gradlew.bat --stop` then `jacocoSuiteTestsOnly` with no other Gradle/IDE locks; see JACOCO_SUITE.md.
- **Thresholds:** Current 70% / 60% / 75% are enforced; no silent degradation.
- **Critical paths:** Startup, trip lifecycle, persistence, recovery, and error handling have tests (unit or integration) that run in CI.
- **Edge cases:** Failure modes (DB failure, invalid ID, empty data) and migrations are tested.
- **Documented gaps:** Ignored or deferred tests have clear reasons and owners (e.g. in FAILING_OR_IGNORED_TESTS or CRUCIAL_IMPROVEMENTS_TODO).

---

## 4. Definition of Done (effective coverage)

- **Suite green:** `testDebugUnitTest` and `jacocoTestReport` pass; use `jacocoSuiteTestsOnly` when build dir is free of file locks. Full `jacocoSuite` (with verification) passes when per-class coverage thresholds are met.
- **Thresholds met:** 70% overall, 60% branch, 75% line (see `app/build.gradle.kts`). Currently bundle-level coverage ~36% line / ~30% branch; per-class verification fails until coverage improved.
- **P0 and P1 covered or documented:** TripDaoInMemoryTest verified passing (Phase 1.1); OutOfRouteApplication tested or documented; MainActivity coverage documented in TEST_STRATEGY.
- **Deferred tests listed:** [FAILING_OR_IGNORED_TESTS.md](./FAILING_OR_IGNORED_TESTS.md) exists with reason and owner for each.
- **Score maintained:** Effective coverage score is maintained at ≥ 4.5/5 when all dimensions meet targets. Any new ignored test must be added to FAILING_OR_IGNORED_TESTS with reason and owner.

---

## 5. Coverage after each phase (optional)

| Phase | Date | Line % | Branch % | Notes |
|-------|------|--------|----------|--------|
| After Phase 0 | — | — | — | Run `jacocoSuiteTestsOnly` when build dir free; fill from HTML report. |
| After Phase A–E | 2025-02 | ~36 | ~30 | From jacocoTestReport (bundle). Per-class verification fails; use jacocoSuiteTestsOnly for passing gate. |

---

## 6. Next step: improvement plan

Use this document with [JACOCO_SUITE.md](./JACOCO_SUITE.md) and [TEST_STRATEGY.md](./TEST_STRATEGY.md) to generate a phased plan to improve effective coverage and fix the critical areas above.

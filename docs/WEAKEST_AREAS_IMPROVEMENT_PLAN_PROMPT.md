# Prompt: Build a Plan to Improve the Weakest Areas of OutOfRouteBuddy

**Use this prompt** with an agent or as a brief to produce a **step-by-step improvement plan** (tasks, phases, file order, dependencies). Do not implement yet—only output the plan.

---

## Context

OutOfRouteBuddy is an Android app (Kotlin, MVVM, Hilt, Room) for tracking out-of-route miles. The codebase has been audited and documented. Below are the **weakest areas** identified from:

- `docs/qa/COVERAGE_SCORE_AND_CRITICAL_AREAS.md`
- `docs/qa/FAILING_OR_IGNORED_TESTS.md`
- `docs/qa/TEST_STRATEGY.md`
- `docs/CRUCIAL_IMPROVEMENTS_TODO.md`
- `docs/QUALITY_AND_ROBUSTNESS_PLAN.md`
- `docs/PROJECT_AUDIT_2025_02_27.md`

---

## Weakest Areas (Evidence-Based)

### 1. Test suite and coverage (highest impact)

- **JaCoCo:** Per-class coverage verification fails; bundle-level is ~36% line / ~30% branch. CI uses `jacocoSuiteTestsOnly` as the passing gate; full `jacocoSuite` (with verification) does not pass.
- **Ignored or incomplete tests:** TripInputViewModelIntegrationTest (dispatcher conflict), TripHistoryByDateViewModelTest (incomplete, needs Application/repository), LocationValidationServiceTest (failing assertion / instrumented work), ThemeScreenshotTest (deferred until Paparazzi), OfflineDataManagerPersistenceTest (one test flaky—DataStore timing in Robolectric). All documented in `docs/qa/FAILING_OR_IGNORED_TESTS.md`.
- **Critical paths under-tested:** OutOfRouteApplication (DB init, crash recovery, WorkManager) has no dedicated unit test; MainActivity has only Robolectric; Workers/SyncWorker/BackgroundSyncService are excluded from JaCoCo and lightly or not unit-tested; persistence/recovery (TripStatePersistence, TripCrashRecoveryManager, OfflineDataManager load/save) are partially covered by integration only.
- **Edge cases:** Migration tests deferred (schema export is true; add when second version exists). Insert/delete failure and empty-list behavior are partially covered in DomainTripRepositoryAdapterTest; error-path exposure to UI is still limited.

**Ask in the plan:** How to raise effective coverage to ≥4.5/5 (e.g. fix or document P0/P1 in COVERAGE_SCORE_AND_CRITICAL_AREAS), get `jacocoCoverageVerification` passing or formally accept `jacocoSuiteTestsOnly`, and resolve or clearly own each ignored/deferred test.

---

### 2. Data layer and error visibility

- **DomainTripRepositoryAdapter (D1):** On exception it logs and emits `null`/`emptyList()`. Callers (UI) never see an error state—no sealed result or error channel. QUALITY_AND_ROBUSTNESS_PLAN and PROJECT_AUDIT both call this out.
- **Database:** `fallbackToDestructiveMigration()` is still enabled (AppDatabase); PROJECT_AUDIT DB1: data loss risk on migration failure. Migration test coverage (T4) is deferred until a second schema version exists.
- **OfflineDataManager:** `saveOfflineStorage()` is fire-and-forget (launch on scope); on save failure only logs—no callback or retry exposed to callers. Load failure starts with empty storage (acceptable) but save failure is invisible to UI.
- **SyncWorker:** `performFullSync()` is a placeholder (TODO when backend sync is available). No real sync behavior to test.

**Ask in the plan:** How to expose repository/save failures to the UI (e.g. sealed result or error Flow) without breaking existing callers, and how to document or reduce reliance on `fallbackToDestructiveMigration` (and add migration tests when schema v2 exists).

---

### 3. Build, config, and maintainability

- **Gradle 9:** Deprecation warnings make the build incompatible with Gradle 9.0. CRUCIAL_IMPROVEMENTS_TODO §1: run with `--warning-mode all`, document in `GRADLE_9_MIGRATION_NOTES.md`, plan plugin updates.
- **Lint:** `abortOnError = false` for lint (PROJECT_AUDIT CFG3)—issues can accumulate; CI may pass with problems.
- **Java version:** Audit noted possible mismatch (root says Java 17; app might reference VERSION_1_8). Confirm and align.
- **Release:** `isMinifyEnabled = false` in release (CFG2)—larger APK, no obfuscation for production.

**Ask in the plan:** Ordered steps to fix Gradle 9 readiness, align Java version, and optionally tighten lint (e.g. abortOnError or separate strict job) and release minification.

---

### 4. Security and production polish

- **Secrets and PII (CRUCIAL §7):** google-services.json in repo; location/trip data storage and transmission; ensure .env and sensitive files in .gitignore; document in `docs/security/SECURITY_NOTES.md` or equivalent.
- **Logging (L1):** Excessive `android.util.Log` in production (MainActivity, TripPersistenceManager, OfflineDataManager, etc.)—performance and potential info leakage. PreferencesManager fallback (e.g. getPeriodMode) is silent—log at debug/warn when falling back.

**Ask in the plan:** A small, actionable security checklist (secrets, PII, .gitignore) and a logging policy (e.g. strip or gate verbose logs in release, add fallback logging where needed).

---

### 5. Product and feature debt (documented, not yet done)

- **Offline persistence:** OfflineDataManager load/save are implemented (DataStore); ensure integration with trip lifecycle and sync is clear and tested (CRUCIAL §2 may be partially done; verify).
- **Location jump detection:** TripStateManager TODO “Implement jump detection” (CRUCIAL §3)—define and implement or document for later.
- **Trip history → details:** TripHistoryByDateDialog TODO “Navigate to trip details if needed” (CRUCIAL §4)—wire if screen exists or add to backlog.
- **Statistics: monthly only:** User request to remove weekly/yearly and keep monthly only (CRUCIAL §9)—ViewModel, repository, and tests need updates; doc references TripInputViewModel, TripRepository, DomainTripRepositoryAdapter, and test files.
- **saveCompletedTrip:** TripStatePersistence.saveCompletedTrip never used (QUALITY_AND_ROBUSTNESS_PLAN R1)—document as future or wire.

**Ask in the plan:** Prioritization of these items (e.g. which are P1 vs backlog) and dependency order (e.g. statistics monthly-only before reports screen).

---

## What to Produce

1. **Phased improvement plan** with:
   - **Phases** (e.g. Phase 1: Unblock suite & critical tests; Phase 2: Data-layer error visibility; Phase 3: Build/Gradle/config; Phase 4: Security & logging; Phase 5: Product/feature debt).
2. **Per phase:** Concrete **tasks** with:
   - **Owner** (e.g. QA, Back-end, Front-end, DevOps, Security).
   - **Files or modules** to touch.
   - **Dependencies** (what must be done before).
   - **Definition of done** (e.g. “jacocoCoverageVerification passes” or “FAILING_OR_IGNORED_TESTS updated with reason and owner”).
3. **References:** Point each task to the relevant doc (COVERAGE_SCORE_AND_CRITICAL_AREAS, QUALITY_AND_ROBUSTNESS_PLAN, CRUCIAL_IMPROVEMENTS_TODO, PROJECT_AUDIT, etc.) so the plan is traceable.
4. **Risks and trade-offs:** Note any “quick win vs proper fix” (e.g. accept jacocoSuiteTestsOnly vs raising coverage to pass verification) and recommend which path to take.

---

## Constraints

- **No UI/layout changes** unless required for wiring (e.g. showing errors to the user).
- **No new features** beyond what is already in the roadmap/briefs; this plan is about **hardening and fixing weak areas**.
- Prefer **small, reviewable steps**; avoid large refactors in a single phase.
- If the plan suggests changing behavior (e.g. repository error API), call out **call-site impact** and suggest migration order.

---

## Optional: One-Page Summary

After the full plan, add a **one-page summary** table:

| Phase | Goal | Key tasks | Owner(s) | Depends on |
|-------|------|-----------|----------|------------|
| 1 | … | … | … | — |
| 2 | … | … | … | Phase 1 |
| … | … | … | … | … |

Use this prompt as-is or adapt the “Weakest Areas” and “What to Produce” sections to match your priorities (e.g. emphasize tests first or security first).

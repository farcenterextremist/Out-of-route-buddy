# Health & Resilience and Code Quality — Improvement Plan Prompt

**Purpose:** Use this prompt to generate a concrete implementation plan that raises the **Health & resilience** dimension and the **Code quality** dimension from **3.5 to 4.5** (on the 1–5 scale) in [FULL_COVERAGE_APP_SCORE.md](FULL_COVERAGE_APP_SCORE.md).

**Current state (from FULL_COVERAGE_APP_SCORE.md):**

| Dimension | Current P | Current C | Target | Gap |
|-----------|-----------|-----------|--------|-----|
| **Health & resilience** | 3.5 | 4 | **4.5** | Health API underused in flows; some error paths not visible; startup/DB recovery could be stronger. |
| **Code quality** | 3.5 | 3.5 | **4.5** | R1 deferred; one remaining `!!`; L1 logging polish; logging consistency and KDoc. |

---

## Context

**OutOfRouteBuddy** is an Android app (Kotlin, MVVM, Hilt, Room) for tracking out-of-route miles. The app already has:

- **Health:** `OutOfRouteApplication.isHealthy()` and `getDatabaseError()`; `DatabaseHealthCheck` with integrity check and `performHealthCheck(database)`; Application and instrumented tests cover health. **Gap:** The full `DatabaseHealthCheck.performHealthCheck()` is not run at app startup and is not wired to `OutOfRouteApplication`’s health state. No UI or flow checks `isHealthy()` before starting a trip or shows “Database unavailable” when unhealthy. Recovery (TripRecoveryDialog, TripStatePersistence, detector reset) is implemented.
- **Error handling:** D1/D2 done (adapter `loadErrors` Flow, delete/update snackbar); C3/C4 done (handleEvent shows Error/CalculationError/SaveError). Some paths may still swallow or not surface errors.
- **Code quality:** QUALITY_AND_ROBUSTNESS_PLAN and LOGGING_POLICY exist. R1 (`saveCompletedTrip` never used) is DEFERRED; R2/R4 done. L2/L3 done. One remaining `!!` in MainActivity (listenerHolder[0]!!). L1: PreferencesManager `getPeriodMode` already logs fallback (AppLogger.d/w)—verify and mark L1 done. ARCHITECTURE.md exists; KDoc is sparse on public APIs.

**References:**

- [FULL_COVERAGE_APP_SCORE.md](FULL_COVERAGE_APP_SCORE.md) — Section 3.2 (Health & resilience), Section 3.8 (Code quality)
- [QUALITY_AND_ROBUSTNESS_PLAN.md](QUALITY_AND_ROBUSTNESS_PLAN.md) — D1, D2, D3, R1, L1, L2, L3 status
- [PROJECT_AUDIT_2025_02_27.md](PROJECT_AUDIT_2025_02_27.md) — audit IDs
- [docs/technical/LOGGING_POLICY.md](technical/LOGGING_POLICY.md) — logging facade and PII rules
- [ARCHITECTURE.md](ARCHITECTURE.md) — layers and persistence

---

## Goal

Produce a **prioritized, implementable plan** (tasks, owners, acceptance criteria, and optional test strategy) that:

1. **Health & resilience:** Raise both Performance and Completion to **4.5** by making health checks part of startup and critical flows, surfacing errors consistently, and ensuring recovery paths are tested and visible.
2. **Code quality:** Raise both Performance and Completion to **4.5** by resolving dead code (R1), removing the remaining unsafe `!!`, completing logging policy (L1), and improving maintainability (KDoc, config in one place).

**Out of scope:** UI layout/styling changes (except minimal wiring for “DB unavailable” or retry); new features; large refactors beyond what’s needed for health and quality.

---

## Health & resilience — Target 4.5

Use the **“5 (Strong)”** column of FULL_COVERAGE_APP_SCORE Section 3.2 as the target:

| Criterion | 5 (Strong) | Current gap |
|-----------|-------------|-------------|
| **Startup** | Clean start; DB/prefs init with fallback | DB init has try/catch; no run of `DatabaseHealthCheck.performHealthCheck()` at startup; no fallback UI (e.g. retry or “DB unavailable”) |
| **DB & services** | isHealthy() / getDatabaseError() used; recovery paths tested | Health exists and is tested; not used in MainActivity or before Start trip; full integrity check not run at startup |
| **Error handling** | Errors logged; UI shows snackbar or retry | D1/D2/C3/C4 done; audit all handleEvent branches and adapter loadErrors consumers for missing snackbar or log |
| **Recovery** | Recovery dialog; persist/restore state; detector reset on end | Implemented; ensure detector reset when user ends trip in-app is tested and documented |

**Concrete directions for the plan:**

1. **Startup health**
   - Run `DatabaseHealthCheck.performHealthCheck(database)` during or immediately after DB initialization (e.g. in `OutOfRouteApplication` after database is created, or on first access). If the check fails, set `databaseError` (or equivalent) so `isHealthy()` returns false and `getDatabaseError()` returns a meaningful exception.
   - Optionally: on first launch or after a failed health check, offer a non-blocking “Database issue” message and a “Retry” or “Rebuild database” action (e.g. in Settings or a one-time dialog), with `DatabaseHealthCheck.rebuildCorruptedDatabase()` as last resort. Document data-loss warning.

2. **Use health in flows**
   - Before starting a trip (e.g. in TripInputViewModel or MainActivity when user taps Start): if `(app as? OutOfRouteApplication)?.isHealthy() == false`, show a snackbar or dialog “Database unavailable. Please retry or restart the app.” and do not start the tracking service.
   - Optionally: expose health state to a Settings “System status” or “Database status” row (read-only) so support/debugging can see “Healthy” vs “Error”.

3. **Error handling audit**
   - List all `handleEvent` branches in TripInputFragment (and any other fragment that shows errors). Ensure every Error, CalculationError, SaveError, and any adapter-loadError path shows a snackbar (or equivalent) and that errors are logged (AppLogger.w/e) with no PII.
   - Ensure TripHistoryViewModel and TripHistoryByDateViewModel observe `loadErrors` / `deleteError` and show snackbar; add tests that verify error path shows UI feedback.

4. **Recovery**
   - Confirm detector reset when user ends trip in-app (ACTION_TRIP_ENDED_FROM_APP or equivalent) is implemented and covered by a test. Document in TEST_STRATEGY or recovery doc.

5. **Tests**
   - Add or extend tests: (1) Application startup when `performHealthCheck` fails sets app unhealthy; (2) Start-trip flow when app is unhealthy shows message and does not start service; (3) Optional: recovery path when DB is rebuilt.

---

## Code quality — Target 4.5

Use the **“5 (Strong)”** column of FULL_COVERAGE_APP_SCORE Section 3.8 as the target:

| Criterion | 5 (Strong) | Current gap |
|-----------|-------------|-------------|
| **Dead code** | saveCompletedTrip wired or deferred with owner; no unused entry points | R1: saveCompletedTrip never used — either wire to trip-end flow (with owner and doc) or remove and document “not needed”; ensure no other unused public entry points |
| **Logging** | Logging facade; no PII in release; LOGGING_POLICY | AppLogger used in PreferencesManager; verify L1 (getPeriodMode fallback log) done and mark PENDING→DONE; audit MainActivity, TripPersistenceManager, OfflineDataManager for raw Log.* and replace with AppLogger where appropriate |
| **Safety** | No unnecessary !!; invalid ID handled | One remaining `!!` in MainActivity (listenerHolder[0]!!); replace with requireNotNull(listenerHolder[0]) or a local val after assignment so no !! |
| **Maintainability** | Clear layers; config in one place; KDoc where needed | ARCHITECTURE present; add KDoc for key public APIs (e.g. TripRepository, DomainTripRepositoryAdapter, TripInputViewModel, TripStatePersistence) and document config (e.g. ValidationConfig, period modes) in one place if not already |

**Concrete directions for the plan:**

1. **R1 saveCompletedTrip**
   - **Option A:** Wire `TripStatePersistence.saveCompletedTrip()` into the trip-end flow (when user confirms End trip) with clear owner and doc; add a test that state is saved.  
   - **Option B:** If product decides GPS metadata via this path is not needed, remove the method (or mark deprecated) and document in QUALITY_AND_ROBUSTNESS_PLAN and code why it’s not used. Update R1 status to DONE either way.

2. **Logging (L1 and consistency)**
   - Confirm PreferencesManager.getPeriodMode() logs at debug/warn on fallback (already has AppLogger.d/w). Mark L1 DONE in QUALITY_AND_ROBUSTNESS_PLAN.
   - Audit: MainActivity, TripPersistenceManager, OfflineDataManager, TripStatePersistence — replace any remaining raw `Log.d`/`Log.v` in hot paths with AppLogger per LOGGING_POLICY; ensure no PII in log messages.

3. **Remove remaining !!**
   - MainActivity: replace `listenerHolder[0]!!` with a safe pattern (e.g. `listenerHolder[0]?.let { navController.addOnDestinationChangedListener(it) }` after assignment, or `requireNotNull(listenerHolder[0])` with a comment). Ensure no NPE in edge cases (e.g. navController or listenerHolder empty).

4. **Maintainability**
   - Add KDoc (summary + params/return where relevant) for: TripRepository (interface), DomainTripRepositoryAdapter (class and main public methods), TripInputViewModel (class and public methods used by Fragment), TripStatePersistence (class and save/load APIs). Optionally: single “Config” or “Constants” section in ARCHITECTURE or technical doc pointing to ValidationConfig, period mode defaults, and notification channel IDs.

5. **Tests**
   - Add or update unit tests: (1) TripInputFragment or ViewModel shows snackbar for Error/SaveError (if not already covered); (2) Optional: test that saveCompletedTrip is called when wired, or that it’s intentionally not called when removed.

---

## Deliverables for the generated plan

1. **Prioritized task list** for Health & resilience and for Code quality (e.g. P0/P1/P2 or Must have / Should have / Nice to have).
2. **Per task:** short description, files/layers to touch, acceptance criteria, and optional test strategy.
3. **Dependencies:** call out any order (e.g. “Run health check at startup” before “Use isHealthy before Start trip”).
4. **Docs to update:** QUALITY_AND_ROBUSTNESS_PLAN.md (R1, L1 status), FULL_COVERAGE_APP_SCORE.md (re-score Health and Code quality to 4.5 after work), TEST_STRATEGY or recovery doc if new tests are added.
5. **Definition of done:** Health & resilience and Code quality dimension scores both reach 4.5 (Performance and Completion) when re-scored using FULL_COVERAGE_APP_SCORE Section 3.2 and 3.8.

---

## How to use this prompt

Copy the sections above (from **Context** through **Deliverables**) into a new document or chat, and ask the agent to:

- **Generate a concrete implementation plan** (tasks, owners, acceptance criteria, and test strategy) that satisfies the Health & resilience and Code quality targets.
- **Output the plan** in a structured format (e.g. markdown with tables or numbered tasks) suitable for backlog or sprint planning.
- **Optionally:** Produce a single consolidated checklist that can be used to track progress and re-score the two dimensions.

---

*This prompt was created from research of FULL_COVERAGE_APP_SCORE.md, QUALITY_AND_ROBUSTNESS_PLAN.md, PROJECT_AUDIT_2025_02_27.md, LOGGING_POLICY.md, ARCHITECTURE.md, and the codebase (OutOfRouteApplication, MainActivity, DatabaseHealthCheck, PreferencesManager, DomainTripRepositoryAdapter).*

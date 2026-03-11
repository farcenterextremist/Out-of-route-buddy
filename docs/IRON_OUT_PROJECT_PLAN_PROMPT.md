# Prompt: Create a Plan to Iron Out the OutOfRouteBuddy Project

**Copy the prompt below** and use it with an AI assistant (or planning session) to generate a phased implementation plan. The audit findings are in `docs/PROJECT_AUDIT_2025_02_27.md`.

---

## The Prompt

```
You are a technical project planner. I need a detailed, phased implementation plan to iron out the OutOfRouteBuddy Android project based on a master audit.

**Context:**
- OutOfRouteBuddy is an Android app (Kotlin, MVVM, Hilt, Room) for tracking out-of-route miles.
- A full audit has been completed. The findings are documented in docs/PROJECT_AUDIT_2025_02_27.md.
- There is also an existing QUALITY_AND_ROBUSTNESS_PLAN.md in docs/ with specific bug IDs (C1, C2, C3, C4, B1, B2, D1–D3, W1, R1–R4, T1–T4, L1–L3).

**Constraints:**
- Do NOT make any UI/layout/styling changes unless explicitly required for wiring (e.g., showing error messages).
- Prefer incremental, low-risk fixes that can be validated with existing tests.
- Each phase should be independently shippable where possible.
- Assume one developer working part-time; estimate effort in "story points" or "days" per phase.

**Your task:**
Create a phased implementation plan that:

1. **Phase 0 — Pre-flight**
   - List all files that need to be modified (with paths).
   - Identify any new tests required before changes.
   - Call out dependencies between phases (e.g., "Phase 2 depends on Phase 1").

2. **Phase 1 — Critical Bugs (P0)**
   - Fix C1: getTripsByStatus never emits (DomainTripRepositoryAdapter).
   - Fix C2: Loading never set to false (TripHistoryViewModel).
   - Include exact code changes or pseudocode for each fix.
   - Add/update unit tests to prevent regression.

3. **Phase 2 — User-Visible Fixes (P1)**
   - Fix C3: Wire Error, CalculationError, SaveError to show Snackbar/toast.
   - Fix C4: Extend domain deleteTrip to return success/failure; wire UI.
   - Fix W1: Refresh TripHistoryByDateViewModel when dialog becomes visible.
   - No UI redesign—only wiring.

4. **Phase 3 — Concurrency & Blocking (P2)**
   - Fix B1: Make UnifiedTripService.getTripStatistics() suspend or expose Flow.
   - Fix B2: Remove runBlocking from PerformanceTracker.
   - Ensure no Main-thread blocking.

5. **Phase 4 — Data Layer & Config (P3)**
   - Fix D1: Log and optionally expose errors in DomainTripRepositoryAdapter.
   - Fix D2: Align domain TripRepository delete/update return types.
   - Fix D3: Remove StateCache from TripInputViewModel or implement invalidation.
   - Fix DB1: Consider removing fallbackToDestructiveMigration (or document risk).
   - Fix DB2: Enable exportSchema = true for Room.
   - Fix CFG1: Align Java version (1.8 vs 17) across build files.
   - Fix CFG2: Enable minification for release (with ProGuard testing).
   - Fix CFG3: Set lint abortOnError = true after resolving issues.

6. **Phase 5 — Dead Code & Polish (P4)**
   - Fix R1–R4: Use saveCompletedTrip or remove; implement or remove autoSaveTripState; fix getCurrentActiveTrip; remove unused StateCache.
   - Fix L1: Introduce conditional logging (debug vs release).
   - Fix L2: Remove !! in CustomCalendarDialog with safe checks.
   - Fix L3: Validate trip ID before delete.
   - Fix S1: Implement SyncWorker.performFullSync() or document as future work.
   - Fix S2: Ensure UnifiedTripService.cleanup() is called from appropriate lifecycle.

7. **Phase 6 — Testing (P5)**
   - Add DomainTripRepositoryAdapter tests (T1).
   - Fix TripHistoryViewModelTest temp dir cleanup (T2).
   - Replace brittle call-order assertions in TripStatisticsWiringTest (T3).
   - Add Room migration tests (T4).
   - Add edge-case tests: insert failure, empty list, delete failure (T5).

8. **Phase 7 — Documentation**
   - Add root README.md with project overview, build instructions, and link to docs/.
   - Update QUALITY_AND_ROBUSTNESS_PLAN.md to mark completed items.
   - Create a CHANGELOG or release notes for the "iron out" release.

**Output format:**
- Use markdown with clear headings for each phase.
- For each fix, include: ID (e.g., C1), file path, brief description, and implementation notes.
- Provide a summary table: Phase | Items | Est. Effort | Dependencies.
- End with a "Definition of Done" checklist for the overall plan.
```

---

## How to Use This Prompt

1. Open `docs/PROJECT_AUDIT_2025_02_27.md` and `docs/QUALITY_AND_ROBUSTNESS_PLAN.md` for reference.
2. Paste the prompt above into your AI assistant or planning tool.
3. Use the generated plan as a roadmap; adjust phases based on your capacity.
4. Track progress by updating the audit doc and QUALITY_AND_ROBUSTNESS_PLAN as items are completed.

---

## Quick Reference: Audit IDs

| ID | Category | Description |
|----|----------|-------------|
| C1–C4 | Critical | Hang, loading, error display, delete failure |
| B1–B2 | Concurrency | runBlocking on Main |
| D1–D3 | Data | Exception swallowing, return types, StateCache |
| DB1–DB3 | Database | Migration fallback, exportSchema, blocking DAO |
| CFG1–CFG4 | Config | Java version, minify, lint, Gradle |
| R1–R4 | Dead code | Unused methods, StateCache |
| L1–L3 | Logging/null | Excessive logs, !!, invalid ID |
| S1–S3 | Services | SyncWorker, cleanup, permissions |
| T1–T5 | Testing | Adapter tests, temp dir, migrations, edge cases |
| W1 | Wiring | Stale history-by-date dialog |

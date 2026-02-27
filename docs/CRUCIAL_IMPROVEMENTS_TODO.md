# Crucial improvements – to-do list (brainstorm)

**Created:** 2025-02-19  
**Purpose:** Prioritized list of high-impact improvements with role delegation. Proposed to the user via email for approval and prioritization.

---

## 1. Build & docs alignment — **DevOps Engineer**

- **Gradle 9 readiness:** Build currently reports "Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0." Run with `--warning-mode all` to identify sources; document in `GRADLE_9_MIGRATION_NOTES.md` and plan plugin updates. *(Ref: `build_warnings.txt`, `GRADLE_9_MIGRATION_NOTES.md`.)*
- **DEPLOYMENT.md vs actual build:** ✅ **Done.** `docs/DEPLOYMENT.md` now correctly states minSdk 24 and Java 17.

**Artifacts:** Updated `docs/DEPLOYMENT.md`, updated `GRADLE_9_MIGRATION_NOTES.md`.

---

## 2. Offline data persistence — **Back-end Engineer**

- **OfflineDataManager:** Two TODOs in `app/.../data/OfflineDataManager.kt`:
  - `loadOfflineStorage()` — implement actual loading from local storage (SharedPreferences, Room, or existing DB).
  - `saveOfflineStorage()` — implement actual saving to local storage.
- Currently these only log; offline trips are not persisted across app restarts. Either implement or document as "future phase" and add a brief to `docs/product/` so Design and QA are aligned.

**Artifacts:** Implementation in `OfflineDataManager.kt` and/or `docs/product/FEATURE_BRIEF_offline_persistence.md`.

---

## 3. Location jump detection — **Back-end Engineer**

- **TripStateManager:** TODO at line 231: "Implement jump detection" for `locationJumps`. Define what counts as a jump (e.g. implausible speed/distance between samples) and implement or document for a later sprint.

**Artifacts:** Implementation in `TripStateManager.kt` and/or short note in `docs/technical/` or feature brief.

---

## 4. Trip history navigation — **Front-end Engineer** (with Back-end if data needed)

- **TripHistoryByDateDialog:** TODO "Navigate to trip details if needed" (line 108). If trip details screen exists, wire the navigation; otherwise add to UI/UX backlog for a "trip detail" screen.

**Artifacts:** Navigation wiring in `TripHistoryByDateDialog.kt` or handoff to UI/UX for flow.

---

## 5. Test health: fix or document — **QA Engineer**

- **TripInputViewModelIntegrationTest:** One test ignored with "TODO: Fix dispatcher conflict with Dispatchers.IO in ViewModel". Fix the test (e.g. inject TestDispatcher) or document in `docs/qa/` and in the test so it’s not forgotten.
- **TripHistoryByDateViewModelTest:** "TODO: These tests are incomplete - they require Application context and repository setup." Either complete with Robolectric/DI or document as integration-only and add to test plan.
- **LocationValidationServiceTest:** Comments reference "PHASE 1 FIXES" (1 failing test), "INSTRUMENTED TESTS" (future device tests), and "REMAINING HEAVY TRAFFIC ENHANCEMENT STEPS". QA to triage: fix, ignore with reason, or move to instrumented suite.
- **ThemeScreenshotTest:** "TODO: Uncomment when Paparazzi is configured." Either add Paparazzi and enable the test or document in `docs/qa/` that screenshot tests are deferred.

**Artifacts:** Fixed tests or `docs/qa/TEST_STRATEGY.md` / test-plan updates and clear `@Ignore` reasons.

---

## 6. Product roadmap and feature briefs — **Project Design / Creative Manager**

- **docs/product/** currently has only README; no `ROADMAP.md` or `FEATURE_BRIEF_*.md`. Create:
  - **ROADMAP.md:** High-level themes and "what’s next" (can align with existing `docs/agents/WORKER_TODOS_AND_IDEAS.md`).
  - At least one **FEATURE_BRIEF_** (e.g. Auto drive or Offline persistence) so Back-end, QA, and UI/UX have a single source of truth for behavior and acceptance criteria.

**Artifacts:** `docs/product/ROADMAP.md`, `docs/product/FEATURE_BRIEF_<name>.md`.

---

## 7. Security and secrets review — **Security Specialist**

- **google-services.json** is in the repo (project id, app id, etc.). Confirm this is acceptable for this project (e.g. not sensitive) and document in a short security note; recommend moving to CI secrets or build-time injection if needed.
- **Location and PII:** Brief review of where location/trip data is stored and transmitted; recommend any hardening (e.g. encryption at rest, no logging of PII).
- **Coordinator email:** Confirm `.env` and `last_reply.txt` are in `.gitignore` and document a one-time checklist for new dev machines (already in WORKER_TODOS; ensure it’s done).

**Artifacts:** Short `docs/security/SECURITY_NOTES.md` or additions to existing security docs.

---

## 8. Single source of truth for improvement list — **File Organizer**

- ✅ **Done.** This document is referenced from `docs/README.md` (Quick links), `docs/agents/team-parameters.md` (Current improvement list), and `docs/agents/WORKER_TODOS_AND_IDEAS.md`.

**Artifacts:** One or two cross-links in `docs/agents/` and/or root README.

---

## 9. Statistics section: monthly only (user preference) — **UI/UX, Front-end, Back-end, QA**

- **User request:** Remove weekly and yearly statistics from the statistics section; keep **monthly only**.
- **UI/UX Specialist:** Update the statistics section spec: one aggregate view (monthly). Remove any wireframe/spec for weekly or yearly tabs/labels. Document in `docs/ux/` or a short note so Front-end and QA align.
- **Front-end Engineer:** In `TripInputViewModel.kt`, load only monthly statistics (remove `weeklyDeferred` / `yearlyDeferred` and `weeklyStatistics` / `yearlyStatistics` from UI state). Update any layout or strings that show "Weekly" / "Yearly" (e.g. statistics row, period picker) to show only monthly. Ref: `TripInputViewModel.kt` (lines ~231–244, ~1012–1014), `presentation/viewmodel/`, `res/` if any statistics labels exist.
- **Back-end Engineer:** In `TripRepository` interface and `DomainTripRepositoryAdapter`, remove or deprecate `getWeeklyTripStatistics()` and `getYearlyTripStatistics()`; keep `getMonthlyTripStatistics()`. Implementations in `data/repository/` and any callers (besides ViewModel) must be updated. Ref: `domain/repository/TripRepository.kt`, `data/repository/DomainTripRepositoryAdapter.kt`.
- **QA Engineer:** Update or remove tests that assert on weekly/yearly statistics. In `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, and any mock that stubs `getWeeklyTripStatistics` / `getYearlyTripStatistics`, switch to monthly-only expectations. Ref: `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, `TestConfig.kt` (statistics_button description).

**Artifacts:** UI/UX spec or note; ViewModel and UI changes; repository interface and impl; updated tests.

---

## Summary table

| # | Improvement area              | Owner(s)                    | Priority (suggested) |
|---|------------------------------|-----------------------------|----------------------|
| 1 | Build & docs alignment       | DevOps                      | High                 |
| 2 | Offline data persistence     | Back-end                    | High                 |
| 3 | Location jump detection      | Back-end                    | Medium               |
| 4 | Trip history → details       | Front-end                   | Medium               |
| 5 | Test health (fix or document)| QA                          | High                 |
| 6 | ROADMAP + FEATURE_BRIEF(s)   | Design / Creative Manager   | High                 |
| 7 | Security and secrets review  | Security Specialist         | High                 |
| 8 | Cross-link improvement list  | File Organizer              | Low                  |
| 9 | Statistics: monthly only     | UI/UX, Front-end, Back-end, QA | High              |

---

*Next step: User reviews this list via email and replies with priorities, approvals, or changes. Coordinator will update this doc and assign work accordingly.*

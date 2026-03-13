# Crucial improvements – to-do list (brainstorm)

**Created:** 2025-02-19  
**Purpose:** Prioritized list of high-impact improvements with role delegation. Proposed to the user via email for approval and prioritization.

---

## 1. Build & docs alignment — **DevOps Engineer**

- **Gradle 9 migration:** ✅ **Done.** Project now uses **Gradle 9.0.0** with Kotlin 2.0.21, Compose Compiler plugin, and AGP 8.13. Wrapper updated; `buildDir` replaced with `layout.buildDirectory`; Kotlin/Compose/KSP versions aligned. See `GRADLE_9_MIGRATION_NOTES.md`. *(Removed from heavy list — migration complete.)*
- **DEPLOYMENT.md vs actual build:** ✅ **Done.** `docs/DEPLOYMENT.md` now correctly states minSdk 24 and Java 17.

**Artifacts:** Updated `docs/DEPLOYMENT.md`, `GRADLE_9_MIGRATION_NOTES.md`; Gradle 9.0.0, Kotlin 2.0.21, `gradle-wrapper.properties`, `build.gradle.kts`, `libs.versions.toml`, `app/build.gradle.kts`.

---

## 2. Offline data persistence — **Back-end Engineer**

- ✅ **Done.** `OfflineDataManager.loadOfflineStorage()` and `saveOfflineStorage()` use DataStore + Gson. Offline trips and sync state survive app restart. See `docs/technical/OFFLINE_PERSISTENCE.md`.
- **SyncWorker / backend sync:** Full sync and offline backend upload are **deferred until backend is available**. Single reference: `SyncWorker.performFullSync()` (see in-code KDoc) and [docs/technical/OFFLINE_PERSISTENCE.md](technical/OFFLINE_PERSISTENCE.md). No behavioral change until backend exists.

**Artifacts:** Implemented; see OFFLINE_PERSISTENCE.md. Feature brief: `docs/product/FEATURE_BRIEF_offline_persistence.md`.

---

## 3. Location jump detection — **Back-end Engineer**

- ✅ **Done.** `TripStateManager` implements jump detection in `updateGpsMetadata()`: implied speed (distance/time) above `JUMP_SPEED_THRESHOLD_MS` (120 mph) counts as a jump; `locationJumps` is persisted in GPS metadata and to `TripEntity.locationJumpsDetected`. See `docs/technical/JUMP_DETECTION_AND_TRIP_STATE.md` (if present) or in-code KDoc.

**Artifacts:** Implementation in `TripStateManager.kt`; no further work required for this item.

---

## 4. Trip history navigation — **Front-end Engineer** (with Back-end if data needed)

- **TripHistoryByDateDialog / TripHistoryStatCardAdapter:** TripDetailsFragment exists. Navigation from history list (e.g. tap on stat card) to TripDetailsFragment is **not wired** (adapter comment: "Tap to expand/collapse metadata; no navigation to TripDetailsFragment"). Wire navigation when product prioritizes trip-detail-from-history, or keep on UI/UX backlog.
- **Status (Codebase Audit improvement 5):** Documented here as the single source for "trip history → details" status. Wiring deferred to UI/UX backlog until product prioritizes.

**Artifacts:** Navigation wiring in `TripHistoryByDateDialog.kt` / adapter or handoff to UI/UX for flow.

---

## 5. Test health: fix or document — **QA Engineer**

- **TripInputViewModelIntegrationTest:** One test ignored with "TODO: Fix dispatcher conflict with Dispatchers.IO in ViewModel". Fix the test (e.g. inject TestDispatcher) or document in `docs/qa/` and in the test so it’s not forgotten.
- **TripHistoryByDateViewModelTest:** "TODO: These tests are incomplete - they require Application context and repository setup." Either complete with Robolectric/DI or document as integration-only and add to test plan.
- **LocationValidationServiceTest:** Comments reference "PHASE 1 FIXES" (1 failing test) and "REMAINING HEAVY TRAFFIC ENHANCEMENT STEPS". QA to triage: fix in unit suite or ignore with reason. *(Improvement Loop runs unit tests only; no instrumented tests in this environment.)*
- **ThemeScreenshotTest:** "TODO: Uncomment when Paparazzi is configured." Either add Paparazzi and enable the test or document in `docs/qa/` that screenshot tests are deferred.

**Artifacts:** `docs/qa/FAILING_OR_IGNORED_TESTS.md`, `docs/qa/TEST_STRATEGY.md`. See FAILING_OR_IGNORED_TESTS for current status (several items resolved).

---

## 6. Product roadmap and feature briefs — **Project Design / Creative Manager**

- ✅ **Done.** `docs/product/ROADMAP.md` exists with high-level themes and next steps. Multiple FEATURE_BRIEF docs exist (Auto drive, Offline persistence, Reports, Stat cards, Monthly stats). Back-end, QA, and UI/UX have a single source of truth.
  - **ROADMAP.md:** High-level themes and "what’s next" (can align with existing `docs/agents/WORKER_TODOS_AND_IDEAS.md`).
  - At least one **FEATURE_BRIEF_** (e.g. Auto drive or Offline persistence) so Back-end, QA, and UI/UX have a single source of truth for behavior and acceptance criteria.

**Artifacts:** `docs/product/ROADMAP.md`, `docs/product/FEATURE_BRIEF_*.md` — maintain and add new briefs as features are prioritized.

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
- **Weakest Areas Plan Phase 5.4:** Documented here; implementation deferred until UI change is approved (no unwarranted UI changes). When implementing: ViewModel load only monthly; remove/deprecate getWeeklyTripStatistics/getYearlyTripStatistics; update tests per below.
- **UI/UX Specialist:** Update the statistics section spec: one aggregate view (monthly). Remove any wireframe/spec for weekly or yearly tabs/labels. Document in `docs/ux/` or a short note so Front-end and QA align.
- **Front-end Engineer:** In `TripInputViewModel.kt`, load only monthly statistics (remove `weeklyDeferred` / `yearlyDeferred` and `weeklyStatistics` / `yearlyStatistics` from UI state). Update any layout or strings that show "Weekly" / "Yearly" (e.g. statistics row, period picker) to show only monthly. Ref: `TripInputViewModel.kt` (lines ~231–244, ~1012–1014), `presentation/viewmodel/`, `res/` if any statistics labels exist.
- **Back-end Engineer:** In `TripRepository` interface and `DomainTripRepositoryAdapter`, remove or deprecate `getWeeklyTripStatistics()` and `getYearlyTripStatistics()`; keep `getMonthlyTripStatistics()`. Implementations in `data/repository/` and any callers (besides ViewModel) must be updated. Ref: `domain/repository/TripRepository.kt`, `data/repository/DomainTripRepositoryAdapter.kt`.
- **QA Engineer:** Update or remove tests that assert on weekly/yearly statistics. In `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, and any mock that stubs `getWeeklyTripStatistics` / `getYearlyTripStatistics`, switch to monthly-only expectations. Ref: `TripStatisticsWiringTest.kt`, `TripInputViewModelIntegrationTest.kt`, `TestConfig.kt` (statistics_button description).

**Artifacts:** UI/UX spec or note; ViewModel and UI changes; repository interface and impl; updated tests.

---

## Summary table

| # | Improvement area              | Owner(s)                    | Status |
|---|------------------------------|-----------------------------|--------|
| 1 | Build & docs alignment       | DevOps                      | ✅ Done (Gradle 9.0.0 ✅; DEPLOYMENT.md ✅) |
| 2 | Offline data persistence     | Back-end                    | ✅ Done |
| 3 | Location jump detection      | Back-end                    | ✅ Done |
| 4 | Trip history → details       | Front-end                   | Deferred to UI/UX backlog |
| 5 | Test health (fix or document)| QA                          | In progress (see FAILING_OR_IGNORED_TESTS) |
| 6 | ROADMAP + FEATURE_BRIEF(s)   | Design / Creative Manager   | ✅ Done |
| 7 | Security and secrets review  | Security Specialist         | In progress (SECURITY_NOTES.md ✅) |
| 8 | Cross-link improvement list  | File Organizer              | ✅ Done |
| 9 | Statistics: monthly only     | UI/UX, Front-end, Back-end, QA | High (deferred until UI approved) |
| — | **Hub & Loop system**        | All agents / Loop Master      | 50 TODOs in [HUB_AND_LOOP_FUTURE_TODOS.md](agents/HUB_AND_LOOP_FUTURE_TODOS.md) (Hub, Loop Master, Universal Loop Prompt, sandboxing, full axis). 48 in [CYBER_SECURITY_AND_HUB_2026-03-11_TODOS.md](agents/CYBER_SECURITY_AND_HUB_2026-03-11_TODOS.md) (today's build follow-ups) if present. |

---

*Next step: User reviews this list via email and replies with priorities, approvals, or changes. Coordinator will update this doc and assign work accordingly.*

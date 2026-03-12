# Tasks index — single entry point for all TODOs and tasks

**Purpose:** One place to find every kind of task. No duplicate lists; this index points to the authoritative docs and lists only **in-code TODOs** that are not already in CRUCIAL or worker lists.

**Last updated:** 2025-03-11

---

## Where tasks live

| Source | Purpose | Link |
|--------|---------|------|
| **Crucial improvements** | Prioritized high-impact list; role-owned; summary table with status | [docs/CRUCIAL_IMPROVEMENTS_TODO.md](CRUCIAL_IMPROVEMENTS_TODO.md) |
| **Worker todos + one idea each** | Per-role task lists and backlog ideas | [docs/agents/WORKER_TODOS_AND_IDEAS.md](agents/WORKER_TODOS_AND_IDEAS.md) |
| **Comprehensive agent todos** | Single checklist per agent (CRUCIAL + worker + monthly stats); check off here and in source | [docs/agents/COMPREHENSIVE_AGENT_TODOS.md](agents/COMPREHENSIVE_AGENT_TODOS.md) |
| **Failing/ignored tests** | QA: which tests are ignored or failing, reason, owner | [docs/qa/FAILING_OR_IGNORED_TESTS.md](qa/FAILING_OR_IGNORED_TESTS.md) |
| **Product roadmap** | What's next; feature briefs | [docs/product/ROADMAP.md](product/ROADMAP.md) |

---

## CRUCIAL summary (quick reference)

| # | Area | Status |
|---|------|--------|
| 1 | Build & docs alignment (Gradle 9; DEPLOYMENT) | In progress |
| 2 | Offline data persistence | Done |
| 3 | Location jump detection | Done |
| 4 | Trip history → details | Deferred to UI/UX backlog |
| 5 | Test health | In progress (see FAILING_OR_IGNORED_TESTS) |
| 6 | ROADMAP + FEATURE_BRIEF(s) | Done |
| 7 | Security and secrets review | In progress (SECURITY_NOTES.md exists) |
| 8 | Cross-link improvement list | Done |
| 9 | Statistics: monthly only | Deferred until UI approved |

---

## In-code TODOs (not in CRUCIAL or worker lists)

These are the only **code comment TODOs** that are not already covered by CRUCIAL or worker docs. When you address one, remove the comment and optionally add to CRUCIAL or a feature brief.

| File | Line (approx) | TODO |
|------|----------------|------|
| `app/.../services/OfflineSyncService.kt` | ~326 | Update trip with merged data when merge path is wired |
| `app/.../services/StandaloneOfflineService.kt` | ~86 | Migrate to Android Keystore + EncryptedSharedPreferences (or DataStore+Tink); see SECURITY_NOTES |
| `app/.../services/LocationValidationServiceTest.kt` | ~15, ~24 | Future: instrumented tests for location validation; tests for remaining heavy-traffic steps (see docs/qa/FAILING_OR_IGNORED_TESTS.md) |
| `app/.../services/TrafficAnalyticsTest.kt` | ~5 | Add tests when TrafficAnalytics or traffic pattern analytics are implemented |

---

## Test-related TODOs (documented in QA)

- **LocationValidationServiceTest:** One test @Ignore; heavy-traffic steps — see [FAILING_OR_IGNORED_TESTS.md](qa/FAILING_OR_IGNORED_TESTS.md).
- **ThemeScreenshotTest:** Deferred until Paparazzi configured — see [TEST_STRATEGY.md](qa/TEST_STRATEGY.md).

---

## Duplicates removed / cleaned

- **CRUCIAL #2, #3, #6, #8** marked Done in both CRUCIAL_IMPROVEMENTS_TODO and COMPREHENSIVE_AGENT_TODOS.
- **CRUCIAL #5:** TripInputViewModelIntegrationTest and TripHistoryByDateViewModelTest marked resolved in COMPREHENSIVE_AGENT_TODOS; status in CRUCIAL points to FAILING_OR_IGNORED_TESTS.
- **OFFLINE_PERSISTENCE.md:** Removed outdated "When implementing, remove the TODOs..." line (implementation complete).
- **Section 5 (Test health)** in CRUCIAL now references FAILING_OR_IGNORED_TESTS as single source for test status.

---

*When you complete a task, check it off in COMPREHENSIVE_AGENT_TODOS (and in CRUCIAL or WORKER_TODOS if it came from there). Remove or update in-code TODOs when implemented.*

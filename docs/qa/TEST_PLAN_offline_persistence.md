# Test plan — Offline persistence

**Feature:** OfflineDataManager load/save persistence  
**Brief:** [docs/technical/OFFLINE_PERSISTENCE.md](../technical/OFFLINE_PERSISTENCE.md)  
**Owner (tests):** QA Engineer

---

## Scenarios (what to test)

| # | Scenario | Type | Priority | How to run |
|---|----------|------|----------|------------|
| 1 | Load/save round-trip: save trip, create new OfflineDataManager, verify trip loaded | unit (Robolectric) | P0 | `./gradlew testDebugUnitTest --tests "*OfflineDataManagerPersistenceTest*"` |
| 2 | Sync status survives restart | unit (Robolectric) | P1 | Same |
| 3 | Empty storage loads as empty | unit (Robolectric) | P1 | Same |
| 4 | clearAllOfflineData persists (no data after restart) | unit (Robolectric) | P1 | Same |
| 5 | Survival across process death | instrumented | P2 | `./gradlew connectedDebugAndroidTest` (add test when needed) |

---

## Acceptance criteria (from brief)

- [x] Trips saved offline survive app restart
- [x] Pending/failed sync counts and sync status survive restart
- [x] No data loss on normal process death
- [x] Respect Known Truths (offline queue only; no bypass of Clear semantics)

---

## How to run

- **Unit:** `./gradlew testDebugUnitTest --tests "com.example.outofroutebuddy.data.OfflineDataManagerPersistenceTest"`
- **Instrumented:** `./gradlew connectedDebugAndroidTest` (when scenario 5 is added)

---

## Handoff on failure

When a test fails due to a bug in production code: hand off to **Back-end** with reproduction steps, expected vs actual, and environment.

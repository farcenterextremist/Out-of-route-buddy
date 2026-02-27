# SSOT test scenarios (Known Truths)

**Purpose:** Tests that explicitly verify behavior described in `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`. Add or extend these to prevent regressions when someone changes persistence, recovery, or calendar logic.

**Owner:** QA Engineer. Implementation of tests in `app/src/test/` and `app/src/androidTest/`.

---

## Scenarios to cover (Board-adopted)

| Truth | What to verify | Suggested test location |
|-------|----------------|-------------------------|
| **Only End trip writes to Room** | Clear trip never inserts a row; End trip does. | Unit: ViewModel or repository test that asserts no insert on clear, insert on end. |
| **Recovery precedence** | (1) Application.recoveredTripState (2) TripPersistenceManager (3) inactive. | Unit or integration: loadInitialData or recovery path; mock recovery vs persistence and assert which wins. |
| **TripTrackingService is source for live miles** | ViewModel observes TripTrackingService for live miles; no other source. | Unit: mock TripTrackingService and assert ViewModel emits its values. |
| **Monthly stats from Room only** | getMonthlyTripStatistics() for current month; no separate store. | Unit: repository or use-case test; assert stats come from TripDao/getMonthlyTripStatistics. |
| **Calendar "days with trips" from Room** | getTripsByDateRange(); ViewModel builds datesWithTripsInPeriod. | Unit: assert date list is derived from trip startTime in given range. |

---

## Existing tests to extend

- **SsotKnownTruthsTest:** Dedicated SSOT verification tests for Clear never inserts, End trip inserts. See `app/src/test/.../ssot/SsotKnownTruthsTest.kt`.
- **TripStatisticsWiringTest:** `clearTrip does not insert trip into repository`, `endTrip saves trip to repository`.
- **Recovery / loadInitialData:** TripPersistenceRecoveryTest, TripRecoveryDialogRobolectricTest verify persistence and recovery flows.
- **Repository / TripDao:** Assert monthly stats and date-range queries are used as single source.

---

## When adding tests

- Follow existing patterns in `app/src/test/` (e.g. `runBlocking`, test fixtures).
- One focused test per truth where possible; avoid giant catch-all tests.
- Document any ignored or flaky tests in `docs/qa/FAILING_OR_IGNORED_TESTS.md`.

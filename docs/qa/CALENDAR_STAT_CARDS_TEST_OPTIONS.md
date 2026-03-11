# Test options: Calendar yellow circle, Stat cards, Monthly/yearly stats

**Purpose:** Explore simulation and instrumented test options to verify the three plan tasks (calendar days with yellow circle, stat card wiring, monthly/yearly statistics).  
**Related:** [calendar_stats_wiring_plan](../../.cursor/plans/calendar_stats_wiring_plan_a2091fba.plan.md), [TEST_STRATEGY.md](./TEST_STRATEGY.md).

---

## Current coverage (already in place)

| Task | Existing tests | Gap |
|------|----------------|-----|
| **1. Calendar yellow circle** | `TripInputViewModelIntegrationTest`: getDatesWithTripsForCalendarRange (distinct days, **midnight-spanning both days**, **empty when no trips**, **filter to requested range only**, **empty when repository throws**). | — |
| **2. Stat card wiring** | `TripHistoryByDateViewModelTest`: loadTripsForDate (filters by date, empty results, **midnight-spanning trip included**, **empty when repository throws**). `TripHistoryByDateDialogTest` (dialog creation). `StatCardCalendarWiringSimulationTest` (period → stats + datesWithTripsInPeriod). | No test that dialog UI shows stat cards (would need Robolectric/instrumented). |
| **3. Monthly/yearly stats** | `TripStatisticsWiringTest`; `StatCardCalendarWiringSimulationTest`: period selection → **both period and year stats set**, **datesWithTripsInPeriod includes both days for midnight-spanning trip**, end trip → **period + year stats refreshed**. | — |

---

## Option A: Simulation tests (unit / JVM, no device)

**Where:** `app/src/test/`  
**Run:** `./gradlew :app:testDebugUnitTest`  
**Pros:** Fast, no emulator, CI-friendly.  
**Cons:** No real UI or real DB; mocks only.

### Task 1 – Calendar yellow circle (midnight-spanning)

- **Add in:** `TripInputViewModelIntegrationTest` or a dedicated `CalendarDatesWithTripsSimulationTest`.
- **Scenario:** Mock `getTripsByDateRange(expandedMin, expandedMax)` to return **one** trip with `startTime = Dec 14 23:00`, `endTime = Dec 15 01:00`. Call `getDatesWithTripsForCalendarRange(minDate, maxDate)` with range including Dec 14–15.
- **Assert:** `result.size == 2` and result contains both Dec 14 start-of-day and Dec 15 start-of-day (by millis or normalized date).
- **Effort:** Low. Reuses existing ViewModel + mock pattern; `computeDatesWithTrips` is already used inside `getDatesWithTripsForCalendarRange`.

### Task 2 – Stat card wiring

- **Option A1 – ViewModel only (already done):** `TripHistoryByDateViewModelTest.loadTripsForDate includes midnight-spanning trip when selected day is within range` already verifies repository overlap → ViewModel exposes trip for a date. No new test required for "adapter receives trips" at ViewModel level.
- **Option A2 – Adapter / list content:** Add a small test (e.g. in `TripHistoryStatCardAdapterTest` or a simulation test) that builds `TripHistoryStatCardAdapter` with a list of 1–2 trips (including midnight-spanning), and asserts item count and that key fields (e.g. trip date text, miles) are set on the views (requires Robolectric or a simple instrumented test to inflate the item layout). Prefer **Robolectric** in `app/src/test` so it stays in unit test suite.
- **Option A3 – Dialog + ViewModel (Robolectric):** In `TripHistoryByDateDialogTest`, use `FragmentScenario` (Robolectric) to launch `TripHistoryByDateDialog` with a fixed date, inject a test `TripRepository` (or use Hilt test module) that returns a known list of trips, then assert the RecyclerView has the expected item count and optionally that a stat card shows expected text. Requires Hilt test setup and possibly replacing the ViewModel's repository in test.

### Task 3 – Monthly/yearly stats

- **Add in:** `StatCardCalendarWiringSimulationTest` or `TripStatisticsWiringTest`.
- **Scenario:** After `refreshStatisticsAfterSave` (or after period selection + end trip), assert `state.periodStatistics != null && state.yearStatistics != null` and that `mapPeriodToSummary(state.periodStatistics)` and `mapPeriodToSummary(state.yearStatistics)` return non-null with consistent semantics (e.g. year stats for Jan–Dec of the same year as period).
- **Optional:** One test that explicitly sets repository to return different totals for period vs year and asserts the two summary values differ (proves two rows are fed from two sources).

---

## Option B: Robolectric tests (JVM with Android SDK, no device)

**Where:** `app/src/test/`  
**Run:** `./gradlew :app:testDebugUnitTest`  
**Pros:** Can launch fragments/dialogs and use Android views; still fast and CI-friendly.  
**Cons:** Some API differences from real device; Hilt + FragmentScenario setup needed for dialogs.

### Task 1 – Calendar yellow circle

- **Limited value:** The yellow circle is drawn by `DaysWithTripsDecorator` inside `MaterialCalendarView`. A Robolectric test could launch `CustomCalendarDialog`, pass `datesWithTrips` with two dates (e.g. Dec 14 and Dec 15), and assert that the decorator is present or that the calendar has the expected number of decorators; it would **not** easily assert the actual drawn circle. Prefer **simulation test** (Option A) for "both days in list"; use manual or instrumented test for visual.

### Task 2 – Stat card wiring

- **Dialog + RecyclerView:** Use `FragmentScenario.launchInContainer(TripHistoryByDateDialog::class.java, args, R.style.Theme_...)` with a test date. With a **test module** that provides a `TripRepository` returning a fixed list of trips, the dialog's ViewModel will load that list and the adapter will bind it. Assert:
  - RecyclerView child count = trip count.
  - At least one child has text matching expected miles or date (e.g. `onView(withId(R.id.trip_miles)).check(matches(withText(containsString("125.5"))))`).
- **CustomCalendarDialog → TripHistoryByDateDialog:** Harder: would need to trigger date click and then find the history dialog. Possible but more brittle; better as instrumented E2E or manual.

### Task 3 – Monthly/yearly stats

- **TripInputFragment stats rows:** Launch `TripInputFragment` (e.g. via MainActivity or directly with a test NavController), then either:
  - Set ViewModel state (if test can replace ViewModel or state) so `periodStatistics` and `yearStatistics` are non-null, then assert `monthlyStats` and `yearlyStats` views (e.g. `R.id.total_miles` inside each row) are visible and show non-placeholder text, or
  - Rely on simulation tests (Option A) for "both stats set together" and use Robolectric only for "stats section visible when expanded."
- **Effort:** Medium (fragment launch, possibly Hilt test module for ViewModel/Repository).

---

## Option C: Instrumented tests (device/emulator)

**Where:** `app/src/androidTest/`  
**Run:** `./gradlew :app:connectedDebugAndroidTest` (or CI when configured).  
**Pros:** Real UI, real (or in-memory) DB; highest confidence.  
**Cons:** Slower; needs device/emulator; flakier (timing, permissions).

### Task 1 – Calendar yellow circle

- **Scenario:** With an in-memory or test DB that has **one midnight-spanning trip** (e.g. Feb 28 23:00 → Mar 1 01:00), launch app, open calendar (tap Statistics → calendar button or equivalent), then:
  - **Option C1:** Assert that the calendar view is displayed and that two specific day views (Feb 28 and Mar 1) are present and have a "selected" or "decorated" state (if the calendar exposes accessibility or test tags). Many calendar widgets do **not** expose per-day decorator state to Espresso; then the test may be limited to "calendar opens and shows the month."
  - **Option C2:** Rely on **screenshot or visual regression** (e.g. Paparazzi / screenshot test) to detect if the yellow circle is present on the right days. Not yet in project per TEST_STRATEGY.
  - **Recommendation:** Use **simulation test** (Option A) for correctness of "both days in list"; treat "yellow circle visible on device" as **manual** or a future screenshot test.

### Task 2 – Stat card wiring

- **Scenario:** Seed device DB (or use test build with in-memory DB) with 1–2 trips for a known date. Launch app, open calendar, tap that date. When `TripHistoryByDateDialog` opens:
  - Assert dialog is visible (e.g. `onView(withText(containsString("Trips for"))).check(matches(isDisplayed()))`).
  - Assert RecyclerView has expected number of items (e.g. `onView(withId(R.id.trip_list_recycler_view)).check(matches(hasChildCount(2)))`).
  - Optionally assert a stat card shows expected miles or OOR text.
- **Requires:** Test database seeding (e.g. Room in-memory + insert trips, or a test API that injects data). Hilt `@TestInstallIn` or build flavor for test DB can provide a test `TripRepository` that reads from seeded data.

### Task 3 – Monthly/yearly stats

- **Scenario:** Seed DB with trips in current month and in current year. Launch app, expand statistics section (tap Statistics). Assert:
  - `monthlyStats` row (or first stats row) shows non-placeholder text (e.g. total miles > 0).
  - `yearlyStats` row (or second stats row) shows non-placeholder text.
  - Optionally assert specific values if test controls exact trip data.
- **Requires:** Same test DB / seeding as Task 2; and stable way to expand the stats section (e.g. `onView(withId(R.id.statistics_button)).perform(click())` then check for `monthlyStats` / `yearlyStats` views).

---

## Recommended combination

| Task | Preferred addition | Alternative |
|------|--------------------|-------------|
| **1. Yellow circle** | **Simulation:** One test in `TripInputViewModelIntegrationTest` (or `CalendarDatesWithTripsSimulationTest`) that a midnight-spanning trip yields **two** dates from `getDatesWithTripsForCalendarRange`. | Instrumented: "calendar opens and shows month" only; visual check manual or later screenshot. |
| **2. Stat cards** | **Simulation:** Already covered by `TripHistoryByDateViewModelTest` (overlap + midnight-spanning). **Optional:** Robolectric `TripHistoryByDateDialogTest` that launches dialog with test repo and asserts RecyclerView item count + one stat card text. | Instrumented: seed DB, open calendar, tap day, assert dialog and list count + one card text. |
| **3. Monthly/yearly** | **Simulation:** One test in `StatCardCalendarWiringSimulationTest` or `TripStatisticsWiringTest` that after `refreshStatisticsAfterSave` (or period selection) both `periodStatistics` and `yearStatistics` are non-null and `mapPeriodToSummary` returns non-null for both. Optional: assert period vs year totals differ when repo returns different data. | Instrumented: seed DB, expand stats, assert both stats rows show non-placeholder. |

**Summary:** Add **2–3 simulation tests** (midnight-spanning calendar dates; both period/year stats set together; optionally dialog stat card list content with Robolectric). Add **1–2 instrumented tests** only if you want E2E: "calendar → tap day → history dialog shows N cards" and "stats section shows monthly + yearly rows" with seeded data.

---

## Implementation checklist (if implementing)

- [x] **Simulation – getDatesWithTripsForCalendarRange midnight-spanning:** *(TripInputViewModelIntegrationTest.getDatesWithTripsForCalendarRange_returnsBothDaysForMidnightSpanningTrip.)*
- [x] **Simulation – period + year stats both set:** *(StatCardCalendarWiringSimulationTest.`SIMULATION - Period selection sets both period and year statistics for stats rows`.)*
- [x] **Simulation – getDatesWithTripsForCalendarRange empty when no trips:** *(getDatesWithTripsForCalendarRange_returnsEmptyWhenNoTrips.)*
- [x] **Simulation – getDatesWithTripsForCalendarRange filters to requested range:** *(getDatesWithTripsForCalendarRange_filtersToRequestedRangeOnly.)*
- [x] **Simulation – getDatesWithTripsForCalendarRange when repository throws:** *(getDatesWithTripsForCalendarRange_returnsEmptyWhenRepositoryThrows.)*
- [x] **Simulation – datesWithTripsInPeriod includes both days for midnight-spanning trip:** *(StatCardCalendarWiringSimulationTest.`SIMULATION - datesWithTripsInPeriod includes both days for midnight-spanning trip in period`.)*
- [x] **Simulation – end trip sets both period and year statistics:** *(assertion added in `SIMULATION - End trip refreshes period stats and calendar when period includes today`.)*
- [x] **Simulation – loadTripsForDate when repository throws:** *(TripHistoryByDateViewModelTest.`loadTripsForDate when repository throws emits empty list`.)*
- [ ] **Robolectric (optional) – TripHistoryByDateDialog with trips:** FragmentScenario + test Repository returning 1 trip; assert RecyclerView item count and one stat card text (e.g. miles).
- [ ] **Instrumented (optional) – Calendar → history dialog:** Seed DB with 1 trip for a date; open calendar; tap that date; assert dialog title and RecyclerView has 1 item.
- [ ] **Instrumented (optional) – Stats rows:** Seed DB with trips; open stats; assert monthly and yearly rows visible and non-placeholder.

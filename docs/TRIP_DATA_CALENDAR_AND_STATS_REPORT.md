# Trip Data → Calendar & Monthly Statistics: Brainstorm + Test Report

**Date:** 2026-02-21  
**Scope:** Ensuring saved trip data is sent to the calendar and monthly statistics; tests added; full suite run.

---

## 1. Brainstorm Summary (Agent Output)

### 1.1 Trip data sources

- **Room:** Primary for saved trips. `TripEntity`, `TripDao` (e.g. `getTripsForDateRange`, `getTripsForDate`), `AppDatabase`.
- **Repository:** `TripRepository.insertTrip()` writes one trip; `getTripsByDateRange(start, end)` and `getTripStatistics(start, end)` / `getMonthlyTripStatistics()` read.
- **Domain adapter:** `DomainTripRepositoryAdapter` implements domain interface; `getMonthlyTripStatistics()` uses **current calendar month** (1st–last day).
- **SharedPreferences:** `TripPersistenceManager` for **active** trip recovery only; not used for calendar or monthly stats.

### 1.2 Calendar data flow

- **Producer:** `TripInputViewModel.updatePeriodStatistics()` builds `datesWithTripsInPeriod` from `tripRepository.getTripsByDateRange(startDate, endDate).first()`, normalizing each trip’s `startTime` to start-of-day and deduplicating.
- **Consumer:** `TripInputFragment.showCalendarPicker()` passes `viewModel.uiState.value.datesWithTripsInPeriod` into `CustomCalendarDialog.newInstance(..., datesWithTrips = datesWithTrips)`. Dialog uses `DaysWithTripsDecorator` to show dots on those days.
- **Why calendar might not show saved trips:** (1) Period scope – only selected period’s dates are in the list; (2) timing – dialog is a snapshot at open time; (3) `updatePeriodStatistics` not run after save or failing; (4) date normalization/timezone mismatch; (5) trip not actually saved (e.g. insert failed).

### 1.3 Monthly statistics flow

- **Source:** `TripInputViewModel.refreshAggregateStatistics()` calls `tripRepository.getMonthlyTripStatistics()` and maps to `SummaryStatistics` → `uiState.monthlyStatistics`.
- **Implementation:** `DomainTripRepositoryAdapter.getMonthlyTripStatistics()` uses **current calendar month** (1st 00:00:00 to last moment). **Custom period is not used for “monthly” stats** – only for selected period and calendar.
- **Why monthly stats can be empty/stale:** (1) Only current month is queried; (2) `refreshAggregateStatistics()` not called after save or failing; (3) insert/read ordering (new row not visible yet); (4) exceptions only logged, state not updated.

### 1.4 Improvement ideas (from brainstorm)

**Calendar:**

- Use Room as single source of truth (already the case).
- Keep calling `refreshSelectedPeriod()` after `insertTrip` in `endTrip()` (and after clear).
- Optionally refresh when returning to statistics (e.g. `onResume`).
- Either have calendar read a Flow of “dates with trips” or document snapshot-at-open and hint to reopen after ending a trip.
- One shared “start of day” normalization for ViewModel, history, and DAO range queries.
- **Tests:** Integration test that insert + `updatePeriodStatistics` yields correct `datesWithTripsInPeriod`; UI/behavior test that after end trip, state/dialog includes new date.

**Monthly statistics:**

- Keep single source of truth (Room).
- Ensure `refreshAggregateStatistics()` after every write path (`endTrip`, `clearTrip`); optionally when statistics section becomes visible.
- After `insertTrip`, ensure refresh runs after insert is visible (same dispatcher / suspend ordering).
- Optionally support “monthly” by Custom period via `getMonthlyTripStatistics(periodMode)`.
- **Tests:** Unit test that repository with one trip in current month returns stats including it; test that after `endTrip()`, `monthlyStatistics` reflects the new trip.

**Cross-cutting:** Single timezone for “today”/start of month/start of day; guard test: insert trip then immediately query range and monthly stats and assert trip is included.

---

## 2. Tests added (this session)

**File:** `app/src/test/java/com/example/outofroutebuddy/presentation/viewmodel/TripStatisticsWiringTest.kt`

1. **`datesWithTripsInPeriod reflects saved trips from repository for selected period`**  
   - Repository returns two trips with distinct `startTime` dates in range.  
   - Trigger: `onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)`.  
   - Asserts: `datesWithTripsInPeriod.size == 2` and contains the two normalized start-of-day dates.

2. **`datesWithTripsInPeriod is empty when repository returns no trips for period`**  
   - `getTripsByDateRange` returns empty list.  
   - Asserts: `datesWithTripsInPeriod.isEmpty()`.

3. **`datesWithTripsInPeriod deduplicates multiple trips on same day`**  
   - Repository returns two trips same calendar day (different times).  
   - Asserts: `datesWithTripsInPeriod.size == 1`.

4. **`updatePeriodStatistics sets periodStatistics and datesWithTripsInPeriod from repository`**  
   - Repository returns one trip and matching `TripStatistics`.  
   - Asserts: `periodStatistics` (totalTrips, totalDistance, averageOorPercentage) and `datesWithTripsInPeriod.size == 1`.

These close the gap identified by the explore agent: **no previous test asserted that repository-returned (saved) trips drive `datesWithTripsInPeriod` or period statistics.**

---

## 3. Full test suite run

- **Unit tests:** `.\gradlew.bat :app:testDebugUnitTest` was run. **Execution failed** with:  
  `Unable to delete directory ... test-results\testDebugUnitTest\binary` (file lock, likely from a previous test run or IDE).  
- **Recommendation:** Close other Gradle/IDE processes using the project, then re-run:
  - Unit: `.\gradlew.bat :app:testDebugUnitTest`
  - Instrumented (device/emulator): `.\gradlew.bat :app:connectedDebugAndroidTest`
  - Both: `.\gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest`

---

## 4. Email / notification note

Automated email sending is not available in this environment. Use this report as the deliverable: you can forward **this file** or copy the sections above into an email after re-running the test suite locally.

---

## 5. Quick reference (files)

| Area              | Files |
|-------------------|--------|
| Trip persistence  | `data/entities/TripEntity.kt`, `data/dao/TripDao.kt`, `data/repository/TripRepository.kt`, `DomainTripRepositoryAdapter.kt` |
| Calendar flow     | `TripInputViewModel.kt` (updatePeriodStatistics, datesWithTripsInPeriod), `TripInputFragment.kt` (showCalendarPicker), `CustomCalendarDialog.kt` |
| Monthly stats     | `TripInputViewModel.kt` (refreshAggregateStatistics, monthlyStatistics), `DomainTripRepositoryAdapter.getMonthlyTripStatistics()` |
| New tests         | `presentation/viewmodel/TripStatisticsWiringTest.kt` (4 new tests) |

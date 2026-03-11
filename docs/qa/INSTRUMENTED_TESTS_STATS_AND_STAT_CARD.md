# Instrumented Tests: Monthly/Yearly Statistics and Stat Card (Calendar Trips)

**Purpose:** Design and implement instrumented tests that populate the database with trips, then verify that (1) **monthly and yearly statistics** are shown correctly on the trip input screen, and (2) the **Stat Card / Trip Card** list in the calendar flow shows the expected trips for a selected date.

**Related:** [FEATURE_BRIEF_stat_cards_calendar_history.md](../product/FEATURE_BRIEF_stat_cards_calendar_history.md), [COVERAGE_SCORE_AND_CRITICAL_AREAS.md](./COVERAGE_SCORE_AND_CRITICAL_AREAS.md), [TEST_STRATEGY.md](./TEST_STRATEGY.md).

---

## 1. Scope

| Area | What is tested | Where it appears |
|------|----------------|-------------------|
| **Monthly statistics** | Total miles, OOR miles, OOR % for the selected period (current month in STANDARD mode) | TripInputFragment: `binding.monthlyStats` row |
| **Yearly statistics** | Total miles, OOR miles, OOR % for Jan 1–Dec 31 of the period's year | TripInputFragment: `binding.yearlyStats` row |
| **Days with trips (chips)** | List of dates in the current period that have at least one trip; chips are clickable | TripInputFragment: `daysWithTripsContainer` |
| **Stat Card / Trip Card** | List of trips for a date when user clicks a day in the calendar | TripHistoryByDateDialog: RecyclerView with TripHistoryStatCardAdapter |

---

## 2. Data flow (brief)

- **Stats source:** TripInputViewModel holds `periodStatistics`, `yearStatistics`, and `datesWithTripsInPeriod`. These are refreshed by `refreshStatisticsAfterSave()` and `refreshPeriodForCalendar()`, which call the domain TripRepository: `getTripStatistics(start, end)`, `getMonthlyTripStatistics()`, and the same repository for trips in range to compute `datesWithTripsInPeriod`.
- **Repository:** DomainTripRepositoryAdapter uses the data-layer TripRepository (TripDao). Monthly = current month start/end; yearly = Jan 1–Dec 31 of current year.
- **Calendar:** CustomCalendarDialog receives `datesWithTripsInPeriod` (or extended range via `getDatesWithTripsForCalendarRange`). Clicking a date opens TripHistoryByDateDialog, which loads trips via `getTripsOverlappingDay(startOfDay, endOfDay)`.
- **Stat Card:** TripHistoryStatCardAdapter shows each trip with date, time range, miles, OOR; expandable metadata.

---

## 3. Test data setup

- Use the **production database** from the instrumented process: `(ApplicationProvider.getApplicationContext() as OutOfRouteApplication).database.tripDao()`.
- Insert **TripEntity** rows directly so that:
  - At least one trip falls in the **current month** (and thus in current year) so monthly and yearly stats are non-zero.
  - At least one trip is associated with a **specific calendar day** (set `date`, and optionally `tripStartTime`/`tripEndTime` for that day) so that the calendar shows a day with trips and the by-date dialog shows that trip.
- **Cleanup:** Either clear trips in `@After` or use a dedicated test that does not depend on an empty DB (e.g. assert deltas or known totals). Prefer clearing in `@After` so tests are isolated.

**Example entity (current month, today):**

- `date` = start of today (or any day in current month).
- `loadedMiles`, `bounceMiles`, `actualMiles`, `oorMiles`, `oorPercentage` = known values (e.g. 100, 10, 110, 0, 0 or 5.0, 4.5).
- `tripStartTime` / `tripEndTime` = same day (or null; fallback to `date` for overlap).

---

## 4. Scenarios

### 4.1 Monthly and yearly statistics

1. **Setup:** Clear trips (optional). Insert one or more TripEntity(s) with dates in the current month and known totals (e.g. one trip: 100 loaded, 10 bounce, 110 actual → total miles 110, OOR 0 if actual = dispatched).
2. **Run:** Launch MainActivity; dismiss startup/recovery dialog if present.
3. **Trigger load:** TripInputFragment is the default; ViewModel loads period/year stats on init. If the period is STANDARD, current month is selected; stats should update. Optionally expand the statistics section if it is collapsed (`statistics_button`).
4. **Assert:**  
   - Monthly row: total miles, OOR miles, OOR % match expected (format: `formatStatisticMiles` = `"%1$,.1f"`, `formatStatisticPercentage` = `"%.1f%%"`).  
   - Yearly row: same for the full year (if only this month has data, yearly can equal monthly).

**View IDs (from binding):** The statistics section uses `monthly_stats` and `yearly_stats` (includes for `statistics_row`). The row layout has `total_miles`, `oor_miles`, `oor_percentage` (or equivalent). Prefer asserting text within the correct row to avoid matching the wrong row.

### 4.2 Days-with-trips chips (calendar section on trip input)

1. **Setup:** Same as 4.1; ensure at least one trip in the current period.
2. **Run:** Launch MainActivity; dismiss dialog.
3. **Assert:** The "days with trips" section is visible; `daysWithTripsContainer` has at least one chip; chip text matches the expected date format (e.g. "MMM d").

### 4.3 Stat Card / Trip Card (calendar → date → list)

1. **Setup:** Insert one or more TripEntity(s) for a **specific date** (e.g. today), with `tripStartTime`/`tripEndTime` set so that `getTripsOverlappingDay` returns them.
2. **Run:** Launch MainActivity; dismiss dialog.
3. **Open calendar:** Expand statistics if needed; click `statistics_calendar_button` to open CustomCalendarDialog (after `refreshPeriodForCalendar()`).
4. **Select date:** In the calendar, click the day that has the inserted trip(s).
5. **Assert:** TripHistoryByDateDialog is shown; trip count text (e.g. "1 trip") and/or RecyclerView item count matches; at least one Stat Card shows expected content (e.g. miles text like "110.0 mi", or OOR text).

**Ids in dialog:** `trip_count_text`, `trip_list_recycler_view`, `empty_state_text`. Stat card item: `item_trip_history_stat_card` with `trip_miles`, `trip_oor`, `trip_date`.

---

## 5. Implementation notes

- **Threading:** Use `runBlocking` (or the test runner’s equivalent) to insert/delete trips on the main thread or a test dispatcher; ensure DB is written before launching the activity or performing actions.
- **Timing:** After launching the activity and after opening the calendar, use `IdlingResource`/`Espresso.onIdle()` or short `Thread.sleep` only if necessary; prefer waiting for specific view state (e.g. view visible with expected text).
- **Permissions:** Grant location (and any other required) permissions via `GrantPermissionRule` so the app does not show permission dialogs that block the test.
- **Dialogs:** Dismiss startup/recovery dialog at the start of each test (reuse the pattern from MainActivityBasicTest / MainActivityDeviceSmokeTest).
- **Formatting:** Statistics use `Locale.US` in the app (e.g. `String.format(Locale.US, "%1$,.1f", value)`). Use the same locale when building expected strings if comparing exact text.

---

## 6. Files to add/modify

| File | Action |
|------|--------|
| `docs/qa/INSTRUMENTED_TESTS_STATS_AND_STAT_CARD.md` | This design doc |
| `app/src/androidTest/.../StatisticsAndStatCardInstrumentedTest.kt` | New: instrumented test class for scenarios 4.1–4.3 |

---

## 7. Definition of done

- [x] Design doc (this file) reviewed and in repo.
- [ ] Instrumented test class runs on device/emulator.
- [x] At least one test populates DB and asserts monthly (and optionally yearly) statistics text.
- [x] At least one test populates DB, opens calendar, selects a date with trips, and asserts Stat Card / Trip Card list (count and/or content).
- [x] Tests clean up DB or are documented as non-isolated (e.g. depend on known state).
- [x] No unwarranted UI changes; only test code and this doc added.

**Note:** Run instrumented tests with:
`./gradlew :app:connectedDebugAndroidTest` (or `connectedDebugAndroidTest` on Windows).
Tests require a device or emulator; they clear the trip DB in @Before/@After and insert known data for assertions.

**Clean state so TripInputFragment shows first:**  
The test’s `@Before` clears crash-recovery SharedPreferences (`trip_crash_recovery`) and calls `OutOfRouteApplication.clearRecoveredState()`, so the recovery dialog should not appear. It also sets **period onboarding as already seen** in `OutOfRouteBuddyPreferences` (`has_seen_period_onboarding` = true) and `period_mode` = STANDARD, so the "Choose Your Tracking Period" dialog is skipped and the trip screen shows with current-month stats. If the dialog still appears, a fallback helper clicks Standard and Continue. If the device still shows a different first screen (e.g. onboarding or another destination), clear app data once before running tests:

```bash
adb shell pm clear com.example.outofroutebuddy
```

Then run `connectedDebugAndroidTest`. No need to uninstall; `pm clear` wipes data and the next launch starts fresh.

**Run in one step (clear + tests):**  
From the **repository root**, run:

- **PowerShell:** `.\scripts\run-stats-stat-card-instrumented-tests.ps1`
- **CMD / double-click:** `scripts\run-stats-stat-card-instrumented-tests.bat`

Each script runs `adb shell pm clear com.example.outofroutebuddy` then executes only the Statistics and Stat Card instrumented test class. Use this when you want a clean app state and the stats/stat-card tests in a single command.

**If Gradle reports "Cannot access output property 'resultsDir'" or "Failed to create MD5 hash for ... utp.0.log.lck":**  
This is a file-lock / incremental-build issue when collecting test results. Workaround:

1. Stop Gradle daemons: `.\gradlew.bat --stop` (or `./gradlew --stop`).
2. Delete test output dirs: remove `app\build\outputs\androidTest-results` and `app\build\outputs\connected_android_test_additional_output` (if present).
3. Run tests again **without** configuration cache:  
   `.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments=class=com.example.outofroutebuddy.StatisticsAndStatCardInstrumentedTest" --no-daemon --no-configuration-cache`

After that, the run should complete and report pass/fail correctly.

**Intricate and limit-testing scenarios (additional tests):**  
The suite includes tests that stress the UI with more complex flows and sporadic actions: aggregate stats for multiple trips (two days), rapid expand/collapse of the statistics section, open stat-card dialog → close → reopen same chip, rapid double-tap on a day chip, and sequential open/close/reopen with two chips present. These help ensure the statistics and calendar stat-card flow hold up under repeated or quick user actions.

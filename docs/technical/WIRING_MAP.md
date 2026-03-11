# Wiring map (one-page)

**Owner:** File Organizer / Back-end  
**Related:** `docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md`, `docs/technical/TRIP_PERSISTENCE_END_CLEAR.md`.

---

## UI → ViewModel → data

```
TripInputFragment (fragment_trip_input.xml)
    │
    ├─ bindings: loaded_miles_input, bounce_miles_input, start_trip_button, pause_button,
    │            statistics_button, statistics_calendar_button, settings (toolbar),
    │            total_miles_output, oor_*, selected_period_value, days_with_trips_container,
    │            monthly_stats (Month row), yearly_stats (Year row)
    │
    ├─ observe: viewModel.uiState → updateUI(state)
    │            viewModel.events  → handleEvent(event)
    │
    └─ actions: calculateTrip / endTrip / clearTrip / pauseTrip / resumeTrip
                showCalendarPicker → CustomCalendarDialog (onPeriodConfirmed → viewModel; onHistoryDateClicked → showTripHistoryForDate)
                showSettingsDialog → Help & Info, Period mode (PreferencesManager via viewModel)
                days-with-trips chips → showTripHistoryForDate(date) → TripHistoryByDateDialog

                    ▼
TripInputViewModel
    │
    ├─ state: uiState (periodStatistics, yearStatistics, monthlyStatistics, selectedPeriodLabel, datesWithTripsInPeriod, …)
    ├─ load: loadInitialData() → crash recovery OR TripPersistenceManager.loadSavedTripState()
    │        refreshAggregateStatistics() → tripRepository.getMonthlyTripStatistics()
    │        updatePeriodStatistics() → getTripStatistics(period) + getTripStatistics(year) + getTripsByDateRange → periodStatistics, yearStatistics, datesWithTripsInPeriod
    │
    ├─ trip lifecycle: calculateTrip → TripTrackingService.startService, saveTripStateForPersistence, startAutoSave
    │                  endTrip → insertTrip (Room), clearTripPersistence, refreshStatisticsAfterSave → periodStatistics, yearStatistics, datesWithTripsInPeriod
    │                  clearTrip → no insert, clearTripPersistence, refreshAggregateStatistics
    │
    ├─ observers: TripTrackingService.tripMetrics → actualMiles, saveTripStateForPersistence
    │             unifiedLocationService.realTimeGpsData; tripStateManager.tripState
    │
    └─ deps: TripRepository (domain), TripTrackingService, TripPersistenceManager, TripStateManager,
             PreferencesManager, UnifiedTripService, UnifiedLocationService, UnifiedOfflineService,
             TripCrashRecoveryManager
```

---

## Data layer

```
TripRepository (domain)  ←  DomainTripRepositoryAdapter  ←  TripRepository (data)  ←  TripDao  ←  Room (trips)
     │                              │                              │
     │  getMonthlyTripStatistics    │  getTripsByDateRange         │  insertTrip
     │  getTripStatistics           │  insertTrip                  │  getTripsForDateRange
     │  getTripsByDateRange         │  (map domain Trip ↔ data)    │
     ▼                              ▼                              ▼
TripInputViewModel, TripHistoryByDateViewModel, etc.          TripEntity (trips table)
```

---

## Services

```
TripTrackingService (FusedLocationProvider)
    → tripMetrics (StateFlow) → ViewModel.observeGpsTrackingData → actualMiles, saveTripStateForPersistence
    start/stop/pause/resume from ViewModel

UnifiedLocationService  → realTimeGpsData, getLocationStatistics (secondary)
UnifiedTripService      → getCurrentPeriodDates, calculateTrip (OOR)
UnifiedOfflineService   → saveDataWithOfflineFallback (ViewModel)

TripCrashRecoveryManager → 30s auto-save → SharedPreferences; OutOfRouteApplication.recoveredTripState → loadInitialData (first)
TripPersistenceManager   → saveActiveTripState, loadSavedTripState, clearSavedTripState → loadInitialData (second)
```

---

## Recovery precedence

```
App launch
    → loadInitialData()
        → 1. If OutOfRouteApplication.recoveredTripState active → restore, clearRecoveredState
        → 2. Else if TripPersistenceManager.loadSavedTripState() → restore, sync TripStateManager
        → 3. Else trip inactive
    → refreshAggregateStatistics(), initializeSelectedPeriodFromPreferences()
```

---

## Calendar & history

```
Statistics “View” (calendar button) → CustomCalendarDialog
    datesWithTripsInPeriod from viewModel.uiState → DaysWithTripsDecorator (blue dots)
    onPeriodConfirmed → viewModel.onCalendarPeriodSelected
    onHistoryDateClicked → showTripHistoryForDate(date)

showTripHistoryForDate(date) → TripHistoryByDateDialog
    TripHistoryByDateViewModel → repository.getTripsByDateRange(startOfDay, endOfDay) → RecyclerView
```

---

## End Trip → Monthly/Yearly Statistics flow

```
[User taps End Trip]
  → TripInputViewModel.endTrip()
  → tripRepository.insertTrip(tripData)   // trip date = end date (when user taps End Trip)
  → refreshStatisticsAfterSave(selectedPeriod)
      → getMonthlyTripStatistics()
      → if selectedPeriod != null:
          getTripStatistics(periodStart, periodEnd)   // Month row
          getTripStatistics(yearStart, yearEnd)       // Year row (Jan 1–Dec 31 of period's year)
          getTripsByDateRange → datesWithTripsInPeriod
        else:
          getCurrentPeriodDates → same flow for current period
  → _uiState.update(periodStatistics, yearStatistics, datesWithTripsInPeriod)
  → Fragment observes uiState → updateStatisticsRow(monthlyStats, yearlyStats)
  → Display updates
```

**Trip date for period grouping:** End date (when user taps End Trip). Stored via `DomainTripRepositoryAdapter` mapping `trip.startTime` to `TripEntity.date`.

**Future (trip cards):** Selectable boxes in calendar for a specific date; Month/Year stats aggregate from completed trips (same as today).

---

*For full detail see docs/agents/CURRENT_WIRING_PLAN.md and linked technical docs.*

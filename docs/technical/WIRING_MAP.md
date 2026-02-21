# Wiring map (one-page)

**Owner:** File Organizer / Back-end  
**Related:** `docs/agents/CURRENT_WIRING_PLAN.md`.

---

## UI → ViewModel → data

```
TripInputFragment (fragment_trip_input.xml)
    │
    ├─ bindings: loaded_miles_input, bounce_miles_input, start_trip_button, pause_button,
    │            statistics_button, statistics_calendar_button, settings (toolbar),
    │            total_miles_output, oor_*, selected_period_value, days_with_trips_container, monthly_stats
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
    ├─ state: uiState (monthlyStatistics, selectedPeriodLabel, datesWithTripsInPeriod, periodStatistics*, …)
    ├─ load: loadInitialData() → crash recovery OR TripPersistenceManager.loadSavedTripState()
    │        refreshAggregateStatistics() → tripRepository.getMonthlyTripStatistics()
    │        updatePeriodStatistics() → getTripStatistics + getTripsByDateRange → datesWithTripsInPeriod
    │
    ├─ trip lifecycle: calculateTrip → TripTrackingService.startService, saveTripStateForPersistence, startAutoSave
    │                  endTrip → insertTrip (Room), clearTripPersistence, refreshAggregateStatistics
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

*For full detail see docs/agents/CURRENT_WIRING_PLAN.md and linked technical docs.*

# Quality and Robustness Plan

**Scope:** Backend, data, domain, services, ViewModels, and wiring to UI only. No frontend layout or styling changes unless required for wiring (e.g. showing errors to the user).

This plan was produced from multi-agent exploration of the codebase (backend quality, test coverage, architecture and single source of truth).

---

## 1. Critical production bugs

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| C1 | **getTripsByStatus never emits** | `DomainTripRepositoryAdapter.getTripsByStatus()` | `getAllTrips()` is an infinite Flow; `collect` never completes so `emit(domainTrips)` is never reached. Callers hang. Use `.first()` (or one-shot API) to get one snapshot, then emit from the flow. |
| C2 | **Loading never set to false** | `TripHistoryViewModel.loadTrips()` | `repository.getAllTrips().collect { }` never completes, so `finally { _isLoading.value = false }` never runs. Set `_isLoading.value = false` on first emission inside `collect`. |
| C3 | **Error events not shown to user** | `TripInputFragment.handleEvent()` | `TripEvent.Error`, `CalculationError`, `SaveError` have empty branches; user never sees the message. **Wiring:** Call `showSnackbar(event.message)` (or equivalent) for these events. |
| C4 | **Delete failure not surfaced** | `TripHistoryViewModel` / `TripHistoryByDateViewModel` + repository | Domain `deleteTrip` returns `Unit`; data layer returns `Boolean`. Extend domain contract (e.g. return success/failure or throw) and **wire** UI to show Snackbar on delete failure. |

---

## 2. Concurrency and blocking

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| B1 | **runBlocking can block Main** | `UnifiedTripService.getTripStatistics()` | Uses `runBlocking { statisticsMutex.withLock { ... } }`. If called from Main, it blocks. Make the API suspend and call from a coroutine on a background dispatcher, or expose Flow/StateFlow. |
| B2 | **runBlocking in performance logger** | `PerformanceTracker.logPerformance()` | Same risk. Use a non-blocking path (e.g. lock that doesn’t require runBlocking, or dispatch updates to a background scope). |

---

## 3. Data consistency and repository

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| D1 | **Domain adapter swallows exceptions** | `DomainTripRepositoryAdapter` (getTripById, getTripsByDateRange, getTripsByStatus) | On exception, emits `null`/`emptyList()` without logging or exposing cause. Log at warn/error and optionally expose error state (e.g. sealed result) so callers can react. |
| D2 | **Delete/update return value dropped** | Domain `TripRepository` returns `Unit` for delete/update; data layer returns `Boolean`. | Align domain with data (return success/failure or throw on failure) so UI can show delete/update failure (wiring). |
| D3 | **StateCache never invalidated** | `TripInputViewModel` receives `StateCache` but does not use it; or if used elsewhere, cache is not invalidated on insert/update/delete. | Either remove StateCache from TripInputViewModel constructor if unused, or invalidate cache after any trip mutation. |

---

## 4. Wiring and UX (wiring only)

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| W1 | **History-by-date dialog stale** | `TripHistoryByDateViewModel` uses `getTripsByDateRange(...).first()` once; no refresh when DB changes. | Refresh when dialog becomes visible (e.g. call `loadTripsForDate(currentDate)` in onResume or when fragment/dialog is shown) so list is fresh after a save elsewhere. |

---

## 5. Dead or redundant code

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| R1 | **saveCompletedTrip never used** | `TripStatePersistence.saveCompletedTrip(actualMiles)` saves with GPS metadata; ViewModel `endTrip()` uses domain `insertTrip(trip)` without it. | Either use saveCompletedTrip when ending a trip (so DB gets metadata), or remove the method if not needed. |
| R2 | **autoSaveTripState empty** | `TripStatePersistence.autoSaveTripState()` is no-op. | Remove the method or implement and use from one place. |
| R3 | **getCurrentActiveTrip always null** | `DomainTripRepositoryAdapter.getCurrentActiveTrip()` always emits null; active trip is in TripStateManager. | Remove from domain interface and adapter, or implement from TripStateManager if something should consume it. |
| R4 | **StateCache unused in ViewModel** | `TripInputViewModel` has `StateCache` in constructor but never uses it. | Remove from constructor and DI for TripInputViewModel (if not used elsewhere for this screen). |

---

## 6. Test quality and robustness

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| T1 | **DomainTripRepositoryAdapter untested** | No dedicated tests for mapping and one-shot flows. | Add unit test for DomainTripRepositoryAdapter (data → domain mapping, getTripsByDateRange/getTripsByStatus behavior). |
| T2 | **TripHistoryViewModelTest temp dir** | Creates temp directory in @Before, never deletes in @After. | Delete the temp directory in @After to avoid accumulation. |
| T3 | **Call-order assertion brittle** | `TripStatisticsWiringTest`: asserts callOrder.contains("insertTrip") and "getMonthlyStats". | Prefer asserting observable outcomes (e.g. final UI state or repository state) instead of call order. |
| T4 | **Edge-case coverage** | Missing tests for repository insert failure, empty list, delete failure on critical paths. | Add at least one test each: insert failure in endTrip path, empty list handling, delete failure surface. |

---

## 7. Logging and null safety (lower priority)

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| L1 | **PreferencesManager fallback silent** | `getPeriodMode()` catches and returns STANDARD without logging. | Log at debug/warn when falling back to default so corrupted pref is visible. |
| L2 | **Unsafe !! in CustomCalendarDialog** | `periodStartDate!!`, `periodEndDate!!` when adding decorators. | Use safe check after calculatePeriodBoundaries; early-return or show error if null. |
| L3 | **Invalid ID in deleteTrip** | `DomainTripRepositoryAdapter.deleteTrip(trip)` uses `trip.id.toLongOrNull() ?: 0L`; 0 may match no row. | Validate ID (e.g. require positive) and/or propagate "invalid id" to caller. |

---

## Implementation order (recommended)

1. **C1, C2** – Fix hang and loading flag (critical behavior).
2. **C3, C4, D2, W1** – User-visible wiring: show errors and delete failure; refresh history-by-date when shown.
3. **B1, B2** – Remove runBlocking from hot paths.
4. **D1, D3, R1–R4** – Data consistency, logging, dead code removal.
5. **T1–T4** – Test fixes and added coverage.
6. **L1–L3** – Logging and null-safety polish.

---

## Out of scope for this plan

- UI layout, styling, or new screens (except wiring listed above).
- Full single-source-of-truth refactor for trip state (TripStateManager vs ViewModel vs persistence).
- Introducing TripTrackingController interface (optional follow-up).
- Moving trip input draft from Fragment SharedPreferences to ViewModel (optional follow-up).
- Parameterized tests (nice-to-have).

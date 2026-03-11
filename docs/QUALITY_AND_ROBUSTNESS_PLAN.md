# Quality and Robustness Plan

**Scope:** Backend, data, domain, services, ViewModels, and wiring to UI only. No frontend layout or styling changes unless required for wiring (e.g. showing errors to the user).

This plan was produced from multi-agent exploration of the codebase (backend quality, test coverage, architecture and single source of truth).

**Health & resilience:** Startup database health check and start-trip guard (block Start when unhealthy) are implemented (OutOfRouteApplication, TripInputViewModel).

---

## 1. Critical production bugs

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| C1 | **getTripsByStatus never emits** | `DomainTripRepositoryAdapter.getTripsByStatus()` | Use `.first()` to get one snapshot. | DONE |
| C2 | **Loading never set to false** | `TripHistoryViewModel.loadTrips()` | Set `_isLoading.value = false` on first emission inside `collect`. | DONE |
| C3 | **Error events not shown to user** | `TripInputFragment.handleEvent()` | Call `showSnackbar(event.message)` for Error, CalculationError, SaveError. | DONE |
| C4 | **Delete failure not surfaced** | `TripHistoryViewModel` / `TripHistoryByDateViewModel` | Domain returns `Boolean`; UI observes `deleteError` SharedFlow. | DONE |

---

## 2. Concurrency and blocking

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| B1 | **runBlocking can block Main** | `UnifiedTripService.getTripStatistics()` | API is suspend; callers use coroutines. | DONE |
| B2 | **runBlocking in performance logger** | `PerformanceTracker.logPerformance()` | Uses synchronized, not runBlocking. | DONE |
---

## 3. Data consistency and repository

| ID | Finding | Location | Action |
|----|---------|----------|--------|
| D1 | **Domain adapter swallows exceptions** | `DomainTripRepositoryAdapter` (getTripById, getTripsByDateRange, getTripsByStatus) | On exception, emits `null`/`emptyList()` without logging or exposing cause. Log at warn/error and optionally expose error state (e.g. sealed result) so callers can react. |
| D2 | **Delete/update return value dropped** | Domain `TripRepository` returns `Unit` for delete/update; data layer returns `Boolean`. | Align domain with data (return success/failure or throw on failure) so UI can show delete/update failure (wiring). |
| D3 | **StateCache never invalidated** | `TripInputViewModel` receives `StateCache` but does not use it; or if used elsewhere, cache is not invalidated on insert/update/delete. | Either remove StateCache from TripInputViewModel constructor if unused, or invalidate cache after any trip mutation. |

---

## 4. Wiring and UX (wiring only)

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| W1 | **History-by-date dialog stale** | `TripHistoryByDateViewModel` | Call `loadTripsForDate` in onResume. | DONE |

---

## 5. Dead or redundant code

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| R1 | **saveCompletedTrip never used** | `TripStatePersistence` | Documented as future work; not wired. Wire to trip-end flow when GPS metadata persistence via this path is needed (Weakest Areas Plan Phase 5.5). | DONE — Wired in TripInputViewModel.endTrip(); saves with GPS metadata. |
| R2 | **autoSaveTripState empty** | `TripStatePersistence` | Removed no-op method and call. | DONE |
| R3 | **getCurrentActiveTrip always null** | `DomainTripRepositoryAdapter` | Not in current codebase. | N/A |
| R4 | **StateCache unused in ViewModel** | `TripInputViewModel` | StateCache not in constructor. | DONE |

---

## 6. Test quality and robustness

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| T1 | **DomainTripRepositoryAdapter untested** | | Add unit tests for mapping, getTripsByStatus, deleteTrip invalid ID. | DONE |
| T2 | **TripHistoryViewModelTest temp dir** | | Delete temp dir in @After. | DONE |
| T3 | **Call-order assertion brittle** | `TripStatisticsWiringTest` | Uses coVerify and outcome assertions. | DONE |
| T4 | **Edge-case coverage** | | Migration tests deferred (schema export enabled; add test when second version exists). Insert/delete failure and empty list covered in DomainTripRepositoryAdapterTest. Other edge cases (invalid ID, data layer throws) covered; D1 logging in place in DomainTripRepositoryAdapter. | DONE |

---

## 7. Logging and null safety (lower priority)

| ID | Finding | Location | Action | Status |
|----|---------|----------|--------|--------|
| L1 | **PreferencesManager fallback silent** | `getPeriodMode()` | Log at debug/warn when falling back. | DONE — PreferencesManager.getPeriodMode() logs fallback with AppLogger.d/w. |
| L2 | **Unsafe !! in CustomCalendarDialog** | | Safe check `if (start == null \|\| end == null) return`. | DONE |
| L3 | **Invalid ID in deleteTrip** | `DomainTripRepositoryAdapter` | Validate ID; return false for invalid. | DONE |

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

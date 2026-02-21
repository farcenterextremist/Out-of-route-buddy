# GPS and location wiring

**Owner:** Back-end / Front-end  
**Related:** `docs/agents/CURRENT_WIRING_PLAN.md`, `TripInputViewModel.kt`, `TripTrackingService.kt`, `UnifiedLocationService.kt`.

---

## Two paths

### 1. TripTrackingService (primary for live trip miles)

- **Used for:** Live distance during an active trip (“Total Miles” on the trip input screen).
- **Started/stopped by:** `TripInputViewModel.calculateTrip()` → `TripTrackingService.startService(application, loadedMiles, bounceMiles)`; `endTrip()` / `clearTrip()` → `TripTrackingService.stopService(application)`; `pauseTrip()` / `resumeTrip()` → `pauseService()` / `resumeService()`.
- **Implementation:** Uses `FusedLocationProviderClient` directly in the service. Accumulates distance in a location callback and exposes it via `TripTrackingService.tripMetrics` (`StateFlow<TripMetrics>` with `totalMiles`, `oorMiles`).
- **ViewModel:** `observeGpsTrackingData()` collects `TripTrackingService.tripMetrics` and updates `_uiState.actualMiles`; also calls `updateTripProgress()` and `saveTripStateForPersistence()` on each emission.
- **Flow:** GPS (FusedLocationProvider) → TripTrackingService → tripMetrics → ViewModel → UI.

### 2. UnifiedLocationService (secondary / stats)

- **Used for:** `realTimeGpsData` in ViewModel `observeLocationData()` (can update actualMiles when trip is not active); `getLocationStatistics()` for the location stats feature.
- **Does not:** Start or stop TripTrackingService. Does not drive the primary “live miles” during a trip.
- **ViewModel:** Injected; `unifiedLocationService.realTimeGpsData.collect { ... }` and `getLocationStatistics()`.

---

## When each is used

| Scenario | Primary source |
|----------|----------------|
| Active trip, “Total Miles” updating in real time | TripTrackingService.tripMetrics |
| Trip not active, optional GPS display | UnifiedLocationService.realTimeGpsData |
| Location statistics (e.g. settings/reports) | UnifiedLocationService.getLocationStatistics() |

---

## Recommendation

- **Single source for “live trip miles”:** TripTrackingService is that source. UnifiedLocationService remains for stats and non-trip display. If consolidating later, prefer keeping TripTrackingService as the single source for trip distance and document any deprecation of realTimeGpsData for trip UI.

# Trip recovery wiring and precedence

**Owner:** Back-end  
**Related:** `docs/agents/CURRENT_WIRING_PLAN.md`, `docs/technical/TRIP_PERSISTENCE_END_CLEAR.md`, `TripInputViewModel.loadInitialData()`, `TripCrashRecoveryManager`, `TripPersistenceManager`.

---

## Two recovery sources

### 1. Crash recovery (TripCrashRecoveryManager)

- **What it is:** A 30-second auto-save of a minimal “recoverable” state (loadedMiles, bounceMiles, actualMiles, isActive, etc.) stored in SharedPreferences. On next app launch, if the app did not shut down cleanly, `OutOfRouteApplication` sets `recoveredTripState` from `TripCrashRecoveryManager.initialize()`.
- **When it runs:** ViewModel calls `crashRecoveryManager.startAutoSave { ... }` when a trip becomes active (`calculateTrip()`, `continueRecoveredTrip()`); `stopAutoSave()` and `clearRecoveryData()` on `endTrip()`, `clearTrip()`, `resetTrip()`, or error.
- **Data:** Lightweight snapshot (no full trip entity). Used to restore UI and resume TripTrackingService after a crash.

### 2. Persistence (TripPersistenceManager)

- **What it is:** Full saved trip state (trip object, loaded/bounce/actual miles, optional location/metadata) in SharedPreferences (`trip_persistence`). Used for “resume trip” across normal app restarts and for the in-app recovery dialog (e.g. MainActivity / TripRecoveryDialog).
- **When it’s saved:** ViewModel calls `tripPersistenceManager.saveActiveTripState(...)` (and `updateTripProgress()`) during an active trip; `clearSavedTripState()` on End trip, Clear trip, or start new trip. 24h recovery timeout; expired state is cleared on load.
- **Data:** Full `SavedTripState` (trip + miles). Can drive `continueRecoveredTrip()` or “Start new trip.”

---

## Precedence in loadInitialData()

**Order is fixed:**

1. **Crash recovery first:** If `OutOfRouteApplication.recoveredTripState != null` and `isActive`, restore UI from that state, start auto-save, then `OutOfRouteApplication.clearRecoveredState()`. Do not read TripPersistenceManager for this path.
2. **Else, persistence:** If `tripPersistenceManager.loadSavedTripState() != null`, treat as active trip: set `hasLoadedPersistedState`, sync `tripStateManager.startTrip(...)`, restore `currentTrip` and UI, start auto-save if active.
3. **Else:** Set trip inactive; no recovery.

So: **crash snapshot wins over full persistence** when both exist (e.g. after a crash that occurred while persistence was also being written). After a normal app kill, only persistence is present and is used.

---

## No double prompt

- Recovered state is consumed once: crash path clears `recoveredTripState`; persistence path does not show a second “recover?” if the user already continued from crash recovery. The recovery dialog (e.g. TripRecoveryDialog) is driven by MainActivity / persistence, not by crash recovery; ViewModel restore happens in `loadInitialData()` before the user sees the main UI, so the user is not asked twice for the same trip.

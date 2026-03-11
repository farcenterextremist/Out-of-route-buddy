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

## Process death and recovery (Blind Spot Plan §2)

**When the process is killed** (e.g. app in background, system reclaims memory):

- **What is restored:** On next launch, `OutOfRouteApplication` and `TripInputViewModel.loadInitialData()` run. If the process was killed during an active trip:
  - **Crash path:** If `TripCrashRecoveryManager` had written a 30-second snapshot before kill, `recoveredTripState` is set and the crash-recovery path restores UI and trip state; `clearRecoveredState()` is called so it is consumed once.
  - **Persistence path:** If no crash snapshot (or it was cleared), `TripPersistenceManager.loadSavedTripState()` is used. If it returns a saved state (within 24h recovery window), the ViewModel restores trip state, syncs TripStateManager, and the user sees the trip as active; MainActivity may show the recovery dialog (TripRecoveryDialog) so the user can "Continue trip" or "Start new trip."
- **What the user sees:** After process death and relaunch, the user either lands on the main screen with the trip already restored (no dialog) or sees the recovery dialog offering "Continue trip" or "Start new trip." No second prompt for the same recovery.
- **Rotation / configuration change:** Activities do not use `android:configChanges`; rotation recreates the Activity. ViewModel survives (process is not killed), so trip state is preserved. This is intentional; ViewModel + saved instance state (navigation state in MainActivity) handle configuration change.

**Testing:** Process kill during an active trip is not yet covered by an automated test. Manual test: start a trip, send app to background, then force-stop the app (or use "Don't keep activities"); relaunch and confirm recovery dialog or restored trip state.

---

## No double prompt

- Recovered state is consumed once: crash path clears `recoveredTripState`; persistence path does not show a second “recover?” if the user already continued from crash recovery. The recovery dialog (e.g. TripRecoveryDialog) is driven by MainActivity / persistence, not by crash recovery; ViewModel restore happens in `loadInitialData()` before the user sees the main UI, so the user is not asked twice for the same trip.

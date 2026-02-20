# Jump detection and trip state persistence

**Owner:** Back-end  
**Purpose:** Define location jump detection and document trip state persistence.  
**Related:** 25-point #12–13, CRUCIAL #3, `TripStateManager.kt`, `TripStatePersistence.kt`.

---

## 1. Location jump detection (TripStateManager)

**Current:** `GpsMetadata.locationJumps` is present but never incremented; `updateGpsMetadata()` has `// TODO: Implement jump detection` (line ~231).

**Definition (recommended):** A "jump" is a pair of consecutive location samples such that the implied speed (distance / time) exceeds a plausible threshold (e.g. 120 mph or configurable). Optionally, also treat as jump if horizontal accuracy is very poor and distance is large.

**Implementation options:**

- In `TripStateManager.updateLocation()` (or before calling `updateGpsMetadata`): compare `newLocation` with `currentState.lastLocation`. If `lastLocation` is null, no jump. Otherwise compute distance (e.g. Haversine) and time delta; if speed > threshold (e.g. 53.6 m/s for 120 mph), increment `locationJumps`.
- Expose threshold as a constant or from `PreferencesManager` for tuning.
- After implementation, remove the TODO and add a unit test (e.g. two points 1 km apart 1 second apart → one jump).

---

## 2. Trip state persistence across process death

**Current:** `TripStatePersistence` and `TripPersistenceManager` persist active trip state (e.g. to preferences/DB). ViewModel loads persisted state on startup and can restore an active trip; it syncs with `TripStateManager` so the rest of the app sees the active state. See `TripInputViewModel` (load persisted state, sync TripStateManager).

**Behavior:**

- **Save:** When a trip is active, state is saved (e.g. on state change or periodically) so that after process death the app can restore "trip in progress."
- **Load:** On app start, persistence layer loads saved state; ViewModel applies it and restores TripStateManager so services and UI see the active trip.
- **Documentation:** This file serves as the doc. Key files: `TripStatePersistence.kt`, `TripPersistenceManager`, `TripInputViewModel` (load + sync).

**Verification:** Manual test: start trip, kill app (or force-stop), reopen → trip should still show as active and data preserved. QA can add this to regression checklist.

---

*Implement jump detection in TripStateManager when ready; keep trip state persistence as-is and reference this doc.*

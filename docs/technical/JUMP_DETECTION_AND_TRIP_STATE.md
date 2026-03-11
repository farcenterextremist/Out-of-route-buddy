# Jump detection and trip state persistence

**Owner:** Back-end  
**Purpose:** Define location jump detection and document trip state persistence.  
**Related:** 25-point #12–13, CRUCIAL #3, `TripStateManager.kt`, `TripStatePersistence.kt`.

---

## 1. Location jump detection (TripStateManager)

**Status: implemented.** Jump detection is implemented in `TripStateManager.updateGpsMetadata()`: implied speed (distance/time) between consecutive samples is compared to `JUMP_SPEED_THRESHOLD_MS` (120 mph in m/s); if exceeded, `locationJumps` is incremented. See `TripStateManager.kt` around lines 198–220. No TODO remaining.

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

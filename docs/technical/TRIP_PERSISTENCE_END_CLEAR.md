# Trip persistence: End trip vs Clear trip

**Owner:** Back-end  
**Related:** `docs/agents/MONTHLY_STATS_PERSISTENCE_CALENDAR_PLAN.md`, `docs/ux/END_TRIP_FLOW_UX.md`, `docs/product/FEATURE_BRIEF_monthly_stats_and_persistence.md`.

---

## Confirmed behavior

### End trip

1. **ViewModel:** `TripInputViewModel.endTrip()` builds a completed `Trip` and calls `tripRepository.insertTrip(tripData)`.
2. **Repository:** `DomainTripRepositoryAdapter.insertTrip()` maps domain `Trip` to data layer and calls `dataRepository.insertTrip(dataTrip)`.
3. **Storage:** `TripRepository` (data) writes to Room via `TripDao.insertTrip(tripEntity)`. One new row in the `trips` table.
4. **After save:** ViewModel calls `refreshAggregateStatistics()` and `refreshSelectedPeriod()`, so monthly stats and period/calendar views reflect the new trip.
5. **In-progress state:** ViewModel calls `clearTripPersistence()` so TripStatePersistence/TripStateManager no longer hold this trip as in-progress.

**Result:** Trip is stored in Room; it is included in `getMonthlyTripStatistics()` and `getTripsByDateRange()`; it appears in calendar and history.

### Clear trip

1. **ViewModel:** `TripInputViewModel.clearTrip()` stops GPS, stops auto-save, calls `clearTripPersistence()`, resets UI state, sets `currentTrip = null`.
2. **No insert:** There is no call to `tripRepository.insertTrip()`. No row is written to the `trips` table.
3. **Refresh:** ViewModel still calls `refreshAggregateStatistics()` and `refreshSelectedPeriod()` so the UI shows current DB state (unchanged by this clear).

**Result:** Trip is not stored; it does not count in monthly stats and does not appear in calendar or history.

---

## Single source of truth

- **Monthly statistics:** `TripRepository.getMonthlyTripStatistics()` (domain) → data layer → Room queries for current month. Only rows in `trips` are included.
- **Calendar / history:** `TripRepository.getTripsByDateRange(startDate, endDate)` reads from the same Room `trips` table. Date boundaries (timezone, start/end of day) are consistent with monthly stats.
- **No separate “calendar” store.** All persisted trip data lives in Room.

---

## Metadata and extension points

- **Current entity:** `TripEntity` in Room has structured fields: date, miles, OOR, GPS metadata (accuracy, points, duration, speed, etc.), trip start/end times, last location (lat/lng/time). See `app/.../data/entities/TripEntity.kt`.
- **Recommendation:** Prefer adding new **typed columns** or small, versioned structures in Room over free-form text blobs that might contain PII. See `docs/security/SECURITY_NOTES.md` for PII and logging.

---

## OfflineDataManager

- **Current:** In-memory only; `loadOfflineStorage()` and `saveOfflineStorage()` are stubs. See `docs/technical/OFFLINE_PERSISTENCE.md`.
- **When implementing:** Load on init, save on mutation; use DataStore or Room for persistence so offline trips and sync state survive app restart. No change to End/Clear trip semantics: only **End trip** writes a completed trip to the main Room store; OfflineDataManager is for offline queue/sync, not for bypassing Clear trip.

---

*This doc is the back-end reference for “what gets persisted” and “what monthly stats and calendar read from.”*

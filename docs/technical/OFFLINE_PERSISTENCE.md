# OfflineDataManager — load/save persistence

**Owner:** Back-end  
**Purpose:** Document current state and plan for persisting offline storage across app restarts.  
**Related:** 25-point #11, CRUCIAL #2, `app/.../data/OfflineDataManager.kt`, `docs/agents/CURRENT_WIRING_PLAN.md`.

---

## Status: implemented (2025-02)

**Implementation complete.** `loadOfflineStorage()` and `saveOfflineStorage()` use DataStore + Gson. Offline trips and sync state survive app restart.

---

## Audit (Phase 1d — 2025-02)

### OfflineStorage schema

- **OfflineStorage:** trips (Map), analytics (List), settings (Map), lastBackup, storageSize, tripCount, analyticsCount
- **OfflineTrip:** id, localId, tripData (Map), gpsData (Map?), timestamp, syncStatus, conflictResolution, retryCount, lastSyncAttempt
- **OfflineAnalytics:** id, event, parameters (Map), timestamp, syncStatus, retryCount

### Call sites

- **OfflineDataManager** (init): `loadOfflineStorage()` on startup
- **saveTripOffline, saveAnalyticsOffline, updateTripSyncStatus, resolveTripConflict, cleanupOldestTrips, cleanupOldestAnalytics, clearOfflineData, clearAllOfflineData:** all call `saveOfflineStorage()` after mutation
- **Consumers:** OfflineSyncService, OfflineSyncCoordinator, OfflineServiceCoordinator

### Persistence format proposal

| Option | Pros | Cons |
|--------|------|------|
| **DataStore (JSON)** | Simple; handles nested maps/dates; atomic; no schema migration for OfflineStorage structure | Need JSON serialization (kotlinx.serialization or Gson) |
| **Room** | Already in project; transactional | Requires new entities for OfflineTrip/OfflineAnalytics; schema changes need migrations; maps are awkward |
| **SharedPreferences** | No new deps | Size limits; not ideal for complex nested data |

**Recommendation:** **DataStore** with JSON (kotlinx.serialization). Store `OfflineStorage` as a single JSON blob. Version the format (e.g. `offline_storage_v1`) for future migration.

### Room migration tests

- **Current:** No explicit migration tests. `TripDaoInMemoryTest` and `TestDatabaseModule` use in-memory DB (no migrations run).
- **Recommendation:** Add `AppDatabaseMigrationTest` using `Room.inMemoryDatabaseBuilder` with `createFromAsset` or `addMigrations` to verify MIGRATION_1_2 runs without error. See `androidx.room.testing.MigrationTestHelper` for instrumented tests.

---

## Current state

- **Persistence:** `OfflineDataManager` uses DataStore + Gson for `loadOfflineStorage()` and `saveOfflineStorage()`. Load on init; save after every mutation. Offline trips and sync state survive app restart. **Verified (Weakest Areas Plan Phase 5.1):** Call sites — init, saveTripOffline, saveAnalyticsOffline, updateTripSyncStatus, resolveTripConflict, cleanupOldestTrips, cleanupOldestAnalytics, clearOfflineData; consumers — OfflineSyncService, OfflineSyncCoordinator, OfflineServiceCoordinator.

---

## Intended behavior (for implementation)

1. **Load on init:** On app start, read persisted offline storage from DataStore (JSON) and set `_offlineStorage.value`.
2. **Save on change:** After any mutation, persist the current `OfflineStorage` to DataStore.
3. **Format:** Versioned JSON (e.g. `offline_storage_v1`). Use kotlinx.serialization for OfflineStorage, OfflineTrip, OfflineAnalytics.

---

## Acceptance criteria (Design scope)

1. **Trips saved offline survive app restart.** User saves a trip while offline; kills app; reopens; trip is still in offline queue.
2. **Pending/failed sync counts and sync status survive restart.** SyncStatus (PENDING, FAILED, etc.) and retryCount persist.
3. **No data loss on normal process death.** No corruption or partial writes on crash.
4. **Respect Known Truths.** OfflineDataManager is for offline queue/sync only; does not bypass Clear semantics or create a second source for monthly stats/calendar.

## Migration strategy

- **v1 format:** Single JSON blob keyed `offline_storage_v1`. If format changes, add `offline_storage_v2` and migration logic in load.
- **Backward compatibility:** If load fails (corrupt/missing), start with empty OfflineStorage; log warning.
- **CI:** OfflineDataManagerPersistenceTest runs in `.github/workflows/android-tests.yml` via `./gradlew testDebugUnitTest`.

---

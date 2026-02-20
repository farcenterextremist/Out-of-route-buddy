# OfflineDataManager — load/save persistence

**Owner:** Back-end  
**Purpose:** Document current state and plan for persisting offline storage across app restarts.  
**Related:** 25-point #11, CRUCIAL #2, `app/.../data/OfflineDataManager.kt`.

---

## Current state

- **In-memory only:** `OfflineDataManager` keeps trips and analytics in a `MutableStateFlow<OfflineStorage>`. `loadOfflineStorage()` and `saveOfflineStorage()` are stubs (TODO: implement actual loading/saving to SharedPreferences, SQLite, or DataStore).
- **Effect:** On process death or app restart, offline trips and pending sync state are lost. In-memory operations (saveTripOffline, updateTripSyncStatus, etc.) work within a session.

---

## Intended behavior (for implementation)

1. **Load on init:** On app start, read persisted offline storage from disk (e.g. JSON in DataStore or rows in Room) and set `_offlineStorage.value`.
2. **Save on change:** After any mutation (saveTripOffline, updateTripSyncStatus, cleanup, etc.), persist the current `OfflineStorage` (or delta) to disk.
3. **Format:** Use a stable, versioned format so we can migrate later. Prefer DataStore or Room for robustness.

---

## Acceptance

- Trips saved offline survive app restart.
- Pending/failed sync counts and sync status survive restart.
- No data loss on normal process death.

---

*When implementing, remove the TODOs in `loadOfflineStorage()` and `saveOfflineStorage()` and add tests for load/save round-trip.*

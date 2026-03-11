# Feature brief: Offline persistence (deferred)

**Status:** Deferred  
**Owner:** Back-end  
**Reference:** CRUCIAL_IMPROVEMENTS_TODO §2, Advanced Improvement Plan Pillar 6.

---

## Summary

OfflineDataManager provides in-memory offline storage and sync-status tracking. **Persistent load/save** (so offline trips survive app restarts) are **not yet implemented**; they are formally deferred until product prioritizes or a backend is available.

---

## Current behavior

- **Load:** `loadOfflineStorage()` reads from DataStore when present; on first run or failure, starts with empty storage. Data is held in memory (`_offlineStorage` StateFlow).
- **Save:** `saveOfflineStorage()` and related writes persist to DataStore (fire-and-forget). So offline data **is** persisted for the current process; cross-restart persistence depends on DataStore and is implemented but may need verification (see FAILING_OR_IGNORED_TESTS for OfflineDataManagerPersistenceTest).
- **SyncWorker / backend:** Full sync and backend upload are deferred until a backend exists. Single reference: `SyncWorker.performFullSync()` and [docs/technical/OFFLINE_PERSISTENCE.md](../technical/OFFLINE_PERSISTENCE.md).

---

## Deferred work (when prioritized)

1. Confirm or fix DataStore persistence so offline trips survive app restarts (and fix or document OfflineDataManagerPersistenceTest).
2. Implement or refine sync-to-backend when backend API is available.

---

*See [CRUCIAL_IMPROVEMENTS_TODO.md](../CRUCIAL_IMPROVEMENTS_TODO.md) §2 and [OFFLINE_PERSISTENCE.md](../technical/OFFLINE_PERSISTENCE.md).*

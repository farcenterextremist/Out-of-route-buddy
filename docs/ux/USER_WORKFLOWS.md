# User workflows

**Purpose:** Short, stepwise description of key user and data workflows for support, QA, and future changes. Ref: Advanced Settings and Workflow Plan Phase 2.1.

---

## Trip lifecycle

1. **Start trip:** User enters loaded/bounce miles (optional) and taps Start Trip. If **Auto-Start Trip** is enabled in Settings, tracking can start on app launch (product-dependent).
2. **Tracking:** TripTrackingService runs; GPS updates drive live OOR/total miles. State is persisted via TripPersistenceManager and TripCrashRecoveryManager (30s snapshot).
3. **End trip:** User taps End Trip → confirmation → ViewModel calls tripRepository.insertTrip(); persistence cleared; stats/period refreshed. Trip is saved to Room and appears in monthly stats and history. See [TRIP_PERSISTENCE_END_CLEAR.md](../technical/TRIP_PERSISTENCE_END_CLEAR.md).
4. **Clear trip:** User taps Cancel/Clear → confirmation → ViewModel clears persistence and resets UI; **no** insert to Room. Trip is not saved and does not count in stats. See [TRIP_PERSISTENCE_END_CLEAR.md](../technical/TRIP_PERSISTENCE_END_CLEAR.md).

---

## Recovery

1. **App launch:** OutOfRouteApplication and TripInputViewModel.loadInitialData() run.
2. **Load order:** (1) If TripCrashRecoveryManager had a 30s snapshot (crash path), restored from recoveredTripState; (2) else if TripPersistenceManager has saved state within 24h, restored from persistence; (3) else trip inactive.
3. **User sees:** Either main screen with trip already restored, or recovery dialog (Continue trip / Start new trip). No second prompt for the same recovery. See [RECOVERY_WIRING.md](../technical/RECOVERY_WIRING.md).

---

## Export

1. **Entry:** User opens trip history (or relevant screen) and triggers export (e.g. Export Data).
2. **Flow:** TripHistoryViewModel.exportTrips() or exportToPDF() loads trips from repository, calls TripExporter.exportToCSV() or exportToPDF().
3. **Output:** File is written to app cache (context.cacheDir); user gets system share sheet to send/save the file. No server upload. See [SECURITY_NOTES.md](../security/SECURITY_NOTES.md) §12 (Data export and deletion).

---

## Data management

1. **Entry:** Settings → Data & privacy.
2. **Delete old data:** User taps "Delete old data from device" → confirmation → DataManagementViewModel.deleteOldDataFromDevice(12). Trips older than 12 months are removed from local storage only. No server; data is local only.
3. **Clear all:** User taps "Clear all trip data from device" → confirmation → DataManagementViewModel.clearAllDataFromDevice(). All trips removed from device. No server. See [SECURITY_NOTES.md](../security/SECURITY_NOTES.md) §12.

---

*Technical refs: [RECOVERY_WIRING.md](../technical/RECOVERY_WIRING.md), [TRIP_PERSISTENCE_END_CLEAR.md](../technical/TRIP_PERSISTENCE_END_CLEAR.md), [SETTINGS_AND_SERVICES.md](../technical/SETTINGS_AND_SERVICES.md), [SECURITY_NOTES.md](../security/SECURITY_NOTES.md).*

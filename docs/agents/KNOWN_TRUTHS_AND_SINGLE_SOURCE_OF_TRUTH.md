# Known truths and single source of truth — agent training

**Purpose:** Feed this document into agent training and context so all agents share the same foundational facts and single sources of truth (SSOT). Use it before refactors, new features, or explorations.  
**Owner:** Coordinator / File Organizer  
**Related:** `docs/technical/WIRING_MAP.md`, `docs/agents/CURRENT_WIRING_PLAN.md`, `docs/technical/TRIP_PERSISTENCE_END_CLEAR.md`, `docs/technical/RECOVERY_WIRING.md`, `docs/technical/GPS_AND_LOCATION_WIRING.md`, `docs/technical/OFFLINE_PERSISTENCE.md`, `docs/security/SECURITY_NOTES.md`.

---

## 1. How to use this doc

- **Agents:** When working on the OutOfRouteBuddy Android app, treat this doc as canonical for “what is true” and “who owns what data.”
- **Before changing persistence, recovery, calendar, or statistics:** Check the SSOT section and the linked technical docs.
- **Naming / structure:** Follow existing conventions; see “Known truths — structure and conventions.”
- **If something is ambiguous about the obvious solid foundational workflow:** Ask the user.

---

## 2. Known truths — architecture and layers

- **App:** Single Android application module (`:app`). No separate `domain` or `data` Gradle modules; code lives under `app/src/main/java/...` with package structure reflecting domain/data/presentation.
- **UI → ViewModel → data:** TripInputFragment binds to TripInputViewModel; ViewModel uses TripRepository (domain), TripTrackingService, TripPersistenceManager, PreferencesManager, UnifiedTripService, UnifiedLocationService, UnifiedOfflineService, TripCrashRecoveryManager. No UI talks to Room or SharedPreferences directly.
- **Repository chain:** Domain `TripRepository` ← `DomainTripRepositoryAdapter` ← data `TripRepository` ← `TripDao` ← Room (`trips` table). All reads/writes of saved trips go through this chain.
- **DI:** Hilt. ViewModels and repositories are provided via DI modules (e.g. RepositoryModule, DatabaseModule, ServiceModule).
- **Navigation:** MainActivity has NavHostFragment; nav graph has a single start destination (TripInputFragment). SettingsFragment exists but is not in the nav graph; settings are shown as a dialog from TripInputFragment (toolbar Settings button → showSettingsDialog).

---

## 3. Known truths — trip lifecycle and semantics

- **End trip:** ViewModel builds completed `Trip`, calls `tripRepository.insertTrip(tripData)`. One new row in Room `trips` table. Then ViewModel calls `refreshAggregateStatistics()`, `refreshSelectedPeriod()`, and `clearTripPersistence()`. Result: trip is saved; it appears in monthly stats, calendar, and history.
- **Clear trip:** ViewModel stops GPS, clears persistence, resets UI, sets `currentTrip = null`. **No** `insertTrip`. Result: trip is **not** saved; it does not appear in monthly stats, calendar, or history.
- **Only End trip writes to the main trip store.** Clear trip never inserts. OfflineDataManager (when implemented) is for offline queue/sync, not for bypassing Clear semantics.

---

## 4. Known truths — recovery precedence

- **Two recovery sources:** (1) TripCrashRecoveryManager — 30s auto-save to SharedPreferences; on next launch, `OutOfRouteApplication.recoveredTripState` can restore. (2) TripPersistenceManager — full trip state in SharedPreferences (`trip_persistence`); 24h timeout; used for “resume trip” and recovery dialog.
- **Precedence in `loadInitialData()`:** (1) If `OutOfRouteApplication.recoveredTripState` active → restore from that, clear recovered state. (2) Else if `TripPersistenceManager.loadSavedTripState()` returns state → restore from that, sync TripStateManager. (3) Else trip inactive. **Crash recovery wins over persistence** when both exist.

---

## 5. Known truths — GPS and live trip miles

- **Primary source for “Total Miles” during an active trip:** TripTrackingService. It uses FusedLocationProviderClient; exposes `tripMetrics` (StateFlow); started/stopped/paused by ViewModel. ViewModel observes `tripMetrics` and updates `actualMiles` and persistence.
- **UnifiedLocationService:** Secondary; used for `realTimeGpsData` (e.g. when trip not active) and `getLocationStatistics()`. It does **not** start/stop TripTrackingService and is **not** the single source for live trip miles.

---

## 6. Known truths — calendar and monthly statistics

- **Monthly statistics:** Computed from Room only. `TripRepository.getMonthlyTripStatistics()` (domain) → DomainTripRepositoryAdapter → current **calendar month** (1st 00:00:00 to last moment). Only rows in `trips` are included. No separate “monthly” store.
- **Calendar / period / “days with trips”:** Same Room source. `getTripsByDateRange(startDate, endDate)` and `getTripStatistics(startDate, endDate)` feed `updatePeriodStatistics()` in ViewModel, which builds `datesWithTripsInPeriod` (distinct start-of-day dates from trip `startTime`). Fragment passes `datesWithTripsInPeriod` to CustomCalendarDialog; dialog uses DaysWithTripsDecorator for dots. **No separate calendar store.**
- **Period mode:** STANDARD = 1st of month to last of month. CUSTOM = Thursday before first Friday. Boundaries computed by UnifiedTripService (STANDARD) and PeriodCalculationService (CUSTOM).

---

## 7. Known truths — structure and conventions

- **PreferencesManager** (file `OutOfRouteBuddyPreferences`): Period mode, last loaded/bounce miles, trip active flag. Used by ViewModel for period template and convenience restore.
- **SettingsManager** (file `app_settings`): Theme, distance units, GPS settings, notifications, auto-start, auto-save, etc. Used by Settings UI (dialog and PreferenceFragmentCompat). Same file is used by `preferences.xml` (SettingsFragment) and TripInputFragment settings dialog; theme must be read from this file so selection UI reflects saved value.
- **Build/Config:** `BuildConfig` and `ValidationConfig` in `core/config`; use consistent naming (e.g. UPPER_SNAKE_CASE for constants).
- **Security:** No PII in logs; trip/location data stay on-device (Room, SharedPreferences); no free-form text blobs for PII. See `docs/security/SECURITY_NOTES.md`.

---

## 8. Known truths — deferred or optional

- **OfflineDataManager:** `loadOfflineStorage()` and `saveOfflineStorage()` are stubs; persistence across app restart is **formally deferred**. See `docs/technical/OFFLINE_PERSISTENCE.md`.
- **periodStatistics:** In TripInputUiState but **not** bound to any view. Only `monthlyStatistics` drives the visible statistics row. periodStatistics is kept for possible future use (e.g. “Period totals” row).
- **SyncWorker / BackgroundSyncService:** Do not currently write trip data to Room; branches are stubs.

---

## 9. Single sources of truth (SSOT)

| What | Single source of truth | Where / how |
|------|-------------------------|-------------|
| **Saved (completed) trips** | Room `trips` table | TripDao; all reads/writes via TripRepository (domain) → DomainTripRepositoryAdapter → data TripRepository → TripDao. |
| **Monthly statistics** | Room via `getMonthlyTripStatistics()` | DomainTripRepositoryAdapter.getMonthlyTripStatistics() = getTripStatistics(startOfMonth, endOfMonth). No other store. |
| **Calendar “days with trips”** | Room via `getTripsByDateRange()` | ViewModel.updatePeriodStatistics() calls getTripsByDateRange; builds datesWithTripsInPeriod from trip startTime (start-of-day). Fragment passes to CustomCalendarDialog. |
| **Period mode (Standard vs Custom)** | PreferencesManager | KEY_PERIOD_MODE; savePeriodMode / getPeriodMode. ViewModel and template dialog read/write via PreferencesManager. |
| **Theme (light/dark/system)** | SettingsManager | Key `theme_preference` in SharedPreferences file `app_settings`. Applied at app startup (OutOfRouteApplication) and when user changes in Settings or trip Settings dialog. |
| **Distance units (miles/km)** | SettingsManager | Key `distance_units` in `app_settings`. |
| **Active (in-progress) trip state** | TripPersistenceManager + TripStateManager | TripPersistenceManager: SharedPreferences `trip_persistence` (full state; 24h). TripStateManager: in-memory tripState Flow; sync’d from persistence on load. |
| **Crash recovery snapshot** | TripCrashRecoveryManager | SharedPreferences (KEY_APP_RUNNING, KEY_TRIP_STATE); 30s auto-save; OutOfRouteApplication.recoveredTripState. |
| **Live trip miles (during active trip)** | TripTrackingService.tripMetrics | StateFlow&lt;TripMetrics&gt;; FusedLocationProvider in TripTrackingService. ViewModel observes and updates UI and persistence. |
| **Period date boundaries** | UnifiedTripService + PeriodCalculationService | STANDARD: UnifiedTripService.getCurrentPeriodDates(PeriodMode.STANDARD) = 1st to last of month. CUSTOM: PeriodCalculationService.calculateCustomPeriodStart/End (Thursday before first Friday). |

---

## 10. File and layer ownership (quick reference)

| Layer / concern | Primary files / types |
|-----------------|------------------------|
| Trip persistence (End/Clear, Room) | TripInputViewModel, TripRepository (domain + data), DomainTripRepositoryAdapter, TripDao, TripEntity, AppDatabase |
| Recovery | TripCrashRecoveryManager, TripPersistenceManager, TripStatePersistence, TripStateManager, OutOfRouteApplication, loadInitialData() |
| GPS / live miles | TripTrackingService, UnifiedLocationService, TripInputViewModel.observeGpsTrackingData |
| Period / calendar | PreferencesManager, UnifiedTripService, PeriodCalculationService, TripInputViewModel.updatePeriodStatistics, CustomCalendarDialog, TripInputFragment.showCalendarPicker |
| Theme / app settings | SettingsManager, OutOfRouteApplication.applyThemePreference, SettingsFragment, TripInputFragment (settings dialog) |
| History by date | TripHistoryByDateViewModel, TripHistoryByDateDialog, getTripsByDateRange |
| Wiring reference | docs/technical/WIRING_MAP.md, docs/agents/CURRENT_WIRING_PLAN.md, docs/technical/TRIP_PERSISTENCE_END_CLEAR.md |

---

## 11. Foundational workflow (summary)

1. **User starts trip** → ViewModel.calculateTrip → TripTrackingService.startService, save state to TripPersistenceManager, start crash auto-save.
2. **User ends trip** → ViewModel.endTrip → insertTrip (Room), clearTripPersistence, refreshAggregateStatistics, refreshSelectedPeriod.
3. **User clears trip** → ViewModel.clearTrip → no insert, clearTripPersistence, refreshAggregateStatistics.
4. **App launch** → loadInitialData: crash recovery else persistence else inactive; then refreshAggregateStatistics, initializeSelectedPeriodFromPreferences (→ updatePeriodStatistics).
5. **Calendar / stats** → Always read from Room via TripRepository; datesWithTripsInPeriod and monthly stats from same source.

If you have questions on the obvious solid foundational workflow, ask the user.

---

*Keep this doc in sync with TRIP_PERSISTENCE_END_CLEAR.md, WIRING_MAP.md, and CURRENT_WIRING_PLAN.md when changing persistence or wiring.*

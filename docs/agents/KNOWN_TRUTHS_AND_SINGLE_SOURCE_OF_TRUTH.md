# Known truths and single source of truth — for our agents

**Purpose:** This doc is the canonical reference for all agents. Use it for training and context so every role shares the same facts and single sources of truth (SSOT). Check it before refactors, new features, or explorations.  
**Owner:** Coordinator / File Organizer  
**Related:** `docs/technical/WIRING_MAP.md`, `docs/technical/TRIP_PERSISTENCE_END_CLEAR.md`, `docs/technical/RECOVERY_WIRING.md`, `docs/technical/GPS_AND_LOCATION_WIRING.md`, `docs/technical/OFFLINE_PERSISTENCE.md`, `docs/security/SECURITY_NOTES.md`.

---

## Goal (concise)

**OutOfRouteBuddy** helps delivery drivers and fleet operators track **out-of-route (OOR) miles** accurately. The app is the single place to log loaded and bounce miles, run trips with GPS, and see monthly statistics and history so users can report and improve OOR performance. All agent work (design, UX, code, QA, security, comms) should support this goal and stay consistent with the SSOT below.

---

## How agents use this doc

- Treat this doc as **canonical** for “what is true” and “who owns what data.”
- **Before changing** persistence, recovery, calendar, or statistics: read the SSOT section and the linked technical docs.
- Follow existing **naming and structure** (see conventions below).
- **If something is ambiguous** about the foundational workflow: ask the user.

---

## Single sources of truth (SSOT)

| What | Single source of truth | Where / how |
|------|------------------------|-------------|
| **Saved (completed) trips** | Room `trips` table | All reads/writes via domain TripRepository → DomainTripRepositoryAdapter → data TripRepository → TripDao. |
| **Monthly statistics** | Room via `getMonthlyTripStatistics()` | Current calendar month (1st to last). No other store. |
| **Calendar “days with trips”** | Room via `getTripsByDateRange()` | ViewModel builds `datesWithTripsInPeriod` from trip `startTime`; fragment passes to CustomCalendarDialog. |
| **Period mode (Standard vs Custom)** | PreferencesManager | KEY_PERIOD_MODE. STANDARD = 1st–last of month; CUSTOM = Thursday before first Friday. |
| **Theme (light/dark/system)** | SettingsManager | Key `theme_preference` in `app_settings`. Applied at startup and when user changes in Settings. |
| **Distance units** | SettingsManager | Key `distance_units` in `app_settings`. |
| **Active (in-progress) trip state** | TripPersistenceManager + TripStateManager | SharedPreferences `trip_persistence` (24h); TripStateManager in-memory Flow synced from it. |
| **Crash recovery snapshot** | TripCrashRecoveryManager | 30s auto-save; `OutOfRouteApplication.recoveredTripState`. Crash recovery wins over persistence when both exist. |
| **Live trip miles (during active trip)** | TripTrackingService.tripMetrics | StateFlow from FusedLocationProvider. ViewModel observes; no other source for live miles. |
| **Period date boundaries** | UnifiedTripService + PeriodCalculationService | STANDARD: 1st to last of month. CUSTOM: Thursday before first Friday. |

---

## Foundational workflow

1. **Start trip** → ViewModel.calculateTrip → TripTrackingService.startService, save state to TripPersistenceManager, start crash auto-save.
2. **End trip** → ViewModel.endTrip → insertTrip (Room), clearTripPersistence, refreshAggregateStatistics, refreshSelectedPeriod. Trip appears in monthly stats, calendar, and history.
3. **Clear trip** → ViewModel.clearTrip → **no** insertTrip; clearTripPersistence, refreshAggregateStatistics. Trip is **not** saved.
4. **App launch** → loadInitialData: crash recovery else persistence else inactive; then refreshAggregateStatistics, initializeSelectedPeriodFromPreferences → updatePeriodStatistics.
5. **Calendar / stats** → Always from Room via TripRepository; no separate calendar or monthly store.

---

## Key truths (no contradictions)

- **Only End trip writes to the trip store.** Clear trip never inserts. OfflineDataManager (when implemented) is for offline queue/sync, not for bypassing Clear semantics.
- **UI → ViewModel → data:** No UI talks to Room or SharedPreferences directly. TripInputFragment binds to TripInputViewModel; ViewModel uses TripRepository (domain), TripTrackingService, TripPersistenceManager, PreferencesManager, UnifiedTripService, etc.
- **Repository chain:** Domain TripRepository ← DomainTripRepositoryAdapter ← data TripRepository ← TripDao ← Room (`trips` table).
- **Recovery precedence:** (1) OutOfRouteApplication.recoveredTripState → (2) TripPersistenceManager.loadSavedTripState() → (3) inactive.
- **UnifiedLocationService** is not the source for live trip miles; TripTrackingService is.
- **Security:** No PII in logs; trip/location data on-device only. See `docs/security/SECURITY_NOTES.md`.
- **Deferred:** OfflineDataManager, SyncWorker/BackgroundSyncService do not write trip data to Room; stubs only. periodStatistics in UiState is not bound to any view; only monthlyStatistics drives the visible stats row.
- **Back-end policy (Board-adopted):** No new persistence paths for trip data. Any new feature that "saves" trip-related data must go through the existing repository chain and respect End vs Clear. When OfflineDataManager/SyncWorker are implemented, they must not bypass Clear semantics or create a second source for monthly stats or calendar. Period boundaries stay in UnifiedTripService + PeriodCalculationService; document changes in this doc.

---

## Conventions and ownership (quick reference)

- **PreferencesManager** (`OutOfRouteBuddyPreferences`): Period mode, last loaded/bounce miles, trip active flag.
- **SettingsManager** (`app_settings`): Theme, distance units, GPS settings, etc. Theme must be read from here so Settings UI reflects saved value.
- **Build/Config:** `BuildConfig` / `ValidationConfig` in `core/config`; UPPER_SNAKE_CASE for constants.

| Layer / concern | Primary files / types |
|-----------------|------------------------|
| Trip persistence (End/Clear, Room) | TripInputViewModel, TripRepository (domain + data), DomainTripRepositoryAdapter, TripDao, AppDatabase |
| Recovery | TripCrashRecoveryManager, TripPersistenceManager, TripStateManager, OutOfRouteApplication, loadInitialData() |
| GPS / live miles | TripTrackingService, TripInputViewModel.observeGpsTrackingData |
| Period / calendar | PreferencesManager, UnifiedTripService, PeriodCalculationService, TripInputViewModel.updatePeriodStatistics, CustomCalendarDialog |
| Theme / settings | SettingsManager, OutOfRouteApplication.applyThemePreference, SettingsFragment, TripInputFragment (settings dialog) |
| History by date | TripHistoryByDateViewModel, getTripsByDateRange |
| Wiring reference | docs/technical/WIRING_MAP.md, docs/technical/TRIP_PERSISTENCE_END_CLEAR.md |

---

*Keep this doc in sync with TRIP_PERSISTENCE_END_CLEAR.md and WIRING_MAP.md when changing persistence or wiring.*

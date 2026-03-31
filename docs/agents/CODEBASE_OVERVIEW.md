# Codebase Overview — For Jarvey

**Purpose:** One-page summary so Jarvey can answer "how does X work?", "where does Y happen?", and "what does Z do?" from project facts. Distilled from WIRING_MAP.md and KNOWN_TRUTHS. Do not invent; cite this or the linked docs.

---

## App layers and key classes

- **presentation/** — UI: TripInputFragment (trip screen; toolbar **gear** → advanced Settings; **hamburger** → drawer: Trip, Rankings/history, Trip tracking prefs focus, statistics period, About & Help), TripHistoryByDateFragment, SettingsFragment, LudacrisSettingsFragment (Look & feel → **Ludacris settings**, optional live trip card metrics). ViewModels: TripInputViewModel (trip lifecycle, stats, period), TripHistoryByDateViewModel.
- **domain/** — Trip model, TripRepository interface. No Android deps.
- **data/** — TripRepository impl, TripDao, AppDatabase (Room), DomainTripRepositoryAdapter (maps domain ↔ data). TripPersistenceManager (save/load active trip state), TripStateManager (in-memory state), shared-pool export/archive services for additive outbound sharing.
- **services/** — TripTrackingService (FusedLocationProvider → live miles via `TripMetrics` StateFlow, plus optional **Ludacris** aggregates: TZ offset count, elevation min/max, max speed); TripCrashRecoveryManager; UnifiedLocationService, UnifiedTripService, UnifiedOfflineService.
- **workers/** — SyncWorker, WorkManagerInitializer (sync/deferred work).

---

## Main flows

- **Start trip:** ViewModel.calculateTrip → TripTrackingService.startService, save state to TripPersistenceManager, start crash auto-save.
- **End trip:** ViewModel.endTrip → tripRepository.insertTrip (Room), clearTripPersistence, refreshAggregateStatistics. Trip appears in monthly stats, calendar, history. Only End trip writes to the trip store.
- **Shared-pool export:** Human `GOLD` trips may be exported outward as additive bundles for the local shared pool; export does not replace Room as the source of truth.
- **Virtual fleet:** Synthetic fleet batches are generated outside Room and exported separately so they do not contaminate production trip history/statistics.
- **Clear trip:** ViewModel.clearTrip → no insert; clearTripPersistence, refresh stats. Trip is not saved.
- **Recovery on launch:** loadInitialData() → (1) OutOfRouteApplication.recoveredTripState else (2) TripPersistenceManager.loadSavedTripState() else (3) inactive. Recovery wins over persistence when both exist.
- **Monthly stats & calendar:** Room via getMonthlyTripStatistics(), getTripsByDateRange(). ViewModel builds datesWithTripsInPeriod; CustomCalendarDialog shows days with trips. Period mode (Standard vs Custom) from PreferencesManager.

---

## Where things live (quick)

| What | Where |
|------|--------|
| TripInputViewModel | app/.../presentation/viewmodel/TripInputViewModel.kt |
| TripRepository (domain) | app/.../domain/repository/TripRepository.kt |
| TripRepository (data), TripDao | app/.../data/repository/, data/dao/ |
| TripTrackingService, TripCrashRecoveryManager | app/.../services/ |
| TripPersistenceManager | app/.../data/TripPersistenceManager.kt |
| Jarvey (coordinator) | scripts/coordinator-email/ |

Full file list: project index in context. Full wiring: docs/technical/WIRING_MAP.md, docs/agents/KNOWN_TRUTHS_AND_SINGLE_SOURCE_OF_TRUTH.md. Kotlin style and best practices: docs/technical/KOTLIN_BEST_PRACTICES.md.

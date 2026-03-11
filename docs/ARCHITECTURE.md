# Architecture overview

**Purpose:** Short overview of app layers and where persistence/recovery live. Ref: PROJECT_AUDIT DOC1, DOC2.

---

## Layers

- **App / UI:** MVVM. Activities (MainActivity, SplashActivity) host fragments; ViewModels (TripInputViewModel, TripHistoryViewModel, etc.) hold state and call domain/data. Compose and XML layouts in `presentation/`.
- **Domain:** Repository interface ([TripRepository](../app/src/main/java/com/example/outofroutebuddy/domain/repository/TripRepository.kt)), use cases (e.g. CalculateTripOorUseCase), and domain models (Trip, TripStatistics, PeriodMode). No Android dependencies.
- **Data:** Room database ([AppDatabase](../app/src/main/java/com/example/outofroutebuddy/data/AppDatabase.kt), TripDao, TripEntity), DataStore/SharedPreferences (PreferencesManager, TripPersistenceManager), and the adapter that implements the domain repository ([DomainTripRepositoryAdapter](../app/src/main/java/com/example/outofroutebuddy/data/repository/DomainTripRepositoryAdapter.kt)).
- **Services:** Location and trip tracking (UnifiedTripService, TripTrackingService, TripStateManager), offline and sync (OfflineDataManager, SyncWorker), and recovery (TripCrashRecoveryManager, TripStatePersistence).

---

## Persistence and recovery

- **Trips:** Saved via domain `TripRepository.insertTrip()` → DomainTripRepositoryAdapter → data TripRepository → Room. Trip state (draft/active) is persisted by TripPersistenceManager (SharedPreferences) and restored on launch; TripStatePersistence coordinates with TripStateManager and TripPersistenceManager for auto-save and recovery.
- **Preferences:** PreferencesManager (period mode, onboarding, last loaded/bounce miles).
- **Offline:** OfflineDataManager (DataStore) for offline trip and analytics storage; SyncWorker performs sync when backend is available.

---

## Configuration and constants

- **Drive-detect thresholds:** [ValidationConfig](../app/src/main/java/com/example/outofroutebuddy/validation/ValidationConfig.kt) (e.g. walking speed MPH, min duration, highway context).
- **Period mode defaults:** [PreferencesManager](../app/src/main/java/com/example/outofroutebuddy/data/PreferencesManager.kt) (STANDARD if unset or invalid).
- **Notification channel:** [TripTrackingService](../app/src/main/java/com/example/outofroutebuddy/services/TripTrackingService.kt) and BuildConfig.NOTIFICATION_CHANNEL_ID.
- **Notification features (tiny icon, pull-down, bubble):** See [NOTIFICATION_FEATURES.md](NOTIFICATION_FEATURES.md).

---

*Keep this doc minimal; update only when structure meaningfully changes. See [technical/](technical/) for detailed wiring and ADRs.*

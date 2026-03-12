# Redundant, Dead, and Unneeded Code Report

Generated from manual search and Detekt static analysis.

---

## 1. Unused / redundant constants

### BuildConfig (`core/config/BuildConfig.kt`)

- **`ACTION_START_TRIP`**, **`ACTION_END_TRIP`**  
  **Resolved (2025-03-11).** TripTrackingService now uses BuildConfig for these; string values aligned. See core/config/README.md and TripTrackingService.kt.

- **`NOTIFICATION_CHANNEL_ID`** (`"trip_tracking_channel"`)  
  Not used by any Kotlin code. `TripTrackingService` uses a private constant `"TripTrackingChannel"`. Only referenced in README.  
  **Suggestion:** Remove from BuildConfig and update README, or have TripTrackingService use BuildConfig and a single channel ID.

- **Other BuildConfig constants**  
  Many (e.g. `DEFAULT_ANIMATION_DURATION`, `TOAST_DURATION_*`, `DEBUG_MODE`, `PERFORMANCE_MONITORING`, `CRASH_REPORTING_ENABLED`, `ANALYTICS_ENABLED`, feature flags, test timeouts) are only referenced in README, not in app code.  
  **Suggestion:** Either start using them where appropriate or mark/document as “documentation / future use” and trim over time.

---

## 2. Unused private members (Detekt: UnusedPrivateMember / UnusedPrivateProperty)

| File | Item | Note |
|------|------|------|
| **OfflineDataManager** | `networkStateManager`, `preferencesManager` | Injected but never used |
| **OfflineDataManager** | Parameter `_errorMessage` | Unused callback parameter |
| **TripStatePersistence** | `createTempTripFromState` | Private function never called |
| **MainActivity** | `setupCrashlyticsTestTriggers` | Private function never called |
| **MainActivity** | `navigateToTripHistory`, `navigateToStatistics` | Private functions never called |
| **MainActivity** | `KEY_LAST_SCREEN`, `radioGroup` | Private property never used |
| **MainActivity** | Parameter `viewModel` (e.g. in lambda) | Unused parameter |
| **Trip.kt** (models) | `MIN_ACTUAL_MILES` | Private constant never used |
| **CustomCalendarDialog** | `normalizeToStartOfDay`, `isSelectionLocked` | Private function/property never used |
| **TripRecoveryDialog** | `TAG` | Private constant never used |
| **SettingsFragment** | `tripInputViewModel` | Injected but never used |
| **TripInputFragment** | `showTripCalculationDialog`, `showSingleDatePicker`, `showRangeDatePicker`, `startOfDay`, `endOfDay`, `getFirstAndLastDayOfMonth`, `showHistoryDatePickerForCurrentPeriod` | Private functions never called (likely legacy/refactor leftovers) |
| **TripInputViewModel** | `refreshSelectedPeriod`, `getUserFriendlyValidationMessage` | Private functions never called |
| **TripInputViewModel** | `tripStatePersistence`, `backgroundSyncService`, `optimizedGpsDataFlow`, `validationFramework` | Injected but never used |
| **GpsSynchronizationService** | `ARRIVAL_ESTIMATION_WINDOW_MINUTES` | Private constant never used |
| **HealthCheckManager** | `context` | Private property never used |
| **LocationServiceCoordinator** | `context` | Private property never used |
| **OfflineServiceCoordinator** | `context`, `preferencesManager`, `simpleOfflineService`, `standaloneOfflineService` | All constructor params stored but never used |
| **OfflineSyncCoordinator** | `offlineDataManager`, `tripRepository` | Private properties never used |
| **OfflineSyncService** | `context`, `preferencesManager`, `mergedData` | Private properties never used |
| **OfflineSyncService** | Parameters `_trip`, `_analytic` (e.g. in callbacks) | Unused parameters |
| **OptimizedGpsDataFlow** | `processGpsBatch`, `BATCH_TIMEOUT_MS`, `MIN_ACCURACY_METERS` | Private function/constants never used |
| **PeriodCalculationService** | `logger` | Private property never used |
| **SimpleOfflineService** | `KEY_OFFLINE_TRIPS` | Private constant never used |
| **SimpleOfflineService** | Parameters `trips`, `analytics` (in some function) | Unused parameters |
| **TripPersistenceManager** | `preferencesManager` | Injected but never used |
| **DriveStateClassifier** | Parameter `lastLocation` | Unused parameter |
| **LocationValidationService** | Parameters `_baseAccuracy`, `_trafficMode`, `_pattern` | Unused (underscore-prefixed) |

---

## 3. Empty / no-op code

- **TripInputFragment** (lines 918, 982, 991): `override fun writeToParcel(dest: ..., flags: Int) {}`  
  Required by `Parcelable`; empty body is intentional. Detekt reports `EmptyFunctionBlock`; can be suppressed or left as-is.

- **UnifiedOfflineService** (saveOfflineTrips, saveOfflineAnalytics): Placeholder no-ops with comments. Intentional; optionally suppress or keep as documented placeholders.

---

## 4. Redundant indirection

- **LocationValidationService** companion object: Many constants (e.g. `DEFAULT_*`, `VEHICLE_*`, `MPS_TO_MPH`, etc.) only delegate to `ValidationConfig`. They are used by production and test code.  
  **Suggestion:** Over time, migrate call sites to use `ValidationConfig` directly and remove the companion re-exports (comment in code says “will be removed in future versions”).

---

## 5. References to missing doc

- **TripDetailsFragment**, **SyncWorker**: Reference `docs/CRUCIAL_IMPROVEMENTS_TODO.md`. **Verified:** File exists. No action needed.

---

## 6. Detekt summary (high level)

- **UnusedPrivateMember / UnusedPrivateProperty / UnusedParameter:** See table in §2.
- **EmptyFunctionBlock:** See §3.
- **WildcardImport:** Several files use `kotlinx.coroutines.*` (and similar); consider replacing with explicit imports if project style requires it.
- **TooManyFunctions / LargeClass / LongMethod / LongParameterList:** Many classes/functions exceed Detekt thresholds; refactors would be incremental and not “dead code” per se.
- **TooGenericExceptionCaught:** Widespread; improve by catching more specific exception types where possible.

---

## Refactor Priority Quadrant (Data-Driven)

Prioritize refactors by **change frequency** and **quality**:

| | Low quality (many issues) | High quality |
|--|---------------------------|--------------|
| **High change frequency** | **Highest priority** — fix first | Medium priority |
| **Low change frequency** | Medium priority | Lowest priority |

**Rule:** High-change + poor-quality modules = highest ROI. When Code Quality is focus, consider fixing one test or refactoring one item from high-change areas first. See [LOOP_TIERING.md](docs/automation/LOOP_TIERING.md) § Code structure review.

---

## Recommended next steps

1. **Low risk:** Remove or document unused BuildConfig service constants and update README so they’re not misleading.
2. **Medium risk (per file):** Remove unused private functions and constants (e.g. `createTempTripFromState`, `KEY_LAST_SCREEN`, `MIN_ACTUAL_MILES`, date picker helpers in TripInputFragment) after confirming nothing calls them (including tests/search).
3. **Higher risk:** Unused constructor/injected parameters (e.g. OfflineServiceCoordinator, TripInputViewModel): confirm they aren’t required for DI or future use before removing; consider marking with a comment or opening a follow-up task.
4. **Optional:** Add `@Suppress("EmptyFunctionBlock")` for Parcelable `writeToParcel` stubs and for intentional placeholder bodies, or leave as-is and accept the Detekt findings.

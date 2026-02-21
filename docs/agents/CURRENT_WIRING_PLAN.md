# Current Wiring — Full Agent Consultation Plan

**Created:** 2026-02-20  
**Coordinator:** Master Branch Coordinator  
**Purpose:** Map all current wiring across the OutOfRouteBuddy app (trip input, ViewModel, data layer, persistence, services, calendar, settings, navigation) and produce an extensive plan for consistency, gaps, and next steps. All agents consulted via parallel exploration.

---

## 1. Executive summary (Coordinator)

- **Scope:** This plan documents end-to-end wiring from UI to Room: TripInputFragment ↔ TripInputViewModel ↔ TripRepository / TripTrackingService / TripPersistenceManager / Unified* services, plus calendar, history-by-date, settings, and navigation.
- **Findings:** (1) Trip input and monthly/period stats are wired; End trip saves to Room and refreshes stats; Clear trip does not persist. (2) Two GPS paths exist: TripTrackingService (FusedLocationProvider, drives live miles) and UnifiedLocationService (separate flow). (3) Two recovery sources: TripCrashRecoveryManager (30s auto-save, recovered on next launch) and TripPersistenceManager (full trip state; recovery dialog). (4) periodStatistics is in state but not bound to any view; only monthlyStatistics drives the visible stats row. (5) OfflineDataManager load/save is stubbed; SyncWorker/BackgroundSyncService do not currently write trip data. (6) Nav graph has a single destination (TripInputFragment); SettingsFragment exists but is not in the graph.
- **Recommended focus:** Unify or document dual GPS/recovery paths; surface or remove unused state (periodStatistics); implement or formally defer OfflineDataManager persistence; add nav destinations for history/settings if product requires; run full wiring regression (QA).

---

## 2. Trip input & ViewModel wiring (Front-end / explored)

**Layout & fragment**
- **Files:** `app/src/main/res/layout/fragment_trip_input.xml`, `TripInputFragment.kt`, `statistics_row.xml` (included as `monthly_stats`).
- **Bound views:** `loaded_miles_input`, `bounce_miles_input`; `start_trip_button`, `pause_button`, `statistics_button`, `statistics_calendar_button`; `custom_toolbar_layout.settingsButton`; `total_miles_output`, `oor_miles_output`, `oor_percentage_output`; `statistics_content`, `selected_period_value`, `days_with_trips_label` / `days_with_trips_container_wrapper` / `days_with_trips_container`, `monthly_stats` (StatisticsRowBinding).
- **Click handlers:** Start → if active `showEndTripConfirmation()` else validate + `viewModel.calculateTrip(...)`; Statistics → toggle expand; Calendar → `showCalendarPicker()`; Settings → `showSettingsDialog()`; Pause → `viewModel.pauseTrip()` / `resumeTrip()`. End Trip dialog: End → `viewModel.endTrip()`; Clear → `showClearTripConfirmation()` → `viewModel.clearTrip()`; Continue → dismiss.

**State observed**
- **uiState:** `isTripActive`, `isPaused` → button text, pause visibility/icon, input enabled; `loadedMiles`, `bounceMiles`, `actualMiles`, `oorMiles`, `oorPercentage` → Today’s info; `monthlyStatistics` → `updateStatisticsRow(binding.monthlyStats, ...)`; `selectedPeriodLabel` → `selected_period_value`; `datesWithTripsInPeriod` → chips in `days_with_trips_container`, click → `showTripHistoryForDate(date)`.
- **events:** Collected and passed to `handleEvent()`; currently no UI in handler (events reflected via state).

**ViewModel flows & dependencies**
- **State:** `_uiState` / `uiState` (StateFlow&lt;TripInputUiState&gt;); `_events` / `events` (SharedFlow&lt;TripEvent&gt;); `currentTrip: Trip?`.
- **Init:** `loadInitialData()`, `observeTripState()`, `observeLocationData()`, `observeGpsTrackingData()`.
- **Load:** `loadInitialData()` → crash recovery or `tripPersistenceManager.loadSavedTripState()`; then `refreshAggregateStatistics()` (monthly), `initializeSelectedPeriodFromPreferences()` → `updatePeriodStatistics(..., emitEvent = false)`.
- **User actions:** `calculateTrip` → UnifiedTripService.calculateTrip, set currentTrip, TripTrackingService.startService, startAutoSave, saveTripStateForPersistence, preferencesManager.saveLast*; `endTrip` → OOR calc, TripTrackingService.stopService, insertTrip, clearTripPersistence, refreshAggregateStatistics + refreshSelectedPeriod; `clearTrip` → stop service, clear persistence, reset state, refresh stats; `pauseTrip` / `resumeTrip` → TripTrackingService.pause/resume; `onCalendarPeriodSelected` / `savePeriodMode` → updatePeriodStatistics.
- **Observers:** tripStateManager.tripState → updateUiWithTripState; unifiedLocationService.realTimeGpsData → actualMiles/gpsQuality; TripTrackingService.tripMetrics → actualMiles, updateTripProgress, saveTripStateForPersistence.
- **Dependencies used:** tripRepository (getMonthlyTripStatistics, getTripStatistics, getTripsByDateRange, insertTrip); TripTrackingService (start/stop/pause/resume, tripMetrics); tripPersistenceManager (load/save/clear); tripStateManager (tripState, startTrip sync); preferencesManager (period mode, last loaded/bounce); unifiedTripService (period dates, calculateTrip); unifiedLocationService (realTimeGpsData); unifiedOfflineService (saveDataWithOfflineFallback); crashRecoveryManager (start/stop auto-save, clearRecoveryData). **stateCache, backgroundSyncService, optimizedGpsDataFlow** are injected but not referenced in these flows.

**Decision (periodStatistics):** `periodStatistics` remains in TripInputUiState for possible future use (e.g. a “Period totals” row or export). It is **not** bound to any view today; only `monthlyStatistics` drives the visible statistics row. No UI change required unless product adds a period-level summary.

---

## 3. Data layer & persistence wiring (Back-end / explored)

**Repository chain**
- **Domain:** `domain/repository/TripRepository.kt` — getAllTrips, getTripById, getTripsByDateRange, getTripStatistics, getMonthlyTripStatistics, insertTrip, updateTrip, deleteTrip, etc.
- **Adapter:** `DomainTripRepositoryAdapter.kt` — implements domain interface; maps domain Trip ↔ data Trip; delegates to data TripRepository.
- **Data:** `data/repository/TripRepository.kt` — takes TripDao; insertTrip(Trip, gpsMetadata?), getTripsForDateRange, getAllTrips, getTripById, etc.
- **DI:** RepositoryModule provides data repo and domain adapter; DatabaseModule provides AppDatabase and TripDao.

**Room**
- **AppDatabase:** entities TripEntity, version 2, migration for GPS columns.
- **TripDao:** insertTrip(TripEntity), getTripsForDateRange(startDate, endDate), getAllTrips, getTripById, getTripsFromDate, getTripsForDate, updateTrip, deleteTrip, deleteAllTrips, getTripCount, getTripCountForDate.
- **TripEntity:** id, date, loadedMiles, bounceMiles, actualMiles, oorMiles, oorPercentage, createdAt, GPS fields, tripStartTime, tripEndTime, lastLocation*, etc.
- **Monthly stats:** Implemented in DomainTripRepositoryAdapter: getMonthlyTripStatistics() = getTripStatistics(startOfMonth, endOfMonth) using getTripsByDateRange + in-memory aggregation.

**Trip state persistence**
- **TripStateManager:** tripState StateFlow, startTrip, endTrip, updateLocation, restoreTripState; persists active/last loaded/bounce to PreferencesManager.
- **TripPersistenceManager:** SharedPreferences (trip_persistence); saveActiveTripState, updateTripProgress, loadSavedTripState(), clearSavedTripState(); 24h recovery timeout.
- **TripStatePersistence:** Bridges TripStateManager.TripState ↔ TripPersistenceManager (save/load); no direct DB write for in-progress state.
- **When saved:** ViewModel saveTripStateForPersistence() → TripPersistenceManager (and updateTripProgress on GPS). TripStatePersistence.saveTripState() also writes via TripPersistenceManager.
- **When cleared:** endTrip(), clearTrip(), startNewTrip() → clearTripPersistence() → tripPersistenceManager.clearSavedTripState(). Recovery expired → clear in loadSavedTripState.
- **End vs Clear:** End = insert to Room + clear persistence; Clear = no insert + clear persistence.

**OfflineDataManager**
- **File:** `data/OfflineDataManager.kt`. In-memory OfflineStorage (trips, analytics); sync status. loadOfflineStorage() and saveOfflineStorage() are stubs (TODO); data lost on process death. Used by OfflineSyncService, OfflineSyncCoordinator.

---

## 4. Services & GPS / crash recovery wiring (Back-end & DevOps / explored)

**TripTrackingService**
- **Start/stop:** ViewModel calculateTrip() and continueRecoveredTrip() → startService(application, loadedMiles, bounceMiles [, initialTotalMiles]); endTrip() and clearTrip() → stopService(application); pauseTrip() / resumeTrip() → pauseService / resumeService.
- **Exposes:** tripMetrics (StateFlow&lt;TripMetrics&gt; — totalMiles, oorMiles); serviceState; gpsTrackingData. ViewModel observeGpsTrackingData() updates actualMiles and calls updateTripProgress / saveTripStateForPersistence.
- **Implementation:** Uses FusedLocationProviderClient directly (not UnifiedLocationService). GPS → TripTrackingService → tripMetrics → ViewModel.

**Unified* services**
- **UnifiedLocationService:** realTimeGpsData (ViewModel observeLocationData), getLocationStatistics(); separate path from TripTrackingService.
- **UnifiedTripService:** getCurrentPeriodDates, calculateTrip (OOR), getTripStatistics; listens to TripStateManager but does not start/stop TripTrackingService.
- **UnifiedOfflineService:** saveDataWithOfflineFallback (ViewModel on calculate and endTrip fallback), getOfflineStatistics(); placeholder getOfflineTrips/getOfflineAnalytics.

**Crash recovery**
- **TripCrashRecoveryManager:** KEY_APP_RUNNING / KEY_TRIP_STATE in SharedPreferences; initialize() returns RecoverableTripState?; startAutoSave(getTripState) every 30s; stopAutoSave(), clearRecoveryData().
- **OutOfRouteApplication:** recoveredTripState = crashRecoveryManager.initialize(); cleared after restore in ViewModel.
- **loadInitialData():** (1) If OutOfRouteApplication.recoveredTripState active → restore UI, startAutoSave, clearRecoveredState. (2) Else tripPersistenceManager.loadSavedTripState() → sync tripStateManager, restore currentTrip and UI, startAutoSave if active. (3) Else set inactive. Then refreshAggregateStatistics, initializeSelectedPeriodFromPreferences.
- **Two recovery sources:** Crash (TripCrashRecoveryManager → Application) and Persistence (TripPersistenceManager); MainActivity can show TripRecoveryDialog for persisted state.

**Background / workers**
- **BackgroundSyncService:** Android Service; periodic cache/state/GPS/integrity; ViewModel has it injected but no direct trip read/write.
- **SyncWorker / WorkManagerInitializer:** schedulePeriodicSync, scheduleCacheCleanup, scheduleDataIntegrityCheck; doWork() branches are stubs; no trip DB access.

---

## 5. Calendar, history-by-date, settings, navigation (UI/UX & Front-end / explored)

**Calendar**
- **Trigger:** statistics_calendar_button → showCalendarPicker(). Fragment passes periodMode, referenceDate, datesWithTrips from viewModel.uiState to CustomCalendarDialog.newInstance(..., datesWithTrips).
- **Callbacks:** onPeriodConfirmed → viewModel.onCalendarPeriodSelected(periodMode, startDate, endDate); onHistoryDateClicked → showTripHistoryForDate(date).
- **Decorators:** StartDateDecorator (green), EndDateDecorator (red), DaysWithTripsDecorator (blue DotSpan) using datesWithTripsMillis from ARG_DATES_WITH_TRIPS.
- **datesWithTripsInPeriod:** Built in updatePeriodStatistics from getTripsByDateRange → distinct start-of-day dates; exposed in state; fragment passes to dialog.

**Trip history by date**
- **Entry points:** Calendar non-boundary click; days-with-trips chips; legacy picker path; history date picker.
- **showTripHistoryForDate(date):** TripHistoryByDateDialog.newInstance(date) → show. Dialog uses TripHistoryByDateViewModel: loadTripsForDate(date) → repository.getTripsByDateRange(startOfDay, endOfDay) → filter/sort → _trips; adapter on RecyclerView; empty/loading state; onDeleteClick → viewModel.deleteTrip(trip).

**Settings**
- **Trigger:** customToolbarLayout.settingsButton → showSettingsDialog(). Dialog: Mode row → showModeSelectDialog(); Template row (period) → showTemplateSelectDialog() (viewModel.getCurrentPeriodMode(), savePeriodMode, getCurrentPeriodModeDisplayText()); Help & Info → showHelpInfoDialog() (DialogHelpInfoBinding, static XML including statistics_section_hint).
- **Period mode:** PreferencesManager KEY_PERIOD_MODE; savePeriodMode(PeriodMode), getPeriodMode(); ViewModel delegates; savePeriodMode also calls calculateCurrentPeriodStatistics().

**Navigation**
- **MainActivity:** NavHostFragment, nav_graph_main, startDestination tripInputFragment; state save/restore in savedInstanceState. navigateToTripInput/History/Statistics currently go to tripInputFragment (single destination).
- **Nav graph:** One fragment (TripInputFragment). SettingsFragment exists but is not in the graph; settings are a dialog from TripInputFragment.

---

## 6. Design / Creative Manager

- **Product wiring alignment:** Trip input → End trip = save & count in monthly stats; Clear trip = discard. Calendar and history show only saved trips; datesWithTrips and decorator reflect that. Help & Info carries the statistics hint. Design intent is reflected in current wiring.
- **Recommendation:** Keep a single source of truth for “what counts” (Room; End trip only). Any new screen (e.g. dedicated history or settings) should consume the same repository and period/preferences contracts.

---

## 7. UI/UX Specialist

- **Wiring for UX:** End/Clear dialogs and copy are wired; statistics section shows monthly stats and clickable days-with-trips; calendar shows period boundaries and days-with-trips dots; Help & Info holds the hint. Accessibility: ensure all new interactive elements (chips, calendar) have contentDescription where applicable.
- **Recommendation:** If periodStatistics is ever shown (e.g. “Period totals” separate from “Monthly”), wire it to a dedicated row or clarify in spec that only monthly is shown.

---

## 8. Back-end Engineer

- **Persistence wiring:** Documented in docs/technical/TRIP_PERSISTENCE_END_CLEAR.md. End = Room insert + clear persistence; Clear = no insert + clear persistence. getMonthlyTripStatistics and getTripsByDateRange read from Room; calendar and history use same source.
- **OfflineDataManager:** loadOfflineStorage/saveOfflineStorage stubbed; see docs/technical/OFFLINE_PERSISTENCE.md. Implement when offline-across-restart is in scope; otherwise keep documented as deferred.
- **Dual recovery:** TripCrashRecoveryManager (30s snapshot) and TripPersistenceManager (full state, 24h). Both feed loadInitialData; document precedence (crash first, then persistence) and ensure no double-prompt.

---

## 9. DevOps Engineer

- **Build:** No wiring change required for current plan. If Room schema or migrations change, version bump and test migrations.
- **Observability:** Optional non-PII logging for “trip saved” vs “trip cleared” for quality metrics. Ensure no coordinates or user identifiers in logs.

---

## 10. QA Engineer

- **Wiring regression:** (1) End trip → one row in DB, monthly stats and calendar/history update; (2) Clear trip → no new row, stats unchanged; (3) Persistence: end trip, kill app, reopen → trip in history and stats; (4) Dialog copy and Help & Info hint. Unit tests: TripStatisticsWiringTest (endTrip, clearTrip, refresh), TripInputFragmentBehaviorRobolectricTest (dialogs, chips). Run full unit test suite and fix any unrelated failures.
- **Recommendation:** Add instrumentation or UI test for “expand statistics → see days with trips → tap day → history dialog” and “calendar → tap day with dot → history dialog.”

---

## 11. Security Specialist

- **Wiring and data:** Trip data stays in Room (app-private). TripPersistenceManager and TripCrashRecoveryManager use SharedPreferences; no trip data in logs. Per docs/security/SECURITY_NOTES.md: no PII in logs; structured metadata; Clear trip avoids persisting bad data.
- **Recommendation:** Audit any new wiring (e.g. OfflineDataManager persistence) for same rules; no credentials in trip records.

---

## 12. File Organizer

- **Docs:** This plan lives at docs/agents/CURRENT_WIRING_PLAN.md. Related: docs/technical/TRIP_PERSISTENCE_END_CLEAR.md, docs/technical/OFFLINE_PERSISTENCE.md, docs/technical/GPS_AND_LOCATION_WIRING.md, docs/technical/RECOVERY_WIRING.md, docs/technical/WIRING_MAP.md, docs/ux/END_TRIP_FLOW_UX.md, docs/agents/MONTHLY_STATS_PERSISTENCE_CALENDAR_PLAN.md, docs/agents/DARK_MODE_AND_BUTTON_VISIBILITY_PLAN.md, docs/product/FEATURE_BRIEF_monthly_stats_and_persistence.md.
- **Wiring map:** See docs/technical/WIRING_MAP.md (one-page diagram).

---

## 13. Human-in-the-Loop Manager

- **Handoff to user:** This plan is the reference for “how everything is wired.” Use it before refactors (e.g. splitting ViewModel, adding nav destinations, or implementing OfflineDataManager persistence) to avoid breaking End/Clear semantics, recovery, or calendar/history consistency.
- **Next steps:** Prioritize (1) QA wiring regression and optional instrumentation tests; (2) document or unify dual GPS/recovery if team finds it confusing; (3) decide whether to bind periodStatistics or remove it from state; (4) implement or formally defer OfflineDataManager load/save.

---

## 14. Implementation checklist (wiring-focused)

| # | Task | Owner | Notes |
|---|------|--------|-------|
| 1 | QA: Run full unit test suite; fix failures | QA | Done: clearTrip test assertion fixed (“not be saved”); 848 tests, 1 skipped |
| 2 | QA: Manual/instrumentation: End/Clear, persistence, calendar days, history dialog | QA | Per test plan above |
| 3 | Document dual GPS path (TripTrackingService vs UnifiedLocationService) | Back-end / Front-end | Done: docs/technical/GPS_AND_LOCATION_WIRING.md |
| 4 | Document dual recovery (CrashRecoveryManager vs TripPersistenceManager) precedence | Back-end | Done: docs/technical/RECOVERY_WIRING.md |
| 5 | Decide: bind periodStatistics to UI or remove from state | Front-end / Design | Done: keep in state for future use; no UI binding |
| 6 | Implement or defer OfflineDataManager load/save | Back-end | Done: formally deferred; see OFFLINE_PERSISTENCE.md |
| 7 | Optional: Add nav destinations for History / Settings screens | Front-end / Design | Deferred |
| 8 | Optional: One-page wiring diagram in docs/technical/ | File Organizer | Done: docs/technical/WIRING_MAP.md |

---

*When executing wiring changes, run the QA checklist and keep TRIP_PERSISTENCE_END_CLEAR.md and this plan in sync.*

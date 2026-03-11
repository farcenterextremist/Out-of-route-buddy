# Changelog

## [Unreleased] - Iron Out Release

### Fixed
- C1: `getTripsByStatus` now uses `.first()` to avoid hanging
- C2: `TripHistoryViewModel.loadTrips()` sets loading false on first emission
- C3: Error, CalculationError, SaveError events show Snackbar to user
- C4: Delete failure surfaced via `deleteError` SharedFlow
- W1: TripHistoryByDateDialog refreshes on `onResume`
- B2: PerformanceTracker uses `synchronized` instead of runBlocking
- R4: StateCache removed from TripInputViewModel
- L3: `deleteTrip` validates ID before delete (returns false for invalid)
- CustomCalendarDialog: Fixed `monthValue` → `month` for CalendarDay

### Changed
- DB2: Room `exportSchema = true` with schema directory
- CFG1: Java version aligned to 17
- R2: Removed no-op `autoSaveTripState` from TripStatePersistence
- Added verification tests for DomainTripRepositoryAdapter and TripHistoryViewModel

### Added
- Root README with build instructions
- DB1 comment for fallbackToDestructiveMigration
- S1 TODO for SyncWorker full sync implementation
- S2 documentation for UnifiedTripService cleanup

### Documentation
- Updated PROJECT_AUDIT_2025_02_27.md with verified status
- QUALITY_AND_ROBUSTNESS_PLAN items marked complete where applicable
- **Polishing Plan:** Logging policy (docs/technical/LOGGING_POLICY.md); README quick-check vs full verification; KDoc for TripRepository, DomainTripRepositoryAdapter, TripInputViewModel, TripStatePersistence, OfflineDataManager; ARCHITECTURE.md; terminology and UI consistency docs (docs/ux/TERMINOLOGY_AND_COPY.md, docs/ux/UI_CONSISTENCY.md); L1 PreferencesManager fallback logging; lifecycle/state logs downgraded to Log.v in MainActivity and TripInputViewModel; accessibility content description for stat card delete button

### Release readiness
- Google Play first-release packet added in `docs/release/GOOGLE_PLAY_FIRST_RELEASE_PACKET.md`
- First-release go/no-go checklist added in `docs/release/FIRST_RELEASE_GO_NO_GO.md`
- DEPLOYMENT.md aligned with the real Gradle release config and Android Studio `.aab` signing path
- Release build blocker fixed by replacing an invalid drawable asset that could not compile in AAPT
- Hot-path logging redacted to avoid raw coordinates and completed-trip database IDs in reviewed release paths
- OfflineDataManager startup/save persistence was hardened to avoid async load/save races, and its Robolectric persistence regression test now runs without `@Ignore`
- Trip service recovery state now uses synchronous SharedPreferences commits so start/end recovery markers are less likely to be lost during abrupt process changes

### Known limitations
- Instrumented tests and manual device validation are still required before Play upload because no connected device/emulator was available during this repo-side verification pass
- Screenshot coverage remains deferred until Paparazzi is configured
- Some recovery/application flow coverage is still deferred to instrumented testing because Robolectric does not fully reproduce service/app lifecycle behavior

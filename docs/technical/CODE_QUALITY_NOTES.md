# Code Quality Notes

Notes from Phase 4a code quality cleanup (Project Health Improvement Plan).

## Linting

- **abortOnError:** `false` in `app/build.gradle.kts`
- **Lint report:** Run `./gradlew lint`; report at `app/build/reports/lint-results*.html`
- **Rationale:** Allows builds to succeed while addressing lint issues incrementally. Set to `true` when all lint issues are resolved for stricter CI (e.g. `./gradlew lint` must pass).
- **Disabled checks:** ObsoleteLintCustomCheck, GradleDependency, NewerVersionAvailable

## util vs utils Package

- **util/:** Core utilities (PerformanceTracker, InputValidator, TripExporter, etc.)
- **utils/:** Extensions (e.g. DateExtensions). DistanceFormatter was removed; optional future consolidation into `util/` to avoid breaking changes in one pass.

## Legacy Naming

| Location | Notes |
|----------|-------|
| `LocationValidationService` | `DEFAULT_*` constants delegate to `ValidationConfig`; kept for backward compatibility. Can be removed when all call sites use `ValidationConfig` directly. |
| `core/config/README.md` | Documents legacy constants that reference centralized configs. |

## Dead Code / No-Ops

| Location | Notes |
|----------|-------|
| `TripStatePersistence.saveCompletedTrip()` | Unused; `TripInputViewModel.endTrip()` uses `TripRepository.insertTrip(trip)` instead. Retain if GPS-metadata path is needed later, or remove. |
| `TripStatePersistence.autoSaveTripState()` | **Removed (R2).** Was no-op; `TripPersistenceManager` handles auto-save via `saveTripState()`. |
| **OfflineDataManager.saveOfflineStorage()** | Save failure is log-only; no callback or UI. Retry effectively on next init. Optional: add Flow/callback for save failure and wire in sync status UI (Weakest Areas plan Phase 2.4). |

## Detekt

- **Status:** Integrated (Phase 1.2 of Code Tidy plan). Run `./gradlew detekt`.
- **Config:** `app/config/detekt/detekt.yml`; `maxIssues: 500` to allow incremental cleanup.
- **Findings:** TooManyFunctions, LongMethod, LongParameterList, NestedBlockDepth, TooGenericExceptionCaught, EmptyFunctionBlock (Parcelable stubs). TrafficAnalyticsTest.kt: placeholder added. See detekt report for full list.

## Future Cleanup

- Run `./gradlew lint` and address remaining issues before setting `abortOnError = true`.
- **Logging policy (L1, Weakest Areas Phase 4.2):** In release, strip or gate verbose `android.util.Log` (e.g. guard with `BuildConfig.DEBUG` or log level). **Implemented as pattern:** MainActivity, OfflineDataManager, and TripPersistenceManager use a `debugLog(msg)` helper that only calls `Log.d` when `BuildConfig.DEBUG` is true. Apply the same pattern in other heavy Log users as follow-up. See QUALITY_AND_ROBUSTNESS_PLAN L1. **PII:** Do not log coordinates, trip IDs that could be linked to a user, or other PII; see docs/security/SECURITY_NOTES.md §2.
- Migrate call sites from `LocationValidationService.DEFAULT_*` to `ValidationConfig` and remove legacy constants.
- Decide on `saveCompletedTrip` retention vs removal.
- Reduce detekt `maxIssues` as findings are addressed.

# Code Quality Notes

Notes from Phase 4a code quality cleanup (Project Health Improvement Plan).

## Linting

- **abortOnError:** `false` in `app/build.gradle.kts`
- **Lint report:** Run `./gradlew lint`; report at `app/build/reports/lint-results*.html`
- **Rationale:** Allows builds to succeed while addressing lint issues incrementally. Set to `true` when all lint issues are resolved for stricter CI (e.g. `./gradlew lint` must pass).
- **Disabled checks:** ObsoleteLintCustomCheck, GradleDependency, NewerVersionAvailable

## util vs utils Package

- **util/:** Core utilities (PerformanceTracker, InputValidator, TripExporter, etc.)
- **utils/:** Formatters and extensions (DistanceFormatter, DateExtensions)
- **Action:** Documented; optional future consolidation into `util/` to avoid breaking changes in one pass.

## Legacy Naming

| Location | Notes |
|----------|-------|
| `LocationValidationService` | `DEFAULT_*` constants delegate to `ValidationConfig`; kept for backward compatibility. Can be removed when all call sites use `ValidationConfig` directly. |
| `core/config/README.md` | Documents legacy constants that reference centralized configs. |

## Dead Code / No-Ops

| Location | Notes |
|----------|-------|
| `TripStatePersistence.saveCompletedTrip()` | Unused; `TripInputViewModel.endTrip()` uses `TripRepository.insertTrip(trip)` instead. Retain if GPS-metadata path is needed later, or remove. |
| `TripStatePersistence.autoSaveTripState()` | No-op; kept for API compatibility. `TripPersistenceManager` handles auto-save. |

## Detekt

- **Status:** Integrated (Phase 1.2 of Code Tidy plan). Run `./gradlew detekt`.
- **Config:** `app/config/detekt/detekt.yml`; `maxIssues: 500` to allow incremental cleanup.
- **Findings:** TooManyFunctions, LongMethod, LongParameterList, NestedBlockDepth, TooGenericExceptionCaught, EmptyFunctionBlock (Parcelable stubs). TrafficAnalyticsTest.kt: placeholder added. See detekt report for full list.

## Future Cleanup

- Run `./gradlew lint` and address remaining issues before setting `abortOnError = true`.
- Migrate call sites from `LocationValidationService.DEFAULT_*` to `ValidationConfig` and remove legacy constants.
- Decide on `saveCompletedTrip` retention vs removal.
- Reduce detekt `maxIssues` as findings are addressed.

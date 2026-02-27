# OutOfRouteBuddy – Attack surface summary (Purple exercise 2025-02-22)

Used by Red Team to scope targets for this exercise. Can be reused for future Purple runs.

## In scope (tested)

- **Trip export:** CSV and report generation and share via FileProvider. Entry: TripHistoryFragment → TripHistoryViewModel.exportTrips() / exportToPDF() → TripExporter → cache + share.
- **Trip delete:** TripHistoryFragment → TripHistoryViewModel.deleteTrip() → DomainTripRepositoryAdapter / TripRepository.deleteTrip().
- **FileProvider:** res/xml/file_paths.xml (cache-path, files-path, external-cache-path); TripExporter uses cacheDir + fixed filenames only.

## Not in scope this run

- Phishing/social engineering (Specialist role); location data exfiltration; backup/restore; sync workers; DAO/repository injection; deep link or intent handling.

## Key files

- ViewModel: `app/.../presentation/ui/history/TripHistoryViewModel.kt`
- Exporter: `app/.../util/TripExporter.kt`
- FileProvider: `app/src/main/res/xml/file_paths.xml`
- Repository: `app/.../data/repository/TripRepository.kt`, `DomainTripRepositoryAdapter.kt`

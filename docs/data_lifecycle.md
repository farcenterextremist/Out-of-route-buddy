# Data lifecycle: delete on device, keep on server

## Overview

The app supports a path where the user can **delete trip data on the device** (for space or privacy) while the app (or a future backend) **may keep a copy on the server** for analytics or training.

- **User action:** "Delete old data from device" or "Clear all trip data from device" in Settings → Data & privacy.
- **App flow:** Export the affected trips (via `TripArchiveService`), then delete them locally. Server-side retention and use for data training sets is the responsibility of the backend.
- **Current implementation:** `TripArchiveService` is a no-op (logs and returns success). Replace with a real upload when server-side retention is available.

## Flow

1. User taps "Delete old data from device" (trips older than 12 months) or "Clear all trip data from device".
2. App shows a confirmation dialog.
3. On confirm, the app:
   - Fetches the trips that will be deleted (by date range or all).
   - Calls `TripArchiveService.exportBeforeLocalDelete(trips)` so data can be sent to the server.
   - If export succeeds: deletes those trips locally (`deleteTripsOlderThan` or `clearAllTrips`).
   - If export fails: does not delete locally and shows an error.
4. A toast shows success or failure.

## Components

- **TripArchiveService** (domain): Interface for exporting trips before local delete. Implementations may log, upload to an API, or write to a file.
- **DefaultTripArchiveService** (data): No-op implementation that logs and returns success.
- **DataManagementViewModel**: Runs the export-then-delete flow; used by Settings.
- **TripRepository.deleteTripsOlderThan(cutoffDate)**: Deletes trips with date strictly before `cutoffDate`.

## Adding a real server upload

1. Implement `TripArchiveService` with an API call (e.g. POST trips to your backend).
2. Bind your implementation in the DI module (e.g. `RepositoryModule`) instead of `DefaultTripArchiveService`.
3. Ensure the server stores and uses data according to your policy (e.g. training, analytics).

## Verification

- In Settings → Data & privacy, tap "Delete old data from device" or "Clear all trip data from device", confirm the dialog, and check that a success toast appears and local data is removed.
- Check logcat for `TripArchiveService` / `exportBeforeLocalDelete` to confirm the export step runs.

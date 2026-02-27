package com.example.outofroutebuddy.domain.data

import com.example.outofroutebuddy.domain.models.Trip

/**
 * Service for exporting trip data before local delete, so the server can retain it for
 * analytics or training. The app guarantees "upload then delete locally" when the user
 * chooses "delete from device (keep on server)". Server-side retention and use for
 * data training sets is the responsibility of the backend.
 */
interface TripArchiveService {
    /**
     * Export the given trips (e.g. to server) before the app deletes them locally.
     * Implementations may log, upload to an API, or write to a file for manual upload.
     *
     * @param trips Trips that are about to be deleted from the device
     * @return Success if export completed (or was skipped); failure if export failed and local delete should not proceed
     */
    suspend fun exportBeforeLocalDelete(trips: List<Trip>): Result<Unit>
}

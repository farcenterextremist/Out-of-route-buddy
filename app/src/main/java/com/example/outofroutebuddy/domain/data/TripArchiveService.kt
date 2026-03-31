package com.example.outofroutebuddy.domain.data

import com.example.outofroutebuddy.domain.models.Trip

/**
 * Service for exporting trip data before local delete.
 * In the current local shared-pool phase, this writes additive export bundles for later
 * desktop import rather than replacing Room as the app's source of truth.
 */
interface TripArchiveService {
    /**
     * Export the given trips before the app deletes them locally.
     * Implementations may write to a local bundle, upload to an API, or no-op.
     *
     * @param trips Trips that are about to be deleted from the device
     * @return Success if export completed (or was skipped); failure if export failed and local delete should not proceed
     */
    suspend fun exportBeforeLocalDelete(trips: List<Trip>): Result<Unit>
}

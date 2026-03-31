package com.example.outofroutebuddy.domain.data

import com.example.outofroutebuddy.domain.models.Trip

/**
 * Exports additive trip bundles for the local shared pool.
 * This does not replace Room as the source of truth; it only writes outward-facing copies.
 */
interface TripSharedPoolExportService {
    suspend fun exportGoldTrips(
        trips: List<Trip>,
        reason: SharedPoolExportReason,
    ): Result<SharedPoolExportReceipt>
}

data class SharedPoolExportReceipt(
    val batchId: String,
    val filePath: String,
    val exportedTripCount: Int,
)

enum class SharedPoolExportReason {
    NEW_HUMAN_TRIP,
    UPDATED_HUMAN_TRIP,
    BEFORE_LOCAL_DELETE,
    BEFORE_LOCAL_CLEAR,
    MANUAL_RANGE_EXPORT,
}

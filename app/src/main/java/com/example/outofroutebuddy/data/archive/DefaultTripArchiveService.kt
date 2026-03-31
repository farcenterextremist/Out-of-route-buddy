package com.example.outofroutebuddy.data.archive

import android.util.Log
import com.example.outofroutebuddy.domain.data.TripArchiveService
import com.example.outofroutebuddy.domain.data.SharedPoolExportReason
import com.example.outofroutebuddy.domain.data.TripSharedPoolExportService
import com.example.outofroutebuddy.domain.models.Trip
import javax.inject.Inject

/**
 * Default implementation of [TripArchiveService] for the local shared-pool phase.
 * Exports additive GOLD bundles before local delete/clear operations proceed.
 */
class DefaultTripArchiveService @Inject constructor(
    private val sharedPoolExportService: TripSharedPoolExportService,
) : TripArchiveService {
    override suspend fun exportBeforeLocalDelete(trips: List<Trip>): Result<Unit> {
        return sharedPoolExportService.exportGoldTrips(
            trips = trips,
            reason = SharedPoolExportReason.BEFORE_LOCAL_DELETE,
        ).map { receipt ->
            Log.i(
                TAG,
                "exportBeforeLocalDelete: exported_trip_count=${receipt.exportedTripCount} file_written=${receipt.filePath.isNotBlank()}",
            )
            Unit
        }
    }

    companion object {
        private const val TAG = "TripArchiveService"
    }
}

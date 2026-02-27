package com.example.outofroutebuddy.data.archive

import android.util.Log
import com.example.outofroutebuddy.domain.data.TripArchiveService
import com.example.outofroutebuddy.domain.models.Trip
import javax.inject.Inject

/**
 * No-op implementation of [TripArchiveService]. Logs the export request and returns success.
 * Replace with a real implementation (e.g. API upload) when server-side retention is available.
 * Server is responsible for retaining and using data for training; this app only sends data before local delete.
 */
class DefaultTripArchiveService @Inject constructor() : TripArchiveService {
    override suspend fun exportBeforeLocalDelete(trips: List<Trip>): Result<Unit> {
        Log.i(TAG, "exportBeforeLocalDelete: trip_count=${trips.size} (no-op; data may be sent to server in future)")
        return Result.success(Unit)
    }

    companion object {
        private const val TAG = "TripArchiveService"
    }
}

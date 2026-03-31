package com.example.outofroutebuddy.data.archive

import android.content.Context
import android.util.Log
import com.example.outofroutebuddy.di.IoDispatcher
import com.example.outofroutebuddy.domain.data.SharedPoolExportReason
import com.example.outofroutebuddy.domain.data.SharedPoolExportReceipt
import com.example.outofroutebuddy.domain.data.TripSharedPoolExportService
import com.example.outofroutebuddy.domain.models.Trip
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes additive shared-pool bundles to app-local storage for desktop import.
 * This preserves Room as the trip source of truth while making GOLD data available outward.
 */
@Singleton
class LocalSharedPoolExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TripSharedPoolExportService {

    override suspend fun exportGoldTrips(
        trips: List<Trip>,
        reason: SharedPoolExportReason,
    ): Result<SharedPoolExportReceipt> =
        withContext(ioDispatcher) {
            runCatching {
                val bundle = SharedPoolTripExportBundleFactory.createHumanTripBundle(trips, reason)
                if (bundle.trips.isEmpty()) {
                    return@runCatching SharedPoolExportReceipt(
                        batchId = bundle.metadata.batchId,
                        filePath = "",
                        exportedTripCount = 0,
                    )
                }

                val exportDir = SharedPoolExportStorage.resolveExportDirectory(context)
                val exportFile = File(exportDir, "${bundle.metadata.batchId}.json")
                exportFile.writeText(Gson().toJson(bundle), Charsets.UTF_8)

                SharedPoolExportReceipt(
                    batchId = bundle.metadata.batchId,
                    filePath = exportFile.absolutePath,
                    exportedTripCount = bundle.trips.size,
                )
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to write shared-pool export bundle", error)
        }

    companion object {
        private const val TAG = "LocalSharedPoolExport"
    }
}

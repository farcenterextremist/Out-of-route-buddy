package com.example.outofroutebuddy.data.archive

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.outofroutebuddy.domain.data.SharedPoolExportReason
import com.example.outofroutebuddy.domain.models.DataTier
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class LocalSharedPoolExportServiceTest {

    @Test
    fun exportGoldTrips_writesBundleWithGoldTripsOnly() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val exportDir = context.getExternalFilesDir("shared_pool_exports") ?: File(context.filesDir, "shared_pool_exports")
        exportDir.deleteRecursively()

        val service = LocalSharedPoolExportService(
            context = context,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        val goldTrip = Trip(
            id = "101",
            loadedMiles = 120.0,
            bounceMiles = 15.0,
            actualMiles = 150.0,
            oorMiles = 15.0,
            oorPercentage = 11.11,
            startTime = Date(1_700_000_000_000),
            endTime = Date(1_700_000_360_000),
            status = TripStatus.COMPLETED,
            dataTier = DataTier.GOLD,
        )
        val syntheticTrip = Trip(
            id = "202",
            loadedMiles = 80.0,
            bounceMiles = 5.0,
            actualMiles = 92.0,
            oorMiles = 7.0,
            oorPercentage = 8.24,
            startTime = Date(1_700_100_000_000),
            endTime = Date(1_700_100_360_000),
            status = TripStatus.COMPLETED,
            dataTier = DataTier.PLATINUM,
        )

        val receipt = service.exportGoldTrips(
            trips = listOf(goldTrip, syntheticTrip),
            reason = SharedPoolExportReason.MANUAL_RANGE_EXPORT,
        ).getOrThrow()

        assertEquals(1, receipt.exportedTripCount)
        assertTrue(receipt.filePath.isNotBlank())

        val exportFile = File(receipt.filePath)
        assertTrue(exportFile.exists())

        val json = exportFile.readText()
        assertTrue(json.contains("\"sourceRecordId\":\"101\""))
        assertTrue(json.contains("\"dataTier\":\"GOLD\""))
        assertFalse(json.contains("\"sourceRecordId\":\"202\""))
        assertFalse(json.contains("\"dataTier\":\"PLATINUM\""))
    }
}

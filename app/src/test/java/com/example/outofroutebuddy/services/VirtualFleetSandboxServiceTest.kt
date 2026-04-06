package com.example.outofroutebuddy.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class VirtualFleetSandboxServiceTest {

    @Test
    fun generateBundle_marksAllTripsAsSyntheticTiers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = VirtualFleetSandboxService(
            context = context,
            ioDispatcher = StandardTestDispatcher(),
            fleetDataGenerator = VirtualFleetDataGenerator(),
        )

        val bundle = service.generateBundle(
            VirtualFleetGenerationRequest(
                managerCount = 1,
                driverCount = 3,
                tripsPerDriver = 2,
                seed = 99L,
                referenceDatasets = listOf("FMCSA_SAFER", "BTS_FAF"),
            ),
        )

        assertEquals(6, bundle.trips.size)
        assertTrue(bundle.trips.all { it.dataTier == "PLATINUM" || it.dataTier == "SILVER" })
        assertTrue(bundle.metadata.contaminationCheck.contains("Room trips untouched"))
    }

    @Test
    fun generateAndExport_writesVirtualFleetBundleFile() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val exportDir = context.getExternalFilesDir("shared_pool_exports") ?: File(context.filesDir, "shared_pool_exports")
        exportDir.deleteRecursively()

        val service = VirtualFleetSandboxService(
            context = context,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            fleetDataGenerator = VirtualFleetDataGenerator(),
        )

        val receipt = service.generateAndExport(
            VirtualFleetGenerationRequest(driverCount = 2, tripsPerDriver = 1, seed = 7L),
        ).getOrThrow()

        assertEquals(2, receipt.tripCount)
        assertTrue(receipt.filePath.isNotBlank())
        assertTrue(File(receipt.filePath).exists())
    }
}

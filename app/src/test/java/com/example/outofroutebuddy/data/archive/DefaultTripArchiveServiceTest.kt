package com.example.outofroutebuddy.data.archive

import com.example.outofroutebuddy.domain.data.SharedPoolExportReason
import com.example.outofroutebuddy.domain.data.SharedPoolExportReceipt
import com.example.outofroutebuddy.domain.data.TripSharedPoolExportService
import com.example.outofroutebuddy.domain.models.Trip
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for [DefaultTripArchiveService] (no-op implementation).
 * Verifies it returns success and does not throw.
 */
class DefaultTripArchiveServiceTest {

    private lateinit var service: DefaultTripArchiveService

    @Before
    fun setUp() {
        service = DefaultTripArchiveService(
            object : TripSharedPoolExportService {
                override suspend fun exportGoldTrips(
                    trips: List<Trip>,
                    reason: SharedPoolExportReason,
                ): Result<SharedPoolExportReceipt> =
                    Result.success(
                        SharedPoolExportReceipt(
                            batchId = "test-batch",
                            filePath = "",
                            exportedTripCount = trips.size,
                        ),
                    )
            },
        )
    }

    @Test
    fun exportBeforeLocalDelete_emptyList_returnsSuccess() = runTest {
        val result = service.exportBeforeLocalDelete(emptyList())
        assertTrue(result.isSuccess)
    }

    @Test
    fun exportBeforeLocalDelete_nonEmptyList_returnsSuccess() = runTest {
        val trips = listOf(
            Trip(
                id = "1",
                loadedMiles = 10.0,
                bounceMiles = 2.0,
                actualMiles = 12.0,
                startTime = Date(),
                status = com.example.outofroutebuddy.domain.models.TripStatus.COMPLETED,
            ),
        )
        val result = service.exportBeforeLocalDelete(trips)
        assertTrue(result.isSuccess)
    }
}

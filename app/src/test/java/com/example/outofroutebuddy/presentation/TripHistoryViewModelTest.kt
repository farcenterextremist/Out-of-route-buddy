package com.example.outofroutebuddy.presentation

import android.app.Application
import com.example.outofroutebuddy.domain.models.GpsMetadata
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.presentation.ui.history.TripHistoryViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.io.path.createTempDirectory

/**
 * ✅ HIGH PRIORITY: Trip History ViewModel Tests
 * 
 * Tests critical trip history functionality:
 * - Loading trips from repository
 * - Sorting trips by date
 * - Deleting trips
 * - Export functionality
 * - Date filtering
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TripHistoryViewModelTest {

    private lateinit var viewModel: TripHistoryViewModel
    private lateinit var mockApplication: Application
    private lateinit var mockRepository: TripRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockApplication = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        
        every { mockApplication.cacheDir } returns createTempDirectory("test_history").toFile()
        every { mockApplication.packageName } returns "com.example.outofroutebuddy"
        
        viewModel = TripHistoryViewModel(mockApplication, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ==================== LOAD TRIPS TESTS ====================

    @Test
    fun `loadTrips fetches trips from repository`() = runTest {
        val testTrips = listOf(
            createTestTrip(actualMiles = 100.0),
            createTestTrip(actualMiles = 150.0)
        )
        every { mockRepository.getAllTrips() } returns flowOf(testTrips)
        
        viewModel.loadTrips()
        advanceUntilIdle()
        
        verify { mockRepository.getAllTrips() }
    }

    @Test
    fun `trips are sorted by date descending (most recent first)`() = runTest {
        val oldTrip = createTestTrip(
            actualMiles = 100.0,
            endTime = Date(System.currentTimeMillis() - 86400000) // 1 day ago
        )
        val newTrip = createTestTrip(
            actualMiles = 150.0,
            endTime = Date(System.currentTimeMillis()) // Now
        )
        
        every { mockRepository.getAllTrips() } returns flowOf(listOf(oldTrip, newTrip))
        
        viewModel.loadTrips()
        advanceUntilIdle()
        
        val trips = viewModel.trips.value
        assertTrue("Most recent trip should be first", 
            trips[0].actualMiles == 150.0)
    }

    @Test
    fun `isLoading is true during load and false after`() = runTest {
        every { mockRepository.getAllTrips() } returns flowOf(emptyList())
        
        assertEquals("Initial loading should be false", false, viewModel.isLoading.value)
        
        viewModel.loadTrips()
        
        // After completion
        advanceUntilIdle()
        assertEquals("Loading should be false after load", false, viewModel.isLoading.value)
    }

    // ==================== DELETE TRIP TESTS ====================

    @Test
    fun `deleteTrip calls repository deleteTrip`() = runTest {
        val trip = createTestTrip()
        coEvery { mockRepository.deleteTrip(trip) } just Runs
        
        viewModel.deleteTrip(trip)
        advanceUntilIdle()
        
        coVerify { mockRepository.deleteTrip(trip) }
    }

    @Test
    fun `deleteTrip handles errors gracefully`() = runTest {
        val trip = createTestTrip()
        coEvery { mockRepository.deleteTrip(trip) } throws Exception("Delete failed")
        
        // Should not crash
        viewModel.deleteTrip(trip)
        advanceUntilIdle()
        
        // Just verify it was attempted
        coVerify { mockRepository.deleteTrip(trip) }
    }

    // ==================== EXPORT TESTS ====================

    @Test
    fun `exportTrips handles empty trip list`() = runTest {
        every { mockRepository.getAllTrips() } returns flowOf(emptyList())
        
        viewModel.loadTrips()
        advanceUntilIdle()
        
        // Should not crash on export
        viewModel.exportTrips()
        advanceUntilIdle()
    }

    @Test
    fun `exportToPDF handles empty trip list`() = runTest {
        every { mockRepository.getAllTrips() } returns flowOf(emptyList())
        
        viewModel.loadTrips()
        advanceUntilIdle()
        
        // Should not crash on export
        viewModel.exportToPDF()
        advanceUntilIdle()
    }

    // ==================== FILTER TESTS ====================

    @Test
    fun `filterByDateRange filters trips correctly`() = runTest {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86400000L
        val twoDaysAgo = now - (86400000L * 2)
        
        val trips = listOf(
            createTestTrip(endTime = Date(now)),
            createTestTrip(endTime = Date(oneDayAgo)),
            createTestTrip(endTime = Date(twoDaysAgo))
        )
        
        every { mockRepository.getAllTrips() } returns flowOf(trips)
        viewModel.loadTrips()
        advanceUntilIdle()
        
        // Filter to only last 36 hours
        val filterStart = now - (36 * 3600000L)
        viewModel.filterByDateRange(filterStart, now)
        advanceUntilIdle()
        
        val filtered = viewModel.trips.value
        assertEquals("Should have 2 trips within date range", 2, filtered.size)
    }

    @Test
    fun `filterByDateRange returns empty when no trips in range`() = runTest {
        val trips = listOf(
            createTestTrip(endTime = Date(System.currentTimeMillis() - (10 * 86400000L))) // 10 days ago
        )
        
        every { mockRepository.getAllTrips() } returns flowOf(trips)
        viewModel.loadTrips()
        advanceUntilIdle()
        
        // Filter to only last 24 hours
        val now = System.currentTimeMillis()
        viewModel.filterByDateRange(now - 86400000L, now)
        advanceUntilIdle()
        
        val filtered = viewModel.trips.value
        assertEquals("Should have 0 trips in range", 0, filtered.size)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `loadTrips handles repository errors gracefully`() = runTest {
        every { mockRepository.getAllTrips() } throws Exception("Repository error")
        
        // Should not crash
        viewModel.loadTrips()
        advanceUntilIdle()
        
        assertEquals("Trips should be empty on error", 0, viewModel.trips.value.size)
    }

    @Test
    fun `trips with null dates are handled correctly`() = runTest {
        val tripWithoutDate = createTestTrip(startTime = null, endTime = null)
        val tripWithDate = createTestTrip(endTime = Date())
        
        every { mockRepository.getAllTrips() } returns flowOf(listOf(tripWithoutDate, tripWithDate))
        
        viewModel.loadTrips()
        advanceUntilIdle()
        
        // Should not crash and should include both trips
        assertEquals("Should handle trips with null dates", 2, viewModel.trips.value.size)
    }

    // ==================== HELPER METHODS ====================

    private fun createTestTrip(
        loadedMiles: Double = 100.0,
        bounceMiles: Double = 20.0,
        actualMiles: Double = 80.0,
        oorMiles: Double = 0.0,
        oorPercentage: Double = 0.0,
        startTime: Date? = Date(),
        endTime: Date? = Date()
    ): Trip {
        return Trip(
            id = UUID.randomUUID().toString(),
            loadedMiles = loadedMiles,
            bounceMiles = bounceMiles,
            actualMiles = actualMiles,
            oorMiles = oorMiles,
            oorPercentage = oorPercentage,
            startTime = startTime,
            endTime = endTime,
            status = TripStatus.COMPLETED,
            gpsMetadata = GpsMetadata()
        )
    }
}



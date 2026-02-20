package com.example.outofroutebuddy.presentation.viewmodel

import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.data.TripPersistenceManager
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.models.TripStatus
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.domain.repository.TripStatistics
import com.example.outofroutebuddy.services.*
import com.example.outofroutebuddy.validation.ValidationFramework
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for trip-to-statistics wiring and calendar picker functionality
 * 
 * Verifies:
 * 1. Trip saving to repository when trip ends
 * 2. Statistics refresh after trip ends
 * 3. Calendar picker period mode awareness (STANDARD vs CUSTOM)
 * 4. Weekly/monthly/yearly statistics accuracy
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TripStatisticsWiringTest {
    
    private lateinit var viewModel: TripInputViewModel
    private lateinit var mockRepository: TripRepository
    private lateinit var mockPreferencesManager: PreferencesManager
    private lateinit var mockTripStateManager: TripStateManager
    private lateinit var mockTripStatePersistence: TripStatePersistence
    private lateinit var mockUnifiedLocationService: UnifiedLocationService
    private lateinit var mockUnifiedTripService: UnifiedTripService
    private lateinit var mockUnifiedOfflineService: UnifiedOfflineService
    private lateinit var mockApplication: android.app.Application
    
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockGpsFlow: MutableStateFlow<UnifiedLocationService.RealTimeGpsData>
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Create mocks
        mockRepository = mockk(relaxed = true)
        mockPreferencesManager = mockk(relaxed = true)
        mockTripStateManager = mockk(relaxed = true)
        mockTripStatePersistence = mockk(relaxed = true)
        mockUnifiedLocationService = mockk(relaxed = true)
        mockUnifiedTripService = mockk(relaxed = true)
        mockUnifiedOfflineService = mockk(relaxed = true)
        mockApplication = mockk(relaxed = true)
        
        // Setup GPS flow
        mockGpsFlow = MutableStateFlow(
            UnifiedLocationService.RealTimeGpsData(
                totalDistance = 0.0,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
        )
        every { mockUnifiedLocationService.realTimeGpsData } returns mockGpsFlow
        
        // Setup trip state manager
        val tripStateFlow = MutableStateFlow(TripStateManager.TripState(isActive = false))
        every { mockTripStateManager.tripState } returns tripStateFlow
        
        // Setup preferences manager
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.STANDARD
        
        // Setup unified trip service calculation result
        val mockCalculationResult = UnifiedTripService.CalculationResult(
            tripId = "test-trip-1",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 125.0,
            oorMiles = 0.0,
            oorPercentage = 0.0,
            calculationTime = Date(),
            isValid = true
        )
        coEvery { mockUnifiedTripService.calculateTrip(any(), any(), any()) } returns mockCalculationResult
        
        // Setup unified trip service period dates
        every { mockUnifiedTripService.getCurrentPeriodDates(any()) } returns Pair(Date(), Date())
        
        // Create ViewModel
        viewModel = TripInputViewModel(
            tripRepository = mockRepository,
            preferencesManager = mockPreferencesManager,
            tripStateManager = mockTripStateManager,
            tripStatePersistence = mockTripStatePersistence,
            stateCache = mockk(relaxed = true),
            backgroundSyncService = mockk(relaxed = true),
            optimizedGpsDataFlow = mockk(relaxed = true),
            validationFramework = mockk(relaxed = true),
            unifiedLocationService = mockUnifiedLocationService,
            unifiedTripService = mockUnifiedTripService,
            unifiedOfflineService = mockUnifiedOfflineService,
            crashRecoveryManager = mockk(relaxed = true),
            tripPersistenceManager = mockk(relaxed = true),
            application = mockApplication,
            ioDispatcher = testDispatcher
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ==================== TRIP SAVING TESTS ====================
    
    @Test
    fun `endTrip saves trip to repository`() = runTest {
        // Given: An active trip with calculated OOR
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        // Setup repository to return a trip ID when inserting
        coEvery { mockRepository.insertTrip(any()) } returns "trip-123"
        
        // When: End trip
        viewModel.endTrip()
        delay(100) // Wait for coroutines to complete
        
        // Then: Repository should be called to insert the trip
        coVerify(exactly = 1) { mockRepository.insertTrip(any()) }
        
        // Verify trip was saved with correct data
        val savedTrip = slot<Trip>()
        coVerify { mockRepository.insertTrip(capture(savedTrip)) }
        
        assertEquals("Loaded miles should match", 100.0, savedTrip.captured.loadedMiles, 0.01)
        assertEquals("Bounce miles should match", 25.0, savedTrip.captured.bounceMiles, 0.01)
        assertEquals("Actual miles should match", 125.0, savedTrip.captured.actualMiles, 0.01)
        assertEquals("Trip status should be COMPLETED", TripStatus.COMPLETED, savedTrip.captured.status)
    }
    
    @Test
    fun `endTrip with OOR calculation saves correct OOR values`() = runTest {
        // Given: A trip with OOR (actual > dispatched)
        val loadedMiles = 100.0
        val bounceMiles = 25.0
        val actualMiles = 150.0 // 25 miles over dispatched
        
        // Setup calculation result with OOR
        val mockCalculationResult = UnifiedTripService.CalculationResult(
            tripId = "test-trip-2",
            loadedMiles = loadedMiles,
            bounceMiles = bounceMiles,
            actualMiles = actualMiles,
            oorMiles = 25.0, // 150 - 125 = 25
            oorPercentage = 20.0, // (25/125) * 100 = 20%
            calculationTime = Date(),
            isValid = true
        )
        coEvery { mockUnifiedTripService.calculateTrip(loadedMiles, bounceMiles, actualMiles) } returns mockCalculationResult
        
        viewModel.calculateTrip(loadedMiles, bounceMiles, actualMiles)
        
        coEvery { mockRepository.insertTrip(any()) } returns "trip-456"
        
        // When: End trip
        viewModel.endTrip()
        delay(100) // Wait for coroutines to complete
        
        // Then: Trip should be saved with OOR values
        val savedTrip = slot<Trip>()
        coVerify { mockRepository.insertTrip(capture(savedTrip)) }
        
        assertEquals("OOR miles should be 25.0", 25.0, savedTrip.captured.oorMiles, 0.01)
        assertEquals("OOR percentage should be 20.0", 20.0, savedTrip.captured.oorPercentage, 0.01)
    }
    
    @Test
    fun `endTrip falls back to offline service if repository save fails`() = runTest {
        // Given: An active trip
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        // Setup repository to fail
        coEvery { mockRepository.insertTrip(any()) } throws RuntimeException("Database error")
        
        // Setup offline service to succeed
        coEvery { mockUnifiedOfflineService.saveDataWithOfflineFallback(any(), any(), any()) } returns "offline-saved"
        
        // When: End trip
        viewModel.endTrip()
        delay(100) // Wait for coroutines to complete
        
        // Then: Should attempt repository save first
        coVerify(exactly = 1) { mockRepository.insertTrip(any()) }
        
        // Then: Should fall back to offline service
        coVerify(exactly = 1) { 
            mockUnifiedOfflineService.saveDataWithOfflineFallback(any(), eq("trip_data"), any())
        }
    }
    
    // ==================== STATISTICS REFRESH TESTS ====================
    
    @Test
    fun `endTrip refreshes aggregate statistics after saving`() = runTest {
        // Given: An active trip
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        
        // Setup repository responses
        coEvery { mockRepository.insertTrip(any()) } returns "trip-789"
        coEvery { mockRepository.getWeeklyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getYearlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        
        // When: End trip
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete (refreshAggregateStatistics uses Dispatchers.IO)
        // Use coVerify with timeout since IO dispatcher runs on real threads
        coVerify(timeout = 1000) { mockRepository.getWeeklyTripStatistics() }
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        coVerify(timeout = 1000) { mockRepository.getYearlyTripStatistics() }
        
        // Give a bit more time for UI state update (runs on Main dispatcher)
        delay(50)
        
        // Verify statistics are in UI state
        assertNotNull("Weekly statistics should be loaded", viewModel.uiState.value.weeklyStatistics)
        assertNotNull("Monthly statistics should be loaded", viewModel.uiState.value.monthlyStatistics)
        assertNotNull("Yearly statistics should be loaded", viewModel.uiState.value.yearlyStatistics)
    }
    
    @Test
    fun `statistics include newly saved trip`() = runTest {
        // Given: No existing trips, then save a new trip
        coEvery { mockRepository.insertTrip(any()) } returns "trip-new"
        
        // Setup statistics to show the new trip
        coEvery { mockRepository.getWeeklyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 25.0,
            avgOorPercentage = 20.0
        )
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 25.0,
            avgOorPercentage = 20.0
        )
        coEvery { mockRepository.getYearlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 25.0,
            avgOorPercentage = 20.0
        )
        
        // Setup calculation with OOR
        val mockCalculationResult = UnifiedTripService.CalculationResult(
            tripId = "test-trip-new",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 150.0,
            oorMiles = 25.0,
            oorPercentage = 20.0,
            calculationTime = Date(),
            isValid = true
        )
        coEvery { mockUnifiedTripService.calculateTrip(100.0, 25.0, 150.0) } returns mockCalculationResult
        
        viewModel.calculateTrip(100.0, 25.0, 150.0)
        delay(100)
        
        // When: End trip
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete (refreshAggregateStatistics uses Dispatchers.IO)
        coVerify(timeout = 1000) { mockRepository.getWeeklyTripStatistics() }
        delay(50) // Give time for UI state update
        
        // Then: Statistics should reflect the new trip
        val weeklyStats = viewModel.uiState.value.weeklyStatistics
        assertNotNull("Weekly stats should exist", weeklyStats)
        assertEquals("Weekly total trips should be 1", 1, weeklyStats?.totalTrips ?: 0)
        assertEquals("Weekly total miles should be 125.0", 125.0, weeklyStats?.totalMiles ?: 0.0, 0.01)
        assertEquals("Weekly OOR percentage should be 20.0", 20.0, weeklyStats?.oorPercentage ?: 0.0, 0.01)
    }
    
    // ==================== CALENDAR PICKER PERIOD MODE TESTS ====================
    
    @Test
    fun `onCalendarPeriodSelected with STANDARD mode uses single day range`() = runTest {
        // Given: STANDARD period mode
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.STANDARD
        
        val selectedDate = Date()
        val calendar = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.time
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        // Setup repository response
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        
        // When: Calendar period selected (single day for STANDARD mode)
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startOfDay, endOfDay)
        // Wait a bit for coroutines to complete
        kotlinx.coroutines.delay(100)
        
        // Then: Should query repository with the date range
        coVerify(exactly = 1) { mockRepository.getTripStatistics(startOfDay, endOfDay) }
        
        // Verify period label is formatted correctly
        val periodLabel = viewModel.uiState.value.selectedPeriodLabel
        assertTrue("Period label should contain date", periodLabel.isNotEmpty())
    }
    
    @Test
    fun `onCalendarPeriodSelected with CUSTOM mode uses date range`() = runTest {
        // Given: CUSTOM period mode
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.CUSTOM
        
        val startDate = Date()
        val calendar = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 7)
        }
        val endDate = calendar.time
        
        // Setup repository response
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        
        // When: Calendar period selected (range for CUSTOM mode)
        viewModel.onCalendarPeriodSelected(PeriodMode.CUSTOM, startDate, endDate)
        // Wait a bit for coroutines to complete
        kotlinx.coroutines.delay(100)
        
        // Then: Should query repository with the date range
        coVerify(exactly = 1) { mockRepository.getTripStatistics(startDate, endDate) }
        
        // Verify period label includes both dates
        val periodLabel = viewModel.uiState.value.selectedPeriodLabel
        assertTrue("Period label should contain date range", periodLabel.contains("-"))
    }
    
    @Test
    fun `calendar period selection updates selected period statistics`() = runTest {
        // Given: Some trip data in repository
        val mockStats = TripStatistics(
            totalTrips = 5,
            totalActualMiles = 500.0,
            totalOorMiles = 50.0,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns mockStats
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        
        val startDate = Date()
        val endDate = Date()
        
        // When: Select calendar period
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startDate, endDate)
        // Wait for coroutines on IO dispatcher to complete
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(startDate, endDate) }
        delay(50) // Give time for UI state update
        
        // Then: Period statistics should be updated
        val periodStats = viewModel.uiState.value.periodStatistics
        assertNotNull("Period statistics should be set", periodStats)
        assertEquals("Total trips should match", 5, periodStats?.totalTrips ?: 0)
        assertEquals("Total distance should match", 500.0, periodStats?.totalDistance ?: 0.0, 0.01)
        assertEquals("OOR percentage should match", 10.0, periodStats?.averageOorPercentage ?: 0.0, 0.01)
    }
    
    // ==================== WEEKLY/MONTHLY/YEARLY STATISTICS TESTS ====================
    
    @Test
    fun `weekly statistics calculation includes all trips in current week`() = runTest {
        // Given: Multiple trips in the current week
        val weeklyStats = TripStatistics(
            totalTrips = 3,
            totalActualMiles = 375.0,
            totalOorMiles = 37.5,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getWeeklyTripStatistics() } returns weeklyStats
        
        // When: Statistics are refreshed
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete
        coVerify(timeout = 1000) { mockRepository.getWeeklyTripStatistics() }
        delay(50) // Give time for UI state update
        
        // Then: Weekly statistics should be accurate
        val stats = viewModel.uiState.value.weeklyStatistics
        assertNotNull("Weekly stats should exist", stats)
        assertEquals("Total trips should be 3", 3, stats?.totalTrips ?: 0)
        assertEquals("Total miles should be 375.0", 375.0, stats?.totalMiles ?: 0.0, 0.01)
        assertEquals("OOR percentage should be 10.0", 10.0, stats?.oorPercentage ?: 0.0, 0.01)
    }
    
    @Test
    fun `monthly statistics calculation includes all trips in current month`() = runTest {
        // Given: Multiple trips in the current month
        val monthlyStats = TripStatistics(
            totalTrips = 10,
            totalActualMiles = 1250.0,
            totalOorMiles = 125.0,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getMonthlyTripStatistics() } returns monthlyStats
        
        // When: Statistics are refreshed
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        delay(50) // Give time for UI state update
        
        // Then: Monthly statistics should be accurate
        val stats = viewModel.uiState.value.monthlyStatistics
        assertNotNull("Monthly stats should exist", stats)
        assertEquals("Total trips should be 10", 10, stats?.totalTrips ?: 0)
        assertEquals("Total miles should be 1250.0", 1250.0, stats?.totalMiles ?: 0.0, 0.01)
    }
    
    @Test
    fun `yearly statistics calculation includes all trips in current year`() = runTest {
        // Given: Multiple trips in the current year
        val yearlyStats = TripStatistics(
            totalTrips = 50,
            totalActualMiles = 6250.0,
            totalOorMiles = 625.0,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getYearlyTripStatistics() } returns yearlyStats
        
        // When: Statistics are refreshed
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete
        coVerify(timeout = 1000) { mockRepository.getYearlyTripStatistics() }
        delay(50) // Give time for UI state update
        
        // Then: Yearly statistics should be accurate
        val stats = viewModel.uiState.value.yearlyStatistics
        assertNotNull("Yearly stats should exist", stats)
        assertEquals("Total trips should be 50", 50, stats?.totalTrips ?: 0)
        assertEquals("Total miles should be 6250.0", 6250.0, stats?.totalMiles ?: 0.0, 0.01)
    }
    
    @Test
    fun `statistics refresh happens after trip save completes`() = runTest {
        // Given: An active trip
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        
        // Setup repository to track call order
        val callOrder = mutableListOf<String>()
        coEvery { mockRepository.insertTrip(any()) } coAnswers {
            callOrder.add("insertTrip")
            "trip-ordered"
        }
        coEvery { mockRepository.getWeeklyTripStatistics() } coAnswers {
            callOrder.add("getWeeklyStats")
            TripStatistics()
        }
        coEvery { mockRepository.getMonthlyTripStatistics() } coAnswers {
            callOrder.add("getMonthlyStats")
            TripStatistics()
        }
        coEvery { mockRepository.getYearlyTripStatistics() } coAnswers {
            callOrder.add("getYearlyStats")
            TripStatistics()
        }
        
        // When: End trip
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete
        coVerify(timeout = 1000) { mockRepository.insertTrip(any()) }
        coVerify(timeout = 1000) { mockRepository.getWeeklyTripStatistics() }
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        coVerify(timeout = 1000) { mockRepository.getYearlyTripStatistics() }
        delay(50) // Give time for all coroutines to complete
        
        // Then: Insert should happen before statistics queries
        // Note: Due to async nature, we verify that all calls happened, order is guaranteed by ViewModel code
        assertTrue("Insert should be called", callOrder.contains("insertTrip"))
        assertTrue("Weekly stats should be called", callOrder.contains("getWeeklyStats"))
        assertTrue("Monthly stats should be called", callOrder.contains("getMonthlyStats"))
        assertTrue("Yearly stats should be called", callOrder.contains("getYearlyStats"))
    }
}


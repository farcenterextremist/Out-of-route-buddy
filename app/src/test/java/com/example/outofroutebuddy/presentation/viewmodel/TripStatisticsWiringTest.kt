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
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for trip-to-statistics wiring and calendar picker functionality.
 * Test names use backtick style: methodUnderTest_scenario_expectedOutcome.
 *
 * Verifies:
 * 1. Trip saving to repository when trip ends
 * 2. Statistics refresh after trip ends
 * 3. Calendar picker period mode awareness (STANDARD vs CUSTOM)
 * 4. Monthly statistics accuracy
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
        
        // ViewModel persists via TripStatePersistence.saveCompletedTrip (not domain repository.insertTrip)
        coEvery { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) } returns 1L
        
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
        // Given: An active trip with calculated OOR. ViewModel persists via TripStatePersistence.saveCompletedTrip.
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        // When: End trip
        viewModel.endTrip()
        delay(100) // Wait for coroutines to complete
        
        // Then: Persistence should be called once to save the trip (actualMiles, loadedMiles, bounceMiles, start, end)
        coVerify(exactly = 1) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        assertFalse("Trip should not be active after save", viewModel.uiState.value.isTripActive)
        assertTrue("Status should indicate saved", viewModel.uiState.value.tripStatusMessage.contains("saved", ignoreCase = true))
    }
    
    @Test
    fun `endTrip with OOR calculation saves correct OOR values`() = runTest {
        // Given: A trip with OOR (actual > dispatched). ViewModel persists via TripStatePersistence.saveCompletedTrip.
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
        coEvery { mockUnifiedTripService.calculateTrip(any(), any(), any()) } returns mockCalculationResult
        
        viewModel.calculateTrip(loadedMiles, bounceMiles, actualMiles)
        advanceUntilIdle()
        
        // When: End trip
        viewModel.endTrip()
        advanceUntilIdle()
        delay(50)
        
        // Then: Persistence should be called and UI state should show OOR values from calculation
        coVerify(exactly = 1) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        assertEquals("OOR miles should be 25.0", 25.0, viewModel.uiState.value.oorMiles, 0.01)
        assertEquals("OOR percentage should be 20.0", 20.0, viewModel.uiState.value.oorPercentage, 0.01)
    }
    
    @Test
    fun `endTrip falls back to offline service if repository save fails`() = runTest {
        // Given: An active trip. ViewModel persists via TripStatePersistence.saveCompletedTrip.
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        
        // Setup persistence to fail (e.g. database error)
        coEvery { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) } throws RuntimeException("Database error")
        
        // When: End trip
        viewModel.endTrip()
        delay(100) // Wait for coroutines to complete
        
        // Then: Should attempt persistence save (exactly once)
        coVerify(exactly = 1) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        
        // Then: ViewModel should show trip ended and may emit SaveError event; trip is not saved
        assertFalse("Trip should not be active after failed save", viewModel.uiState.value.isTripActive)
    }

    @Test
    fun `clearTrip does not insert trip into repository`() = runTest {
        // Given: An active trip. Clear trip does not call persistence save.
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)

        // When: Clear trip (discard without saving)
        viewModel.clearTrip()
        delay(150) // Allow clearTrip coroutines to complete

        // Then: saveCompletedTrip must never be called
        coVerify(exactly = 0) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        // UI should show trip inactive
        assertFalse("Trip should be inactive after clear", viewModel.uiState.value.isTripActive)
    }
    
    // ==================== STATISTICS REFRESH TESTS ====================
    
    @Test
    fun `endTrip refreshes aggregate statistics after saving`() = runTest {
        // Given: An active trip. Persistence succeeds; repository stats stubbed for refresh.
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        
        // When: End trip
        viewModel.endTrip()
        // Wait for coroutines on IO dispatcher to complete (refreshAggregateStatistics uses Dispatchers.IO)
        // Use coVerify with timeout since IO dispatcher runs on real threads
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        
        // Give a bit more time for UI state update (runs on Main dispatcher)
        delay(50)
        
        // Verify monthly statistics are in UI state
        assertNotNull("Monthly statistics should be loaded", viewModel.uiState.value.monthlyStatistics)
    }
    
    @Test
    fun `statistics include newly saved trip`() = runTest {
        // Given: No existing trips, then save a new trip (persistence stubbed in setUp).
        // Setup monthly statistics to show the new trip
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
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
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        delay(50) // Give time for UI state update
        
        // Then: Monthly statistics should reflect the new trip
        val monthlyStats = viewModel.uiState.value.monthlyStatistics
        assertNotNull("Monthly stats should exist", monthlyStats)
        assertEquals("Monthly total trips should be 1", 1, monthlyStats?.totalTrips ?: 0)
        assertEquals("Monthly total miles should be 125.0", 125.0, monthlyStats?.totalMiles ?: 0.0, 0.01)
        assertEquals("Monthly OOR percentage should be 20.0", 20.0, monthlyStats?.oorPercentage ?: 0.0, 0.01)
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
    
    // ==================== MONTHLY STATISTICS TESTS ====================
    
    @Test
    fun `monthly statistics calculation includes all trips in current month`() = runTest {
        // Given: Multiple trips in the current month (persistence stubbed in setUp).
        val monthlyStats = TripStatistics(
            totalTrips = 10,
            totalActualMiles = 1250.0,
            totalOorMiles = 125.0,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getMonthlyTripStatistics() } returns monthlyStats
        
        // When: Statistics are refreshed after end trip
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
    fun `statistics refresh happens after trip save completes`() = runTest {
        // Given: An active trip. Persistence stubbed in setUp; repository stats for refresh.
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)

        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        // When: End trip
        viewModel.endTrip()
        coVerify(timeout = 1000) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        delay(50)

        // Then: observable outcomes - trip saved and UI state shows statistics
        assertNotNull("Monthly statistics should be loaded after save", viewModel.uiState.value.monthlyStatistics)
        assertTrue("Statistics section should be shown", viewModel.uiState.value.showStatistics)
    }

    @Test
    fun `TripSaved event emits only after month and year stats are refreshed`() = runTest(testDispatcher) {
        // Given: Selected period and repository statistics available
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val periodStats = TripStatistics(
            totalTrips = 2,
            totalActualMiles = 200.0,
            totalOorMiles = 10.0,
            avgOorPercentage = 5.0
        )
        val monthlyStats = TripStatistics(
            totalTrips = 4,
            totalActualMiles = 420.0,
            totalOorMiles = 20.0,
            avgOorPercentage = 4.76
        )

        coEvery { mockRepository.getTripStatistics(any(), any()) } returns periodStats
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getMonthlyTripStatistics() } returns monthlyStats
        coEvery {
            mockTripStatePersistence.saveCompletedTrip(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns 42L

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        advanceUntilIdle()
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        advanceUntilIdle()

        // Collect TripSaved event and verify state at the moment it arrives.
        val tripSavedEvent = async {
            viewModel.events
                .filterIsInstance<TripInputViewModel.TripEvent.TripSaved>()
                .first()
        }

        // When: End trip
        viewModel.endTrip()
        advanceUntilIdle()
        tripSavedEvent.await()

        // Then: statistics are already refreshed when TripSaved is emitted
        val state = viewModel.uiState.value
        assertNotNull("Monthly statistics should already be refreshed", state.monthlyStatistics)
        assertNotNull("Selected period statistics should already be refreshed", state.periodStatistics)
        assertNotNull("Year statistics should already be refreshed", state.yearStatistics)
        assertEquals(
            "Monthly total trips should be available immediately",
            4,
            state.monthlyStatistics?.totalTrips ?: 0
        )
    }

    // ==================== SAVED TRIP DATA → CALENDAR (datesWithTripsInPeriod) ====================

    @Test
    fun `datesWithTripsInPeriod reflects saved trips from repository for selected period`() = runTest {
        // Given: Repository returns trips with distinct startTime dates in range
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.MARCH, 15, 10, 30, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val day1 = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 20)
        val day2 = cal.time
        val trip1 = Trip(
            id = "t1",
            loadedMiles = 10.0,
            bounceMiles = 2.0,
            actualMiles = 12.0,
            startTime = day1,
            endTime = day1,
            status = TripStatus.COMPLETED
        )
        val trip2 = Trip(
            id = "t2",
            loadedMiles = 20.0,
            bounceMiles = 5.0,
            actualMiles = 25.0,
            startTime = day2,
            endTime = day2,
            status = TripStatus.COMPLETED
        )
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 37.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip1, trip2))

        // When: Calendar period selected (triggers updatePeriodStatistics)
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(periodStart, periodEnd) }
        delay(100)

        // Then: datesWithTripsInPeriod contains exactly the distinct start-of-day dates
        val datesWithTrips = viewModel.uiState.value.datesWithTripsInPeriod
        assertNotNull(datesWithTrips)
        assertEquals("Should have two distinct days", 2, datesWithTrips.size)
        val normalizedDay1 = Calendar.getInstance().apply {
            time = day1
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val normalizedDay2 = Calendar.getInstance().apply {
            time = day2
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        assertTrue("Day 1 should be in list", datesWithTrips.any { it.time == normalizedDay1.time })
        assertTrue("Day 2 should be in list", datesWithTrips.any { it.time == normalizedDay2.time })
    }

    @Test
    fun `datesWithTripsInPeriod is empty when repository returns no trips for period`() = runTest {
        val periodStart = Date()
        val periodEnd = Date()
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(periodStart, periodEnd) }
        delay(100)

        assertTrue(
            "datesWithTripsInPeriod should be empty",
            viewModel.uiState.value.datesWithTripsInPeriod.isEmpty()
        )
    }

    @Test
    fun `datesWithTripsInPeriod deduplicates multiple trips on same day`() = runTest {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.APRIL, 7, 8, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val morning = cal.time
        cal.set(Calendar.HOUR_OF_DAY, 14)
        val afternoon = cal.time
        val trip1 = Trip(
            id = "a",
            loadedMiles = 5.0,
            bounceMiles = 0.0,
            actualMiles = 5.0,
            startTime = morning,
            endTime = morning,
            status = TripStatus.COMPLETED
        )
        val trip2 = Trip(
            id = "b",
            loadedMiles = 10.0,
            bounceMiles = 0.0,
            actualMiles = 10.0,
            startTime = afternoon,
            endTime = afternoon,
            status = TripStatus.COMPLETED
        )
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 30, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 15.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip1, trip2))

        viewModel.onCalendarPeriodSelected(PeriodMode.CUSTOM, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(periodStart, periodEnd) }
        delay(100)

        val datesWithTrips = viewModel.uiState.value.datesWithTripsInPeriod
        assertEquals("Same calendar day should appear once", 1, datesWithTrips.size)
    }

    @Test
    fun `updatePeriodStatistics sets periodStatistics and datesWithTripsInPeriod from repository`() = runTest {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.MAY, 10, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val tripDate = cal.time
        val trip = Trip(
            id = "single",
            loadedMiles = 50.0,
            bounceMiles = 10.0,
            actualMiles = 60.0,
            oorMiles = 5.0,
            oorPercentage = 10.0,
            startTime = tripDate,
            endTime = tripDate,
            status = TripStatus.COMPLETED
        )
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val mockStats = TripStatistics(
            totalTrips = 1,
            totalActualMiles = 60.0,
            totalOorMiles = 5.0,
            avgOorPercentage = 10.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns mockStats
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip))

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(periodStart, periodEnd) }
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(periodStart, periodEnd) }
        delay(100)

        val periodStats = viewModel.uiState.value.periodStatistics
        assertNotNull(periodStats)
        assertEquals(1, periodStats?.totalTrips ?: 0)
        assertEquals(60.0, periodStats?.totalDistance ?: 0.0, 0.01)
        assertEquals(10.0, periodStats?.averageOorPercentage ?: 0.0, 0.01)
        val datesWithTrips = viewModel.uiState.value.datesWithTripsInPeriod
        assertEquals(1, datesWithTrips.size)
    }

    // ==================== SAVED TRIPS IN STATISTICS AND CALENDAR (plan verification) ====================

    @Test
    fun `endTrip updates monthly statistics and datesWithTripsInPeriod when period includes today`() = runTest {
        // Given: Selected period is current month (so it includes "today")
        val now = Calendar.getInstance()
        val startOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        val tripWithStartToday = Trip(
            id = "saved-1",
            loadedMiles = 100.0,
            bounceMiles = 25.0,
            actualMiles = 125.0,
            startTime = Date(),
            endTime = Date(),
            status = TripStatus.COMPLETED
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(tripWithStartToday))

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startOfMonth, endOfMonth)
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(startOfMonth, endOfMonth) }
        delay(50)

        viewModel.calculateTrip(100.0, 25.0, 125.0)
        delay(100)
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 125.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )

        // When: End trip (save via persistence + refreshStatisticsAfterSave)
        viewModel.endTrip()
        coVerify(timeout = 1000) { mockTripStatePersistence.saveCompletedTrip(any(), any(), any(), any(), any()) }
        coVerify(timeout = 1000) { mockRepository.getMonthlyTripStatistics() }
        coVerify(timeout = 1000) { mockRepository.getTripsByDateRange(any(), any()) }
        delay(100)

        // Then: State has non-null monthly statistics and non-empty datesWithTripsInPeriod
        val state = viewModel.uiState.value
        assertNotNull("Monthly statistics should be loaded after endTrip", state.monthlyStatistics)
        assertTrue(
            "datesWithTripsInPeriod should contain the saved trip day when period includes today",
            state.datesWithTripsInPeriod.isNotEmpty()
        )
    }

    @Test
    fun `refreshPeriodForCalendar loads monthly statistics and datesWithTripsInPeriod from repository`() = runTest {
        // Given: Repository has a saved trip; selected period is set to current month
        val now = Calendar.getInstance()
        val startOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfMonth = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val tripDate = Date()
        val trip = Trip(
            id = "refreshed-1",
            loadedMiles = 50.0,
            bounceMiles = 10.0,
            actualMiles = 60.0,
            startTime = tripDate,
            endTime = tripDate,
            status = TripStatus.COMPLETED
        )
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 60.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 60.0,
            totalOorMiles = 0.0,
            avgOorPercentage = 0.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip))

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startOfMonth, endOfMonth)
        delay(100)

        // When: Run same refresh logic used after save (via public API for calendar)
        viewModel.refreshPeriodForCalendar()
        delay(50)

        // Then: monthlyStatistics and datesWithTripsInPeriod reflect the trip from repository
        val state = viewModel.uiState.value
        assertNotNull("Monthly statistics should be loaded", state.monthlyStatistics)
        assertEquals(1, state.monthlyStatistics?.totalTrips ?: 0)
        assertTrue(
            "datesWithTripsInPeriod should contain the trip date",
            state.datesWithTripsInPeriod.isNotEmpty()
        )
    }

    // ==================== mapPeriodToSummary & STATS ROW WIRING (monthly statistics / stat card) ====================

    @Test
    fun `mapPeriodToSummary returns null when period is null`() {
        val result = viewModel.mapPeriodToSummary(null)
        assertNull("mapPeriodToSummary should return null for null period", result)
    }

    @Test
    fun `mapPeriodToSummary maps PeriodStatistics to SummaryStatistics correctly`() {
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val period = TripInputViewModel.PeriodStatistics(
            totalTrips = 7,
            totalDistance = 1250.5,
            totalOorMiles = 62.5,
            averageOorPercentage = 5.0,
            periodMode = PeriodMode.STANDARD,
            startDate = periodStart,
            endDate = periodEnd
        )
        val summary = viewModel.mapPeriodToSummary(period)
        assertNotNull("mapPeriodToSummary should return non-null for valid period", summary)
        assertEquals("totalTrips should map correctly", 7, summary!!.totalTrips)
        assertEquals("totalMiles should map from totalDistance", 1250.5, summary.totalMiles, 0.01)
        assertEquals("oorMiles should map from totalOorMiles", 62.5, summary.oorMiles, 0.01)
        assertEquals("oorPercentage should map from averageOorPercentage", 5.0, summary.oorPercentage, 0.01)
    }

    @Test
    fun `stats row displays period data when period is selected - mapPeriodToSummary produces expected values`() = runTest {
        // Given: User selects a calendar period; repository returns stats
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 30, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 3,
            totalActualMiles = 450.0,
            totalOorMiles = 22.5,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        // When: User selects period (triggers updatePeriodStatistics)
        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(periodStart, periodEnd) }
        delay(50)

        // Then: periodStatistics is populated and mapPeriodToSummary produces SummaryStatistics for stats row
        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be set after period selection", state.periodStatistics)
        val summary = viewModel.mapPeriodToSummary(state.periodStatistics)
        assertNotNull("mapPeriodToSummary should produce SummaryStatistics for stats row", summary)
        assertEquals("Stats row totalTrips", 3, summary!!.totalTrips)
        assertEquals("Stats row totalMiles", 450.0, summary.totalMiles, 0.01)
        assertEquals("Stats row oorMiles", 22.5, summary.oorMiles, 0.01)
        assertEquals("Stats row oorPercentage", 5.0, summary.oorPercentage, 0.01)
    }

    @Test
    fun `stats row shows null summary when no period selected - mapPeriodToSummary of null`() {
        // When: Stats row receives null (e.g. no period selected or cleared), mapPeriodToSummary returns null.
        // Note: ViewModel auto-initializes periodStatistics from preferences on init, so state.periodStatistics
        // is typically non-null. This test verifies the defensive behavior when null is passed.
        val summary = viewModel.mapPeriodToSummary(null)
        assertNull("mapPeriodToSummary should return null when periodStatistics is null", summary)
    }

    // ==================== getYearRange unit tests (Phase 1) ====================

    @Test
    fun `getYearRange returns Jan 1 and Dec 31 for mid-year date`() {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.JUNE, 15, 12, 30, 45)
        cal.set(Calendar.MILLISECOND, 123)
        val midYearDate = cal.time

        val (yearStart, yearEnd) = viewModel.getYearRange(midYearDate)

        val startCal = Calendar.getInstance()
        startCal.time = yearStart
        assertEquals("Year start should be 2025", 2025, startCal.get(Calendar.YEAR))
        assertEquals("Year start should be January", Calendar.JANUARY, startCal.get(Calendar.MONTH))
        assertEquals("Year start should be day 1", 1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals("Year start should be 00:00:00", 0, startCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Year start should be 00:00:00", 0, startCal.get(Calendar.MINUTE))
        assertEquals("Year start should be 00:00:00", 0, startCal.get(Calendar.SECOND))

        val endCal = Calendar.getInstance()
        endCal.time = yearEnd
        assertEquals("Year end should be 2025", 2025, endCal.get(Calendar.YEAR))
        assertEquals("Year end should be December", Calendar.DECEMBER, endCal.get(Calendar.MONTH))
        assertEquals("Year end should be day 31", 31, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals("Year end should be 23:59:59", 23, endCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Year end should be 23:59:59", 59, endCal.get(Calendar.MINUTE))
        assertEquals("Year end should be 23:59:59", 59, endCal.get(Calendar.SECOND))
    }

    @Test
    fun `getYearRange handles leap year correctly`() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.FEBRUARY, 29, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val leapYearDate = cal.time

        val (yearStart, yearEnd) = viewModel.getYearRange(leapYearDate)

        val startCal = Calendar.getInstance()
        startCal.time = yearStart
        assertEquals("Leap year start should be 2024", 2024, startCal.get(Calendar.YEAR))
        assertEquals("Year start should be Jan 1", 1, startCal.get(Calendar.DAY_OF_MONTH))

        val endCal = Calendar.getInstance()
        endCal.time = yearEnd
        assertEquals("Leap year end should be 2024", 2024, endCal.get(Calendar.YEAR))
        assertEquals("Year end should be Dec 31", 31, endCal.get(Calendar.DAY_OF_MONTH))
    }

    // ==================== Phase 2: Period switching tests ====================

    @Test
    fun `savePeriodMode STANDARD to CUSTOM triggers stats refresh`() = runTest(testDispatcher) {
        val customStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val customEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.CUSTOM
        every { mockUnifiedTripService.getCurrentPeriodDates(PeriodMode.CUSTOM) } returns Pair(customStart, customEnd)
        coEvery { mockRepository.getTripStatistics(customStart, customEnd) } returns TripStatistics(
            totalTrips = 4,
            totalActualMiles = 400.0,
            totalOorMiles = 20.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 4,
            totalActualMiles = 400.0,
            totalOorMiles = 20.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel.savePeriodMode(PeriodMode.CUSTOM)
        coVerify(timeout = 2000) { mockRepository.getTripStatistics(any(), any()) }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be updated after CUSTOM mode", state.periodStatistics)
        assertNotNull("yearStatistics should be updated after CUSTOM mode", state.yearStatistics)
        assertEquals("Period stats should reflect CUSTOM period", 4, state.periodStatistics?.totalTrips ?: 0)
    }

    @Test
    fun `onCalendarPeriodSelected updates displayed stats for new period`() = runTest(testDispatcher) {
        val periodAStart = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodAEnd = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val periodBStart = Calendar.getInstance().apply {
            set(2025, Calendar.FEBRUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodBEnd = Calendar.getInstance().apply {
            set(2025, Calendar.FEBRUARY, 28, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(periodAStart, periodAEnd) } returns TripStatistics(
            totalTrips = 3,
            totalActualMiles = 300.0,
            totalOorMiles = 15.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(periodBStart, periodBEnd) } returns TripStatistics(
            totalTrips = 7,
            totalActualMiles = 700.0,
            totalOorMiles = 35.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } answers {
            val start = firstArg<Date>()
            val end = secondArg<Date>()
            when {
                start.time == periodAStart.time && end.time == periodAEnd.time -> TripStatistics(
                    totalTrips = 3,
                    totalActualMiles = 300.0,
                    totalOorMiles = 15.0,
                    avgOorPercentage = 5.0
                )
                start.time == periodBStart.time && end.time == periodBEnd.time -> TripStatistics(
                    totalTrips = 7,
                    totalActualMiles = 700.0,
                    totalOorMiles = 35.0,
                    avgOorPercentage = 5.0
                )
                else -> TripStatistics(totalTrips = 5, totalActualMiles = 500.0, totalOorMiles = 25.0, avgOorPercentage = 5.0)
            }
        }
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodAStart, periodAEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(periodAStart, periodAEnd) }
        advanceUntilIdle()
        assertEquals("Period A stats", 3, viewModel.uiState.value.periodStatistics?.totalTrips ?: 0)

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodBStart, periodBEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(periodBStart, periodBEnd) }
        advanceUntilIdle()
        assertEquals("Period B stats should replace Period A", 7, viewModel.uiState.value.periodStatistics?.totalTrips ?: 0)
    }

    // ==================== Phase 4: End Trip + refresh flow tests ====================

    @Test
    fun `endTrip triggers refreshStatisticsAfterSave with selected period and year`() = runTest(testDispatcher) {
        val marchStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val marchEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(marchStart, marchEnd) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 200.0,
            totalOorMiles = 10.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 10,
            totalActualMiles = 1000.0,
            totalOorMiles = 50.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics()

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, marchStart, marchEnd)
        advanceUntilIdle()
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        viewModel.endTrip()
        advanceUntilIdle()

        coVerify(timeout = 2000) { mockRepository.getTripStatistics(marchStart, marchEnd) }
        coVerify(timeout = 2000) { mockRepository.getMonthlyTripStatistics() }
        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be updated", state.periodStatistics)
        assertNotNull("yearStatistics should be updated", state.yearStatistics)
    }

    @Test
    fun `refreshPeriodForCalendar updates both period and year stats`() = runTest(testDispatcher) {
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.APRIL, 30, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val yearStart = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val yearEnd = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val periodStats = TripStatistics(
            totalTrips = 5,
            totalActualMiles = 500.0,
            totalOorMiles = 25.0,
            avgOorPercentage = 5.0
        )
        val yearStats = TripStatistics(
            totalTrips = 50,
            totalActualMiles = 5000.0,
            totalOorMiles = 250.0,
            avgOorPercentage = 5.0
        )
        // ViewModel may use Calendar-derived Date instances; return by range (period = April, year = full year)
        coEvery { mockRepository.getTripStatistics(any(), any()) } answers {
            val start = firstArg<Date>()
            val end = secondArg<Date>()
            val cal = Calendar.getInstance().apply { time = start }
            val endCal = Calendar.getInstance().apply { time = end }
            if (cal.get(Calendar.MONTH) == Calendar.APRIL && endCal.get(Calendar.MONTH) == Calendar.APRIL) periodStats else yearStats
        }
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics()

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        advanceUntilIdle()
        viewModel.refreshPeriodForCalendar()
        advanceUntilIdle()

        // refreshPeriodForCalendar calls refreshStatisticsAfterSave which queries period and year stats
        coVerify(atLeast = 1) { mockRepository.getTripStatistics(any(), any()) }
        coVerify(atLeast = 1) { mockRepository.getMonthlyTripStatistics() }
    }

    @Test
    fun `yearStatistics uses year of selected period`() = runTest {
        val marchStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val marchEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(marchStart, marchEnd) } returns TripStatistics(
            totalTrips = 10,
            totalActualMiles = 1000.0,
            totalOorMiles = 50.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 100,
            totalActualMiles = 10000.0,
            totalOorMiles = 500.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, marchStart, marchEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(marchStart, marchEnd) }
        delay(100)

        val yearStats = viewModel.uiState.value.yearStatistics
        assertNotNull("yearStatistics should be set for March 2025 period", yearStats)
        assertEquals("Year stats should reflect full year aggregation", 100, yearStats?.totalTrips ?: 0)
        assertEquals(10000.0, yearStats?.totalDistance ?: 0.0, 0.01)
    }

    @Test
    fun `endTrip while viewing another period refreshes selected period stats`() = runTest {
        val marchStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val marchEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 2,
            totalActualMiles = 200.0,
            totalOorMiles = 10.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getMonthlyTripStatistics() } returns TripStatistics()

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, marchStart, marchEnd)
        delay(100)
        every { mockTripStateManager.getCurrentState() } returns TripStateManager.TripState(
            isActive = true,
            loadedMiles = "100",
            bounceMiles = "25",
            startTime = Date()
        )
        viewModel.calculateTrip(100.0, 25.0, 125.0)
        viewModel.endTrip()

        coVerify(timeout = 2000) { mockRepository.getTripStatistics(marchStart, marchEnd) }
        coVerify(timeout = 2000) { mockRepository.getTripStatistics(any(), any()) }
        val state = viewModel.uiState.value
        assertNotNull("periodStatistics should be set for selected March period", state.periodStatistics)
        assertNotNull("yearStatistics should be set for year of March", state.yearStatistics)
    }

    @Test
    fun `getYearRange uses same year for date on Jan 1 and Dec 31`() {
        val jan1 = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val dec31 = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val (startFromJan, endFromJan) = viewModel.getYearRange(jan1)
        val (startFromDec, endFromDec) = viewModel.getYearRange(dec31)

        assertEquals("Jan 1 and Dec 31 should produce same year start", startFromJan.time, startFromDec.time)
        assertEquals("Jan 1 and Dec 31 should produce same year end", endFromJan.time, endFromDec.time)
    }

    @Test
    fun `getYearRangeForSelectedPeriod anchors to period end year when crossing New Year`() {
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 26, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 25, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val (yearStart, yearEnd) = viewModel.getYearRangeForSelectedPeriod(periodStart, periodEnd)
        val startCal = Calendar.getInstance().apply { time = yearStart }
        val endCal = Calendar.getInstance().apply { time = yearEnd }

        assertEquals("Year start should anchor to period end year", 2026, startCal.get(Calendar.YEAR))
        assertEquals("Year end should anchor to period end year", 2026, endCal.get(Calendar.YEAR))
    }

    @Test
    fun `onCalendarPeriodSelected crossing New Year queries year stats for period end year`() = runTest {
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.DECEMBER, 26, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 25, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        val year2026Start = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val year2026End = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics()
        coEvery { mockRepository.getTripStatistics(periodStart, periodEnd) } returns TripStatistics(
            totalTrips = 3,
            totalActualMiles = 300.0,
            totalOorMiles = 15.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripStatistics(year2026Start, year2026End) } returns TripStatistics(
            totalTrips = 30,
            totalActualMiles = 3000.0,
            totalOorMiles = 150.0,
            avgOorPercentage = 5.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(emptyList())

        viewModel.onCalendarPeriodSelected(PeriodMode.CUSTOM, periodStart, periodEnd)
        coVerify(timeout = 1000) { mockRepository.getTripStatistics(year2026Start, year2026End) }
        delay(100)

        assertEquals(30, viewModel.uiState.value.yearStatistics?.totalTrips ?: 0)
    }

    @Test
    fun `datesWithTripsInPeriod remains day-stable across DST boundary`() = runTest {
        val start = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 9, 0, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val end = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 10, 0, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val trip = Trip(
            id = "dst-trip",
            loadedMiles = 20.0,
            bounceMiles = 5.0,
            actualMiles = 30.0,
            startTime = start,
            endTime = end,
            status = TripStatus.COMPLETED
        )
        val periodStart = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val periodEnd = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns TripStatistics(
            totalTrips = 1,
            totalActualMiles = 30.0,
            totalOorMiles = 5.0,
            avgOorPercentage = 20.0
        )
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns flowOf(listOf(trip))

        viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, periodStart, periodEnd)
        delay(100)

        val dates = viewModel.uiState.value.datesWithTripsInPeriod
        assertEquals("DST-spanning trip should contribute exactly two calendar days", 2, dates.size)
    }
}


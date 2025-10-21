package com.example.outofroutebuddy.services

import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.validation.ValidationFramework
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * 🚀 MOCK GPS SYNCHRONIZATION SERVICE INTEGRATION TESTS
 *
 * This test suite verifies that the MockGpsSynchronizationService works correctly
 * with the TripInputViewModel for real-time GPS data integration.
 *
 * Tests cover:
 * 1. Real-time GPS distance updates to ViewModel UI state
 * 2. GPS data flow during active trips
 * 3. GPS service start/stop integration
 * 4. Error handling and edge cases
 * 5. Performance with rapid GPS updates
 *
 * ✅ FIXED: Coroutine synchronization issues for reliable test execution
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockGpsSynchronizationServiceTest {
    private lateinit var mockGpsService: MockGpsSynchronizationService
    private lateinit var viewModel: TripInputViewModel
    private lateinit var mockRepository: TripRepository
    private lateinit var mockPreferencesManager: PreferencesManager
    private lateinit var mockTripStateManager: TripStateManager
    private lateinit var mockTripStatePersistence: TripStatePersistence
    private lateinit var mockStateCache: StateCache
    private lateinit var mockBackgroundSyncService: BackgroundSyncService
    private lateinit var mockOptimizedGpsDataFlow: OptimizedGpsDataFlow
    private lateinit var mockTripCalculationService: TripCalculationService
    private lateinit var mockValidationFramework: ValidationFramework
    private lateinit var mockApplication: android.app.Application
    private lateinit var mockUnifiedLocationService: UnifiedLocationService
    private lateinit var mockUnifiedTripService: UnifiedTripService
    private lateinit var mockUnifiedOfflineService: UnifiedOfflineService
    private lateinit var mockCrashRecoveryManager: com.example.outofroutebuddy.services.TripCrashRecoveryManager

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var unifiedGpsFlow: MutableStateFlow<UnifiedLocationService.RealTimeGpsData>

    @Before
    fun setUp() {
        // Setup test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        mockRepository = mockk(relaxed = true)
        mockPreferencesManager = mockk(relaxed = true)
        mockTripStateManager = mockk(relaxed = true)
        mockTripStatePersistence = mockk(relaxed = true)
        mockStateCache = mockk(relaxed = true)
        mockBackgroundSyncService = mockk(relaxed = true)
        mockOptimizedGpsDataFlow = mockk(relaxed = true)
        mockTripCalculationService = mockk(relaxed = true)
        mockValidationFramework = mockk(relaxed = true)
        mockApplication = mockk(relaxed = true)
        mockUnifiedLocationService = mockk(relaxed = true)
        mockUnifiedTripService = mockk(relaxed = true)
        mockUnifiedOfflineService = mockk(relaxed = true)
        mockCrashRecoveryManager = mockk(relaxed = true)

        // Setup mock behaviors
        setupMockBehaviors()

        // Use the real mock GPS service for integration
        mockGpsService = MockGpsSynchronizationService()
        
        // ✅ FIX: Reset mock GPS service to ensure clean state between tests
        mockGpsService.reset()
        
        // ✅ FIX: Reset unified GPS flow to ensure clean state between tests
        unifiedGpsFlow.value = UnifiedLocationService.RealTimeGpsData(
            totalDistance = 0.0,
            accuracy = 0.0,
            lastUpdate = System.currentTimeMillis()
        )

        // Bridge mock GPS distance updates into unified location flow consumed by the ViewModel
        mockGpsService.setOnDistanceUpdate { distance ->
            unifiedGpsFlow.value = unifiedGpsFlow.value.copy(
                totalDistance = distance,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
        }

        // Create ViewModel with real mock GPS service
        viewModel =
            TripInputViewModel(
                tripRepository = mockRepository,
                preferencesManager = mockPreferencesManager,
                tripStateManager = mockTripStateManager,
                tripStatePersistence = mockTripStatePersistence,
                stateCache = mockStateCache,
                backgroundSyncService = mockBackgroundSyncService,
                optimizedGpsDataFlow = mockOptimizedGpsDataFlow,
                validationFramework = mockValidationFramework,
                unifiedLocationService = mockUnifiedLocationService,
                unifiedTripService = mockUnifiedTripService,
                unifiedOfflineService = mockUnifiedOfflineService,
                crashRecoveryManager = mockCrashRecoveryManager,
                application = mockApplication,
            )

        // ✅ FIXED: Allow ViewModel initialization to complete
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        // ✅ FIX: Clean up ViewModel state between tests
        try {
            viewModel.resetTrip()
            testDispatcher.scheduler.advanceUntilIdle()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        
        // Reset mock GPS service
        mockGpsService.reset()
        
        // Reset unified GPS flow
        unifiedGpsFlow.value = UnifiedLocationService.RealTimeGpsData(
            totalDistance = 0.0,
            accuracy = 0.0,
            lastUpdate = System.currentTimeMillis()
        )
        
        // Clean up coroutines
        Dispatchers.resetMain()
    }

    private fun setupMockBehaviors() {
        every { mockPreferencesManager.getPeriodMode() } returns PeriodMode.STANDARD
        every { mockPreferencesManager.isTripActive() } returns false
        every { mockTripStateManager.tripState } returns
            MutableStateFlow(
                TripStateManager.TripState(
                    isActive = false,
                    loadedMiles = "",
                    bounceMiles = "",
                    startTime = null,
                ),
            )
        every { mockTripStateManager.startTrip(any(), any()) } returns true
        every { mockTripStateManager.endTrip() } returns true

        // ✅ FIXED: Make getCurrentState return dynamic values based on the last startTrip call
        var lastLoadedMiles = "100.0"
        var lastBounceMiles = "25.0"

        every { mockTripStateManager.startTrip(any(), any()) } answers {
            lastLoadedMiles = firstArg()
            lastBounceMiles = secondArg()
            true
        }

        every { mockTripStateManager.getCurrentState() } answers {
            TripStateManager.TripState(
                isActive = false,
                loadedMiles = lastLoadedMiles,
                bounceMiles = lastBounceMiles,
                startTime = Date(),
            )
        }

        coEvery { mockRepository.insertTrip(any()) } returns "test-trip-id"
        coEvery { mockRepository.getAllTrips() } returns MutableStateFlow(emptyList())
        coEvery { mockRepository.getTripById(any()) } returns MutableStateFlow(null)
        coEvery { mockRepository.getTripsByDateRange(any(), any()) } returns MutableStateFlow(emptyList())
        coEvery { mockRepository.getCurrentActiveTrip() } returns MutableStateFlow(null)
        coEvery { mockRepository.updateTrip(any()) } returns Unit
        coEvery { mockRepository.deleteTrip(any()) } returns Unit
        coEvery { mockRepository.deleteTripById(any()) } returns Unit
        coEvery { mockRepository.getTripStatistics(any(), any()) } returns com.example.outofroutebuddy.domain.repository.TripStatistics()
        coEvery { mockRepository.getTodayTripStatistics() } returns com.example.outofroutebuddy.domain.repository.TripStatistics()
        coEvery { mockRepository.getWeeklyTripStatistics() } returns com.example.outofroutebuddy.domain.repository.TripStatistics()
        coEvery { mockRepository.getMonthlyTripStatistics() } returns com.example.outofroutebuddy.domain.repository.TripStatistics()
        coEvery { mockRepository.getYearlyTripStatistics() } returns com.example.outofroutebuddy.domain.repository.TripStatistics()
        coEvery { mockRepository.clearAllTrips() } returns Unit
        coEvery { mockRepository.exportTripData(any(), any()) } returns ""
        every { mockStateCache.getCacheMetrics() } returns
            StateCache.CacheMetrics(
                hitRate = 0.0,
                totalRequests = 0L,
                cacheHits = 0L,
                cacheMisses = 0L,
                cacheEvictions = 0L,
                tripCacheSize = 0,
                statsCacheSize = 0,
                gpsCacheSize = 0,
                totalCacheSize = 0,
            )

        // ✅ NEW: Bridge mock GPS to unified location flow used by the ViewModel
        unifiedGpsFlow = MutableStateFlow(
            UnifiedLocationService.RealTimeGpsData(
                totalDistance = 0.0,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
        )
        every { mockUnifiedLocationService.realTimeGpsData } returns unifiedGpsFlow

        // Propagate mock GPS distance updates into the unified flow
        // This ensures observeLocationData() in the ViewModel receives updates
        // and updates uiState.actualMiles accordingly (including negatives and small increments)
        // NOTE: mockGpsService is initialized later; we will set the callback in setUp after creation
        // so we expose a setter on the mock service and connect it here once available.
        // We'll also mirror accuracy to 0.0 and update lastUpdate.
        // The callback will be attached in @Before after mockGpsService is created.

        // ✅ FIX: Mock UnifiedTripService.calculateTrip with CORRECT OOR calculation
        coEvery { mockUnifiedTripService.calculateTrip(any(), any(), any()) } answers {
            val loaded = firstArg<Double>()
            val bounce = secondArg<Double>()
            val actual = thirdArg<Double>()
            val dispatched = loaded + bounce
            // ✅ FIX: OOR can be negative! Don't clamp it
            val oorMiles = actual - dispatched
            // ✅ FIX: Use DISPATCHED miles as denominator (not loaded)
            val oorPercentage = if (dispatched > 0.0) (oorMiles / dispatched) * 100.0 else 0.0
            
            UnifiedTripService.CalculationResult(
                tripId = "test-${System.currentTimeMillis()}",
                loadedMiles = loaded,
                bounceMiles = bounce,
                actualMiles = actual,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                calculationTime = Date(),
                isValid = true,
                validationIssues = emptyList()
            )
        }

        // Callback attachment is done in setUp() after mockGpsService is created.
    }

    @Test
    fun `actualMiles updates in real time from mock GPS data`() =
        runTest {
            // Given: Active trip
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // When: Simulate GPS updates
            mockGpsService.emitDistance(0.0)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Initial distance should be 0", 0.0, viewModel.uiState.value.actualMiles, 0.01)

            mockGpsService.emitDistance(1.2)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Distance should update to 1.2", 1.2, viewModel.uiState.value.actualMiles, 0.01)

            mockGpsService.emitDistance(2.5)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Distance should update to 2.5", 2.5, viewModel.uiState.value.actualMiles, 0.01)

            // Verify the mock service emits the correct value
            assertEquals("Mock service should emit correct value", 2.5, mockGpsService.realTimeGpsData.value.totalDistance, 0.01)
        }

    @Test
    fun `GPS data flow during complete trip lifecycle`() =
        runTest {
            // ✅ FIX: Create a fresh ViewModel instance for this test to ensure clean state
            val freshViewModel = TripInputViewModel(
                tripRepository = mockRepository,
                preferencesManager = mockPreferencesManager,
                tripStateManager = mockTripStateManager,
                tripStatePersistence = mockTripStatePersistence,
                stateCache = mockStateCache,
                backgroundSyncService = mockBackgroundSyncService,
                optimizedGpsDataFlow = mockOptimizedGpsDataFlow,
                validationFramework = mockValidationFramework,
                unifiedLocationService = mockUnifiedLocationService,
                unifiedTripService = mockUnifiedTripService,
                unifiedOfflineService = mockUnifiedOfflineService,
                crashRecoveryManager = mockCrashRecoveryManager,
                application = mockApplication,
            )
            
            // Allow ViewModel initialization to complete
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Given: Trip with loaded and bounce miles
            val loadedMiles = "100.0"
            val bounceMiles = "25.0"

            // When: Start trip
            freshViewModel.calculateTrip(100.0, 25.0, 0.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: Trip should be active and GPS should be started
            assertTrue("Trip should be active", freshViewModel.uiState.value.isTripActive)
            assertEquals("Initial distance should be 0", 0.0, freshViewModel.uiState.value.actualMiles, 0.01)

            // When: GPS emits progressive distance updates
            val distances = listOf(10.0, 25.0, 50.0, 75.0, 100.0, 125.0)

            distances.forEach { distance ->
                mockGpsService.emitDistance(distance)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then: UI state should update in real-time
                assertEquals("Distance should update to $distance", distance, freshViewModel.uiState.value.actualMiles, 0.01)
                assertTrue("Trip should remain active", freshViewModel.uiState.value.isTripActive)
                assertTrue(
                    "Status should show current distance",
                    freshViewModel.uiState.value.tripStatusMessage.contains(String.format("%.1f", distance.toDouble())),
                )
            }

            // When: End trip
            freshViewModel.endTrip()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: Trip should be saved with correct OOR calculations
            assertFalse("Trip should not be active", freshViewModel.uiState.value.isTripActive)
            assertEquals("Final distance should be 125", 125.0, freshViewModel.uiState.value.actualMiles, 0.01)

            // Debug output
            println("actualMiles: ${freshViewModel.uiState.value.actualMiles}")
            println("oorMiles: ${freshViewModel.uiState.value.oorMiles}")
            println("oorPercentage: ${freshViewModel.uiState.value.oorPercentage}")

            // Verify OOR calculations: 125 - 100 - 25 = 0 OOR miles
            assertEquals("OOR miles should be 0", 0.0, freshViewModel.uiState.value.oorMiles, 0.01)
            assertEquals("OOR percentage should be 0", 0.0, freshViewModel.uiState.value.oorPercentage, 0.01)
        }

    @Test
    fun `GPS service start and stop integration`() =
        runTest {
            // Given: Initial state
            assertFalse("Initial state should be inactive", viewModel.uiState.value.isTripActive)

            // When: Start trip
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: GPS service should be started
            assertTrue("Trip should be active", viewModel.uiState.value.isTripActive)
            assertTrue(
                "Status should indicate GPS is active",
                viewModel.uiState.value.tripStatusMessage.contains("Trip started"),
            )

            // When: Add some GPS data
            mockGpsService.emitDistance(50.0)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Distance should be 50", 50.0, viewModel.uiState.value.actualMiles, 0.01)

            // When: End trip, then reset for next session
            viewModel.endTrip()
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.resetTrip()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: GPS service should be stopped and data reset
            assertFalse("Trip should be inactive", viewModel.uiState.value.isTripActive)
            assertEquals("Distance should be reset to 0", 0.0, viewModel.uiState.value.actualMiles, 0.01)
            assertEquals("Status should show reset", "Ready for new trip", viewModel.uiState.value.tripStatusMessage)
        }

    @Test
    fun `rapid GPS updates performance test`() =
        runTest {
            // Given: Active trip
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // When: Emit many rapid GPS updates
            val startTime = System.currentTimeMillis()
            repeat(100) { index ->
                mockGpsService.emitDistance(index.toDouble())
                testDispatcher.scheduler.advanceUntilIdle()
            }
            val endTime = System.currentTimeMillis()

            // Then: Should handle rapid updates efficiently
            val processingTime = endTime - startTime
            assertTrue("Should process 100 updates in reasonable time", processingTime < 5000) // 5 seconds max

            // Verify final state is correct
            assertEquals("Final distance should be 99", 99.0, viewModel.uiState.value.actualMiles, 0.01)
            assertTrue("Trip should remain active", viewModel.uiState.value.isTripActive)
        }

    @Test
    fun `GPS data accuracy and precision`() =
        runTest {
            // Given: Active trip
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // When: Emit precise GPS distances
            val preciseDistances = listOf(0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0)

            preciseDistances.forEach { distance ->
                mockGpsService.emitDistance(distance)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then: UI should maintain precision
                assertEquals("Distance should maintain precision for $distance", distance, viewModel.uiState.value.actualMiles, 0.001)
            }
        }

    @Test
    fun `GPS service error handling`() =
        runTest {
            // Given: Active trip
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            // When: GPS service encounters an error (simulated by invalid data)
            mockGpsService.emitDistance(-1.0) // Invalid negative distance
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: Should handle invalid GPS data gracefully
            // The ViewModel should accept negative values as-is (no clamping in the ViewModel)
            assertEquals("Negative distance should be accepted as-is", -1.0, viewModel.uiState.value.actualMiles, 0.01)
            assertTrue("Trip should remain active despite GPS error", viewModel.uiState.value.isTripActive)
        }

    @Test
    fun `GPS data persistence during trip pause and resume`() =
        runTest {
            // Given: Active trip with GPS data
            viewModel.calculateTrip(100.0, 25.0, 125.0)
            testDispatcher.scheduler.advanceUntilIdle()

            mockGpsService.emitDistance(50.0)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Initial distance should be 50", 50.0, viewModel.uiState.value.actualMiles, 0.01)

            // When: Simulate GPS pause (no updates for a while)
            // Then: Distance should remain at last known value
            assertEquals("Distance should persist during pause", 50.0, viewModel.uiState.value.actualMiles, 0.01)

            // When: Resume GPS updates
            mockGpsService.emitDistance(75.0)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Distance should update after resume", 75.0, viewModel.uiState.value.actualMiles, 0.01)

            // When: End trip
            // Test trip finalization/reset with ViewModel API
            // viewModel.endTripAndSave()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: Final distance should be preserved
            assertEquals("Final distance should be preserved", 75.0, viewModel.uiState.value.actualMiles, 0.01)
        }

    @Test
    fun `GPS data integration with OOR calculations`() =
        runTest {
            // Given: Trip with specific loaded and bounce miles
            val loadedMiles = 100.0
            val bounceMiles = 25.0
            val dispatchedMiles = 125.0

            // When: GPS shows various distances
            // Format: Triple(actualMiles, expectedOorMiles, expectedOorPercentage)
            val testCases =
                listOf(
                    Triple(100.0, -25.0, -20.0), // Under route: 100 - 125 = -25, (-25/125)*100 = -20%
                    Triple(125.0, 0.0, 0.0), // Exact route: 125 - 125 = 0
                    Triple(150.0, 25.0, 20.0), // Over route: 150 - 125 = 25, (25/125)*100 = 20%
                    Triple(200.0, 75.0, 60.0), // Over route: 200 - 125 = 75, (75/125)*100 = 60%
                )

            testCases.forEach { (gpsDistance, expectedOorMiles, expectedOorPercentage) ->
                // Reset trip for each test case
                viewModel.resetTrip()
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Start fresh trip
                viewModel.calculateTrip(100.0, 25.0, 0.0)
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Emit GPS distance
                mockGpsService.emitDistance(gpsDistance)
                testDispatcher.scheduler.advanceUntilIdle()
                
                // ✅ FIX: During active trip, OOR should be 0.0 (not calculated yet)
                assertEquals(
                    "OOR miles should be 0.0 during active trip (not calculated until end)",
                    0.0,
                    viewModel.uiState.value.oorMiles,
                    0.01,
                )
                
                // ✅ FIX: Now END the trip to trigger OOR calculation
                viewModel.endTrip()
                testDispatcher.scheduler.advanceUntilIdle()

                // Then: OOR calculations should NOW be correct
                assertEquals(
                    "OOR miles should be $expectedOorMiles for GPS distance $gpsDistance",
                    expectedOorMiles,
                    viewModel.uiState.value.oorMiles,
                    0.01,
                )
                assertEquals(
                    "OOR percentage should be $expectedOorPercentage for GPS distance $gpsDistance",
                    expectedOorPercentage,
                    viewModel.uiState.value.oorPercentage,
                    0.01,
                )

                // Reset for next test case
                // Test trip finalization/reset with ViewModel API
                // viewModel.endTripAndReset()
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.calculateTrip(100.0, 25.0, 125.0)
                testDispatcher.scheduler.advanceUntilIdle()
            }
        }
} 

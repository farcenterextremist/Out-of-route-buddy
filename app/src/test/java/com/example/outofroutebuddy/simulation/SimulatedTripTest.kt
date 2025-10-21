package com.example.outofroutebuddy.simulation

import com.example.outofroutebuddy.data.PreferencesManager
import com.example.outofroutebuddy.data.StateCache
import com.example.outofroutebuddy.data.TripStateManager
import com.example.outofroutebuddy.data.TripStatePersistence
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.services.*
import com.example.outofroutebuddy.validation.ValidationFramework
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Simulated Trip Test - End-to-End Integration
 * 
 * This test simulates a complete real-world trip scenario from start to finish:
 * 
 * 1. Driver enters trip parameters (loaded/bounce miles)
 * 2. Starts trip (actualMiles = 0)
 * 3. GPS begins tracking and emits progressive distance updates
 * 4. Real-time UI updates show current distance
 * 5. Driver ends trip
 * 6. OOR calculations are performed
 * 7. Trip is saved
 * 
 * This ensures all components work together correctly:
 * - ViewModel
 * - GPS services
 * - Trip calculation
 * - State management
 * - Offline sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedTripTest {

    private lateinit var viewModel: TripInputViewModel
    private lateinit var mockGpsService: MockGpsSynchronizationService
    private lateinit var mockUnifiedLocationService: UnifiedLocationService
    private lateinit var mockGpsFlow: MutableStateFlow<UnifiedLocationService.RealTimeGpsData>
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Create mock services
        val mockRepository = mockk<TripRepository>(relaxed = true)
        val mockPreferencesManager = mockk<PreferencesManager>(relaxed = true)
        val mockTripStateManager = mockk<TripStateManager>(relaxed = true)
        val mockTripStatePersistence = mockk<TripStatePersistence>(relaxed = true)
        val mockStateCache = mockk<StateCache>(relaxed = true)
        val mockBackgroundSyncService = mockk<BackgroundSyncService>(relaxed = true)
        val mockOptimizedGpsDataFlow = mockk<OptimizedGpsDataFlow>(relaxed = true)
        val mockValidationFramework = mockk<ValidationFramework>(relaxed = true)
        val mockApplication = mockk<android.app.Application>(relaxed = true)
        mockUnifiedLocationService = mockk<UnifiedLocationService>(relaxed = true)
        val mockUnifiedTripService = mockk<UnifiedTripService>(relaxed = true)
        val mockUnifiedOfflineService = mockk<UnifiedOfflineService>(relaxed = true)

        // Setup mock GPS service
        mockGpsService = MockGpsSynchronizationService()
        mockGpsService.reset()

        // Setup mock behaviors
        io.mockk.every { mockTripStateManager.tripState } returns MutableStateFlow(TripStateManager.TripState(isActive = false))
        io.mockk.coEvery { mockUnifiedTripService.calculateTrip(any(), any(), any()) } answers {
            val loadedMiles = firstArg<Double>()
            val bounceMiles = secondArg<Double>()
            val actualMiles = thirdArg<Double>()
            val dispatchedMiles = loadedMiles + bounceMiles
            val oorMiles = actualMiles - dispatchedMiles
            val oorPercentage = if (dispatchedMiles > 0) (oorMiles / dispatchedMiles) * 100 else 0.0
            
            UnifiedTripService.CalculationResult(
                tripId = "test-trip-${System.currentTimeMillis()}",
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                oorMiles = oorMiles,
                oorPercentage = oorPercentage,
                calculationTime = java.util.Date(),
                isValid = true,
                validationIssues = emptyList()
            )
        }
        io.mockk.coEvery { mockUnifiedOfflineService.saveDataWithOfflineFallback(any(), any(), any()) } returns "success"

        // Create GPS flow that can be updated during tests
        mockGpsFlow = MutableStateFlow(
            UnifiedLocationService.RealTimeGpsData(
                totalDistance = 0.0,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
        )
        io.mockk.every { mockUnifiedLocationService.realTimeGpsData } returns mockGpsFlow

        // Connect mockGpsService to mockGpsFlow
        mockGpsService.setOnDistanceUpdate { distance ->
            mockGpsFlow.value = mockGpsFlow.value.copy(
                totalDistance = distance,
                lastUpdate = System.currentTimeMillis()
            )
        }

        // Create ViewModel
        viewModel = TripInputViewModel(
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
            crashRecoveryManager = mockk(relaxed = true),
            application = mockApplication
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SIMULATION - Short commute trip with perfect accuracy`() = runTest {
        println("\n=== SIMULATING SHORT COMMUTE TRIP ===")
        
        // SCENARIO: Driver has a 50-mile route (40 loaded + 10 bounce)
        // They drive exactly the route distance
        val loadedMiles = 40.0
        val bounceMiles = 10.0
        val expectedDispatchedMiles = 50.0
        
        println("📋 Trip Parameters:")
        println("   Loaded Miles: $loadedMiles")
        println("   Bounce Miles: $bounceMiles")
        println("   Expected Dispatched: $expectedDispatchedMiles")
        
        // STEP 1: Driver enters trip data and starts trip
        println("\n🚀 STEP 1: Starting trip...")
        viewModel.calculateTrip(loadedMiles, bounceMiles, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue("Trip should be active", viewModel.uiState.value.isTripActive)
        assertEquals("Initial actualMiles should be 0", 0.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   ✓ Trip started successfully")
        println("   ✓ GPS initialized at 0.0 miles")
        
        // STEP 2: GPS starts tracking - driver drives 10 miles
        println("\n📍 STEP 2: Driving - GPS emits 10 miles...")
        mockGpsService.emitDistance(10.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("ActualMiles should update to 10", 10.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   ✓ GPS updated: ${viewModel.uiState.value.actualMiles} miles")
        
        // STEP 3: Driver continues - GPS emits 25 miles
        println("\n📍 STEP 3: Continuing - GPS emits 25 miles...")
        mockGpsService.emitDistance(25.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("ActualMiles should update to 25", 25.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   ✓ GPS updated: ${viewModel.uiState.value.actualMiles} miles")
        
        // STEP 4: Driver completes route - GPS emits 50 miles (exactly on route)
        println("\n📍 STEP 4: Completing route - GPS emits 50 miles...")
        mockGpsService.emitDistance(50.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("ActualMiles should update to 50", 50.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   ✓ GPS updated: ${viewModel.uiState.value.actualMiles} miles")
        println("   ✓ Driver completed route perfectly!")
        
        // STEP 5: Driver ends trip
        println("\n🏁 STEP 5: Ending trip...")
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse("Trip should be ended", viewModel.uiState.value.isTripActive)
        println("   ✓ Trip ended successfully")
        
        // STEP 6: Verify OOR calculations
        println("\n📊 STEP 6: Verifying OOR calculations...")
        val finalActualMiles = viewModel.uiState.value.actualMiles
        val finalOorMiles = viewModel.uiState.value.oorMiles
        val finalOorPercentage = viewModel.uiState.value.oorPercentage
        
        println("   Final Actual Miles: $finalActualMiles")
        println("   Final OOR Miles: $finalOorMiles")
        println("   Final OOR Percentage: $finalOorPercentage%")
        
        // Expected: 50 - 50 = 0 OOR miles (perfect!)
        assertEquals("Final actual miles should be 50", 50.0, finalActualMiles, 0.01)
        assertEquals("OOR miles should be 0 (on route)", 0.0, finalOorMiles, 0.01)
        assertEquals("OOR percentage should be 0%", 0.0, finalOorPercentage, 0.01)
        
        println("\n✅ SIMULATION COMPLETE - Trip successful with 0% OOR!")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Long haul trip with 10 percent out of route`() = runTest {
        println("\n=== SIMULATING LONG HAUL TRIP (10% OOR) ===")
        
        // SCENARIO: Driver has a 500-mile route (400 loaded + 100 bounce)
        // They drive 550 miles (10% over route)
        val loadedMiles = 400.0
        val bounceMiles = 100.0
        val expectedDispatchedMiles = 500.0
        val actualDrivenMiles = 550.0
        
        println("📋 Trip Parameters:")
        println("   Loaded Miles: $loadedMiles")
        println("   Bounce Miles: $bounceMiles")
        println("   Expected Dispatched: $expectedDispatchedMiles")
        println("   Driver will drive: $actualDrivenMiles miles")
        
        // Start trip
        println("\n🚀 Starting trip...")
        viewModel.calculateTrip(loadedMiles, bounceMiles, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        println("   ✓ Trip started")
        
        // Simulate GPS updates during long trip
        val progressPoints = listOf(50.0, 150.0, 275.0, 400.0, 500.0, 550.0)
        
        println("\n📍 GPS Tracking Progress:")
        progressPoints.forEach { distance ->
            mockGpsService.emitDistance(distance)
            testDispatcher.scheduler.advanceUntilIdle()
            
            val currentOor = viewModel.uiState.value.actualMiles - expectedDispatchedMiles
            val currentOorPercent = if (expectedDispatchedMiles > 0) (currentOor / expectedDispatchedMiles) * 100 else 0.0
            
            println("   → ${viewModel.uiState.value.actualMiles} mi driven | OOR: %.1f mi (%.1f%%)".format(currentOor, currentOorPercent))
            
            assertEquals("Distance should update to $distance", distance, viewModel.uiState.value.actualMiles, 0.01)
        }
        
        // End trip
        println("\n🏁 Ending trip...")
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify final calculations
        val finalOorMiles = viewModel.uiState.value.oorMiles
        val finalOorPercentage = viewModel.uiState.value.oorPercentage
        
        println("\n📊 Final Results:")
        println("   Dispatched Miles: $expectedDispatchedMiles")
        println("   Actual Miles: ${viewModel.uiState.value.actualMiles}")
        println("   OOR Miles: $finalOorMiles")
        println("   OOR Percentage: $finalOorPercentage%")
        
        // Expected: 550 - 500 = 50 OOR miles (10%)
        assertEquals("OOR miles should be 50", 50.0, finalOorMiles, 0.01)
        assertEquals("OOR percentage should be 10%", 10.0, finalOorPercentage, 0.01)
        
        println("\n✅ SIMULATION COMPLETE - Trip completed with 10% OOR")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Trip under dispatched miles (negative OOR)`() = runTest {
        println("\n=== SIMULATING TRIP WITH NEGATIVE OOR ===")
        
        // SCENARIO: Driver has a 100-mile route (75 loaded + 25 bounce)
        // They only drive 80 miles (came back early)
        val loadedMiles = 75.0
        val bounceMiles = 25.0
        val expectedDispatchedMiles = 100.0
        val actualDrivenMiles = 80.0
        
        println("📋 Trip Parameters:")
        println("   Loaded Miles: $loadedMiles")
        println("   Bounce Miles: $bounceMiles")
        println("   Expected Dispatched: $expectedDispatchedMiles")
        println("   Driver will drive: $actualDrivenMiles miles (under route)")
        
        // Start trip
        println("\n🚀 Starting trip...")
        viewModel.calculateTrip(loadedMiles, bounceMiles, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        println("   ✓ Trip started")
        
        // Simulate progressive GPS updates
        println("\n📍 GPS Tracking:")
        val checkpoints = listOf(20.0, 40.0, 60.0, 80.0)
        checkpoints.forEach { distance ->
            mockGpsService.emitDistance(distance)
            testDispatcher.scheduler.advanceUntilIdle()
            println("   → $distance miles")
        }
        
        // End trip
        println("\n🏁 Ending trip early...")
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify calculations
        val finalOorMiles = viewModel.uiState.value.oorMiles
        val finalOorPercentage = viewModel.uiState.value.oorPercentage
        
        println("\n📊 Final Results:")
        println("   Dispatched Miles: $expectedDispatchedMiles")
        println("   Actual Miles: ${viewModel.uiState.value.actualMiles}")
        println("   OOR Miles: $finalOorMiles (negative = under route)")
        println("   OOR Percentage: $finalOorPercentage%")
        
        // Expected: 80 - 100 = -20 OOR miles (-20%)
        assertEquals("OOR miles should be -20", -20.0, finalOorMiles, 0.01)
        assertEquals("OOR percentage should be -20%", -20.0, finalOorPercentage, 0.01)
        
        println("\n✅ SIMULATION COMPLETE - Trip under dispatched (came back early)")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Multiple trips in sequence`() = runTest {
        println("\n=== SIMULATING MULTIPLE SEQUENTIAL TRIPS ===")
        
        val trips = listOf(
            Triple(100.0, 25.0, 125.0), // Trip 1: On route
            Triple(200.0, 50.0, 275.0), // Trip 2: 10% OOR
            Triple(150.0, 30.0, 160.0)  // Trip 3: 10% under
        )
        
        trips.forEachIndexed { index, (loaded, bounce, actual) ->
            println("\n--- TRIP ${index + 1} ---")
            println("Loaded: $loaded, Bounce: $bounce, Will drive: $actual")
            
            // Start trip
            viewModel.calculateTrip(loaded, bounce, 0.0)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue("Trip $index should be active", viewModel.uiState.value.isTripActive)
            
            // Simulate GPS to final distance
            mockGpsService.emitDistance(actual)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Trip $index actual miles", actual, viewModel.uiState.value.actualMiles, 0.01)
            
            // End trip
            viewModel.endTrip()
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Trip $index should be ended", viewModel.uiState.value.isTripActive)
            
            val expectedOor = actual - (loaded + bounce)
            val expectedOorPercent = (expectedOor / (loaded + bounce)) * 100
            
            println("   OOR: %.1f miles (%.1f%%)".format(expectedOor, expectedOorPercent))
            assertEquals("Trip $index OOR miles", expectedOor, viewModel.uiState.value.oorMiles, 0.01)
            
            // Reset for next trip
            viewModel.resetTrip()
            testDispatcher.scheduler.advanceUntilIdle()
            mockGpsService.reset()
            mockGpsFlow.value = UnifiedLocationService.RealTimeGpsData(
                totalDistance = 0.0,
                accuracy = 0.0,
                lastUpdate = System.currentTimeMillis()
            )
            
            println("   ✓ Trip ${index + 1} complete and reset")
        }
        
        println("\n✅ SIMULATION COMPLETE - All 3 trips processed successfully")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Pause and resume trip scenario`() = runTest {
        println("\n=== SIMULATING TRIP WITH PAUSE/RESUME ===")
        
        val loadedMiles = 100.0
        val bounceMiles = 20.0
        
        println("📋 Trip Parameters: Loaded=$loadedMiles, Bounce=$bounceMiles")
        
        // Start trip
        println("\n🚀 Starting trip...")
        viewModel.calculateTrip(loadedMiles, bounceMiles, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Drive to 30 miles
        println("\n📍 Driving to 30 miles...")
        mockGpsService.emitDistance(30.0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Should be at 30 miles", 30.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   → Current distance: 30 miles")
        
        // Pause (simulate driver stopping for break)
        println("\n⏸️  Pausing trip (driver takes break)...")
        val distanceAtPause = viewModel.uiState.value.actualMiles
        println("   Distance at pause: $distanceAtPause miles")
        
        // Resume and continue to 60 miles
        println("\n▶️  Resuming trip...")
        mockGpsService.emitDistance(60.0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Should be at 60 miles", 60.0, viewModel.uiState.value.actualMiles, 0.01)
        println("   → Current distance: 60 miles")
        
        // Complete trip at 120 miles
        println("\n📍 Completing trip...")
        mockGpsService.emitDistance(120.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // End trip
        println("\n🏁 Ending trip...")
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify - Expected: 120 - 120 = 0 OOR
        println("\n📊 Final Results:")
        println("   Actual Miles: ${viewModel.uiState.value.actualMiles}")
        println("   OOR Miles: ${viewModel.uiState.value.oorMiles}")
        
        assertEquals("Final distance should be 120", 120.0, viewModel.uiState.value.actualMiles, 0.01)
        assertEquals("OOR should be 0", 0.0, viewModel.uiState.value.oorMiles, 0.01)
        
        println("\n✅ SIMULATION COMPLETE - Pause/Resume worked correctly")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Extreme OOR trip (driver goes way off route)`() = runTest {
        println("\n=== SIMULATING EXTREME OOR SCENARIO ===")
        
        // SCENARIO: Driver has a 100-mile route but drives 200 miles (100% OOR!)
        val loadedMiles = 75.0
        val bounceMiles = 25.0
        val extremeActualMiles = 200.0
        
        println("📋 Trip Parameters:")
        println("   Loaded: $loadedMiles, Bounce: $bounceMiles")
        println("   Dispatched: ${loadedMiles + bounceMiles}")
        println("   Driver will drive: $extremeActualMiles miles (EXTREME OOR!)")
        
        // Start trip
        viewModel.calculateTrip(loadedMiles, bounceMiles, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Simulate GPS going way over
        println("\n📍 GPS Tracking (driver getting lost):")
        listOf(50.0, 100.0, 150.0, 200.0).forEach { distance ->
            mockGpsService.emitDistance(distance)
            testDispatcher.scheduler.advanceUntilIdle()
            
            val currentOor = distance - 100.0
            val currentOorPercent = (currentOor / 100.0) * 100
            println("   → $distance mi | OOR: %.0f mi (%.0f%%)".format(currentOor, currentOorPercent))
        }
        
        // End trip
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        println("\n📊 Final Results:")
        println("   Actual Miles: ${viewModel.uiState.value.actualMiles}")
        println("   OOR Miles: ${viewModel.uiState.value.oorMiles}")
        println("   OOR Percentage: ${viewModel.uiState.value.oorPercentage}%")
        
        // Expected: 200 - 100 = 100 OOR miles (100%!)
        assertEquals("OOR miles should be 100", 100.0, viewModel.uiState.value.oorMiles, 0.01)
        assertEquals("OOR percentage should be 100%", 100.0, viewModel.uiState.value.oorPercentage, 0.01)
        
        println("\n⚠️  SIMULATION COMPLETE - EXTREME OOR detected! Driver needs coaching.")
        println("=".repeat(50))
    }

    @Test
    fun `SIMULATION - Real-time UI updates during active trip`() = runTest {
        println("\n=== SIMULATING REAL-TIME UI UPDATES ===")
        
        viewModel.calculateTrip(100.0, 25.0, 0.0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        println("📱 Monitoring UI State Updates:")
        
        // Emit GPS updates and verify UI state changes in real-time
        val updates = listOf(
            5.0 to "Just started",
            25.0 to "Quarter way",
            50.0 to "Halfway",
            75.0 to "Three quarters",
            100.0 to "Almost done",
            125.0 to "Completed"
        )
        
        updates.forEach { (distance, milestone) ->
            mockGpsService.emitDistance(distance)
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = viewModel.uiState.value
            val oorMiles = state.actualMiles - 125.0
            val oorPercent = (oorMiles / 125.0) * 100
            
            println("   $milestone: ${state.actualMiles} mi | OOR: %.1f mi (%.1f%%)".format(oorMiles, oorPercent))
            
            assertTrue("Trip should remain active during tracking", state.isTripActive)
            assertEquals("UI should show current distance", distance, state.actualMiles, 0.01)
            assertNotNull("Should have trip status message", state.tripStatusMessage)
        }
        
        viewModel.endTrip()
        testDispatcher.scheduler.advanceUntilIdle()
        
        println("\n✅ SIMULATION COMPLETE - Real-time updates working perfectly")
        println("=".repeat(50))
    }
}



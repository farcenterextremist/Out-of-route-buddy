package com.example.outofroutebuddy.utils

import android.location.Location
import com.example.outofroutebuddy.services.LocationCache
import com.example.outofroutebuddy.services.LocationValidationService
import com.example.outofroutebuddy.services.PerformanceMonitor
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.services.LocationValidationService.ValidationResult
import java.util.concurrent.ConcurrentHashMap

/**
 * ✅ NEW: Mock service implementations for comprehensive testing
 *
 * This module provides mock implementations of core services for testing:
 * - MockLocationValidationService: Simulates validation behavior
 * - MockLocationCache: Simulates caching behavior
 * - MockPerformanceMonitor: Simulates performance monitoring
 * - MockServiceFactory: Creates mock service instances
 */
object MockServices {

    // ==================== MOCK LOCATION VALIDATION SERVICE ====================

    /**
     * Mock implementation of LocationValidationService for testing
     */
    class MockLocationValidationService : LocationValidationService() {
        
        private val validationResults = mutableMapOf<String, ValidationResult>()
        private var shouldFail = false
        private var failReason = "mock_failure"
        private var validationDelay = 0L

        /**
         * Sets whether the mock service should return failed validations
         */
        fun setShouldFail(shouldFail: Boolean, reason: String = "mock_failure") {
            this.shouldFail = shouldFail
            this.failReason = reason
        }

        /**
         * Sets artificial delay for testing performance
         */
        fun setValidationDelay(delayMs: Long) {
            this.validationDelay = delayMs
        }

        /**
         * Pre-configures validation results for specific locations
         */
        fun setValidationResult(locationKey: String, result: ValidationResult) {
            validationResults[locationKey] = result
        }

        override fun validateLocation(
            location: Location,
            lastLocation: Location?,
            lastUpdateTime: Long,
            lastSpeed: Float,
            config: LocationValidationService.ValidationConfigData
        ): ValidationResult {
            // Simulate delay if configured
            if (validationDelay > 0) {
                Thread.sleep(validationDelay)
            }

            // Check for pre-configured results
            val locationKey = "${location.latitude},${location.longitude}"
            validationResults[locationKey]?.let { return it }

            // Simulate validation logic
            return if (shouldFail) {
                createMockInvalidResult(failReason)
            } else {
                createMockValidResult(location)
            }
        }

        private fun createMockValidResult(location: Location): ValidationResult {
            return ValidationResult.Valid
        }

        private fun createMockInvalidResult(reason: String): ValidationResult {
            return ValidationResult.Invalid(
                reason = "Mock validation failure: $reason",
                severity = LocationValidationService.ValidationSeverity.ERROR
            )
        }
    }

    // ==================== MOCK LOCATION CACHE ====================

    /**
     * Mock implementation of LocationCache for testing
     */
    class MockLocationCache : LocationCache() {
        
        private val cache = ConcurrentHashMap<String, ValidationResult>()
        private var hitRate = 0.8 // 80% default hit rate
        private var shouldFail = false
        private var maxCacheSize = 100

        /**
         * Sets the mock cache hit rate for testing
         */
        fun setHitRate(hitRate: Double) {
            this.hitRate = hitRate.coerceIn(0.0, 1.0)
        }

        /**
         * Sets whether the cache should simulate failures
         */
        fun setShouldFail(shouldFail: Boolean) {
            this.shouldFail = shouldFail
        }

        /**
         * Sets the maximum cache size for testing
         */
        fun setMaxCacheSize(size: Int) {
            this.maxCacheSize = size
        }

        /**
         * Pre-populates the cache with test data
         */
        fun populateCache(locations: List<Location>, results: List<ValidationResult>) {
            locations.zip(results).forEach { (location, result) ->
                val key = createCacheKey(location, createMockContext())
                cache[key] = result
            }
        }

        override suspend fun getCachedValidation(
            location: Location,
            context: LocationCache.ValidationContext
        ): ValidationResult? {
            if (shouldFail) return null

            val key = createCacheKey(location, context)
            return if (Math.random() < hitRate) {
                cache[key]
            } else {
                null
            }
        }

        override suspend fun cacheValidation(
            location: Location,
            context: LocationCache.ValidationContext,
            result: ValidationResult
        ) {
            if (shouldFail) return

            val key = createCacheKey(location, context)
            if (cache.size < maxCacheSize) {
                cache[key] = result
            }
        }

        // Removed override of getCacheStats to avoid overriding a final method
        fun getMockCacheStats(): LocationCache.CachePerformanceStats {
            val totalRequests = 1000 // Mock total requests
            val hits = (totalRequests * hitRate).toLong()
            val misses = totalRequests - hits

            return LocationCache.CachePerformanceStats(
                cacheHits = hits,
                cacheMisses = misses,
                hitRate = hitRate,
                lruCacheSize = cache.size,
                highFrequencyCacheSize = cache.size / 2,
                maxCacheSize = maxCacheSize
            )
        }

        private fun createCacheKey(location: Location, context: LocationCache.ValidationContext): String {
            return "${location.latitude},${location.longitude},${context.speed},${context.locationAge}"
        }

        private fun createMockContext(): LocationCache.ValidationContext {
            return LocationCache.ValidationContext(
                speed = 25f,
                locationAge = System.currentTimeMillis(),
                isVehicle = true
            )
        }
    }

    // ==================== MOCK PERFORMANCE MONITOR ====================

    /**
     * Mock implementation of PerformanceMonitor for testing
     */
    class MockPerformanceMonitor : PerformanceMonitor() {
        
        private val operationTimes = mutableMapOf<String, MutableList<Long>>()
        private val memorySnapshots = mutableListOf<PerformanceMonitor.MemorySnapshot>()
        private var shouldFail = false
        private var mockUptime = 0L

        /**
         * Sets whether the monitor should simulate failures
         */
        fun setShouldFail(shouldFail: Boolean) {
            this.shouldFail = shouldFail
        }

        /**
         * Sets mock uptime for testing
         */
        fun setMockUptime(uptimeMs: Long) {
            this.mockUptime = uptimeMs
        }

        /**
         * Pre-populates operation times for testing
         */
        fun setOperationTimes(operation: String, times: List<Long>) {
            operationTimes[operation] = times.toMutableList()
        }

        override suspend fun trackValidationTime(operation: String, duration: Long) {
            if (shouldFail) return
            
            operationTimes.getOrPut(operation) { mutableListOf() }.add(duration)
        }

        override suspend fun trackMemoryUsage(memoryUsage: Long, context: String) {
            if (shouldFail) return
            
            memorySnapshots.add(
                PerformanceMonitor.MemorySnapshot(
                    memoryUsage = memoryUsage,
                    timestamp = System.currentTimeMillis(),
                    context = context
                )
            )
        }

        override suspend fun generatePerformanceReport(): PerformanceMonitor.PerformanceReport {
            val uptime = mockUptime
            val avgValidationTime = operationTimes.values.flatten().average().toLong()
            val avgMemoryUsage = memorySnapshots.map { it.memoryUsage }.average().toLong()
            
            return PerformanceMonitor.PerformanceReport(
                uptime = uptime,
                totalValidations = operationTimes.values.flatten().size.toLong(),
                averageValidationTime = avgValidationTime,
                peakMemoryUsage = memorySnapshots.maxOfOrNull { it.memoryUsage } ?: 0L,
                averageMemoryUsage = avgMemoryUsage,
                operationAverages = operationTimes.mapValues { (_, timings) -> timings.average().toLong() },
                performanceAlerts = emptyList(),
                recommendations = listOf("Mock performance report")
            )
        }
    }

    // ==================== MOCK SERVICE FACTORY ====================

    /**
     * Factory for creating mock service instances
     */
    object MockServiceFactory {
        
        /**
         * Creates a mock validation service with default settings
         */
        fun createMockValidationService(): MockLocationValidationService {
            return MockLocationValidationService()
        }

        /**
         * Creates a mock cache with default settings
         */
        fun createMockCache(): MockLocationCache {
            return MockLocationCache()
        }

        /**
         * Creates a mock performance monitor with default settings
         */
        fun createMockPerformanceMonitor(): MockPerformanceMonitor {
            return MockPerformanceMonitor()
        }

        /**
         * Creates a complete mock service suite
         */
        fun createMockServiceSuite(): MockServiceSuite {
            return MockServiceSuite(
                validationService = createMockValidationService(),
                cache = createMockCache(),
                performanceMonitor = createMockPerformanceMonitor()
            )
        }
    }

    /**
     * Complete suite of mock services for integration testing
     */
    data class MockServiceSuite(
        val validationService: MockLocationValidationService,
        val cache: MockLocationCache,
        val performanceMonitor: MockPerformanceMonitor
    ) {
        /**
         * Configures all services for a specific test scenario
         */
        fun configureForScenario(scenario: String) {
            when (scenario) {
                "high_performance" -> {
                    validationService.setValidationDelay(0L)
                    cache.setHitRate(0.9)
                    performanceMonitor.setMockUptime(3600000L) // 1 hour
                }
                "low_performance" -> {
                    validationService.setValidationDelay(100L)
                    cache.setHitRate(0.3)
                    performanceMonitor.setMockUptime(60000L) // 1 minute
                }
                "failure_scenario" -> {
                    validationService.setShouldFail(true, "mock_failure")
                    cache.setShouldFail(true)
                    performanceMonitor.setShouldFail(true)
                }
                else -> {
                    // Default configuration
                    validationService.setValidationDelay(10L)
                    cache.setHitRate(0.8)
                    performanceMonitor.setMockUptime(300000L) // 5 minutes
                }
            }
        }
    }
    
    // ==================== MOCK UNIFIED TRIP SERVICE ====================
    
    /**
     * ✅ NEW (#30 Infrastructure): Mock UnifiedTripService
     * 
     * Mock implementation for testing trip calculations, period statistics,
     * and period mode switching without real service dependencies.
     */
    class MockUnifiedTripService {
        private var currentPeriodMode = "STANDARD"
        private val tripHistory = mutableListOf<MockTrip>()
        private var shouldFail = false
        private var failureMessage = "Mock failure"
        
        data class MockTrip(
            val loadedMiles: Double,
            val bounceMiles: Double,
            val actualMiles: Double,
            val oorMiles: Double,
            val oorPercentage: Double,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        data class MockPeriodCalculation(
            val totalTrips: Int,
            val totalDistance: Double,
            val totalOor: Double,
            val averageOorPercentage: Double
        )
        
        fun setPeriodMode(mode: String) {
            currentPeriodMode = mode
        }
        
        fun getPeriodMode(): String = currentPeriodMode
        
        fun calculateTrip(loaded: Double, bounce: Double, actual: Double): MockTrip {
            if (shouldFail) throw Exception(failureMessage)
            
            val dispatched = loaded + bounce
            val oor = actual - dispatched
            val oorPercent = if (dispatched > 0) (oor / dispatched) * 100 else 0.0
            
            val trip = MockTrip(loaded, bounce, actual, oor, oorPercent)
            tripHistory.add(trip)
            return trip
        }
        
        fun calculatePeriodStatistics(): MockPeriodCalculation {
            if (shouldFail) throw Exception(failureMessage)
            
            return MockPeriodCalculation(
                totalTrips = tripHistory.size,
                totalDistance = tripHistory.sumOf { it.actualMiles },
                totalOor = tripHistory.sumOf { it.oorMiles },
                averageOorPercentage = if (tripHistory.isNotEmpty()) {
                    tripHistory.map { it.oorPercentage }.average()
                } else 0.0
            )
        }
        
        fun getTripHistory(): List<MockTrip> = tripHistory.toList()
        fun clearHistory() = tripHistory.clear()
        fun setShouldFail(shouldFail: Boolean, message: String = "Mock failure") {
            this.shouldFail = shouldFail
            this.failureMessage = message
        }
    }
    
    // ==================== MOCK UNIFIED LOCATION SERVICE ====================
    
    /**
     * ✅ NEW (#30 Infrastructure): Mock UnifiedLocationService
     * 
     * Mock implementation for testing GPS tracking, location updates,
     * and validation without real location services.
     */
    class MockUnifiedLocationService {
        private var isTracking = false
        private val locationUpdates = mutableListOf<MockLocation>()
        private var shouldFail = false
        private var failureMessage = "Mock GPS failure"
        private var mockAccuracy = 10f
        
        data class MockLocation(
            val latitude: Double,
            val longitude: Double,
            val accuracy: Float,
            val speed: Float,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        fun startTracking(): Boolean {
            if (shouldFail) throw Exception(failureMessage)
            isTracking = true
            return true
        }
        
        fun stopTracking(): Boolean {
            isTracking = false
            return true
        }
        
        fun isTracking(): Boolean = isTracking
        
        fun addLocationUpdate(lat: Double, lon: Double, speed: Float = 0f) {
            if (!isTracking) throw IllegalStateException("Not tracking")
            if (shouldFail) throw Exception(failureMessage)
            
            locationUpdates.add(MockLocation(lat, lon, mockAccuracy, speed))
        }
        
        fun getLocationHistory(): List<MockLocation> = locationUpdates.toList()
        fun clearLocationHistory() = locationUpdates.clear()
        fun getLocationCount(): Int = locationUpdates.size
        
        fun setMockAccuracy(accuracy: Float) {
            mockAccuracy = accuracy
        }
        
        fun setShouldFail(shouldFail: Boolean, message: String = "Mock GPS failure") {
            this.shouldFail = shouldFail
            this.failureMessage = message
        }
    }
    
    // ==================== MOCK UNIFIED OFFLINE SERVICE ====================
    
    /**
     * ✅ NEW (#30 Infrastructure): Mock UnifiedOfflineService
     * 
     * Mock implementation for testing offline sync, queue management,
     * and network state handling without real network dependencies.
     */
    class MockUnifiedOfflineService {
        private var isOnline = true
        private val syncQueue = mutableListOf<MockSyncItem>()
        private var shouldFail = false
        private var failureMessage = "Mock sync failure"
        private val syncHistory = mutableListOf<MockSyncResult>()
        
        data class MockSyncItem(
            val dataType: String,
            val data: Any,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        data class MockSyncResult(
            val success: Boolean,
            val itemsProcessed: Int,
            val itemsFailed: Int,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        fun setOnline(online: Boolean) {
            isOnline = online
        }
        
        fun isOnline(): Boolean = isOnline
        
        fun queueSync(dataType: String, data: Any) {
            syncQueue.add(MockSyncItem(dataType, data))
        }
        
        fun performSync(): MockSyncResult {
            if (shouldFail) throw Exception(failureMessage)
            if (!isOnline) {
                val result = MockSyncResult(false, 0, syncQueue.size)
                syncHistory.add(result)
                return result
            }
            
            val itemCount = syncQueue.size
            syncQueue.clear()
            val result = MockSyncResult(true, itemCount, 0)
            syncHistory.add(result)
            return result
        }
        
        fun getQueueSize(): Int = syncQueue.size
        fun getSyncHistory(): List<MockSyncResult> = syncHistory.toList()
        fun clearQueue() = syncQueue.clear()
        fun clearHistory() = syncHistory.clear()
        
        fun setShouldFail(shouldFail: Boolean, message: String = "Mock sync failure") {
            this.shouldFail = shouldFail
            this.failureMessage = message
        }
    }
    
    // ==================== UPDATED MOCK SERVICE FACTORY ====================
    
    /**
     * Factory for creating ALL mock service instances (includes unified services)
     */
    object UnifiedMockServiceFactory {
        
        /**
         * Creates a mock unified trip service
         */
        fun createMockTripService(): MockUnifiedTripService {
            return MockUnifiedTripService()
        }
        
        /**
         * Creates a mock unified location service
         */
        fun createMockLocationService(): MockUnifiedLocationService {
            return MockUnifiedLocationService()
        }
        
        /**
         * Creates a mock unified offline service
         */
        fun createMockOfflineService(): MockUnifiedOfflineService {
            return MockUnifiedOfflineService()
        }
        
        /**
         * Creates complete unified service suite
         */
        fun createUnifiedServiceSuite(): UnifiedMockServiceSuite {
            return UnifiedMockServiceSuite(
                tripService = createMockTripService(),
                locationService = createMockLocationService(),
                offlineService = createMockOfflineService()
            )
        }
    }
    
    /**
     * Complete suite of unified mock services
     */
    data class UnifiedMockServiceSuite(
        val tripService: MockUnifiedTripService,
        val locationService: MockUnifiedLocationService,
        val offlineService: MockUnifiedOfflineService
    ) {
        /**
         * Reset all services to clean state
         */
        fun resetAll() {
            tripService.clearHistory()
            tripService.setShouldFail(false)
            
            locationService.stopTracking()
            locationService.clearLocationHistory()
            locationService.setShouldFail(false)
            
            offlineService.setOnline(true)
            offlineService.clearQueue()
            offlineService.clearHistory()
            offlineService.setShouldFail(false)
        }
        
        /**
         * Configure for offline scenario
         */
        fun configureOfflineScenario() {
            offlineService.setOnline(false)
            locationService.setMockAccuracy(50f) // Poor accuracy
        }
        
        /**
         * Configure for online scenario
         */
        fun configureOnlineScenario() {
            offlineService.setOnline(true)
            locationService.setMockAccuracy(10f) // Good accuracy
        }
        
        /**
         * Configure for error scenario
         */
        fun configureErrorScenario() {
            tripService.setShouldFail(true)
            locationService.setShouldFail(true)
            offlineService.setShouldFail(true)
        }
    }
} 

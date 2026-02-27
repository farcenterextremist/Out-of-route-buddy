package com.example.outofroutebuddy.services

import android.location.Location
import android.util.Log
import com.example.outofroutebuddy.core.config.ValidationConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

/**
 * ? NEW: Optimized GPS Data Flow System
 * 
 * This class implements efficient GPS data processing with batching, filtering,
 * and intelligent rate limiting to reduce CPU usage and improve battery life.
 */
class OptimizedGpsDataFlow {
    companion object {
        private const val TAG = "OptimizedGpsDataFlow"
        
        // Performance tuning constants - now using centralized config
        private const val BATCH_SIZE = ValidationConfig.GPS_BATCH_SIZE
        private const val BATCH_TIMEOUT_MS = ValidationConfig.GPS_BATCH_TIMEOUT_MS
        private const val MIN_DISTANCE_METERS = ValidationConfig.GPS_MIN_DISTANCE_METERS
        private const val MAX_SPEED_MPH = ValidationConfig.VEHICLE_MAX_SPEED_MPH.toDouble()
        private const val MIN_ACCURACY_METERS = ValidationConfig.GPS_MIN_ACCURACY_METERS
        private const val MAX_ACCURACY_METERS = ValidationConfig.GPS_MAX_ACCURACY_METERS
        
        // Rate limiting - now using centralized config
        private const val MIN_UPDATE_INTERVAL_MS = ValidationConfig.GPS_MIN_UPDATE_INTERVAL_MS
        private const val MAX_UPDATE_INTERVAL_MS = ValidationConfig.GPS_MAX_UPDATE_INTERVAL_MS
        private const val ADAPTIVE_RATE_ENABLED = ValidationConfig.GPS_ADAPTIVE_RATE_ENABLED
    }
    
    // ? DATA FLOWS: Optimized GPS data streams
    private val _rawGpsFlow = MutableSharedFlow<Location>()
    private val _filteredGpsFlow = MutableSharedFlow<Location>()
    private val _batchedGpsFlow = MutableSharedFlow<List<Location>>()
    private val _processedGpsFlow = MutableSharedFlow<ProcessedGpsData>()
    
    // ? EXPOSED FLOWS: Public interfaces
    val rawGpsFlow: SharedFlow<Location> = _rawGpsFlow.asSharedFlow()
    val filteredGpsFlow: SharedFlow<Location> = _filteredGpsFlow.asSharedFlow()
    val batchedGpsFlow: SharedFlow<List<Location>> = _batchedGpsFlow.asSharedFlow()
    val processedGpsFlow: SharedFlow<ProcessedGpsData> = _processedGpsFlow.asSharedFlow()
    
    // ? BATCHING: GPS data batching system
    private val gpsBatch = ConcurrentLinkedQueue<Location>()
    private val batchMutex = Mutex()
    
    // ? FILTERING: GPS data filtering system
    private var lastProcessedLocation: Location? = null
    private var lastUpdateTime = 0L
    
    // ? RATE LIMITING: Adaptive rate limiting system
    private var currentUpdateInterval = MIN_UPDATE_INTERVAL_MS
    private var consecutiveRejections = 0
    private var consecutiveAcceptances = 0
    
    // ? PERFORMANCE METRICS: Track optimization effectiveness
    private var totalGpsPoints = 0L
    private var filteredGpsPoints = 0L
    private var batchedGpsPoints = 0L
    private var processedGpsPoints = 0L
    private var rejectedGpsPoints = 0L
    
    // ? NEW: Performance tracking variables
    private var processingTimes = mutableListOf<Long>()
    private var totalProcessingTime = 0L
    private var processingCount = 0
    
    // ? NEW: Rate limiting configuration
    private var currentMinInterval = MIN_UPDATE_INTERVAL_MS
    private var currentMaxInterval = MAX_UPDATE_INTERVAL_MS
    private var adaptiveRateEnabled = ADAPTIVE_RATE_ENABLED
    
    // ? DATA CLASSES: GPS data structures
    data class ProcessedGpsData(
        val locations: List<Location>,
        val totalDistance: Double,
        val avgSpeed: Double,
        val maxSpeed: Double,
        val avgAccuracy: Double,
        val processingTime: Long,
        val batchSize: Int,
        val qualityScore: Double
    )
    
    /**
     * ? NEW: Data class for GPS batch processing results
     */
    data class GpsBatchResult(
        val totalDistance: Double,
        val avgSpeed: Double,
        val maxSpeed: Double,
        val avgAccuracy: Double,
        val processingTime: Long,
        val batchSize: Int,
        val qualityScore: Double
    )
    
    data class GpsFlowMetrics(
        val totalPoints: Long,
        val filteredPoints: Long,
        val batchedPoints: Long,
        val processedPoints: Long,
        val rejectedPoints: Long,
        val filterRate: Double,
        val batchEfficiency: Double,
        val processingEfficiency: Double,
        val currentUpdateInterval: Long,
        val avgProcessingTime: Long
    )
    
    init {
        // ? START: Initialize optimized GPS data flow
        startOptimizedFlow()
        Log.d(TAG, "Optimized GPS data flow initialized")
    }
    
    /**
     * ? NEW: Start the optimized GPS data flow pipeline
     */
    private fun startOptimizedFlow() {
        // Start filtering flow
        startFilteringFlow()
        
        // Start batching flow
        startBatchingFlow()
        
        // Start processing flow
        startProcessingFlow()
    }
    
    /**
     * ? NEW: Start GPS data filtering flow
     */
    private fun startFilteringFlow() {
        // Filter raw GPS data based on quality and rate limiting
        rawGpsFlow
            .filter { location -> isLocationValid(location) }
            .filter { location -> shouldProcessLocation(location) }
            .onEach { location ->
                filteredGpsPoints++
                _filteredGpsFlow.emit(location)
            }
            .catch { e ->
                Log.e(TAG, "Error in filtering flow", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
    }
    
    /**
     * ? NEW: Start GPS data batching flow
     */
    private fun startBatchingFlow() {
        // Batch filtered GPS data for efficient processing
        filteredGpsFlow
            .onEach { location ->
                batchMutex.withLock {
                    gpsBatch.offer(location)
                    
                    // Process batch if it's full or timeout reached
                    if (gpsBatch.size >= BATCH_SIZE) {
                        processBatch()
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in batching flow", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
        
        // Start batch timeout processor
        startBatchTimeoutProcessor()
    }
    
    /**
     * ? NEW: Start GPS data processing flow
     */
    private fun startProcessingFlow() {
        // Process batched GPS data
        batchedGpsFlow
            .onEach { locations ->
                val startTime = System.currentTimeMillis()
                
                val processedData = processGpsBatchInternal(locations)
                processedGpsPoints += locations.size
                
                val processingTime = System.currentTimeMillis() - startTime
                trackProcessingTime(processingTime)
                val processedDataWithTime = processedData.copy(processingTime = processingTime)
                
                // Convert GpsBatchResult to ProcessedGpsData for the flow
                val processedGpsData = ProcessedGpsData(
                    locations = locations,
                    totalDistance = processedDataWithTime.totalDistance,
                    avgSpeed = processedDataWithTime.avgSpeed,
                    maxSpeed = processedDataWithTime.maxSpeed,
                    avgAccuracy = processedDataWithTime.avgAccuracy,
                    processingTime = processedDataWithTime.processingTime,
                    batchSize = processedDataWithTime.batchSize,
                    qualityScore = processedDataWithTime.qualityScore
                )
                
                _processedGpsFlow.emit(processedGpsData)
                
                Log.v(TAG, "Processed ${locations.size} GPS points in ${processingTime}ms")
            }
            .catch { e ->
                Log.e(TAG, "Error in processing flow", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
    }
    
    /**
     * ? NEW: Start batch timeout processor
     */
    private fun startBatchTimeoutProcessor() {
        // Use a simple approach without complex coroutine scope
        // This will be called from the init block, so we'll just log for now
        Log.d(TAG, "Batch timeout processor initialized")
    }
    
    /**
     * ? NEW: Process GPS batch
     */
    private fun processBatch() {
        val batch = mutableListOf<Location>()
        repeat(BATCH_SIZE) {
            gpsBatch.poll()?.let { batch.add(it) }
        }
        
        if (batch.isNotEmpty()) {
            batchedGpsPoints += batch.size
            _batchedGpsFlow.tryEmit(batch)
        }
    }
    
    /**
     * ? NEW: Process GPS batch with performance tracking
     */
    private suspend fun processGpsBatch(locations: List<Location>): GpsBatchResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // Process the batch using existing logic
            val result = processGpsBatchInternal(locations)
            
            // Calculate processing time
            val processingTime = System.currentTimeMillis() - startTime
            trackProcessingTime(processingTime)
            
            // Update result with actual processing time
            return result.copy(processingTime = processingTime)
            
        } catch (e: Exception) {
            val processingTime = System.currentTimeMillis() - startTime
            trackProcessingTime(processingTime)
            
            Log.e(TAG, "Error processing GPS batch", e)
            return GpsBatchResult(
                totalDistance = 0.0,
                avgSpeed = 0.0,
                maxSpeed = 0.0,
                avgAccuracy = 0.0,
                processingTime = processingTime,
                batchSize = locations.size,
                qualityScore = 0.0
            )
        }
    }
    
    /**
     * ? NEW: Track processing time for performance monitoring
     */
    private fun trackProcessingTime(processingTime: Long) {
        processingTimes.add(processingTime)
        totalProcessingTime += processingTime
        processingCount++
        
        // Keep only the last 100 processing times for rolling average
        if (processingTimes.size > 100) {
            val removed = processingTimes.removeAt(0)
            totalProcessingTime -= removed
        }
    }
    
    /**
     * ? NEW: Calculate average processing time
     */
    private fun calculateAverageProcessingTime(): Long {
        return if (processingCount > 0) {
            totalProcessingTime / processingCount
        } else {
            0L
        }
    }
    
    /**
     * ? UPDATED: Process GPS batch and calculate metrics (renamed to avoid conflict)
     */
    private fun processGpsBatchInternal(locations: List<Location>): GpsBatchResult {
        if (locations.isEmpty()) {
            return GpsBatchResult(
                totalDistance = 0.0,
                avgSpeed = 0.0,
                maxSpeed = 0.0,
                avgAccuracy = 0.0,
                processingTime = 0,
                batchSize = 0,
                qualityScore = 0.0
            )
        }
        
        var totalDistance = 0.0
        var totalSpeed = 0.0
        var maxSpeed = 0.0
        var totalAccuracy = 0.0
        var speedReadings = 0
        
        // Calculate metrics for the batch
        for (i in locations.indices) {
            val location = locations[i]
            
            // Calculate distance from previous location
            if (i > 0) {
                val distance = location.distanceTo(locations[i - 1])
                totalDistance += distance
            }
            
            // Calculate speed if available
            if (location.hasSpeed()) {
                val speedMph = location.speed * 2.237f
                totalSpeed += speedMph.toDouble()
                speedReadings++
                
                if (speedMph.toDouble() > maxSpeed) {
                    maxSpeed = speedMph.toDouble()
                }
            }
            
            // Accumulate accuracy
            totalAccuracy += location.accuracy.toDouble()
        }
        
        val avgSpeed = if (speedReadings > 0) totalSpeed / speedReadings else 0.0
        val avgAccuracy = totalAccuracy / locations.size.toDouble()
        
        // Calculate quality score based on accuracy and consistency
        val qualityScore = calculateQualityScore(locations, avgAccuracy, avgSpeed)
        
        return GpsBatchResult(
            totalDistance = totalDistance,
            avgSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            avgAccuracy = avgAccuracy,
            processingTime = 0, // Will be set by caller
            batchSize = locations.size,
            qualityScore = qualityScore
        )
    }
    
    /**
     * ? NEW: Calculate quality score for GPS batch
     */
    private fun calculateQualityScore(
        locations: List<Location>,
        avgAccuracy: Double,
        avgSpeed: Double
    ): Double {
        var score = 100.0
        
        // Penalize poor accuracy
        if (avgAccuracy > MAX_ACCURACY_METERS.toDouble()) {
            score -= (avgAccuracy - MAX_ACCURACY_METERS.toDouble()) * 2
        }
        
        // Penalize unrealistic speeds
        if (avgSpeed > MAX_SPEED_MPH) {
            score -= (avgSpeed - MAX_SPEED_MPH) * 5
        }
        
        // Penalize location jumps
        for (i in 1 until locations.size) {
            val distance = locations[i].distanceTo(locations[i - 1])
            val timeDiff = locations[i].time - locations[i - 1].time
            
            if (timeDiff > 0) {
                val speedMph = (distance / 1609.34) / (timeDiff / 3600000.0)
                if (speedMph > MAX_SPEED_MPH) {
                    score -= 10.0
                }
            }
        }
        
        return maxOf(0.0, score)
    }
    
    /**
     * ? NEW: Check if location is valid for processing
     */
    private fun isLocationValid(location: Location): Boolean {
        totalGpsPoints++
        
        // Check accuracy
        if (location.accuracy > MAX_ACCURACY_METERS) {
            rejectedGpsPoints++
            Log.v(TAG, "Rejected GPS point: poor accuracy ${location.accuracy}m")
            return false
        }
        
        // Check for null or invalid coordinates
        if (location.latitude == 0.0 && location.longitude == 0.0) {
            rejectedGpsPoints++
            Log.v(TAG, "Rejected GPS point: invalid coordinates")
            return false
        }
        
        // Check for unrealistic speeds
        if (location.hasSpeed()) {
            val speedMph = location.speed * 2.237f
            if (speedMph > MAX_SPEED_MPH) {
                rejectedGpsPoints++
                Log.v(TAG, "Rejected GPS point: unrealistic speed ${speedMph}mph")
                return false
            }
        }
        
        return true
    }
    
    /**
     * ? NEW: Check if location should be processed based on rate limiting
     */
    private fun shouldProcessLocation(location: Location): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Check minimum update interval
        if (currentTime - lastUpdateTime < currentUpdateInterval) {
            consecutiveRejections++
            updateRateLimit()
            return false
        }
        
        // Check minimum distance from last processed location
        lastProcessedLocation?.let { lastLocation ->
            val distance = location.distanceTo(lastLocation)
            if (distance < MIN_DISTANCE_METERS) {
                consecutiveRejections++
                updateRateLimit()
                return false
            }
        }
        
        // Accept the location
        lastProcessedLocation = location
        lastUpdateTime = currentTime
        consecutiveAcceptances++
        consecutiveRejections = 0
        updateRateLimit()
        
        return true
    }
    
    /**
     * ? NEW: Update rate limiting based on acceptance/rejection patterns
     */
    private fun updateRateLimit() {
        if (!adaptiveRateEnabled) return
        
        // Adjust update interval based on acceptance rate
        val totalRecent = consecutiveAcceptances + consecutiveRejections
        if (totalRecent >= 10) {
            val acceptanceRate = consecutiveAcceptances.toDouble() / totalRecent
            
            when {
                acceptanceRate > 0.8 -> {
                    // High acceptance rate, can increase frequency
                    currentUpdateInterval = maxOf(currentMinInterval, currentUpdateInterval - 100)
                }
                acceptanceRate < 0.3 -> {
                    // Low acceptance rate, decrease frequency
                    currentUpdateInterval = minOf(currentMaxInterval, currentUpdateInterval + 200)
                }
            }
            
            // Reset counters
            consecutiveAcceptances = 0
            consecutiveRejections = 0
            
            Log.v(TAG, "Updated rate limit: ${currentUpdateInterval}ms (acceptance rate: ${String.format("%.1f", acceptanceRate * 100)}%)")
        }
    }
    
    /**
     * ? NEW: Add GPS location to the optimized flow
     */
    fun addGpsLocation(location: Location) {
        _rawGpsFlow.tryEmit(location)
    }
    
    /**
     * ? NEW: Get GPS flow performance metrics
     */
    fun getGpsFlowMetrics(): GpsFlowMetrics {
        val filterRate = if (totalGpsPoints > 0) (filteredGpsPoints.toDouble() / totalGpsPoints) * 100 else 0.0
        val batchEfficiency = if (filteredGpsPoints > 0) (batchedGpsPoints.toDouble() / filteredGpsPoints) * 100 else 0.0
        val processingEfficiency = if (batchedGpsPoints > 0) (processedGpsPoints.toDouble() / batchedGpsPoints) * 100 else 0.0
        val avgProcessingTime = calculateAverageProcessingTime()
        
        return GpsFlowMetrics(
            totalPoints = totalGpsPoints,
            filteredPoints = filteredGpsPoints,
            batchedPoints = batchedGpsPoints,
            processedPoints = processedGpsPoints,
            rejectedPoints = rejectedGpsPoints,
            filterRate = filterRate,
            batchEfficiency = batchEfficiency,
            processingEfficiency = processingEfficiency,
            currentUpdateInterval = currentUpdateInterval,
            avgProcessingTime = avgProcessingTime
        )
    }
    
    /**
     * ? NEW: Reset GPS flow metrics
     */
    fun resetMetrics() {
        totalGpsPoints = 0
        filteredGpsPoints = 0
        batchedGpsPoints = 0
        processedGpsPoints = 0
        rejectedGpsPoints = 0
        consecutiveAcceptances = 0
        consecutiveRejections = 0
        currentUpdateInterval = currentMinInterval
        
        // ? NEW: Reset performance tracking
        processingTimes.clear()
        totalProcessingTime = 0L
        processingCount = 0
        
        Log.d(TAG, "GPS flow metrics reset")
    }
    
    /**
     * ? IMPLEMENTED: Update rate limiting parameters dynamically
     */
    fun updateRateLimiting(
        minInterval: Long = MIN_UPDATE_INTERVAL_MS,
        maxInterval: Long = MAX_UPDATE_INTERVAL_MS,
        enabled: Boolean = ADAPTIVE_RATE_ENABLED
    ) {
        // ? IMPLEMENTED: Dynamic rate limiting parameter updates
        currentMinInterval = minInterval
        currentMaxInterval = maxInterval
        adaptiveRateEnabled = enabled
        
        // Update current interval to be within new bounds
        currentUpdateInterval = currentUpdateInterval.coerceIn(currentMinInterval, currentMaxInterval)
        
        Log.d(TAG, "Rate limiting parameters updated: min=$currentMinInterval, max=$currentMaxInterval, enabled=$adaptiveRateEnabled")
    }
}

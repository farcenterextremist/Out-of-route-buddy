package com.example.outofroutebuddy.data

import android.util.Log

/**
 * 🚀 PERFORMANCE TUNING CONFIGURATION
 *
 * This class provides centralized configuration for all performance-related parameters
 * across the application. It allows fine-tuning of:
 * - Cache sizes and TTL (Time To Live)
 * - GPS processing intervals and batch sizes
 * - Background sync frequencies
 * - Memory management thresholds
 * - Performance monitoring settings
 */
object PerformanceConfig {
    private const val TAG = "PerformanceConfig"

    // ==================== CACHE CONFIGURATION ====================

    /**
     * Cache size limits for different data types
     */
    object Cache {
        const val MAX_STATISTICS_CACHE_SIZE = 50 // Maximum number of cached statistics sets
        const val MAX_TRIP_CACHE_SIZE = 100 // Maximum number of cached trips
        const val MAX_GPS_CACHE_SIZE = 200 // Maximum number of cached GPS points

        /**
         * Cache TTL (Time To Live) in milliseconds
         */
        const val STATISTICS_CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes
        const val TRIP_CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
        const val GPS_CACHE_TTL_MS = 2 * 60 * 1000L // 2 minutes

        /**
         * Cache cleanup intervals
         */
        const val CACHE_CLEANUP_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
        const val CACHE_CLEANUP_THRESHOLD = 0.8 // Cleanup when 80% full
    }

    // ==================== GPS PROCESSING CONFIGURATION ====================

    /**
     * GPS data processing parameters
     */
    object Gps {
        const val BATCH_SIZE = 5 // Process GPS points in batches
        const val BATCH_TIMEOUT_MS = 1000L // Max time to wait for batch
        const val MIN_DISTANCE_METERS = 10.0 // Minimum distance between points
        const val MAX_SPEED_MPH = 80.0 // Maximum realistic speed
        const val MIN_ACCURACY_METERS = 20.0 // Minimum GPS accuracy
        const val MAX_ACCURACY_METERS = 100.0 // Maximum GPS accuracy to accept

        /**
         * Rate limiting parameters
         */
        const val MIN_UPDATE_INTERVAL_MS = 500L // Minimum time between updates
        const val MAX_UPDATE_INTERVAL_MS = 5000L // Maximum time between updates
        const val ADAPTIVE_RATE_ENABLED = true // Enable adaptive rate limiting

        /**
         * GPS quality thresholds
         */
        const val QUALITY_SCORE_THRESHOLD = 70.0 // Minimum quality score to accept
        const val SPEED_VARIANCE_THRESHOLD = 0.3 // Maximum speed variance (30%)
        const val ACCURACY_WEIGHT = 0.4 // Weight for accuracy in quality calculation
        const val SPEED_WEIGHT = 0.3 // Weight for speed in quality calculation
        const val CONSISTENCY_WEIGHT = 0.3 // Weight for consistency in quality calculation
    }

    // ==================== BACKGROUND SYNC CONFIGURATION ====================

    /**
     * Background synchronization parameters
     */
    object BackgroundSync {
        const val SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
        const val SYNC_TIMEOUT_MS = 30 * 1000L // 30 seconds
        const val MAX_RETRY_ATTEMPTS = 3 // Maximum retry attempts
        const val RETRY_DELAY_MS = 10 * 1000L // 10 seconds between retries

        /**
         * Sync priorities
         */
        const val HIGH_PRIORITY_SYNC_INTERVAL_MS = 1 * 60 * 1000L // 1 minute for high priority
        const val LOW_PRIORITY_SYNC_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes for low priority

        /**
         * Data size thresholds
         */
        const val LARGE_DATA_THRESHOLD = 1000 // Number of records considered "large"
        const val CHUNK_SIZE = 100 // Number of records to sync in chunks
    }

    // ==================== MEMORY MANAGEMENT CONFIGURATION ====================

    /**
     * Memory management parameters
     */
    object Memory {
        const val MEMORY_WARNING_THRESHOLD = 0.8 // 80% memory usage triggers warning
        const val MEMORY_CRITICAL_THRESHOLD = 0.95 // 95% memory usage triggers cleanup
        const val MAX_MEMORY_USAGE_MB = 100 // Maximum memory usage in MB

        /**
         * Garbage collection triggers
         */
        const val GC_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
        const val GC_AFTER_LARGE_OPERATION = true // Force GC after large operations
    }

    // ==================== PERFORMANCE MONITORING CONFIGURATION ====================

    /**
     * Performance monitoring parameters
     */
    object Monitoring {
        const val METRICS_COLLECTION_INTERVAL_MS = 60 * 1000L // 1 minute
        const val METRICS_RETENTION_HOURS = 24 // Keep metrics for 24 hours
        const val PERFORMANCE_ALERT_THRESHOLD = 1000L // Alert if operation takes > 1 second

        /**
         * Performance thresholds
         */
        const val EXCELLENT_PERFORMANCE_THRESHOLD = 0.9 // 90% efficiency
        const val GOOD_PERFORMANCE_THRESHOLD = 0.7 // 70% efficiency
        const val FAIR_PERFORMANCE_THRESHOLD = 0.5 // 50% efficiency
    }

    // ==================== DYNAMIC CONFIGURATION ====================

    /**
     * Dynamic configuration that can be updated at runtime
     */
    private var dynamicCacheSize = Cache.MAX_STATISTICS_CACHE_SIZE
    private var dynamicGpsBatchSize = Gps.BATCH_SIZE
    private var dynamicSyncInterval = BackgroundSync.SYNC_INTERVAL_MS

    /**
     * Update cache size based on available memory
     */
    fun updateCacheSize(availableMemoryMB: Long) {
        val newSize =
            when {
                availableMemoryMB > 500 -> Cache.MAX_STATISTICS_CACHE_SIZE * 2
                availableMemoryMB > 200 -> Cache.MAX_STATISTICS_CACHE_SIZE
                availableMemoryMB > 100 -> Cache.MAX_STATISTICS_CACHE_SIZE / 2
                else -> Cache.MAX_STATISTICS_CACHE_SIZE / 4
            }

        if (newSize != dynamicCacheSize) {
            Log.d(TAG, "Updating cache size from $dynamicCacheSize to $newSize (available memory: ${availableMemoryMB}MB)")
            dynamicCacheSize = newSize
        }
    }

    /**
     * Update GPS batch size based on device performance
     */
    fun updateGpsBatchSize(processingTimeMs: Long) {
        val newBatchSize =
            when {
                processingTimeMs < 100 -> dynamicGpsBatchSize + 2
                processingTimeMs > 500 -> maxOf(1, dynamicGpsBatchSize - 1)
                else -> dynamicGpsBatchSize
            }

        if (newBatchSize != dynamicGpsBatchSize) {
            Log.d(TAG, "Updating GPS batch size from $dynamicGpsBatchSize to $newBatchSize (processing time: ${processingTimeMs}ms)")
            dynamicGpsBatchSize = newBatchSize
        }
    }

    /**
     * Update sync interval based on network conditions
     */
    fun updateSyncInterval(networkQuality: Double) {
        val newInterval =
            when {
                networkQuality > 0.8 -> BackgroundSync.SYNC_INTERVAL_MS / 2
                networkQuality < 0.3 -> BackgroundSync.SYNC_INTERVAL_MS * 2
                else -> BackgroundSync.SYNC_INTERVAL_MS
            }

        if (newInterval != dynamicSyncInterval) {
            Log.d(
                TAG,
                "Updating sync interval from $dynamicSyncInterval to $newInterval (network quality: ${String.format(
                    "%.1f",
                    networkQuality,
                )})",
            )
            dynamicSyncInterval = newInterval
        }
    }

    /**
     * Get current dynamic cache size
     */
    fun getCurrentCacheSize(): Int = dynamicCacheSize

    /**
     * Get current dynamic GPS batch size
     */
    fun getCurrentGpsBatchSize(): Int = dynamicGpsBatchSize

    /**
     * Get current dynamic sync interval
     */
    fun getCurrentSyncInterval(): Long = dynamicSyncInterval

    /**
     * Reset all dynamic configurations to defaults
     */
    fun resetToDefaults() {
        dynamicCacheSize = Cache.MAX_STATISTICS_CACHE_SIZE
        dynamicGpsBatchSize = Gps.BATCH_SIZE
        dynamicSyncInterval = BackgroundSync.SYNC_INTERVAL_MS
        Log.d(TAG, "Reset all dynamic configurations to defaults")
    }

    /**
     * Get performance configuration summary
     */
    fun getConfigurationSummary(): String {
        return """
            Performance Configuration Summary:
            - Cache Size: ${getCurrentCacheSize()}
            - GPS Batch Size: ${getCurrentGpsBatchSize()}
            - Sync Interval: ${getCurrentSyncInterval()}ms
            - Memory Thresholds: ${Memory.MEMORY_WARNING_THRESHOLD * 100}% / ${Memory.MEMORY_CRITICAL_THRESHOLD * 100}%
            - Performance Thresholds: ${Monitoring.EXCELLENT_PERFORMANCE_THRESHOLD * 100}% / ${Monitoring.GOOD_PERFORMANCE_THRESHOLD * 100}% / ${Monitoring.FAIR_PERFORMANCE_THRESHOLD * 100}%
            """.trimIndent()
    }
} 

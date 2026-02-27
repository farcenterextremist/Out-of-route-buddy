package com.example.outofroutebuddy.util

import android.util.Log

/**
 * ⚡ Performance Tracker
 * 
 * Tracks operation performance and alerts on slow operations.
 * 
 * ✅ NEW (#28): Performance Monitoring
 * 
 * Features:
 * - Tracks operation duration
 * - Logs operations >100ms
 * - Collects performance statistics
 * - Identifies performance bottlenecks
 * 
 * Use Cases:
 * - Database operations
 * - Calculations
 * - Network calls
 * - Any critical operation
 * 
 * Priority: MEDIUM
 * Impact: Performance insights and optimization
 */
object PerformanceTracker {
    
    private const val TAG = "PerformanceTracker"
    private const val SLOW_THRESHOLD_MS = 100L
    private const val VERY_SLOW_THRESHOLD_MS = 500L
    
    private val operationStats = mutableMapOf<String, OperationStats>()
    
    /**
     * Operation statistics
     */
    data class OperationStats(
        var count: Long = 0,
        var totalDurationMs: Long = 0,
        var minDurationMs: Long = Long.MAX_VALUE,
        var maxDurationMs: Long = 0,
        var slowCount: Long = 0,
        var verySlowCount: Long = 0
    ) {
        val avgDurationMs: Long
            get() = if (count > 0) totalDurationMs / count else 0
            
        val slowPercentage: Double
            get() = if (count > 0) (slowCount.toDouble() / count) * 100.0 else 0.0
    }
    
    /**
     * Track a blocking operation
     * 
     * @param name Operation name for logging
     * @param block Operation to track
     * @return Result of the operation
     */
    inline fun <T> track(name: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - start
            logPerformance(name, duration)
        }
    }
    
    /**
     * Track a suspend operation
     * 
     * @param name Operation name for logging
     * @param block Suspend operation to track
     * @return Result of the operation
     */
    suspend inline fun <T> trackSuspend(name: String, crossinline block: suspend () -> T): T {
        val start = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - start
            logPerformance(name, duration)
        }
    }
    
    /**
     * Log performance metrics
     */
    @PublishedApi
    internal fun logPerformance(name: String, durationMs: Long) {
        // Update statistics (synchronized to avoid runBlocking; callable from any thread)
        synchronized(operationStats) {
            val stats = operationStats.getOrPut(name) { OperationStats() }
            stats.count++
            stats.totalDurationMs += durationMs
            stats.minDurationMs = minOf(stats.minDurationMs, durationMs)
            stats.maxDurationMs = maxOf(stats.maxDurationMs, durationMs)
            if (durationMs > SLOW_THRESHOLD_MS) stats.slowCount++
            if (durationMs > VERY_SLOW_THRESHOLD_MS) stats.verySlowCount++
        }

        // Log based on threshold
        when {
            durationMs > VERY_SLOW_THRESHOLD_MS -> {
                Log.e(TAG, "🐌 VERY SLOW: $name took ${durationMs}ms (>${VERY_SLOW_THRESHOLD_MS}ms)")
            }
            durationMs > SLOW_THRESHOLD_MS -> {
                Log.w(TAG, "⚠️ SLOW: $name took ${durationMs}ms (>${SLOW_THRESHOLD_MS}ms)")
            }
            else -> {
                Log.d(TAG, "✅ $name: ${durationMs}ms")
            }
        }
    }
    
    /**
     * Get statistics for a specific operation
     */
    suspend fun getOperationStats(name: String): OperationStats? = synchronized(operationStats) {
        operationStats[name]
    }

    /**
     * Get all operation statistics
     */
    suspend fun getAllStats(): Map<String, OperationStats> = synchronized(operationStats) {
        operationStats.toMap()
    }

    /**
     * Get slowest operations
     */
    suspend fun getSlowestOperations(limit: Int = 10): List<Pair<String, OperationStats>> = synchronized(operationStats) {
        operationStats.entries
            .sortedByDescending { it.value.avgDurationMs }
            .take(limit)
            .map { it.key to it.value }
    }

    /**
     * Get operations that frequently exceed threshold
     */
    suspend fun getFrequentlySlowOperations(minSlowPercentage: Double = 10.0): List<Pair<String, OperationStats>> = synchronized(operationStats) {
        operationStats.entries
            .filter { it.value.slowPercentage >= minSlowPercentage }
            .sortedByDescending { it.value.slowPercentage }
            .map { it.key to it.value }
    }

    /**
     * Reset statistics
     */
    suspend fun reset() = synchronized(operationStats) {
        operationStats.clear()
        Log.d(TAG, "Performance statistics reset")
    }

    /**
     * Reset statistics for a specific operation
     */
    suspend fun resetOperation(name: String) = synchronized(operationStats) {
        operationStats.remove(name)
        Log.d(TAG, "Performance statistics reset for: $name")
    }

    /**
     * Print performance report
     */
    suspend fun printReport() = synchronized(operationStats) block@ {
        Log.i(TAG, "═══════════════════════════════════════")
        Log.i(TAG, "    PERFORMANCE REPORT")
        Log.i(TAG, "═══════════════════════════════════════")

        if (operationStats.isEmpty()) {
            Log.i(TAG, "No operations tracked yet")
            return@block
        }
        
        val slowest = operationStats.entries
            .sortedByDescending { it.value.avgDurationMs }
            .take(5)
        
        Log.i(TAG, "Top 5 Slowest Operations (by average):")
        slowest.forEachIndexed { index, (name, stats) ->
            Log.i(TAG, "${index + 1}. $name: ${stats.avgDurationMs}ms avg (${stats.count} calls, ${stats.slowCount} slow)")
        }
        
        val frequentlySlow = operationStats.entries
            .filter { it.value.slowPercentage >= 10.0 }
            .sortedByDescending { it.value.slowPercentage }
        
        if (frequentlySlow.isNotEmpty()) {
            Log.w(TAG, "")
            Log.w(TAG, "Operations frequently exceeding threshold:")
            frequentlySlow.forEach { (name, stats) ->
                Log.w(TAG, "- $name: ${stats.slowPercentage.toInt()}% slow (${stats.slowCount}/${stats.count})")
            }
        }
        
        Log.i(TAG, "═══════════════════════════════════════")
    }
}


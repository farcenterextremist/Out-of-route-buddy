package com.example.outofroutebuddy.services

import android.location.Location
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance monitoring service for location validation operations.
 * 
 * Tracks various performance metrics including:
 * - Validation operation durations
 * - Memory usage patterns
 * - Cache effectiveness
 * - Performance bottlenecks
 * 
 * This service helps identify optimization opportunities and monitor
 * system health in production environments.
 */
open class PerformanceMonitor {
    
    private val mutex = Mutex()
    private val operationTimings = ConcurrentHashMap<String, MutableList<Long>>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    private val performanceAlerts = mutableListOf<PerformanceAlert>()
    
    private var totalValidations = 0L
    private var totalValidationTime = 0L
    private var peakMemoryUsage = 0L
    private var startTime = System.currentTimeMillis()
    
    /**
     * Tracks the duration of a validation operation.
     * 
     * @param operationName Name of the operation being tracked
     * @param duration Duration in milliseconds
     */
    open suspend fun trackValidationTime(operation: String, duration: Long) = mutex.withLock {
        operationTimings.getOrPut(operation) { mutableListOf() }.add(duration)
        totalValidations++
        totalValidationTime += duration
        
        // Check for performance alerts
        if (duration > PERFORMANCE_THRESHOLD_MS) {
            performanceAlerts.add(
                PerformanceAlert(
                    operation = operation,
                    duration = duration,
                    timestamp = System.currentTimeMillis(),
                    severity = if (duration > CRITICAL_THRESHOLD_MS) AlertSeverity.CRITICAL else AlertSeverity.WARNING
                )
            )
        }
    }
    
    /**
     * Tracks memory usage at a specific point in time.
     * 
     * @param memoryUsage Current memory usage in bytes
     * @param context Context for the memory snapshot
     */
    open suspend fun trackMemoryUsage(memoryUsage: Long, context: String = "validation") = mutex.withLock {
        val snapshot = MemorySnapshot(
            memoryUsage = memoryUsage,
            timestamp = System.currentTimeMillis(),
            context = context
        )
        
        memorySnapshots.add(snapshot)
        
        if (memoryUsage > peakMemoryUsage) {
            peakMemoryUsage = memoryUsage
        }
        
        // Check for memory alerts
        if (memoryUsage > MEMORY_THRESHOLD_BYTES) {
            performanceAlerts.add(
                PerformanceAlert(
                    operation = "memory_usage",
                    duration = 0,
                    timestamp = System.currentTimeMillis(),
                    severity = AlertSeverity.WARNING,
                    message = "High memory usage: ${memoryUsage} bytes"
                )
            )
        }
    }
    
    /**
     * Tracks cache performance statistics.
     * 
     * @param cacheStats Cache performance statistics
     */
    suspend fun trackCachePerformance(cacheStats: LocationCache.CachePerformanceStats) = mutex.withLock {
        // Log cache performance for analysis
        if (cacheStats.hitRate < CACHE_HIT_RATE_THRESHOLD) {
            performanceAlerts.add(
                PerformanceAlert(
                    operation = "cache_performance",
                    duration = 0,
                    timestamp = System.currentTimeMillis(),
                    severity = AlertSeverity.INFO,
                    message = "Low cache hit rate: ${cacheStats.hitRate}%"
                )
            )
        }
    }
    
    /**
     * Generates a comprehensive performance report.
     * 
     * @return PerformanceReport with detailed metrics and recommendations
     */
    open suspend fun generatePerformanceReport(): PerformanceReport = mutex.withLock {
        val uptime = System.currentTimeMillis() - startTime
        val avgValidationTime = if (totalValidations > 0) {
            totalValidationTime / totalValidations
        } else 0L
        
        val operationAverages = operationTimings.mapValues { (_, timings) ->
            timings.average().toLong()
        }
        
        val recentMemoryUsage = memorySnapshots.takeLast(10).map { it.memoryUsage }
        val avgMemoryUsage = if (recentMemoryUsage.isNotEmpty()) {
            recentMemoryUsage.average().toLong()
        } else 0L
        
        return PerformanceReport(
            uptime = uptime,
            totalValidations = totalValidations,
            averageValidationTime = avgValidationTime,
            peakMemoryUsage = peakMemoryUsage,
            averageMemoryUsage = avgMemoryUsage,
            operationAverages = operationAverages,
            performanceAlerts = performanceAlerts.toList(),
            recommendations = generateRecommendations(avgValidationTime, avgMemoryUsage)
        )
    }
    
    /**
     * Generates performance recommendations based on current metrics.
     * 
     * @param avgValidationTime Average validation time in milliseconds
     * @param avgMemoryUsage Average memory usage in bytes
     * @return List of performance recommendations
     */
    private fun generateRecommendations(avgValidationTime: Long, avgMemoryUsage: Long): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (avgValidationTime > PERFORMANCE_THRESHOLD_MS) {
            recommendations.add("Consider optimizing validation algorithms for faster processing")
        }
        
        if (avgMemoryUsage > MEMORY_THRESHOLD_BYTES) {
            recommendations.add("Review memory usage patterns and consider implementing memory pooling")
        }
        
        if (performanceAlerts.size > 10) {
            recommendations.add("High number of performance alerts detected - review system load")
        }
        
        return recommendations
    }
    
    /**
     * Resets all performance metrics.
     */
    suspend fun resetMetrics() = mutex.withLock {
        operationTimings.clear()
        memorySnapshots.clear()
        performanceAlerts.clear()
        totalValidations = 0L
        totalValidationTime = 0L
        peakMemoryUsage = 0L
        startTime = System.currentTimeMillis()
    }
    
    /**
     * Data class for memory usage snapshots.
     */
    data class MemorySnapshot(
        val memoryUsage: Long,
        val timestamp: Long,
        val context: String
    )
    
    /**
     * Data class for performance alerts.
     */
    data class PerformanceAlert(
        val operation: String,
        val duration: Long,
        val timestamp: Long,
        val severity: AlertSeverity,
        val message: String? = null
    )
    
    /**
     * Enum for alert severity levels.
     */
    enum class AlertSeverity {
        INFO, WARNING, CRITICAL
    }
    
    /**
     * Data class for comprehensive performance reports.
     */
    data class PerformanceReport(
        val uptime: Long,
        val totalValidations: Long,
        val averageValidationTime: Long,
        val peakMemoryUsage: Long,
        val averageMemoryUsage: Long,
        val operationAverages: Map<String, Long>,
        val performanceAlerts: List<PerformanceAlert>,
        val recommendations: List<String>
    )
    
    companion object {
        private const val PERFORMANCE_THRESHOLD_MS = 50L // 50ms threshold for warnings
        private const val CRITICAL_THRESHOLD_MS = 200L // 200ms threshold for critical alerts
        private const val MEMORY_THRESHOLD_BYTES = 100 * 1024 * 1024L // 100MB threshold
        private const val CACHE_HIT_RATE_THRESHOLD = 70.0 // 70% cache hit rate threshold
    }
} 

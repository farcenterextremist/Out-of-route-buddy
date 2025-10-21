package com.example.outofroutebuddy.util

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ⚡ Rate Limiter
 * 
 * Prevents excessive requests by limiting the rate of operations.
 * 
 * ✅ NEW (#10): Rate Limiting for GPS updates and API calls
 * 
 * Features:
 * - Token bucket algorithm
 * - Thread-safe with Mutex
 * - Configurable rate limits
 * - Automatic token refill
 * 
 * Use Cases:
 * - GPS location updates (prevent battery drain)
 * - API calls (prevent rate limit errors)
 * - Database writes (prevent overwhelming DB)
 * 
 * Priority: MEDIUM
 * Impact: Battery life, performance, API quota management
 */
class RateLimiter(
    private val maxRequests: Int,
    private val timeWindowMs: Long
) {
    
    companion object {
        private const val TAG = "RateLimiter"
    }
    
    private val timestamps = mutableListOf<Long>()
    private val mutex = Mutex()
    
    // Statistics
    private var totalRequests = 0L
    private var totalAllowed = 0L
    private var totalRateLimited = 0L
    
    /**
     * Attempt to acquire permission to proceed
     * 
     * @return true if request is allowed, false if rate limited
     */
    suspend fun acquire(): Boolean = mutex.withLock {
        totalRequests++
        val now = System.currentTimeMillis()
        
        // Remove expired timestamps (outside time window)
        timestamps.removeAll { it < now - timeWindowMs }
        
        return if (timestamps.size < maxRequests) {
            // Allow request
            timestamps.add(now)
            totalAllowed++
            
            if (totalRequests % 100 == 0L) {
                Log.d(TAG, "Rate limiter stats: ${totalAllowed}/${totalRequests} allowed (${getSuccessRate()}%)")
            }
            
            true
        } else {
            // Rate limit exceeded
            totalRateLimited++
            
            if (totalRateLimited % 10 == 0L) {
                Log.w(TAG, "⚠️ Rate limited: ${timestamps.size}/$maxRequests in last ${timeWindowMs}ms")
            }
            
            false
        }
    }
    
    /**
     * Try to acquire, with logging on failure
     */
    suspend fun tryAcquire(operationName: String): Boolean {
        val allowed = acquire()
        
        if (!allowed) {
            Log.d(TAG, "Rate limited: $operationName")
        }
        
        return allowed
    }
    
    /**
     * Get current usage count
     */
    suspend fun getCurrentUsage(): Int = mutex.withLock {
        val now = System.currentTimeMillis()
        timestamps.removeAll { it < now - timeWindowMs }
        timestamps.size
    }
    
    /**
     * Get success rate (percentage of allowed requests)
     */
    suspend fun getSuccessRate(): Double = mutex.withLock {
        if (totalRequests == 0L) return@withLock 100.0
        (totalAllowed.toDouble() / totalRequests) * 100.0
    }
    
    /**
     * Get statistics
     */
    suspend fun getStatistics(): RateLimiterStatistics = mutex.withLock {
        RateLimiterStatistics(
            totalRequests = totalRequests,
            totalAllowed = totalAllowed,
            totalRateLimited = totalRateLimited,
            successRate = if (totalRequests > 0) {
                (totalAllowed.toDouble() / totalRequests) * 100.0
            } else {
                100.0
            },
            currentUsage = timestamps.size,
            maxRequests = maxRequests,
            timeWindowMs = timeWindowMs
        )
    }
    
    /**
     * Reset all statistics
     */
    suspend fun reset() = mutex.withLock {
        timestamps.clear()
        totalRequests = 0
        totalAllowed = 0
        totalRateLimited = 0
        Log.d(TAG, "Rate limiter reset")
    }
    
    /**
     * Get time until next token is available
     */
    suspend fun getTimeUntilNextToken(): Long = mutex.withLock {
        if (timestamps.size < maxRequests) return@withLock 0L
        
        val now = System.currentTimeMillis()
        val oldestTimestamp = timestamps.minOrNull() ?: return@withLock 0L
        val timeUntilExpire = (oldestTimestamp + timeWindowMs) - now
        
        return@withLock timeUntilExpire.coerceAtLeast(0L)
    }
    
    /**
     * Rate limiter statistics
     */
    data class RateLimiterStatistics(
        val totalRequests: Long,
        val totalAllowed: Long,
        val totalRateLimited: Long,
        val successRate: Double,
        val currentUsage: Int,
        val maxRequests: Int,
        val timeWindowMs: Long
    )
}



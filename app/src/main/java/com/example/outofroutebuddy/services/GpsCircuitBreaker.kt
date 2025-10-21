package com.example.outofroutebuddy.services

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

/**
 * ⚡ GPS Circuit Breaker
 * 
 * Prevents battery drain from repeated GPS failures by automatically
 * disabling GPS after consecutive failures and using exponential backoff.
 * 
 * ✅ NEW (#6): Circuit Breaker Pattern for GPS Service
 * 
 * Features:
 * - Tracks consecutive GPS failures
 * - Auto-disables after 5 consecutive failures
 * - Exponential backoff: 5s → 10s → 20s → 40s → 60s (max)
 * - Health check before retry
 * - Thread-safe with Mutex
 * 
 * States:
 * - CLOSED: Normal operation, GPS is active
 * - OPEN: GPS disabled due to failures
 * - HALF_OPEN: Testing if GPS has recovered
 * 
 * Priority: HIGH
 * Impact: Prevents battery drain, improves reliability
 */
class GpsCircuitBreaker {
    
    companion object {
        private const val TAG = "GpsCircuitBreaker"
        private const val MAX_FAILURES_BEFORE_OPEN = 5
        private const val MIN_BACKOFF_MS = 5_000L // 5 seconds
        private const val MAX_BACKOFF_MS = 60_000L // 60 seconds
    }
    
    /**
     * Circuit breaker states
     */
    enum class State {
        CLOSED,      // Normal operation
        OPEN,        // Disabled due to failures
        HALF_OPEN    // Testing recovery
    }
    
    private var state = State.CLOSED
    private var consecutiveFailures = 0
    private var lastFailureTime = 0L
    private var totalFailures = 0L
    private var totalSuccesses = 0L
    
    // Thread-safe access
    private val mutex = Mutex()
    
    /**
     * Record a GPS failure
     */
    suspend fun recordFailure(reason: String) = mutex.withLock {
        consecutiveFailures++
        totalFailures++
        lastFailureTime = System.currentTimeMillis()
        
        Log.w(TAG, "GPS failure recorded: $reason (consecutive: $consecutiveFailures)")
        
        when {
            consecutiveFailures >= MAX_FAILURES_BEFORE_OPEN && state == State.CLOSED -> {
                state = State.OPEN
                Log.e(TAG, "⚠️ Circuit breaker OPEN - GPS disabled after $consecutiveFailures failures")
            }
            state == State.HALF_OPEN -> {
                // Failed during test, go back to OPEN
                state = State.OPEN
                Log.w(TAG, "Circuit breaker back to OPEN - test failed")
            }
            else -> {
                // State is already OPEN, just increment failure count
                Log.d(TAG, "Circuit breaker already OPEN, failure count: $consecutiveFailures")
            }
        }
    }
    
    /**
     * Record a GPS success
     */
    suspend fun recordSuccess() = mutex.withLock {
        consecutiveFailures = 0
        totalSuccesses++
        
        if (state == State.HALF_OPEN) {
            state = State.CLOSED
            Log.i(TAG, "✅ Circuit breaker CLOSED - GPS recovered successfully")
        }
        
        Log.d(TAG, "GPS success recorded (total: $totalSuccesses)")
    }
    
    /**
     * Check if GPS attempt is allowed
     * 
     * @return true if GPS can be attempted, false if circuit is open
     */
    suspend fun canAttempt(): Boolean = mutex.withLock {
        when (state) {
            State.CLOSED -> {
                // Normal operation - allow attempt
                true
            }
            State.OPEN -> {
                // Check if enough time has passed for retry
                val backoffTime = calculateBackoff(consecutiveFailures)
                val elapsed = System.currentTimeMillis() - lastFailureTime
                
                if (elapsed >= backoffTime) {
                    state = State.HALF_OPEN
                    Log.i(TAG, "Circuit breaker HALF_OPEN - testing GPS recovery")
                    true
                } else {
                    val remainingMs = backoffTime - elapsed
                    Log.d(TAG, "Circuit breaker OPEN - retry in ${remainingMs / 1000}s")
                    false
                }
            }
            State.HALF_OPEN -> {
                // Allow one test attempt
                Log.d(TAG, "Circuit breaker HALF_OPEN - test attempt allowed")
                true
            }
        }
    }
    
    /**
     * Calculate backoff time based on failure count
     * 
     * Exponential backoff: 5s, 10s, 20s, 40s, 60s (max)
     */
    private fun calculateBackoff(failures: Int): Long {
        // 2^(failures - MAX_FAILURES) * MIN_BACKOFF
        val exponent = failures - MAX_FAILURES_BEFORE_OPEN + 1
        val backoff = MIN_BACKOFF_MS * (1 shl exponent.coerceAtLeast(0))
        return min(backoff, MAX_BACKOFF_MS)
    }
    
    /**
     * Get current circuit breaker state
     */
    suspend fun getState(): CircuitBreakerState = mutex.withLock {
        CircuitBreakerState(
            state = state,
            consecutiveFailures = consecutiveFailures,
            totalFailures = totalFailures,
            totalSuccesses = totalSuccesses,
            lastFailureTime = lastFailureTime,
            nextRetryTime = if (state == State.OPEN) {
                lastFailureTime + calculateBackoff(consecutiveFailures)
            } else {
                null
            }
        )
    }
    
    /**
     * Reset circuit breaker (for testing or manual recovery)
     */
    suspend fun reset() = mutex.withLock {
        state = State.CLOSED
        consecutiveFailures = 0
        Log.i(TAG, "Circuit breaker manually reset")
    }
    
    /**
     * Get statistics
     */
    suspend fun getStatistics(): CircuitBreakerStatistics = mutex.withLock {
        CircuitBreakerStatistics(
            totalFailures = totalFailures,
            totalSuccesses = totalSuccesses,
            successRate = if (totalFailures + totalSuccesses > 0) {
                (totalSuccesses.toDouble() / (totalFailures + totalSuccesses)) * 100.0
            } else {
                0.0
            },
            currentState = state,
            isHealthy = state == State.CLOSED
        )
    }
    
    /**
     * Circuit breaker state snapshot
     */
    data class CircuitBreakerState(
        val state: State,
        val consecutiveFailures: Int,
        val totalFailures: Long,
        val totalSuccesses: Long,
        val lastFailureTime: Long,
        val nextRetryTime: Long?
    )
    
    /**
     * Circuit breaker statistics
     */
    data class CircuitBreakerStatistics(
        val totalFailures: Long,
        val totalSuccesses: Long,
        val successRate: Double,
        val currentState: State,
        val isHealthy: Boolean
    )
}


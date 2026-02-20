package com.example.outofroutebuddy.util

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

/**
 * 🔄 Retry Policy with Exponential Backoff
 * 
 * Implements retry logic with exponential backoff and jitter for failed operations.
 * 
 * ✅ NEW (#5): Exponential Backoff for retries
 * 
 * Features:
 * - Exponential backoff: 1s → 2s → 4s → 8s → 16s → 32s → 60s (max)
 * - Random jitter to prevent thundering herd
 * - Configurable max attempts
 * - Detailed logging
 * - Success/failure tracking
 * 
 * Use Cases:
 * - Network operations
 * - GPS lock attempts
 * - Database operations
 * - Any operation that can temporarily fail
 * 
 * Priority: MEDIUM
 * Impact: Reliability for transient failures
 */
class RetryPolicy(
    private val maxAttempts: Int = 5,
    private val baseDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 60000L,
    private val jitterPercentage: Int = 10
) {
    
    companion object {
        private const val TAG = "RetryPolicy"
        
        /**
         * Default retry policy for network operations
         */
        val NETWORK_RETRY = RetryPolicy(
            maxAttempts = 3,
            baseDelayMs = 2000L,
            maxDelayMs = 30000L
        )
        
        /**
         * Default retry policy for GPS operations
         */
        val GPS_RETRY = RetryPolicy(
            maxAttempts = 5,
            baseDelayMs = 1000L,
            maxDelayMs = 60000L
        )
        
        /**
         * Default retry policy for database operations
         */
        val DATABASE_RETRY = RetryPolicy(
            maxAttempts = 3,
            baseDelayMs = 500L,
            maxDelayMs = 5000L
        )
    }
    
    /**
     * Execute operation with retry logic
     * 
     * @param operationName Name for logging
     * @param operation The operation to execute
     * @return Result<T> with success or failure
     */
    suspend fun <T> executeWithRetry(
        operationName: String,
        operation: suspend (attempt: Int) -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                Log.d(TAG, "Attempting $operationName (attempt ${attempt + 1}/$maxAttempts)")
                
                val result = operation(attempt)
                
                if (attempt > 0) {
                    Log.i(TAG, "✅ $operationName succeeded on attempt ${attempt + 1}")
                } else {
                    Log.d(TAG, "✅ $operationName succeeded on first attempt")
                }
                
                return Result.success(result)
                
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "❌ $operationName failed (attempt ${attempt + 1}/$maxAttempts): ${e.message}")
                
                // If not last attempt, wait before retry
                if (attempt < maxAttempts - 1) {
                    val delayMs = calculateBackoff(attempt)
                    Log.d(TAG, "⏳ Retrying $operationName in ${delayMs}ms...")
                    delay(delayMs)
                }
            }
        }
        
        // All attempts failed
        Log.e(TAG, "❌ $operationName failed after $maxAttempts attempts")
        return Result.failure(lastException ?: Exception("Operation failed after $maxAttempts attempts"))
    }
    
    /**
     * Execute operation with retry, using a simpler API (no attempt parameter)
     */
    suspend fun <T> execute(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return executeWithRetry(operationName) { _ -> operation() }
    }
    
    /**
     * Calculate backoff delay with exponential increase and jitter
     * 
     * Formula: min(baseDelay * 2^attempt, maxDelay) + jitter
     * Jitter: Random 0-10% of delay (prevents thundering herd)
     * 
     * @param attempt Current attempt number (0-indexed)
     * @return Delay in milliseconds
     */
    private fun calculateBackoff(attempt: Int): Long {
        // Exponential backoff: baseDelay * 2^attempt
        val exponentialDelay = baseDelayMs * (1 shl attempt)
        
        // Cap at max delay
        val cappedDelay = min(exponentialDelay, maxDelayMs)
        
        // Add jitter (0-10% random variation)
        val jitterRange = (cappedDelay * jitterPercentage) / 100
        val jitter = if (jitterRange > 0) {
            Random.nextLong(0, jitterRange)
        } else {
            0L
        }
        
        return cappedDelay + jitter
    }
    
    /**
     * Get backoff sequence for preview/testing
     */
    fun getBackoffSequence(): List<Long> {
        return (0 until maxAttempts).map { attempt ->
            val exponentialDelay = baseDelayMs * (1 shl attempt)
            min(exponentialDelay, maxDelayMs)
        }
    }
    
    /**
     * Retry configuration info
     */
    data class RetryConfig(
        val maxAttempts: Int,
        val baseDelayMs: Long,
        val maxDelayMs: Long,
        val jitterPercentage: Int,
        val backoffSequence: List<Long>
    )
    
    /**
     * Get current configuration
     */
    fun getConfig(): RetryConfig {
        return RetryConfig(
            maxAttempts = maxAttempts,
            baseDelayMs = baseDelayMs,
            maxDelayMs = maxDelayMs,
            jitterPercentage = jitterPercentage,
            backoffSequence = getBackoffSequence()
        )
    }
}











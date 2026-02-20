package com.example.outofroutebuddy.util

import android.util.Log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * ⏱️ Timeout Manager
 * 
 * Centralized timeout handling for all long-running operations.
 * 
 * ✅ NEW (#8): Timeout Mechanisms for preventing hanging operations
 * 
 * Prevents:
 * - Indefinite network waits
 * - GPS lock hangs
 * - Database query delays
 * - User frustration from frozen UI
 * 
 * Priority: HIGH
 * Impact: Prevents ANRs and improves responsiveness
 */
object TimeoutManager {
    
    private const val TAG = "TimeoutManager"
    
    /**
     * Standard timeouts for different operation types
     */
    object Timeouts {
        const val NETWORK_OPERATION_MS = 30_000L // 30 seconds
        const val GPS_LOCK_MS = 30_000L // 30 seconds
        const val GPS_UPDATE_MS = 15_000L // 15 seconds
        const val DATABASE_QUERY_MS = 5_000L // 5 seconds
        const val DATABASE_WRITE_MS = 3_000L // 3 seconds
        const val FILE_OPERATION_MS = 10_000L // 10 seconds
        const val CALCULATION_MS = 2_000L // 2 seconds
        const val SYNC_OPERATION_MS = 60_000L // 60 seconds
    }
    
    /**
     * Execute operation with timeout
     * 
     * @param timeoutMs Timeout in milliseconds
     * @param operationName Name for logging
     * @param operation The operation to execute
     * @return Result of operation or null if timeout
     */
    suspend fun <T> withTimeout(
        timeoutMs: Long,
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return try {
            val result = kotlinx.coroutines.withTimeout(timeoutMs) {
                operation()
            }
            Log.d(TAG, "✅ $operationName completed within ${timeoutMs}ms")
            Result.success(result)
            
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "⏱️ $operationName timed out after ${timeoutMs}ms")
            Result.failure(TimeoutException("$operationName timed out after ${timeoutMs}ms", e))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ $operationName failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Execute GPS operation with timeout
     */
    suspend fun <T> withGpsTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.GPS_LOCK_MS, "GPS: $operationName", operation)
    }
    
    /**
     * Execute network operation with timeout
     */
    suspend fun <T> withNetworkTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.NETWORK_OPERATION_MS, "Network: $operationName", operation)
    }
    
    /**
     * Execute database query with timeout
     */
    suspend fun <T> withDatabaseTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.DATABASE_QUERY_MS, "Database: $operationName", operation)
    }
    
    /**
     * Execute database write with timeout
     */
    suspend fun <T> withDatabaseWriteTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.DATABASE_WRITE_MS, "DB Write: $operationName", operation)
    }
    
    /**
     * Execute file operation with timeout
     */
    suspend fun <T> withFileTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.FILE_OPERATION_MS, "File: $operationName", operation)
    }
    
    /**
     * Execute calculation with timeout
     */
    suspend fun <T> withCalculationTimeout(
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        return withTimeout(Timeouts.CALCULATION_MS, "Calculation: $operationName", operation)
    }
    
    /**
     * Custom timeout exception
     */
    class TimeoutException(message: String, cause: Throwable? = null) : Exception(message, cause)
}











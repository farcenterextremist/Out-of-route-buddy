package com.example.outofroutebuddy.util

import android.util.Log

/**
 * 🛡️ Safe Collection Extensions
 * 
 * Extension functions for safe list/array access without crashes.
 * 
 * ✅ NEW (#9): Bounds Checking
 * 
 * Features:
 * - Safe list access
 * - Safe substring
 * - Safe array access
 * - Default values
 * 
 * Prevents:
 * - IndexOutOfBoundsException
 * - StringIndexOutOfBoundsException
 * - Null pointer exceptions
 * 
 * Priority: MEDIUM
 * Impact: Crash prevention
 */

@PublishedApi
internal const val TAG = "SafeCollections"

/**
 * Get element or null (already exists, but here for completeness)
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return getOrNull(index)
}

/**
 * Get element or default value
 */
fun <T> List<T>.getOrDefault(index: Int, default: T): T {
    return getOrElse(index) { default }
}

/**
 * Safe substring with bounds checking
 */
fun String.safeSubstring(start: Int, end: Int = length): String {
    return try {
        val safeStart = start.coerceIn(0, length)
        val safeEnd = end.coerceIn(safeStart, length)
        substring(safeStart, safeEnd)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeSubstring", e)
        ""
    }
}

/**
 * Safe substring from start to end (Java-style)
 */
fun String.safeSubstringRange(range: IntRange): String {
    return try {
        val safeStart = range.first.coerceIn(0, length)
        val safeEnd = (range.last + 1).coerceIn(safeStart, length)
        substring(safeStart, safeEnd)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeSubstringRange", e)
        ""
    }
}

/**
 * Get first element or null
 */
fun <T> List<T>.firstOrNull(): T? {
    return if (isNotEmpty()) this[0] else null
}

/**
 * Get last element or null
 */
fun <T> List<T>.lastOrNull(): T? {
    return if (isNotEmpty()) this[size - 1] else null
}

/**
 * Safe character access
 */
fun String.safeCharAt(index: Int): Char? {
    return if (index in indices) this[index] else null
}

/**
 * Partition list safely with size check
 */
fun <T> List<T>.safePartition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    return try {
        partition(predicate)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safePartition", e)
        emptyList<T>() to emptyList<T>()
    }
}

/**
 * Safe chunking
 */
fun <T> List<T>.safeChunked(size: Int): List<List<T>> {
    return try {
        if (size <= 0) {
            Log.w(TAG, "Invalid chunk size: $size")
            listOf(this)
        } else {
            chunked(size)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeChunked", e)
        listOf(this)
    }
}

/**
 * Safe take
 */
fun <T> List<T>.safeTake(n: Int): List<T> {
    return try {
        if (n <= 0) emptyList()
        else take(n.coerceAtMost(size))
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeTake", e)
        emptyList()
    }
}

/**
 * Safe drop
 */
fun <T> List<T>.safeDrop(n: Int): List<T> {
    return try {
        if (n <= 0) this
        else drop(n.coerceAtMost(size))
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeDrop", e)
        this
    }
}

/**
 * Safe slice
 */
fun <T> List<T>.safeSlice(range: IntRange): List<T> {
    return try {
        val safeStart = range.first.coerceIn(0, size)
        val safeEnd = (range.last + 1).coerceIn(safeStart, size)
        slice(safeStart until safeEnd)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeSlice", e)
        emptyList()
    }
}

/**
 * Find with safe default
 */
fun <T> List<T>.findOrDefault(predicate: (T) -> Boolean, default: T): T {
    return try {
        find(predicate) ?: default
    } catch (e: Exception) {
        Log.e(TAG, "Error in findOrDefault", e)
        default
    }
}

/**
 * Safe map with error handling
 */
inline fun <T, R> List<T>.safeMap(transform: (T) -> R): List<R> {
    return try {
        map(transform)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeMap", e)
        emptyList()
    }
}

/**
 * Safe filter with error handling
 */
inline fun <T> List<T>.safeFilter(predicate: (T) -> Boolean): List<T> {
    return try {
        filter(predicate)
    } catch (e: Exception) {
        Log.e(TAG, "Error in safeFilter", e)
        this
    }
}


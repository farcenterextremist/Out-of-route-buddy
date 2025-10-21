package com.example.outofroutebuddy.services

import android.location.Location
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance-optimized cache for location validation results.
 * 
 * This cache reduces redundant validation calculations by storing
 * validation results for similar location data. It uses a combination
 * of LRU cache for recent results and a concurrent map for high-frequency
 * location patterns.
 * 
 * @property maxCacheSize Maximum number of cached validation results
 * @property cacheHitRate Tracks cache effectiveness for performance monitoring
 */
open class LocationCache(
    private val maxCacheSize: Int = 100
) {
    
    private val lruCache = LruCache<String, CachedValidationResult>(maxCacheSize)
    private val highFrequencyCache = ConcurrentHashMap<String, CachedValidationResult>()
    private val mutex = Mutex()
    
    private var cacheHits = 0L
    private var cacheMisses = 0L
    
    /**
     * Generates a cache key for a location based on its characteristics.
     * 
     * @param location The GPS location to cache
     * @param context Additional validation context (speed, accuracy, etc.)
     * @return A unique cache key for this location+context combination
     */
    private fun generateCacheKey(location: Location, context: ValidationContext): String {
        return buildString {
            append("lat_${location.latitude.toInt()}")
            append("_lon_${location.longitude.toInt()}")
            append("_acc_${location.accuracy.toInt()}")
            append("_speed_${context.speed.toInt()}")
            append("_age_${context.locationAge.toInt()}")
        }
    }
    
    /**
     * Retrieves a cached validation result if available.
     * 
     * @param location The GPS location to validate
     * @param context Additional validation context
     * @return Cached validation result or null if not found
     */
    open suspend fun getCachedValidation(
        location: Location,
        context: ValidationContext
    ): LocationValidationService.ValidationResult? = mutex.withLock {
        val cacheKey = generateCacheKey(location, context)
        
        // Check high-frequency cache first (for common patterns)
        highFrequencyCache[cacheKey]?.let { cached ->
            if (isCacheEntryValid(cached)) {
                cacheHits++
                return cached.result
            } else {
                highFrequencyCache.remove(cacheKey)
            }
        }
        
        // Check LRU cache
        lruCache.get(cacheKey)?.let { cached ->
            if (isCacheEntryValid(cached)) {
                cacheHits++
                return cached.result
            } else {
                lruCache.remove(cacheKey)
            }
        }
        
        cacheMisses++
        return null
    }
    
    /**
     * Caches a validation result for future use.
     * 
     * @param location The GPS location that was validated
     * @param context Additional validation context
     * @param result The validation result to cache
     */
    open suspend fun cacheValidation(
        location: Location,
        context: ValidationContext,
        result: LocationValidationService.ValidationResult
    ) = mutex.withLock {
        val cacheKey = generateCacheKey(location, context)
        val cachedResult = CachedValidationResult(
            result = result,
            timestamp = System.currentTimeMillis(),
            location = location,
            context = context
        )
        
        // Store in LRU cache
        lruCache.put(cacheKey, cachedResult)
        
        // If this is a high-frequency pattern, also store in concurrent cache
        if (isHighFrequencyPattern(location, context)) {
            highFrequencyCache[cacheKey] = cachedResult
        }
    }
    
    /**
     * Checks if a cached entry is still valid (not expired).
     * 
     * @param cached The cached validation result
     * @return true if the cache entry is still valid
     */
    private fun isCacheEntryValid(cached: CachedValidationResult): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - cached.timestamp
        
        // Cache entries are valid for 5 minutes
        return cacheAge < CACHE_VALIDITY_DURATION_MS
    }
    
    /**
     * Determines if a location pattern is high-frequency (should be cached in concurrent map).
     * 
     * @param location The GPS location
     * @param context Additional validation context
     * @return true if this is a high-frequency pattern
     */
    private fun isHighFrequencyPattern(location: Location, context: ValidationContext): Boolean {
        // Consider patterns with similar accuracy and speed as high-frequency
        return location.accuracy < 20f && context.speed < 30f
    }
    
    /**
     * Clears all cached validation results.
     */
    suspend fun clearCache() = mutex.withLock {
        lruCache.evictAll()
        highFrequencyCache.clear()
    }
    
    /**
     * Gets cache performance statistics.
     * 
     * @return CachePerformanceStats with hit rate and usage information
     */
    fun getCacheStats(): CachePerformanceStats {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) {
            (cacheHits.toDouble() / totalRequests) * 100
        } else 0.0
        
        return CachePerformanceStats(
            cacheHits = cacheHits,
            cacheMisses = cacheMisses,
            hitRate = hitRate,
            lruCacheSize = lruCache.size(),
            highFrequencyCacheSize = highFrequencyCache.size,
            maxCacheSize = maxCacheSize
        )
    }
    
    /**
     * Data class for cached validation results.
     */
    private data class CachedValidationResult(
        val result: LocationValidationService.ValidationResult,
        val timestamp: Long,
        val location: Location,
        val context: ValidationContext
    )
    
    /**
     * Data class for validation context information.
     */
    data class ValidationContext(
        val speed: Float,
        val locationAge: Long,
        val isVehicle: Boolean = true
    )
    
    /**
     * Data class for cache performance statistics.
     */
    data class CachePerformanceStats(
        val cacheHits: Long,
        val cacheMisses: Long,
        val hitRate: Double,
        val lruCacheSize: Int,
        val highFrequencyCacheSize: Int,
        val maxCacheSize: Int
    )
    
    companion object {
        private const val CACHE_VALIDITY_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }
} 

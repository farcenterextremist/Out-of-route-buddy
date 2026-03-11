package com.example.outofroutebuddy.data

import android.util.Log
import com.example.outofroutebuddy.domain.models.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * ✅ NEW: Intelligent State Caching System
 *
 * This class provides intelligent caching for trip data, statistics, and GPS metadata
 * to reduce database queries and improve app performance.
 */
class StateCache {
    companion object {
        private const val TAG = "StateCache"

        // Cache expiration times
        private const val STATS_CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        private const val GPS_CACHE_DURATION_MS = 30 * 1000L // 30 seconds
        private const val TRIP_CACHE_DURATION_MS = 10 * 60 * 1000L // 10 minutes

        // Cache size limits
        private const val MAX_TRIP_CACHE_SIZE = 100
        private const val MAX_GPS_CACHE_SIZE = 50
    }

    // ✅ CACHE STORES: Different types of cached data
    private val tripCache = ConcurrentHashMap<String, CachedTripData>()
    private val statsCache = ConcurrentHashMap<String, CachedStatsData>()
    private val gpsCache = ConcurrentHashMap<String, CachedGpsData>()

    // ✅ CACHE STATE: Track cache health and performance
    private val _cacheState = MutableStateFlow(CacheState())
    val cacheState: StateFlow<CacheState> = _cacheState.asStateFlow()

    // ✅ CACHE METRICS: Performance tracking
    private var cacheHits = 0L
    private var cacheMisses = 0L
    private var cacheEvictions = 0L

    // ✅ CACHE DATA CLASSES
    data class CachedTripData(
        val trips: List<Trip>,
        val timestamp: Long,
        val period: String,
        val isComplete: Boolean = true,
    )

    data class CachedStatsData(
        val stats: StatisticsSet,
        val timestamp: Long,
        val period: String,
        val tripCount: Int,
    )

    data class CachedGpsData(
        val gpsMetadata: Map<String, Any>,
        val timestamp: Long,
        val tripId: Long,
    )

    data class CacheState(
        val isEnabled: Boolean = true,
        val hitRate: Double = 0.0,
        val totalRequests: Long = 0,
        val cacheSize: Int = 0,
        val lastCleanup: Date = Date(),
        val memoryUsage: Long = 0,
        val isHealthy: Boolean = true,
    )

    // Define StatisticsSet locally to avoid circular dependency
    data class StatisticsSet(
        val totalTrips: Int = 0,
        val totalLoadedMiles: Double = 0.0,
        val totalBounceMiles: Double = 0.0,
        val totalActualMiles: Double = 0.0,
        val totalOorMiles: Double = 0.0,
        val avgOorPercentage: Double = 0.0,
    )

    /**
     * ✅ NEW: Get cached trip data or return null if expired/missing
     */
    fun getCachedTrips(period: String): List<Trip>? {
        val cached = tripCache[period]
        if (cached != null && !isExpired(cached.timestamp, TRIP_CACHE_DURATION_MS)) {
            cacheHits++
            Log.v(TAG, "Cache HIT for trips period: $period")
            return cached.trips
        }

        cacheMisses++
        Log.v(TAG, "Cache MISS for trips period: $period")
        return null
    }

    /**
     * ✅ NEW: Cache trip data with intelligent key generation
     */
    fun cacheTrips(
        period: String,
        trips: List<Trip>,
    ) {
        try {
            // Clean up old cache entries if needed
            if (tripCache.size >= MAX_TRIP_CACHE_SIZE) {
                evictOldestEntries(tripCache)
            }

            val cachedData =
                CachedTripData(
                    trips = trips,
                    timestamp = System.currentTimeMillis(),
                    period = period,
                )

            tripCache[period] = cachedData
            updateCacheState()

            Log.d(TAG, "Cached ${trips.size} trips for period: $period")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache trips for period: $period", e)
        }
    }

    /**
     * ✅ NEW: Get cached statistics or return null if expired/missing
     */
    fun getCachedStats(period: String): StatisticsSet? {
        val cached = statsCache[period]
        if (cached != null && !isExpired(cached.timestamp, STATS_CACHE_DURATION_MS)) {
            cacheHits++
            Log.v(TAG, "Cache HIT for stats period: $period")
            return cached.stats
        }

        cacheMisses++
        Log.v(TAG, "Cache MISS for stats period: $period")
        return null
    }

    /**
     * ✅ NEW: Cache statistics data
     */
    fun cacheStats(
        period: String,
        stats: StatisticsSet,
        tripCount: Int,
    ) {
        try {
            val cachedData =
                CachedStatsData(
                    stats = stats,
                    timestamp = System.currentTimeMillis(),
                    period = period,
                    tripCount = tripCount,
                )

            statsCache[period] = cachedData
            updateCacheState()

            Log.d(TAG, "Cached stats for period: $period ($tripCount trips)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache stats for period: $period", e)
        }
    }

    /**
     * ✅ NEW: Get cached GPS data or return null if expired/missing
     */
    fun getCachedGpsData(tripId: Long): Map<String, Any>? {
        val key = "gps_$tripId"
        val cached = gpsCache[key]
        if (cached != null && !isExpired(cached.timestamp, GPS_CACHE_DURATION_MS)) {
            cacheHits++
            Log.v(TAG, "Cache HIT for GPS data")
            return cached.gpsMetadata
        }

        cacheMisses++
        Log.v(TAG, "Cache MISS for GPS data")
        return null
    }

    /**
     * ✅ NEW: Cache GPS metadata
     */
    fun cacheGpsData(
        tripId: Long,
        gpsMetadata: Map<String, Any>,
    ) {
        try {
            // Clean up old GPS cache entries if needed
            if (gpsCache.size >= MAX_GPS_CACHE_SIZE) {
                evictOldestEntries(gpsCache)
            }

            val key = "gps_$tripId"
            val cachedData =
                CachedGpsData(
                    gpsMetadata = gpsMetadata,
                    timestamp = System.currentTimeMillis(),
                    tripId = tripId,
                )

            gpsCache[key] = cachedData
            updateCacheState()

            Log.d(TAG, "Cached GPS data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache GPS data", e)
        }
    }

    /**
     * ✅ NEW: Invalidate cache entries for a specific period
     */
    fun invalidatePeriod(period: String) {
        tripCache.remove(period)
        statsCache.remove(period)
        Log.d(TAG, "Invalidated cache for period: $period")
        updateCacheState()
    }

    /**
     * ✅ NEW: Invalidate all cache entries
     */
    fun invalidateAll() {
        tripCache.clear()
        statsCache.clear()
        gpsCache.clear()
        Log.d(TAG, "Invalidated all cache entries")
        updateCacheState()
    }

    /**
     * ✅ NEW: Clean up expired cache entries
     */
    fun cleanupExpiredEntries() {
        var cleanedCount = 0

        // Clean trip cache
        tripCache.entries.removeIf { (_, cached) ->
            if (isExpired(cached.timestamp, TRIP_CACHE_DURATION_MS)) {
                cleanedCount++
                true
            } else {
                false
            }
        }

        // Clean stats cache
        statsCache.entries.removeIf { (_, cached) ->
            if (isExpired(cached.timestamp, STATS_CACHE_DURATION_MS)) {
                cleanedCount++
                true
            } else {
                false
            }
        }

        // Clean GPS cache
        gpsCache.entries.removeIf { (_, cached) ->
            if (isExpired(cached.timestamp, GPS_CACHE_DURATION_MS)) {
                cleanedCount++
                true
            } else {
                false
            }
        }

        if (cleanedCount > 0) {
            Log.d(TAG, "Cleaned up $cleanedCount expired cache entries")
            updateCacheState()
        }
    }

    /**
     * ✅ NEW: Get cache performance metrics
     */
    fun getCacheMetrics(): CacheMetrics {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) (cacheHits.toDouble() / totalRequests) * 100 else 0.0

        return CacheMetrics(
            hitRate = hitRate,
            totalRequests = totalRequests,
            cacheHits = cacheHits,
            cacheMisses = cacheMisses,
            cacheEvictions = cacheEvictions,
            tripCacheSize = tripCache.size,
            statsCacheSize = statsCache.size,
            gpsCacheSize = gpsCache.size,
            totalCacheSize = tripCache.size + statsCache.size + gpsCache.size,
        )
    }

    /**
     * ✅ NEW: Cache metrics data class
     */
    data class CacheMetrics(
        val hitRate: Double,
        val totalRequests: Long,
        val cacheHits: Long,
        val cacheMisses: Long,
        val cacheEvictions: Long,
        val tripCacheSize: Int,
        val statsCacheSize: Int,
        val gpsCacheSize: Int,
        val totalCacheSize: Int,
    )

    /**
     * ✅ PRIVATE: Check if cache entry is expired
     */
    private fun isExpired(
        timestamp: Long,
        maxAge: Long,
    ): Boolean {
        return System.currentTimeMillis() - timestamp > maxAge
    }

    /**
     * ✅ PRIVATE: Evict oldest cache entries when cache is full
     */
    private fun <T> evictOldestEntries(cache: ConcurrentHashMap<String, T>) {
        val oldestEntry =
            cache.entries.minByOrNull { entry ->
                when (val value = entry.value) {
                    is CachedTripData -> value.timestamp
                    is CachedStatsData -> value.timestamp
                    is CachedGpsData -> value.timestamp
                    else -> Long.MAX_VALUE
                }
            }

        oldestEntry?.let {
            cache.remove(it.key)
            cacheEvictions++
            Log.v(TAG, "Evicted oldest cache entry: ${it.key}")
        }
    }

    /**
     * ✅ PRIVATE: Update cache state for monitoring
     */
    private fun updateCacheState() {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) (cacheHits.toDouble() / totalRequests) * 100 else 0.0
        val totalCacheSize = tripCache.size + statsCache.size + gpsCache.size

        _cacheState.value =
            CacheState(
                isEnabled = true,
                hitRate = hitRate,
                totalRequests = totalRequests,
                cacheSize = totalCacheSize,
                lastCleanup = Date(),
                memoryUsage = estimateMemoryUsage(),
                isHealthy = hitRate > 50.0 && totalCacheSize < 200,
            )
    }

    /**
     * ✅ PRIVATE: Estimate memory usage of cache
     */
    private fun estimateMemoryUsage(): Long {
        // Rough estimation: each cache entry ~1KB
        val totalEntries = tripCache.size + statsCache.size + gpsCache.size
        return totalEntries * 1024L
    }

    // ==================== NEW: VIEWMODEL INTEGRATION METHODS ====================

    /**
     * ✅ NEW: Get cache hit rate for ViewModel
     */
    fun getHitRate(): Double {
        val totalRequests = cacheHits + cacheMisses
        return if (totalRequests > 0) {
            (cacheHits.toDouble() / totalRequests) * 100
        } else {
            0.0
        }
    }

    /**
     * ✅ NEW: Optimize cache for ViewModel
     */
    fun optimize() {
        try {
            Log.d(TAG, "Optimizing cache")

            // Clean up expired entries
            cleanupExpiredEntries()

            // Evict oldest entries if cache is too large
            if (tripCache.size > MAX_TRIP_CACHE_SIZE * 0.8) {
                evictOldestEntries(tripCache)
            }

            if (gpsCache.size > MAX_GPS_CACHE_SIZE * 0.8) {
                evictOldestEntries(gpsCache)
            }

            // Update cache state
            updateCacheState()

            Log.d(TAG, "Cache optimization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Cache optimization failed", e)
        }
    }

    /**
     * ✅ NEW: Update trip cache for ViewModel
     */
    fun updateTripCache(trip: com.example.outofroutebuddy.domain.models.Trip) {
        try {
            // Find existing trip in cache and update it
            val period = "current"
            val existingTrips = tripCache[period]?.trips?.toMutableList() ?: mutableListOf()

            // Remove existing trip if it exists
            existingTrips.removeAll { it.id == trip.id }

            // Add updated trip
            existingTrips.add(trip)

            // Update cache
            cacheTrips(period, existingTrips)

            Log.d(TAG, "Updated trip cache for trip: ${trip.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip cache for trip: ${trip.id}", e)
        }
    }

    /**
     * ✅ NEW: Clear current trip from cache for ViewModel
     */
    fun clearCurrentTrip() {
        try {
            // Remove current trip from cache
            tripCache.remove("current")

            // Clear any GPS data for current trip
            gpsCache.entries.removeIf { (key, _) ->
                key.startsWith("gps_current")
            }

            updateCacheState()
            Log.d(TAG, "Cleared current trip from cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear current trip from cache", e)
        }
    }
} 

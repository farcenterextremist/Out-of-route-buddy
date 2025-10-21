package com.example.outofroutebuddy.services.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for location services
 * 
 * This interface defines the common contract for all location-related services
 * in the application, providing a consistent API for location operations.
 */
interface LocationService {
    
    /**
     * Get current location
     * 
     * @return Flow of current location updates
     */
    fun getCurrentLocation(): Flow<Location?>
    
    /**
     * Get location accuracy
     * 
     * @return Current location accuracy in meters
     */
    fun getLocationAccuracy(): Float
    
    /**
     * Get location speed
     * 
     * @return Current speed in meters per second
     */
    fun getLocationSpeed(): Float
    
    /**
     * Check if location service is active
     * 
     * @return True if location service is active
     */
    fun isLocationServiceActive(): Boolean
    
    /**
     * Start location service
     */
    suspend fun startLocationService()
    
    /**
     * Stop location service
     */
    suspend fun stopLocationService()
    
    /**
     * Get location provider status
     * 
     * @return Location provider status
     */
    fun getLocationProviderStatus(): LocationProviderStatus
    
    /**
     * Get satellite count
     * 
     * @return Number of satellites in view
     */
    fun getSatelliteCount(): Int
    
    /**
     * Get GPS quality percentage
     * 
     * @return GPS quality as percentage (0-100)
     */
    fun getGpsQualityPercentage(): Double
    
    /**
     * Get last location update time
     * 
     * @return Timestamp of last location update
     */
    fun getLastLocationUpdateTime(): Long
    
    /**
     * Check if location permission is granted
     * 
     * @return True if location permission is granted
     */
    fun hasLocationPermission(): Boolean
    
    /**
     * Request location permission
     */
    fun requestLocationPermission()
    
    /**
     * Get location error count
     * 
     * @return Number of consecutive location errors
     */
    fun getLocationErrorCount(): Int
    
    /**
     * Reset location error count
     */
    fun resetLocationErrorCount()
    
    /**
     * Get location service configuration
     * 
     * @return Location service configuration
     */
    fun getLocationServiceConfig(): LocationServiceConfig
}

/**
 * Location provider status
 */
data class LocationProviderStatus(
    val isGpsEnabled: Boolean = false,
    val isNetworkEnabled: Boolean = false,
    val isPassiveEnabled: Boolean = false,
    val gpsAccuracy: Float = 0f,
    val networkAccuracy: Float = 0f,
    val bestProvider: String? = null,
    val lastKnownLocation: Location? = null
)

/**
 * Location service configuration
 */
data class LocationServiceConfig(
    val minUpdateDistance: Float = 5.0f,
    val minUpdateTime: Long = 1000L,
    val maxLocationAge: Long = 30000L,
    val maxDistanceBetweenUpdates: Float = 1000.0f,
    val minDistanceThreshold: Float = 5.0f,
    val maxStationaryTime: Long = 300000L,
    val maxConsecutiveErrors: Int = 10,
    val errorRecoveryInterval: Long = 60000L,
    val errorRecoveryDelay: Long = 5000L,
    val monitoringInterval: Long = 10000L
) 

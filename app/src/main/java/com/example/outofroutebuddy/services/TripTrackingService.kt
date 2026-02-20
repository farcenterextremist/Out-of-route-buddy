package com.example.outofroutebuddy.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.repository.TripRepository
import com.example.outofroutebuddy.models.Trip
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.core.config.BuildConfig
import com.example.outofroutebuddy.util.TimeoutManager
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.*

// ✅ IMPLEMENTED: Handle service crashes and notify the user if trip tracking fails.
// Firebase Crashlytics integration added for comprehensive crash reporting.

const val ACTION_START_TRIP = "ACTION_START_TRIP"
const val ACTION_END_TRIP = "ACTION_END_TRIP"
const val ACTION_PAUSE_TRIP = "ACTION_PAUSE_TRIP"
const val ACTION_RESUME_TRIP = "ACTION_RESUME_TRIP"
const val EXTRA_EXPECTED_MILES = "EXTRA_EXPECTED_MILES"
private const val EXTRA_INITIAL_TOTAL_MILES = "EXTRA_INITIAL_TOTAL_MILES"
    private const val NOTIFICATION_ID = BuildConfig.NOTIFICATION_ID
private const val NOTIFICATION_CHANNEL_ID = "TripTrackingChannel"
private const val TAG = "TripTrackingService"

data class TripMetrics(
    val totalMiles: Double = 0.0,
    val oorMiles: Double = 0.0
)

data class ServiceState(
    val isRunning: Boolean = false,
    val isHealthy: Boolean = true,
    val lastError: String? = null,
    val startTime: Long = 0L,
    val lastLocationUpdate: Long = 0L
)

// ✅ NEW: Enhanced GPS tracking data structure
data class GpsTrackingData(
    val currentAccuracy: Float = 0f,
    val currentSpeedMph: Float = 0f,
    val totalGpsPoints: Int = 0,
    val validGpsPoints: Int = 0,
    val rejectedGpsPoints: Int = 0,
    val gpsQualityPercentage: Float = 0f,
    val locationJumpsDetected: Int = 0,
    val accuracyWarnings: Int = 0,
    val speedAnomalies: Int = 0,
    val tripDurationMinutes: Int = 0,
    val lastLocationUpdate: Long = 0L,
    val gpsSignalStatus: String = "Unknown",
    val avgAccuracy: Float = 0f,
    val minAccuracy: Float = Float.MAX_VALUE,
    val maxAccuracy: Float = 0f,
    val avgSpeedMph: Float = 0f,
    val maxSpeedMph: Float = 0f,
    val totalDistanceMiles: Float = 0f,
    val currentLocation: String = "Unknown",
    val satelliteCount: Int = 0,
    val isHighAccuracyMode: Boolean = false
)

class TripTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: TripRepository
    private lateinit var tripCalculationService: TripCalculationService
    private lateinit var locationValidationService: LocationValidationService

    private var totalDistance = 0.0
    private var loadedMiles = 0.0
    private var bounceMiles = 0.0
    private var lastLocation: Location? = null
    private var lastUpdateTime = 0L
    
    // ✅ ADDED: Service state tracking for crash recovery
    private var serviceState = ServiceState()
    private var consecutiveErrors = 0
    private val maxConsecutiveErrors = 3
    
    // ✅ NEW: Pause state tracking
    private var isPaused = false
    
    // ✅ ENHANCED: GPS metadata tracking for accuracy auditing
    private var totalGpsPoints = 0
    private var validGpsPoints = 0
    private var rejectedGpsPoints = 0
    private var locationJumpsDetected = 0
    private var accuracyWarnings = 0
    private var speedAnomalies = 0
    private var minGpsAccuracy = Double.MAX_VALUE
    private var maxGpsAccuracy = 0.0
    private var totalGpsAccuracy = 0.0
    private var totalSpeedMph = 0.0
    private var maxSpeedMph = 0.0
    private var speedReadings = 0
    private var tripStartTime = 0L
    private var interruptionCount = 0
    private var lastHealthCheckTime = 0L
    private val healthCheckInterval = 30000L // 30 seconds
    
    // ✅ NEW: Enhanced GPS tracking data
    private var currentAccuracy = 0f
    private var currentSpeedMph = 0f
    private var satelliteCount = 0
    private var isHighAccuracyMode = false
    private var currentLocationString = "Unknown"

    companion object {
        private val _tripMetrics = MutableStateFlow(TripMetrics())
        val tripMetrics: StateFlow<TripMetrics> = _tripMetrics.asStateFlow()
        
        private val _serviceState = MutableStateFlow(ServiceState())
        val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()
        
        // ✅ NEW: Enhanced GPS tracking data flow
        private val _gpsTrackingData = MutableStateFlow(GpsTrackingData())
        val gpsTrackingData: StateFlow<GpsTrackingData> = _gpsTrackingData.asStateFlow()

        fun startService(context: Context, loadedMiles: Double, bounceMiles: Double, initialTotalMiles: Double? = null) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_START_TRIP
                    putExtra("EXTRA_LOADED_MILES", loadedMiles)
                    putExtra("EXTRA_BOUNCE_MILES", bounceMiles)
                    initialTotalMiles?.let { putExtra(EXTRA_INITIAL_TOTAL_MILES, it) }
                }
                context.startService(intent)
                Log.d(TAG, "Trip tracking service start requested")
                
                // ✅ ADDED: Log analytics event for trip start
                (context.applicationContext as? OutOfRouteApplication)?.logAnalyticsEvent(
                    "trip_started",
                    mapOf(
                        "loaded_miles" to loadedMiles.toString(),
                        "bounce_miles" to bounceMiles.toString()
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start trip tracking service", e)
                notifyUserOfServiceFailure(context, "Failed to start trip tracking: ${e.message}")
                
                // ✅ ADDED: Report error to Firebase Crashlytics
                (context.applicationContext as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                    "Failed to start trip tracking service",
                    e
                )
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_END_TRIP
                }
                context.startService(intent)
                Log.d(TAG, "Trip tracking service stop requested")
                
                // ✅ ADDED: Log analytics event for trip end
                (context.applicationContext as? OutOfRouteApplication)?.logAnalyticsEvent("trip_ended")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop trip tracking service", e)
                notifyUserOfServiceFailure(context, "Failed to stop trip tracking: ${e.message}")
                
                // ✅ ADDED: Report error to Firebase Crashlytics
                (context.applicationContext as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                    "Failed to stop trip tracking service",
                    e
                )
            }
        }
        
        /**
         * ✅ NEW: Pause trip tracking service
         */
        fun pauseService(context: Context) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_PAUSE_TRIP
                }
                context.startService(intent)
                Log.d(TAG, "Trip tracking service pause requested")
                
                // ✅ ADDED: Log analytics event for trip pause
                (context.applicationContext as? OutOfRouteApplication)?.logAnalyticsEvent("trip_paused")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pause trip tracking service", e)
                notifyUserOfServiceFailure(context, "Failed to pause trip tracking: ${e.message}")
                
                // ✅ ADDED: Report error to Firebase Crashlytics
                (context.applicationContext as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                    "Failed to pause trip tracking service",
                    e
                )
            }
        }
        
        /**
         * ✅ NEW: Resume trip tracking service
         */
        fun resumeService(context: Context) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_RESUME_TRIP
                }
                context.startService(intent)
                Log.d(TAG, "Trip tracking service resume requested")
                
                // ✅ ADDED: Log analytics event for trip resume
                (context.applicationContext as? OutOfRouteApplication)?.logAnalyticsEvent("trip_resumed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resume trip tracking service", e)
                notifyUserOfServiceFailure(context, "Failed to resume trip tracking: ${e.message}")
                
                // ✅ ADDED: Report error to Firebase Crashlytics
                (context.applicationContext as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                    "Failed to resume trip tracking service",
                    e
                )
            }
        }
        
        /**
         * Notify user of service failures
         */
        private fun notifyUserOfServiceFailure(context: Context, message: String) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Trip Tracking Error")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID + 1, notification)
                
                // ✅ ADDED: Log analytics event for service failure
                (context.applicationContext as? OutOfRouteApplication)?.logAnalyticsEvent(
                    "service_failure",
                    mapOf("error_message" to message)
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show error notification", e)
                
                // ✅ ADDED: Report notification failure to Firebase Crashlytics
                (context.applicationContext as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                    "Failed to show error notification",
                    e
                )
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            try {
                // ✅ REFACTORED: Directly process the location. The validation now happens inside updateLocation.
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                    // Reset error counter on any valid or invalid location packet.
                    // A failure is now only a complete lack of packets.
                    consecutiveErrors = 0 
                    updateServiceState(true, null)
                } ?: run {
                    handleLocationFailure("No location in locationResult")
                }
            } catch (e: Exception) {
                handleLocationFailure("Location processing error: ${e.message}")
            }
        }
        
        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            try {
                if (!locationAvailability.isLocationAvailable) {
                    handleLocationFailure("Location services unavailable")
                } else {
                    updateServiceState(true, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling location availability", e)
            }
        }
    }
    
    // ✅ REFACTORED: This is the new, cleaner location update logic with vehicle-specific validation.
    private fun updateLocation(location: Location) {
        try {
            // ✅ NEW: Track GPS metadata for accuracy auditing
            totalGpsPoints++
            totalGpsAccuracy += location.accuracy.toDouble()
            
            if (location.accuracy < minGpsAccuracy) {
                minGpsAccuracy = location.accuracy.toDouble()
            }
            if (location.accuracy > maxGpsAccuracy) {
                maxGpsAccuracy = location.accuracy.toDouble()
            }
            
            // Track speed data if available
            if (location.hasSpeed()) {
                val speedMph = location.speed * 2.237f
                totalSpeedMph += speedMph.toDouble()
                speedReadings++
                
                if (speedMph > maxSpeedMph) {
                    maxSpeedMph = speedMph.toDouble()
                }
            }
            
            // ✅ IMPROVED: Use vehicle-specific validation for better accuracy
            val validatedDistanceInMeters = locationValidationService.getValidatedVehicleDistance(location, lastLocation)

            if (validatedDistanceInMeters > 0) {
                // The distance is valid, add it to the total IF NOT PAUSED
                // Conversion from meters to miles happens here.
                val distanceInMiles = validatedDistanceInMeters / 1609.34
                
                // ✅ CRITICAL FIX: Only accumulate distance if trip is not paused
                if (!isPaused) {
                    totalDistance += distanceInMiles
                    validGpsPoints++
                } else {
                    // Trip is paused - log but don't accumulate distance
                    Log.d(TAG, "Location update received while paused - distance not accumulated")
                    rejectedGpsPoints++
                }
                
                // ✅ NEW: Log detailed tracking information for debugging
                Log.i(
                    TAG, "Valid vehicle location update. " +
                            "Distance: ${String.format(Locale.US, "%.4f", distanceInMiles)} miles, " +
                            "Accuracy: ${String.format(Locale.US, "%.1f", location.accuracy)}m, " +
                            "Speed: ${if (location.hasSpeed()) String.format(Locale.US, "%.1f", location.speed * 2.237) else "N/A"} mph, " +
                            "Total: ${String.format(Locale.US, "%.2f", totalDistance)} miles."
                )
            } else {
                // Location was rejected by validation
                rejectedGpsPoints++
                
                // ✅ NEW: Track specific rejection reasons
                if (location.accuracy > LocationValidationService.VEHICLE_MIN_ACCURACY) {
                    accuracyWarnings++
                }
                
                // Check for speed anomalies
                if (location.hasSpeed()) {
                    val speedMph = location.speed * 2.237f
                    if (speedMph > LocationValidationService.VEHICLE_MAX_SPEED_MPH) {
                        speedAnomalies++
                    }
                }
                
                // ✅ NEW: Log why location was rejected
                Log.d(
                    TAG, "Location rejected by vehicle validation. " +
                            "Accuracy: ${String.format(Locale.US, "%.1f", location.accuracy)}m, " +
                            "Speed: ${if (location.hasSpeed()) String.format(Locale.US, "%.1f", location.speed * 2.237) else "N/A"} mph"
                )
            }
            // If validatedDistanceInMeters is 0, we do nothing, as the point was discarded by the validator.

            // Always update the last location to the newest valid point for the next calculation.
            lastLocation = location
            lastUpdateTime = System.currentTimeMillis()

            val dispatchedMiles = loadedMiles + bounceMiles
            val oorMiles = totalDistance - dispatchedMiles

            _tripMetrics.value = TripMetrics(totalDistance, oorMiles)
            updateNotification("Total: ${String.format(Locale.US, "%.1f", totalDistance)} mi, OOR: ${String.format(Locale.US, "%.1f", oorMiles)} mi")
            
            // ✅ NEW: Broadcast real-time GPS data to UI
            broadcastRealTimeGpsData(location)
            
            // ✅ NEW: Perform periodic health check
            performHealthCheck()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating location", e)
            handleLocationFailure("Location update error: ${e.message}")
        }
    }

    // ❌ REMOVED: isLocationValid is no longer needed as the logic is centralized 
    // in LocationValidationService.getValidatedDistance
    
    private fun handleLocationValidationFailure(location: Location) {
        consecutiveErrors++
        val errorMessage = "Invalid location received: lat=${location.latitude}, lng=${location.longitude}"
        Log.w(TAG, errorMessage)
        
        if (consecutiveErrors >= maxConsecutiveErrors) {
            handleLocationFailure("Too many invalid locations received")
        }
    }
    
    private fun handleLocationFailure(message: String) {
        consecutiveErrors++
        Log.w(TAG, "Location failure: $message (consecutive errors: $consecutiveErrors)")
        
        if (consecutiveErrors >= maxConsecutiveErrors) {
            val errorMessage = "Trip tracking may be inaccurate: $message"
            updateServiceState(false, errorMessage)
            notifyUserOfServiceFailure(this, errorMessage)
            
            // ✅ NEW: Attempt GPS recovery before giving up
            attemptGpsRecovery()
            
            // ✅ ADDED: Report location failure to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Location tracking failure: $message",
                null
            )
        }
    }
    
    /**
     * ✅ NEW: Attempt to recover GPS tracking when it fails.
     * Runs on background scope to avoid blocking the main thread (location callbacks use MainLooper).
     */
    private fun attemptGpsRecovery() {
        serviceScope.launch {
            try {
                Log.i(TAG, "Attempting GPS recovery...")
                stopLocationUpdates()
                delay(2000)
                startLocationUpdates()
                Log.i(TAG, "GPS recovery attempt completed")
            } catch (e: Exception) {
                Log.e(TAG, "GPS recovery failed", e)
            }
        }
    }
    
    /**
     * ✅ NEW: Perform periodic health check to detect GPS issues
     */
    private fun performHealthCheck() {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Only check every 30 seconds
            if (currentTime - lastHealthCheckTime < healthCheckInterval) {
                return
            }
            
            lastHealthCheckTime = currentTime
            
            // Check if we haven't received location updates for too long
            val timeSinceLastUpdate = currentTime - lastUpdateTime
            if (timeSinceLastUpdate > 60000) { // 60 seconds without updates
                Log.w(TAG, "No GPS updates for ${timeSinceLastUpdate / 1000}s - attempting recovery")
                attemptGpsRecovery()
            }
            
            // Check if we're getting too many consecutive errors
            if (consecutiveErrors > 5) {
                Log.w(TAG, "High error count: $consecutiveErrors - attempting recovery")
                attemptGpsRecovery()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d(TAG, "TripTrackingService onCreate started")
            
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            repository = (application as OutOfRouteApplication).tripRepository
            tripCalculationService = TripCalculationService()
            locationValidationService = LocationValidationService()
            
            createNotificationChannel()
            updateServiceState(true, null)
            
            Log.d(TAG, "TripTrackingService onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TripTrackingService", e)
            updateServiceState(false, "Service initialization failed: ${e.message}")
            notifyUserOfServiceFailure(this, "Service initialization failed: ${e.message}")
            
            // ✅ ADDED: Report service initialization failure to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "TripTrackingService initialization failed",
                e
            )
            
            throw e
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            // ✅ IMPROVED (#14): Handle service restart after system kill
            if (intent == null) {
                Log.w(TAG, "⚠️ Service restarted by system (null intent) - attempting recovery")
                handleServiceRestart()
                return START_STICKY // Always restart service
            }
            
            Log.d(TAG, "onStartCommand received: ${intent.action}")
            
            when (intent.action) {
                ACTION_START_TRIP -> {
                    loadedMiles = intent.getDoubleExtra("EXTRA_LOADED_MILES", 0.0)
                    bounceMiles = intent.getDoubleExtra("EXTRA_BOUNCE_MILES", 0.0)
                    // If resuming a recovered trip, seed totalDistance
                    totalDistance = intent.getDoubleExtra(EXTRA_INITIAL_TOTAL_MILES, totalDistance)
                    startTrip()
                }
                ACTION_END_TRIP -> {
                    stopTrip()
                }
                ACTION_PAUSE_TRIP -> {
                    pauseTrip()
                }
                ACTION_RESUME_TRIP -> {
                    resumeTrip()
                }
                "TIMEZONE_CHANGED" -> {
                    handleTimeZoneChange()
                }
                else -> {
                    Log.w(TAG, "Unknown action received: ${intent.action}")
                }
            }
            
            // ✅ IMPROVED (#14): Always return START_STICKY for reliability
            return START_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            updateServiceState(false, "Service command error: ${e.message}")
            notifyUserOfServiceFailure(this, "Service command error: ${e.message}")
            
            // ✅ ADDED: Report service command error to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "TripTrackingService command error",
                e
            )
            
            // ✅ IMPROVED (#14): Return START_STICKY even on error to allow recovery
            return START_STICKY
        }
    }
    
    /**
     * ✅ NEW (#14): Handle service restart after system kill
     */
    private fun handleServiceRestart() {
        try {
            Log.d(TAG, "Attempting to restore service state after restart")
            
            // Try to restore state from SharedPreferences
            val prefs = getSharedPreferences("trip_service_state", Context.MODE_PRIVATE)
            val wasTracking = prefs.getBoolean("was_tracking", false)
            
            if (wasTracking) {
                // Restore trip parameters
                loadedMiles = prefs.getFloat("loaded_miles", 0f).toDouble()
                bounceMiles = prefs.getFloat("bounce_miles", 0f).toDouble()
                totalDistance = prefs.getFloat("total_distance", 0f).toDouble()
                
                Log.i(TAG, "✅ Restored trip state: loaded=$loadedMiles, bounce=$bounceMiles, distance=$totalDistance")
                
                // Show notification to user
                val notification = createNotification(
                    "⚠️ Trip Restored - Tracking resumed after service restart"
                )
                startForeground(NOTIFICATION_ID, notification)
                
                // Resume tracking
                startLocationUpdates()
            } else {
                Log.d(TAG, "No active trip to restore")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring service state", e)
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Service restart recovery error",
                e
            )
        }
    }
    
    /**
     * ✅ NEW (#14): Save service state for recovery after system kill
     */
    private fun saveServiceState() {
        try {
            getSharedPreferences("trip_service_state", Context.MODE_PRIVATE).edit().apply {
                putBoolean("was_tracking", true)
                putFloat("loaded_miles", loadedMiles.toFloat())
                putFloat("bounce_miles", bounceMiles.toFloat())
                putFloat("total_distance", totalDistance.toFloat())
                putLong("start_time", System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "Service state saved for recovery")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving service state", e)
        }
    }
    
    /**
     * ✅ NEW (#14): Clear service state when trip ends normally
     */
    private fun clearServiceState() {
        try {
            getSharedPreferences("trip_service_state", Context.MODE_PRIVATE).edit {clear()}
            Log.d(TAG, "Service state cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing service state", e)
        }
    }
    
    private fun updateServiceState(isHealthy: Boolean, error: String?) {
        serviceState = serviceState.copy(
            isHealthy = isHealthy,
            lastError = error,
            lastLocationUpdate = if (isHealthy) System.currentTimeMillis() else serviceState.lastLocationUpdate
        )
        _serviceState.value = serviceState
    }

    private fun startTrip() {
        try {
            // ✅ FIX: Preserve seeded totalDistance for trip recovery
            // If totalDistance was seeded via EXTRA_INITIAL_TOTAL_MILES (trip recovery),
            // don't reset it to 0.0 - keep the existing value so UI doesn't jump backwards
            val preserveDistance = totalDistance > 0.0
            val seededDistance = if (preserveDistance) totalDistance else 0.0
            
            Log.d(TAG, "Starting trip with loaded: $loadedMiles, bounce: $bounceMiles, ${if (preserveDistance) "resuming from ${seededDistance}mi" else "starting fresh"}")
            
            totalDistance = seededDistance
            lastLocation = null
            lastUpdateTime = System.currentTimeMillis()
            consecutiveErrors = 0
            
            // ✅ NEW: Initialize GPS metadata tracking
            totalGpsPoints = 0
            validGpsPoints = 0
            rejectedGpsPoints = 0
            locationJumpsDetected = 0
            accuracyWarnings = 0
            speedAnomalies = 0
            
            // ✅ NEW (#14): Persist service state for recovery
            saveServiceState()
            minGpsAccuracy = Double.MAX_VALUE
            maxGpsAccuracy = 0.0
            totalGpsAccuracy = 0.0
            totalSpeedMph = 0.0
            maxSpeedMph = 0.0
            speedReadings = 0
            tripStartTime = System.currentTimeMillis()
            interruptionCount = 0
            
            // ✅ FIX: Initialize metrics with seeded distance for trip recovery
            _tripMetrics.value = TripMetrics(totalDistance, 0.0)
            serviceState = serviceState.copy(
                isRunning = true,
                isHealthy = true,
                lastError = null,
                startTime = System.currentTimeMillis()
            )
            _serviceState.value = serviceState
            
            startForeground(NOTIFICATION_ID, createNotification("Trip in progress..."))
            startLocationUpdates()
            
            Log.d(TAG, "Trip started successfully with GPS metadata tracking")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start trip", e)
            updateServiceState(false, "Failed to start trip: ${e.message}")
            notifyUserOfServiceFailure(this, "Failed to start trip: ${e.message}")
            
            // ✅ ADDED: Report trip start failure to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Failed to start trip",
                e
            )
            
            throw e
        }
    }

    private fun stopTrip() {
        try {
            Log.d(TAG, "Stopping trip - totalDistance: $totalDistance miles")
            
            // ✅ NEW (#14): Clear persisted state when trip ends normally
            clearServiceState()
            
            // Log final trip calculations for verification
            val dispatchedMiles = loadedMiles + bounceMiles
            val oorMiles = totalDistance - dispatchedMiles
            val oorPercentage = if (dispatchedMiles > 0) (oorMiles / dispatchedMiles) * 100 else 0.0
            
            Log.d(TAG, "Final trip calculations:")
            Log.d(TAG, "  - Loaded miles: $loadedMiles")
            Log.d(TAG, "  - Bounce miles: $bounceMiles")
            Log.d(TAG, "  - Dispatched miles: $dispatchedMiles")
            Log.d(TAG, "  - Actual miles: $totalDistance")
            Log.d(TAG, "  - OOR miles: $oorMiles")
            Log.d(TAG, "  - OOR percentage: ${String.format(Locale.US, "%.2f%%", oorPercentage)}")
            
            // ✅ ADDED: Log trip completion analytics
            (application as? OutOfRouteApplication)?.logAnalyticsEvent(
                "trip_completed",
                mapOf(
                    "loaded_miles" to loadedMiles.toString(),
                    "bounce_miles" to bounceMiles.toString(),
                    "actual_miles" to totalDistance.toString(),
                    "oor_miles" to oorMiles.toString(),
                    "oor_percentage" to String.format(Locale.US, "%.2f", oorPercentage)
                )
            )
            
            // Don't automatically save the trip - let the ViewModel handle saving decisions
            // The ViewModel will call saveCurrentTrip() if the user chooses to save
            
            _tripMetrics.value = TripMetrics(0.0, 0.0) // Reset metrics
            serviceState = serviceState.copy(isRunning = false, isHealthy = true, lastError = null)
            _serviceState.value = serviceState
            
            stopLocationUpdates()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            
            Log.d(TAG, "Trip stopped successfully - waiting for user save/discard decision")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trip", e)
            updateServiceState(false, "Failed to stop trip: ${e.message}")
            notifyUserOfServiceFailure(this, "Failed to stop trip: ${e.message}")
            
            // ✅ ADDED: Report trip stop failure to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Failed to stop trip",
                e
            )
            
            throw e
        }
    }
    
    /**
     * ✅ NEW: Pause trip tracking (stop accumulating distance)
     */
    private fun pauseTrip() {
        try {
            Log.d(TAG, "Pausing trip tracking")
            
            isPaused = true
            
            // Update notification to show paused state
            updateNotification("Trip PAUSED - Total: ${String.format(Locale.US, "%.1f", totalDistance)} mi")
            
            Log.d(TAG, "Trip paused successfully - distance accumulation stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause trip", e)
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Failed to pause trip",
                e
            )
        }
    }
    
    /**
     * ✅ NEW: Resume trip tracking (resume accumulating distance)
     */
    private fun resumeTrip() {
        try {
            Log.d(TAG, "Resuming trip tracking")
            
            isPaused = false
            
            // Update notification to show active state
            val dispatchedMiles = loadedMiles + bounceMiles
            val oorMiles = totalDistance - dispatchedMiles
            updateNotification("Total: ${String.format(Locale.US, "%.1f", totalDistance)} mi, OOR: ${String.format(Locale.US, "%.1f", oorMiles)} mi")
            
            Log.d(TAG, "Trip resumed successfully - distance accumulation resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume trip", e)
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Failed to resume trip",
                e
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            // ✅ IMPROVED: More resilient location request configuration
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000) // ✅ FIXED: Increased to 15 seconds for better reliability
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setMinUpdateDistanceMeters(20f) // ✅ FIXED: Reduced to 20 meters for more frequent updates
                .setWaitForAccurateLocation(false) // ✅ FIXED: Don't wait for accurate location to prevent blocking
                .setMaxUpdates(2000) // ✅ FIXED: Increased limit for longer trips
                .setMaxUpdateDelayMillis(30000) // ✅ FIXED: Increased max delay to 30 seconds
                .build()
            
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.d(TAG, "Resilient location updates started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location updates", e)
            updateServiceState(false, "Failed to start location updates: ${e.message}")
            notifyUserOfServiceFailure(this, "Failed to start location updates: ${e.message}")
            
            // ✅ ADDED: Report location updates failure to Firebase Crashlytics
            (application as? OutOfRouteApplication)?.reportErrorToCrashlytics(
                "Failed to start location updates",
                e
            )
            
            throw e
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Location updates stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates", e)
        }
    }

    private fun createNotification(text: String) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Out of Route Buddy")
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .build()

    private fun updateNotification(text: String) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification(text))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Trip Tracking Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(serviceChannel)
                Log.d(TAG, "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channel", e)
        }
    }
    
    override fun onDestroy() {
        try {
            Log.d(TAG, "TripTrackingService onDestroy")
            stopLocationUpdates()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * ✅ NEW: Get GPS metadata for trip saving
     * This provides detailed GPS accuracy and validation statistics for auditing.
     */
    fun getGpsMetadata(): Map<String, Any> {
        val tripDurationMinutes = if (tripStartTime > 0) {
            ((System.currentTimeMillis() - tripStartTime) / 60000).toInt()
        } else 0
        
        val avgGpsAccuracy = if (totalGpsPoints > 0) totalGpsAccuracy / totalGpsPoints else 0.0
        val avgSpeedMph = if (speedReadings > 0) totalSpeedMph / speedReadings else 0.0
        
        return mapOf(
            "avgGpsAccuracy" to avgGpsAccuracy,
            "minGpsAccuracy" to (if (minGpsAccuracy == Double.MAX_VALUE) 0.0 else minGpsAccuracy),
            "maxGpsAccuracy" to maxGpsAccuracy,
            "totalGpsPoints" to totalGpsPoints,
            "validGpsPoints" to validGpsPoints,
            "rejectedGpsPoints" to rejectedGpsPoints,
            "tripDurationMinutes" to tripDurationMinutes,
            "avgSpeedMph" to avgSpeedMph,
            "maxSpeedMph" to maxSpeedMph,
            "locationJumpsDetected" to locationJumpsDetected,
            "accuracyWarnings" to accuracyWarnings,
            "speedAnomalies" to speedAnomalies,
            "tripStartTime" to Date(tripStartTime),
            "tripEndTime" to Date(),
            "wasInterrupted" to (interruptionCount > 0),
            "interruptionCount" to interruptionCount,
            "lastLocationLat" to (lastLocation?.latitude ?: 0.0),
            "lastLocationLng" to (lastLocation?.longitude ?: 0.0),
            "lastLocationTime" to (lastLocation?.time?.let { Date(it) } ?: Date())
        )
    }

    /**
     * ✅ NEW: Broadcast real-time GPS data to UI
     */
    private fun broadcastRealTimeGpsData(location: Location) {
        try {
            // Update current GPS metrics
            currentAccuracy = location.accuracy
            currentSpeedMph = if (location.hasSpeed()) location.speed * 2.237f else 0f
            
            // Calculate trip duration
            val tripDurationMinutes = if (tripStartTime > 0) {
                ((System.currentTimeMillis() - tripStartTime) / 60000).toInt()
            } else 0
            
            // Calculate GPS quality percentage
            val gpsQualityPercentage = if (totalGpsPoints > 0) {
                (validGpsPoints.toFloat() / totalGpsPoints) * 100
            } else 0f
            
            // Calculate average accuracy
            val avgAccuracy = if (totalGpsPoints > 0) {
                (totalGpsAccuracy / totalGpsPoints).toFloat()
            } else 0f
            
            // Calculate average speed
            val avgSpeedMph = if (speedReadings > 0) {
                (totalSpeedMph / speedReadings).toFloat()
            } else 0f
            
            // Get GPS signal status
            val gpsSignalStatus = getGpsSignalStatus()
            
            // Format current location
            currentLocationString = String.format(Locale.US, "%.4f, %.4f", location.latitude, location.longitude)
            
            // Create enhanced GPS tracking data
            val gpsData = GpsTrackingData(
                currentAccuracy = currentAccuracy,
                currentSpeedMph = currentSpeedMph,
                totalGpsPoints = totalGpsPoints,
                validGpsPoints = validGpsPoints,
                rejectedGpsPoints = rejectedGpsPoints,
                gpsQualityPercentage = gpsQualityPercentage,
                locationJumpsDetected = locationJumpsDetected,
                accuracyWarnings = accuracyWarnings,
                speedAnomalies = speedAnomalies,
                tripDurationMinutes = tripDurationMinutes,
                lastLocationUpdate = System.currentTimeMillis(),
                gpsSignalStatus = gpsSignalStatus,
                avgAccuracy = avgAccuracy,
                minAccuracy = minGpsAccuracy.toFloat(),
                maxAccuracy = maxGpsAccuracy.toFloat(),
                avgSpeedMph = avgSpeedMph,
                maxSpeedMph = maxSpeedMph.toFloat(),
                totalDistanceMiles = totalDistance.toFloat(),
                currentLocation = currentLocationString,
                satelliteCount = satelliteCount,
                isHighAccuracyMode = isHighAccuracyMode
            )
            
            // Update the GPS tracking data flow
            _gpsTrackingData.value = gpsData
            
            // Broadcast to UI via Intent (simplified - just send basic data)
            val intent = Intent("GPS_TRACKING_UPDATE")
            intent.putExtra("accuracy", gpsData.currentAccuracy)
            intent.putExtra("speed_mph", gpsData.currentSpeedMph)
            intent.putExtra("total_points", gpsData.totalGpsPoints)
            intent.putExtra("valid_points", gpsData.validGpsPoints)
            intent.putExtra("rejected_points", gpsData.rejectedGpsPoints)
            intent.putExtra("gps_quality", gpsData.gpsQualityPercentage)
            intent.putExtra("signal_status", gpsData.gpsSignalStatus)
            intent.putExtra("trip_duration_minutes", gpsData.tripDurationMinutes)
            intent.putExtra("last_update", gpsData.lastLocationUpdate)
            sendBroadcast(intent)
            
            Log.d(TAG, "GPS data broadcasted: $gpsData")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting GPS data", e)
        }
    }
    
    // ✅ NEW: Get GPS signal status based on current conditions
    private fun getGpsSignalStatus(): String {
        return when {
            !serviceState.isHealthy -> "Poor Signal"
            lastUpdateTime == 0L -> "No Signal"
            System.currentTimeMillis() - lastUpdateTime > 30000 -> "Weak Signal"
            currentAccuracy > 20f -> "Weak Signal"
            currentAccuracy <= 5f -> "Excellent Signal"
            else -> "Good Signal"
        }
    }
    
    /**
     * Handle time zone changes during trip tracking
     * This ensures trip data remains consistent when time zones change
     */
    private fun handleTimeZoneChange() {
        try {
            val newTimeZone = TimeZone.getDefault()
            val timeZoneId = newTimeZone.id
            val offset = newTimeZone.getOffset(System.currentTimeMillis())
            
            Log.i(TAG, "Time zone changed during trip: $timeZoneId (offset: ${offset}ms)")
            
            // Update trip start time to reflect new time zone
            if (tripStartTime > 0) {
                val oldTripStartTime = tripStartTime
                tripStartTime = System.currentTimeMillis()
                Log.d(TAG, "Updated trip start time due to time zone change: ${Date(oldTripStartTime)} -> ${Date(tripStartTime)}")
            }
            
            // Update last location time if we have a location
            if (lastLocation != null) {
                lastUpdateTime = System.currentTimeMillis()
                Log.d(TAG, "Updated last location time due to time zone change")
            }
            
            // Log the time zone change for trip auditing
            (application as? OutOfRouteApplication)?.logAnalyticsEvent(
                "timezone_changed_during_trip",
                mapOf(
                    "new_timezone" to timeZoneId,
                    "timezone_offset" to offset.toString(),
                    "trip_duration_minutes" to (if (tripStartTime > 0) {
                        ((System.currentTimeMillis() - tripStartTime) / 60000).toString()
                    } else "0")
                )
            )
            
            Log.d(TAG, "Time zone change handled successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling time zone change", e)
            // Don't crash the service - just log the error
        }
    }
} 

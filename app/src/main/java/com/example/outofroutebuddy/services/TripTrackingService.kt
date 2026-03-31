package com.example.outofroutebuddy.services

import android.annotation.SuppressLint
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.net.Uri
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.repository.TripRepository
import com.example.outofroutebuddy.models.Trip
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.core.config.BuildConfig
import com.example.outofroutebuddy.core.config.DriveDetectConfig
import com.example.outofroutebuddy.core.config.ValidationConfig
import com.example.outofroutebuddy.util.TimeoutManager
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityRecognition
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
import kotlin.math.abs

// ✅ IMPLEMENTED: Handle service crashes and notify the user if trip tracking fails.
// Firebase Crashlytics integration added for comprehensive crash reporting.

// Use BuildConfig for single source of truth (120_MINUTE_IMPROVEMENT_LOOP alignment)
const val ACTION_START_TRIP = BuildConfig.ACTION_START_TRIP
const val ACTION_END_TRIP = BuildConfig.ACTION_END_TRIP
const val ACTION_PAUSE_TRIP = "ACTION_PAUSE_TRIP"
const val ACTION_RESUME_TRIP = "ACTION_RESUME_TRIP"
const val ACTION_TRIP_ENDING_DETECTED = "ACTION_TRIP_ENDING_DETECTED"
const val ACTION_TRIP_CONTINUE_CONFIRMED = "ACTION_TRIP_CONTINUE_CONFIRMED"
const val ACTION_RECOVER_ACTIVE_TRIP = "com.example.outofroutebuddy.services.ACTION_RECOVER_ACTIVE_TRIP"
private const val ACTION_ACTIVITY_TRANSITION_UPDATE = "ACTION_ACTIVITY_TRANSITION_UPDATE"
const val EXTRA_EXPECTED_MILES = "EXTRA_EXPECTED_MILES"
private const val EXTRA_INITIAL_TOTAL_MILES = "EXTRA_INITIAL_TOTAL_MILES"
        private const val EXTRA_START_PAUSED = "EXTRA_START_PAUSED"
    private const val NOTIFICATION_ID = BuildConfig.NOTIFICATION_ID
    private const val COMPLETION_NOTIFICATION_ID = BuildConfig.NOTIFICATION_ID + 1
    private const val ERROR_NOTIFICATION_ID = BuildConfig.NOTIFICATION_ID + 2
    private const val NOTIFICATION_CHANNEL_ID = BuildConfig.NOTIFICATION_CHANNEL_ID
    /** Shown when user swipes app away while "continue tracking after dismiss" is off. */
    private const val PAUSED_ON_TASK_REMOVED_NOTIFICATION_ID = NOTIFICATION_ID + 40
    private const val PREFS_APP_SETTINGS = "app_settings"
    private const val KEY_CONTINUE_TRACKING_AFTER_DISMISS = "continue_tracking_after_app_dismissed"
    private const val TAG = "TripTrackingService"
    private const val PREFS_NOTIFICATION_GUIDANCE = "notification_guidance"
    private const val KEY_NEEDS_NOTIFICATION_GUIDANCE = "trip_notif_guidance_needed"
private const val PREFS_SERVICE_STATE = "trip_service_state"
private const val KEY_WAS_TRACKING = "was_tracking"
private const val KEY_LOADED_MILES = "loaded_miles"
private const val KEY_BOUNCE_MILES = "bounce_miles"
private const val KEY_TOTAL_DISTANCE = "total_distance"
private const val KEY_START_TIME = "start_time"
private const val KEY_IS_PAUSED = "is_paused"

data class TripMetrics(
    val totalMiles: Double = 0.0,
    val oorMiles: Double = 0.0,
    /** Distinct device time-zone offsets seen this trip (phone auto-adjusts when crossing zones). */
    val distinctTimeZoneOffsets: Int = 0,
    val elevationMinMeters: Double? = null,
    val elevationMaxMeters: Double? = null,
    val maxSpeedMph: Double = 0.0,
)

data class ActivityTransitionSignal(
    val inVehicleConfidence: Int = 0,
    val stillConfidence: Int = 0,
    val inVehicleActive: Boolean = false,
    val stillActive: Boolean = false,
    val lastTransitionAtMillis: Long = 0L,
    val source: String = "fallback",
)

data class GeofenceContextSignal(
    val originLat: Double? = null,
    val originLng: Double? = null,
    val distanceFromOriginMeters: Double? = null,
    val isNearOrigin: Boolean = false,
    val dwellInOriginMillis: Long = 0L,
)

data class TripEndingSignalSnapshot(
    val speedMph: Double = 0.0,
    val speedDeltaMphPerSec: Double = 0.0,
    val rollingDistanceMeters: Double = 0.0,
    val headingVarianceDeg: Double = 0.0,
    val gpsAccuracyMeters: Float = 0f,
    val hasDirectSpeed: Boolean = false,
    val lowMotionSampleCount: Int = 0,
    val sustainedStopMillis: Long = 0L,
    val sampleCount: Int = 0,
    val evaluatedAtMillis: Long = 0L,
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
    private enum class TripEndPromptState {
        IN_PROGRESS,
        ENDING_DETECTED,
        USER_CONTINUED_COOLDOWN,
        TRIP_ENDED,
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: TripRepository
    private lateinit var tripCalculationService: TripCalculationService
    private lateinit var locationValidationService: LocationValidationService

    /** Drive-state classifier for auto-excluding walking/stationary miles. */
    private lateinit var driveStateClassifier: DriveStateClassifier
    /** Recent locations for highway-context lookback; trimmed by size and age. */
    private val locationHistory = ArrayDeque<Location>()
    private val driveStateHistoryMaxSize = ValidationConfig.DRIVE_DETECT_HISTORY_MAX_SIZE
    private val driveStateHistoryMaxAgeMs = ValidationConfig.DRIVE_DETECT_HIGHWAY_LOOKBACK_MS

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
    private var tripEndPromptState = TripEndPromptState.IN_PROGRESS
    
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
    private var originDwellStartAtMillis: Long? = null
    private var sustainedStopStartAtMillis: Long? = null

    /** Ludacris card metrics: distinct device TZ offsets + elevation samples (all GPS points). */
    private val ludacrisSeenOffsets = mutableSetOf<Int>()
    private var ludacrisElevMin: Double? = null
    private var ludacrisElevMax: Double? = null

    private fun resetLudacrisTripAggregates() {
        ludacrisSeenOffsets.clear()
        ludacrisElevMin = null
        ludacrisElevMax = null
    }

    private fun recordLudacrisFromLocation(location: Location) {
        ludacrisSeenOffsets.add(TimeZone.getDefault().getOffset(location.time))
        if (location.hasAltitude()) {
            val a = location.altitude
            ludacrisElevMin = ludacrisElevMin?.let { minOf(it, a) } ?: a
            ludacrisElevMax = ludacrisElevMax?.let { maxOf(it, a) } ?: a
        }
    }

    private fun snapshotTripMetrics(oorMiles: Double) {
        _tripMetrics.value = TripMetrics(
            totalMiles = totalDistance,
            oorMiles = oorMiles,
            distinctTimeZoneOffsets = ludacrisSeenOffsets.size,
            elevationMinMeters = ludacrisElevMin,
            elevationMaxMeters = ludacrisElevMax,
            maxSpeedMph = maxSpeedMph,
        )
    }

    private data class SignalSample(
        val timestampMillis: Long,
        val latitude: Double,
        val longitude: Double,
        val speedMph: Double,
        val hasDirectSpeed: Boolean,
        val bearingDeg: Float?,
        val accuracyMeters: Float,
    )

    private val signalSamples = ArrayDeque<SignalSample>()
    private var tripOriginSample: SignalSample? = null
    private val activityRecognitionClient by lazy { ActivityRecognition.getClient(this) }
    private var activityTransitionPendingIntent: PendingIntent? = null

    companion object {
        internal const val PROMPT_STATE_IN_PROGRESS = "IN_PROGRESS"
        internal const val PROMPT_STATE_ENDING_DETECTED = "ENDING_DETECTED"
        internal const val PROMPT_STATE_USER_CONTINUED_COOLDOWN = "USER_CONTINUED_COOLDOWN"
        internal const val PROMPT_STATE_TRIP_ENDED = "TRIP_ENDED"
        private const val ACTIVITY_TRANSITION_REQUEST_CODE = 7401
        private const val RECOVERY_ALARM_REQUEST_CODE = 7402
        private const val ORIGIN_GEOFENCE_RADIUS_METERS = 250.0
        private const val ORIGIN_DWELL_MIN_SPEED_MPH = 3.0
        private const val SIGNAL_WINDOW_MS = 120_000L
        private const val STOP_DWELL_MAX_SPEED_MPH = 2.5
        private const val STOP_DWELL_MAX_ROLLING_DISTANCE_METERS = 70.0
        private const val STOP_DWELL_MAX_ACCURACY_METERS = 50f

        private val _tripMetrics = MutableStateFlow(TripMetrics())
        val tripMetrics: StateFlow<TripMetrics> = _tripMetrics.asStateFlow()
        
        private val _serviceState = MutableStateFlow(ServiceState())
        val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()
        
        // ✅ NEW: Enhanced GPS tracking data flow
        private val _gpsTrackingData = MutableStateFlow(GpsTrackingData())
        val gpsTrackingData: StateFlow<GpsTrackingData> = _gpsTrackingData.asStateFlow()

        /** Current drive state for auto-exclude (walking/stationary); UI can show "Not counting" when WALKING_OR_STATIONARY. */
        private val _driveState = MutableStateFlow(DriveState.DRIVING)
        val driveState: StateFlow<DriveState> = _driveState.asStateFlow()

        private val _activityTransitionSignal = MutableStateFlow(ActivityTransitionSignal())
        val activityTransitionSignal: StateFlow<ActivityTransitionSignal> = _activityTransitionSignal.asStateFlow()

        private val _geofenceContextSignal = MutableStateFlow(GeofenceContextSignal())
        val geofenceContextSignal: StateFlow<GeofenceContextSignal> = _geofenceContextSignal.asStateFlow()

        private val _tripEndingSignalSnapshot = MutableStateFlow(TripEndingSignalSnapshot())
        val tripEndingSignalSnapshot: StateFlow<TripEndingSignalSnapshot> = _tripEndingSignalSnapshot.asStateFlow()

        fun startService(
            context: Context,
            loadedMiles: Double,
            bounceMiles: Double,
            initialTotalMiles: Double? = null,
            startPaused: Boolean = false
        ) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_START_TRIP
                    putExtra("EXTRA_LOADED_MILES", loadedMiles)
                    putExtra("EXTRA_BOUNCE_MILES", bounceMiles)
                    initialTotalMiles?.let { putExtra(EXTRA_INITIAL_TOTAL_MILES, it) }
                    putExtra(EXTRA_START_PAUSED, startPaused)
                }
                ContextCompat.startForegroundService(context, intent)
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

        fun requestRecovery(context: Context, source: String) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_RECOVER_ACTIVE_TRIP
                    putExtra("EXTRA_RECOVERY_SOURCE", source)
                }
                ContextCompat.startForegroundService(context, intent)
                Log.d(TAG, "Trip recovery requested from $source")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request trip recovery from $source", e)
                notifyUserOfServiceFailure(context, "Trip recovery could not restart automatically: ${e.message}")
            }
        }

        fun canShowTripNotifications(context: Context): Boolean {
            val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (!notificationsEnabled) return false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
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

        fun notifyUserContinuedTrip(context: Context) {
            try {
                val intent = Intent(context, TripTrackingService::class.java).apply {
                    action = ACTION_TRIP_CONTINUE_CONFIRMED
                }
                context.startService(intent)
                Log.d(TAG, "Trip continue acknowledged for notification state")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to acknowledge trip continue", e)
            }
        }

        internal fun resolveNotificationText(
            promptState: String,
            inProgressText: String,
            endingText: String,
        ): String {
            return if (promptState == PROMPT_STATE_ENDING_DETECTED) {
                endingText
            } else {
                inProgressText
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
                    .setSmallIcon(R.drawable.ic_notification_truck)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                
                notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
                
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

    private fun updateTripEndingSignals(location: Location) {
        val now = System.currentTimeMillis()
        val previous = if (signalSamples.isNotEmpty()) signalSamples.last() else null
        val deltaSeconds = if (previous != null) {
            ((now - previous.timestampMillis).coerceAtLeast(1L)).toDouble() / 1000.0
        } else {
            1.0
        }
        val distanceFromPreviousMeters = if (previous != null) {
            haversineMeters(
                previous.latitude,
                previous.longitude,
                location.latitude,
                location.longitude,
            )
        } else {
            0.0
        }
        val derivedSpeedMph = if (previous != null && deltaSeconds > 0.0) {
            (distanceFromPreviousMeters / deltaSeconds) * 2.23694
        } else {
            0.0
        }
        val hasDirectSpeed = location.hasSpeed()
        val speedMph = if (hasDirectSpeed) location.speed * 2.237 else derivedSpeedMph
        val sample = SignalSample(
            timestampMillis = now,
            latitude = location.latitude,
            longitude = location.longitude,
            speedMph = speedMph,
            hasDirectSpeed = hasDirectSpeed,
            bearingDeg = if (location.hasBearing()) location.bearing else null,
            accuracyMeters = location.accuracy,
        )
        signalSamples.addLast(sample)
        while (signalSamples.isNotEmpty() && now - signalSamples.first().timestampMillis > SIGNAL_WINDOW_MS) {
            signalSamples.removeFirst()
        }
        if (tripOriginSample == null && location.accuracy <= 40f) {
            tripOriginSample = sample
        }
        val speedDelta = if (previous != null) {
            (sample.speedMph - previous.speedMph) / deltaSeconds
        } else {
            0.0
        }

        var rollingDistanceMeters = 0.0
        var lastPoint: SignalSample? = null
        for (point in signalSamples) {
            if (lastPoint != null) {
                rollingDistanceMeters += haversineMeters(
                    lastPoint.latitude,
                    lastPoint.longitude,
                    point.latitude,
                    point.longitude,
                )
            }
            lastPoint = point
        }

        val bearings = signalSamples.mapNotNull { it.bearingDeg?.toDouble() }
        val headingVariance = if (bearings.size >= 2) {
            val avg = bearings.average()
            bearings.map { abs(it - avg) }.average()
        } else {
            0.0
        }

        val origin = tripOriginSample
        val distanceFromOrigin = if (origin != null) {
            haversineMeters(origin.latitude, origin.longitude, sample.latitude, sample.longitude)
        } else {
            null
        }
        val nearOrigin = distanceFromOrigin != null && distanceFromOrigin <= ORIGIN_GEOFENCE_RADIUS_METERS
        if (nearOrigin && speedMph <= ORIGIN_DWELL_MIN_SPEED_MPH) {
            if (originDwellStartAtMillis == null) {
                originDwellStartAtMillis = now
            }
        } else {
            originDwellStartAtMillis = null
        }
        val dwellMillis = originDwellStartAtMillis?.let { (now - it).coerceAtLeast(0L) } ?: 0L
        val lowMotionSampleCount =
            signalSamples.count { point ->
                point.speedMph <= STOP_DWELL_MAX_SPEED_MPH &&
                    point.accuracyMeters <= STOP_DWELL_MAX_ACCURACY_METERS
            }
        val looksStopped =
            speedMph <= STOP_DWELL_MAX_SPEED_MPH &&
                rollingDistanceMeters <= STOP_DWELL_MAX_ROLLING_DISTANCE_METERS &&
                location.accuracy <= STOP_DWELL_MAX_ACCURACY_METERS
        if (looksStopped) {
            if (sustainedStopStartAtMillis == null) {
                sustainedStopStartAtMillis = now
            }
        } else {
            sustainedStopStartAtMillis = null
        }
        val sustainedStopMillis = sustainedStopStartAtMillis?.let { (now - it).coerceAtLeast(0L) } ?: 0L

        _geofenceContextSignal.value = GeofenceContextSignal(
            originLat = origin?.latitude,
            originLng = origin?.longitude,
            distanceFromOriginMeters = distanceFromOrigin,
            isNearOrigin = nearOrigin,
            dwellInOriginMillis = dwellMillis,
        )
        _tripEndingSignalSnapshot.value = TripEndingSignalSnapshot(
            speedMph = speedMph,
            speedDeltaMphPerSec = speedDelta,
            rollingDistanceMeters = rollingDistanceMeters,
            headingVarianceDeg = headingVariance,
            gpsAccuracyMeters = location.accuracy,
            hasDirectSpeed = hasDirectSpeed,
            lowMotionSampleCount = lowMotionSampleCount,
            sustainedStopMillis = sustainedStopMillis,
            sampleCount = signalSamples.size,
            evaluatedAtMillis = now,
        )

        if (
            tripEndPromptState == TripEndPromptState.ENDING_DETECTED &&
            (speedMph > 8.0 || _activityTransitionSignal.value.inVehicleConfidence >= 85)
        ) {
            tripEndPromptState = TripEndPromptState.IN_PROGRESS
            updateNotification(currentTripNotificationText())
        }
    }

    private fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadius = ValidationConfig.EARTH_RADIUS_METERS
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
    
    // ✅ REFACTORED: This is the new, cleaner location update logic with vehicle-specific validation.
    private fun updateLocation(location: Location) {
        try {
            recordLudacrisFromLocation(location)
            // ✅ Drive-state: maintain recent location history for highway-context lookback.
            lastLocation?.let { prev ->
                locationHistory.addLast(prev)
                while (locationHistory.size > driveStateHistoryMaxSize) locationHistory.removeFirst()
                val nowMs = System.currentTimeMillis()
                while (locationHistory.isNotEmpty() && locationHistory.first().time < nowMs - driveStateHistoryMaxAgeMs) {
                    locationHistory.removeFirst()
                }
            }
            updateTripEndingSignals(location)

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
                // ✅ Drive-state: classify so we only count miles when driving (auto-exclude walking/stationary).
                val currentDriveState = driveStateClassifier.classify(
                    location,
                    lastLocation,
                    if (locationHistory.isEmpty()) null else locationHistory.toList()
                )
                val previousState = _driveState.value
                if (currentDriveState != previousState) {
                    Log.d(TAG, "Drive state transition: ${previousState.name} -> ${currentDriveState.name} at ${System.currentTimeMillis()}")
                }
                _driveState.value = currentDriveState

                // The distance is valid; add it to the total IF NOT PAUSED AND driving (not walking/stationary).
                val distanceInMiles = validatedDistanceInMeters / 1609.34
                val shouldAccumulate = !isPaused && currentDriveState == DriveState.DRIVING

                if (shouldAccumulate) {
                    totalDistance += distanceInMiles
                    validGpsPoints++
                } else {
                    if (isPaused) {
                        Log.d(TAG, "Location update received while paused - distance not accumulated")
                    } else {
                        Log.d(TAG, "Drive state ${currentDriveState.name} - distance not accumulated (auto-exclude)")
                    }
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
                persistActiveTrackingSnapshot()
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

            snapshotTripMetrics(oorMiles)
            updateNotification(currentTripNotificationText())
            persistActiveTrackingSnapshot()
            
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
    
    private fun handleLocationValidationFailure(_location: Location) {
        consecutiveErrors++
        val errorMessage = "Invalid location received from provider"
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
            // Keep pull-down status clean during active trips; recover in background and log only.
            
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
            val driveDetectConfig = DriveDetectConfig.from(
                getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            )
            driveStateClassifier = DriveStateClassifier(config = driveDetectConfig)
            
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
                    // S3: Do not start location tracking without permission
                    if (!hasLocationPermission()) {
                        Log.w(TAG, "Location permission not granted; refusing to start tracking")
                        stopSelf()
                        return START_NOT_STICKY
                    }
                    ensureForegroundStarted()
                    loadedMiles = intent.getDoubleExtra("EXTRA_LOADED_MILES", 0.0)
                    bounceMiles = intent.getDoubleExtra("EXTRA_BOUNCE_MILES", 0.0)
                    // If resuming a recovered trip, seed totalDistance so miles continue correctly
                    totalDistance = intent.getDoubleExtra(EXTRA_INITIAL_TOTAL_MILES, totalDistance)
                    val startPaused = intent.getBooleanExtra(EXTRA_START_PAUSED, false)
                    driveStateClassifier.reset()
                    locationHistory.clear()
                    _driveState.value = DriveState.DRIVING
                    startTrip()
                    // Restore pause state after startTrip() (startTrip() resets isPaused so reapply here)
                    if (startPaused) {
                        isPaused = true
                        updateNotification(currentTripNotificationText())
                    }
                    // Emit recovered totalDistance so UI shows miles immediately without waiting for first location
                    try {
                        snapshotTripMetrics(_tripMetrics.value.oorMiles)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to emit recovered trip metrics", e)
                    }
                }
                ACTION_RECOVER_ACTIVE_TRIP -> {
                    ensureForegroundStarted()
                    handleServiceRestart()
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
                ACTION_TRIP_ENDING_DETECTED -> {
                    tripEndPromptState = TripEndPromptState.ENDING_DETECTED
                    updateNotification(currentTripNotificationText())
                }
                ACTION_TRIP_CONTINUE_CONFIRMED -> {
                    tripEndPromptState = TripEndPromptState.USER_CONTINUED_COOLDOWN
                    updateNotification(currentTripNotificationText())
                }
                ACTION_ACTIVITY_TRANSITION_UPDATE -> {
                    handleActivityTransitionIntent(intent)
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
     * Ensures notification and status-bar icon are reloaded so they appear after app shutdown or crash.
     */
    private fun handleServiceRestart() {
        try {
            Log.d(TAG, "Attempting to restore service state after restart")
            
            // Try to restore state from SharedPreferences
            val prefs = getSharedPreferences(PREFS_SERVICE_STATE, Context.MODE_PRIVATE)
            val wasTracking = prefs.getBoolean(KEY_WAS_TRACKING, false)
            
            if (wasTracking) {
                // S3: Do not resume location tracking without permission
                if (!hasLocationPermission()) {
                    Log.w(TAG, "Location permission not granted; clearing restored trip state")
                    clearServiceState()
                    stopSelf()
                    return
                }
                // Restore trip parameters
                loadedMiles = prefs.getFloat(KEY_LOADED_MILES, 0f).toDouble()
                bounceMiles = prefs.getFloat(KEY_BOUNCE_MILES, 0f).toDouble()
                totalDistance = prefs.getFloat(KEY_TOTAL_DISTANCE, 0f).toDouble()
                isPaused = prefs.getBoolean(KEY_IS_PAUSED, false)
                tripStartTime = prefs.getLong(KEY_START_TIME, 0L)
                
                Log.i(TAG, "Restored trip service state after restart")
                
                // Reload notification and tiny icon: ensure channel exists, clear stale notifications, then show foreground.
                // (Tiny icon = status-bar small icon; pull-down = same notification in shade. See docs/NOTIFICATION_FEATURES.md.)
                tripEndPromptState = TripEndPromptState.IN_PROGRESS
                createNotificationChannel()
                if (!canPostNotifications()) {
                    markNotificationGuidanceNeeded()
                    Log.w(TAG, "Trip notifications blocked after restart")
                } else {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(COMPLETION_NOTIFICATION_ID)
                    notificationManager.cancel(ERROR_NOTIFICATION_ID)
                    TripEndedOverlayService.cancelStaleNotifications(this)
                }
                val notification = createNotification(currentTripNotificationText())
                startForeground(NOTIFICATION_ID, notification)
                
                // Restore service state so UI and metrics are consistent
                snapshotTripMetrics(totalDistance - loadedMiles - bounceMiles)
                serviceState = serviceState.copy(isRunning = true, isHealthy = true, lastError = null)
                _serviceState.value = serviceState
                
                // Resume tracking
                startActivityTransitionUpdates()
                startLocationUpdates()
                
                // Restart overlay monitoring so trip-ended detection works again after crash
                TripEndedOverlayService.startWhenTripActive(this)
                
                Log.d(TAG, "Notification and tracking restored after restart")
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
            val success =
                getSharedPreferences(PREFS_SERVICE_STATE, Context.MODE_PRIVATE).edit().run {
                    putBoolean(KEY_WAS_TRACKING, true)
                    putFloat(KEY_LOADED_MILES, loadedMiles.toFloat())
                    putFloat(KEY_BOUNCE_MILES, bounceMiles.toFloat())
                    putFloat(KEY_TOTAL_DISTANCE, totalDistance.toFloat())
                    putLong(KEY_START_TIME, if (tripStartTime > 0L) tripStartTime else System.currentTimeMillis())
                    putBoolean(KEY_IS_PAUSED, isPaused)
                    commit()
                }
            if (success) {
                Log.d(TAG, "Service state saved for recovery")
            } else {
                Log.e(TAG, "Failed to save service state for recovery")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving service state", e)
        }
    }
    
    /**
     * ✅ NEW (#14): Clear service state when trip ends normally
     */
    private fun clearServiceState() {
        try {
            val success =
                getSharedPreferences(PREFS_SERVICE_STATE, Context.MODE_PRIVATE).edit().run {
                    clear()
                    commit()
                }
            if (success) {
                Log.d(TAG, "Service state cleared")
            } else {
                Log.e(TAG, "Failed to clear service state")
            }
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

    /** S3: Check location permission before starting/resuming GPS tracking. */
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine && coarse
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createActivityTransitionPendingIntent(): PendingIntent {
        val transitionIntent = Intent(this, TripTrackingService::class.java).apply {
            action = ACTION_ACTIVITY_TRANSITION_UPDATE
        }
        return PendingIntent.getService(
            this,
            ACTIVITY_TRANSITION_REQUEST_CODE,
            transitionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun startActivityTransitionUpdates() {
        if (!hasActivityRecognitionPermission()) {
            Log.i(TAG, "Activity recognition permission not granted; detector will use fallback signals")
            _activityTransitionSignal.value = ActivityTransitionSignal(source = "permission_denied")
            return
        }
        val request = ActivityTransitionRequest(
            listOf(
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.STILL)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.STILL)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
            ),
        )
        val pendingIntent = createActivityTransitionPendingIntent()
        activityTransitionPendingIntent = pendingIntent
        try {
            activityRecognitionClient
                .requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Activity recognition transition updates started")
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Activity recognition transition registration failed", error)
                }
        } catch (se: SecurityException) {
            Log.w(TAG, "No permission for activity transitions", se)
            _activityTransitionSignal.value = ActivityTransitionSignal(source = "security_exception")
        }
    }

    private fun stopActivityTransitionUpdates() {
        val pendingIntent = activityTransitionPendingIntent ?: return
        try {
            activityRecognitionClient
                .removeActivityTransitionUpdates(pendingIntent)
                .addOnFailureListener { error ->
                    Log.w(TAG, "Failed to remove activity transition updates", error)
                }
        } catch (se: SecurityException) {
            Log.w(TAG, "No permission for activity transitions (remove)", se)
        }
        activityTransitionPendingIntent = null
    }

    private fun handleActivityTransitionIntent(intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) return
        val result = ActivityTransitionResult.extractResult(intent) ?: return
        for (event in result.transitionEvents) {
            val previous = _activityTransitionSignal.value
            val updated = when (event.activityType) {
                DetectedActivity.IN_VEHICLE -> {
                    val active = event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                    previous.copy(
                        inVehicleActive = active,
                        inVehicleConfidence = if (active) 90 else 20,
                        stillConfidence = if (active) 15 else previous.stillConfidence,
                        lastTransitionAtMillis = event.elapsedRealTimeNanos / 1_000_000L,
                        source = "transition_api",
                    )
                }
                DetectedActivity.STILL -> {
                    val active = event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                    previous.copy(
                        stillActive = active,
                        stillConfidence = if (active) 90 else 20,
                        inVehicleConfidence = if (active) 10 else previous.inVehicleConfidence,
                        lastTransitionAtMillis = event.elapsedRealTimeNanos / 1_000_000L,
                        source = "transition_api",
                    )
                }
                else -> previous
            }
            _activityTransitionSignal.value = updated
        }
    }

    private fun startTrip() {
        try {
            // ✅ FIX: Preserve seeded totalDistance for trip recovery
            // If totalDistance was seeded via EXTRA_INITIAL_TOTAL_MILES (trip recovery),
            // don't reset it to 0.0 - keep the existing value so UI doesn't jump backwards
            val preserveDistance = totalDistance > 0.0
            val seededDistance = if (preserveDistance) totalDistance else 0.0
            
            Log.d(TAG, "Starting trip tracking")
            
            totalDistance = seededDistance
            lastLocation = null
            lastUpdateTime = System.currentTimeMillis()
            consecutiveErrors = 0
            isPaused = false
            signalSamples.clear()
            tripOriginSample = null
            originDwellStartAtMillis = null
            sustainedStopStartAtMillis = null
            _tripEndingSignalSnapshot.value = TripEndingSignalSnapshot()
            _geofenceContextSignal.value = GeofenceContextSignal()
            _activityTransitionSignal.value = ActivityTransitionSignal(source = "fallback")
            
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
            tripEndPromptState = TripEndPromptState.IN_PROGRESS
            resetLudacrisTripAggregates()

            // ✅ FIX: Initialize metrics with seeded distance for trip recovery
            snapshotTripMetrics((totalDistance - loadedMiles - bounceMiles).coerceAtLeast(0.0))
            serviceState = serviceState.copy(
                isRunning = true,
                isHealthy = true,
                lastError = null,
                startTime = System.currentTimeMillis()
            )
            _serviceState.value = serviceState
            
            if (!canPostNotifications()) {
                markNotificationGuidanceNeeded()
                Log.w(TAG, "Trip notifications blocked by system permission/settings")
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Clear stale, one-shot notifications from previous runs.
            notificationManager.cancel(COMPLETION_NOTIFICATION_ID)
            notificationManager.cancel(ERROR_NOTIFICATION_ID)
            // Clear overlay/fallback "trip ended" notifications so they don't linger over the new "trip in progress".
            TripEndedOverlayService.cancelStaleNotifications(this)

            startForeground(NOTIFICATION_ID, createNotification(currentTripNotificationText()))
            startActivityTransitionUpdates()
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
            Log.d(TAG, "Stopping trip tracking")

            // ✅ Drive-state: reset classifier and history for next trip.
            driveStateClassifier.reset()
            locationHistory.clear()
            _driveState.value = DriveState.DRIVING
            
            // ✅ NEW (#14): Clear persisted state when trip ends normally
            clearServiceState()
            
            // Keep release logs high-level so trip-specific data is not exposed in Logcat.
            val dispatchedMiles = loadedMiles + bounceMiles
            val oorMiles = totalDistance - dispatchedMiles
            val oorPercentage = if (dispatchedMiles > 0) (oorMiles / dispatchedMiles) * 100 else 0.0

            Log.d(TAG, "Final trip calculations completed successfully")
            
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
            
            resetLudacrisTripAggregates()
            _tripMetrics.value = TripMetrics() // Reset metrics
            tripEndPromptState = TripEndPromptState.TRIP_ENDED
            stopActivityTransitionUpdates()
            signalSamples.clear()
            tripOriginSample = null
            originDwellStartAtMillis = null
            sustainedStopStartAtMillis = null
            _tripEndingSignalSnapshot.value = TripEndingSignalSnapshot()
            _geofenceContextSignal.value = GeofenceContextSignal()
            _activityTransitionSignal.value = ActivityTransitionSignal(source = "fallback")
            serviceState = serviceState.copy(isRunning = false, isHealthy = true, lastError = null)
            _serviceState.value = serviceState
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(ERROR_NOTIFICATION_ID)
            
            stopLocationUpdates()
            postTripEndedCompletionNotification()
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
            
            // Keep pull-down text fixed per product requirement.
            updateNotification(currentTripNotificationText())
            persistActiveTrackingSnapshot()
            
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
            
            // Keep pull-down text fixed per product requirement.
            updateNotification(currentTripNotificationText())
            
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

    /**
     * Builds the foreground notification shown in the system pull-down (notification shade).
     * Title and content text come from string resources; tap opens MainActivity, and when
     * the notification shows "Trip ending", tap opens directly to the "End trip?" dialog.
     */
    private fun createNotification(
        text: String,
        promptState: TripEndPromptState = tripEndPromptState,
    ): android.app.Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (promptState == TripEndPromptState.ENDING_DETECTED) {
                putExtra(TripEndedOverlayService.EXTRA_OPEN_TRIP_ENDED_DIALOG, true)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_trip_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_truck)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            if (!canPostNotifications()) {
                markNotificationGuidanceNeeded()
                Log.w(TAG, "Skipping notification update; notifications blocked")
                return
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification(text, tripEndPromptState))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    private fun ensureForegroundStarted() {
        if (!canPostNotifications()) {
            markNotificationGuidanceNeeded()
            Log.w(TAG, "Foreground notification visibility is degraded because notifications are blocked")
        }
        startForeground(NOTIFICATION_ID, createNotification(currentTripNotificationText()))
    }

    private fun persistActiveTrackingSnapshot() {
        if (!serviceState.isRunning) return
        saveServiceState()
    }

    private fun scheduleTaskRemovalRecovery() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val recoveryIntent = Intent(this, TripTrackingRecoveryReceiver::class.java).apply {
            action = ACTION_RECOVER_ACTIVE_TRIP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            RECOVERY_ALARM_REQUEST_CODE,
            recoveryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 2_000L,
            pendingIntent,
        )
    }

    private fun currentTripNotificationText(): String {
        if (isPaused && tripEndPromptState == TripEndPromptState.IN_PROGRESS) {
            return getString(R.string.trip_notification_paused)
        }
        return resolveNotificationText(
            promptState = tripEndPromptState.name,
            inProgressText = getString(R.string.trip_notification_in_progress),
            endingText = getString(R.string.trip_notification_ending),
        )
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = getSystemService(NotificationManager::class.java)
                val serviceChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Trip Tracking Channel",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setShowBadge(false)
                    enableVibration(false)
                    // Foreground tracking status should be silent while still visible.
                    setSound(null as Uri?, null as AudioAttributes?)
                }
                manager.createNotificationChannel(serviceChannel)
                Log.d(TAG, "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channel", e)
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return true
    }

    private fun markNotificationGuidanceNeeded() {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NOTIFICATION_GUIDANCE, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_NEEDS_NOTIFICATION_GUIDANCE, true) }
    }

    private fun postTripEndedCompletionNotification() {
        if (!canPostNotifications()) return
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_trip_title))
            .setContentText(getString(R.string.trip_notification_ended))
            .setSmallIcon(R.drawable.ic_notification_truck)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            Log.w(TAG, "Task removed while trip tracking service is active")
            if (!serviceState.isRunning) {
                super.onTaskRemoved(rootIntent)
                return
            }
            val continueTracking = getSharedPreferences(PREFS_APP_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONTINUE_TRACKING_AFTER_DISMISS, true)
            if (!continueTracking) {
                Log.i(
                    TAG,
                    "continue_tracking_after_app_dismissed=false: stopping GPS; trip state kept for resume",
                )
                interruptionCount++
                persistActiveTrackingSnapshot()
                stopActivityTransitionUpdates()
                stopLocationUpdates()
                try {
                    stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } catch (e: Exception) {
                    Log.w(TAG, "stopForeground on task remove", e)
                }
                if (canPostNotifications()) {
                    val openApp = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    val pi = PendingIntent.getActivity(
                        this,
                        PAUSED_ON_TASK_REMOVED_NOTIFICATION_ID,
                        openApp,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    val paused = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_truck)
                        .setContentTitle(getString(R.string.notification_tracking_paused_app_closed_title))
                        .setContentText(getString(R.string.notification_tracking_paused_app_closed_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(PAUSED_ON_TASK_REMOVED_NOTIFICATION_ID, paused)
                }
                serviceState = serviceState.copy(isRunning = false, lastError = null)
                _serviceState.value = serviceState
                stopSelf()
                return
            }
            interruptionCount++
            persistActiveTrackingSnapshot()
            scheduleTaskRemovalRecovery()
            updateNotification(currentTripNotificationText())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle task removal", e)
        } finally {
            super.onTaskRemoved(rootIntent)
        }
    }
    
    override fun onDestroy() {
        try {
            Log.d(TAG, "TripTrackingService onDestroy")
            persistActiveTrackingSnapshot()
            stopActivityTransitionUpdates()
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

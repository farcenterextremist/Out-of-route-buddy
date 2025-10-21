package com.example.outofroutebuddy.core.config

/**
 * ✅ CENTRALIZED VALIDATION CONFIGURATION
 *
 * This file consolidates all validation-related constants and thresholds
 * that were previously scattered across multiple files. Having them
 * centralized makes the codebase more maintainable and ensures
 * consistent behavior across the application.
 *
 * BENEFITS:
 * - Single source of truth for all validation constants
 * - Easy to update thresholds across the app
 * - Consistent naming conventions
 * - Better IDE support and refactoring
 * - Improved maintainability
 *
 * 📚 DOCUMENTATION:
 * - See README.md in this directory for comprehensive usage guide
 * - All constants are documented with KDoc comments
 * - Usage examples available in the documentation
 *
 * 🔧 MAINTENANCE:
 * - When adding new constants, update both this file and README.md
 * - Group constants logically by functionality
 * - Use descriptive names and clear documentation
 * - Test changes thoroughly before committing
 */
object ValidationConfig {
    // ===== LOCATION VALIDATION =====

    /** Maximum age of location data in milliseconds (30 seconds) */
    const val MAX_LOCATION_AGE = 30000L

    /** Maximum acceptable GPS accuracy in meters */
    const val MAX_ACCURACY = 50f

    /** Minimum acceptable GPS accuracy in meters */
    const val MIN_ACCURACY = 5f

    /** Maximum distance between location updates in meters */
    const val MAX_DISTANCE_BETWEEN_UPDATES = 1000f

    /** Minimum distance threshold for normal mode in meters */
    const val MIN_DISTANCE_THRESHOLD = 25f

    /** Maximum speed change between updates in MPH */
    const val MAX_SPEED_CHANGE = 20f // Now in mph for consistency with validation logic

    /** Maximum time allowed in stationary state (in milliseconds) */
    const val MAX_STATIONARY_TIME = 300000L // 5 minutes

    // ===== VEHICLE-SPECIFIC VALIDATION =====

    /** Maximum vehicle speed in MPH */
    const val VEHICLE_MAX_SPEED_MPH = 85f

    /** Minimum vehicle speed in MPH */
    const val VEHICLE_MIN_SPEED_MPH = 2.5f

    /** Vehicle accuracy threshold in meters */
    const val VEHICLE_MIN_ACCURACY = 20f

    /** Maximum vehicle acceleration in m/s² (aligned with VEHICLE_MAX_ACCELERATION_MPH_PER_SEC = 20 mph/s) */
    const val VEHICLE_MAX_ACCELERATION = 8.94f // 20 mph/s = 8.94 m/s²

    /** Minimum vehicle acceleration in m/s² (for deceleration) */
    const val VEHICLE_MIN_ACCELERATION = -15f

    // ===== TRAFFIC DETECTION =====

    /** Speed threshold for traffic detection in MPH */
    const val TRAFFIC_SPEED_THRESHOLD = 15f

    /** Accuracy threshold for traffic mode in meters */
    const val TRAFFIC_ACCURACY_THRESHOLD = 25f

    /** Minimum speed for traffic mode in MPH */
    const val TRAFFIC_MIN_SPEED_MPH = 0.5f

    /** Maximum speed for heavy traffic in MPH */
    const val HEAVY_TRAFFIC_MAX_SPEED_MPH = 10f

    /** Stop frequency threshold for traffic detection */
    const val TRAFFIC_STOP_FREQUENCY_THRESHOLD = 2

    // ===== MICRO-MOVEMENT TRACKING =====

    /** Threshold for micro-movement detection in meters */
    const val MICRO_MOVEMENT_THRESHOLD = 2f

    /** Minimum number of micro-movements for validation */
    const val MICRO_MOVEMENT_MIN_COUNT = 3

    /** Maximum accumulation limit for micro-movements in meters */
    const val MICRO_MOVEMENT_ACCUMULATION_LIMIT = 50f

    /** Time window for micro-movement validation in milliseconds */
    const val MICRO_MOVEMENT_TIME_WINDOW = 60000L

    /** Kalman smoothing factor for micro-movements */
    const val MICRO_MOVEMENT_KALMAN_SMOOTHING = 0.3f

    // ===== GPS ACCURACY ADAPTATION =====

    /** Base accuracy threshold in meters */
    const val BASE_ACCURACY_THRESHOLD = 15f

    /** Traffic mode accuracy multiplier */
    const val TRAFFIC_ACCURACY_MULTIPLIER = 1.5f

    /** Normal mode accuracy multiplier */
    const val NORMAL_ACCURACY_MULTIPLIER = 1.0f

    /** Critical accuracy threshold in meters */
    const val CRITICAL_ACCURACY_THRESHOLD = 30f

    // ===== TRAFFIC STATE MACHINE =====

    /** High confidence threshold for state transitions */
    const val HIGH_CONFIDENCE_THRESHOLD = 0.7f

    /** Medium confidence threshold for state persistence */
    const val MEDIUM_CONFIDENCE_THRESHOLD = 0.5f

    /** State persistence time in milliseconds */
    const val STATE_PERSISTENCE_TIME = 30000L

    /** Hysteresis threshold for state changes */
    const val STATE_HYSTERESIS_THRESHOLD = 0.2f

    // ===== CONVERSION FACTORS =====

    /** Miles per hour to meters per second conversion */
    const val MPH_TO_MPS = 0.44704f

    /** Meters per second to miles per hour conversion */
    const val MPS_TO_MPH = 2.23694f

    // ===== FEATURE FLAGS =====

    /** Whether micro-movement validation is enabled */
    const val MICRO_MOVEMENT_VALIDATION_ENABLED = true

    /** Whether adaptive GPS accuracy is enabled */
    const val ADAPTIVE_GPS_ACCURACY_ENABLED = true

    /** Whether traffic state machine is enabled */
    const val TRAFFIC_STATE_MACHINE_ENABLED = true

    // ==================== ADDITIONAL VEHICLE CONSTANTS ====================

    /** Maximum vehicle acceleration for validation (in mph/s) */
    const val VEHICLE_MAX_ACCELERATION_MPH_PER_SEC = 20f

    // ==================== ADDITIONAL TRAFFIC CONSTANTS ====================

    /** Traffic detection window size (number of recent locations) */
    const val TRAFFIC_DETECTION_WINDOW_SIZE = 10

    /** Traffic average speed threshold (in mph) */
    const val TRAFFIC_AVERAGE_SPEED_THRESHOLD = 15f

    /** Traffic speed variance threshold */
    const val TRAFFIC_SPEED_VARIANCE_THRESHOLD = 0.3f

    /** Minimum locations needed for traffic analysis */
    const val TRAFFIC_DETECTION_MIN_LOCATIONS = 5

    /** Consistency threshold for micro-movement validation */
    const val MICRO_MOVEMENT_CONSISTENCY_THRESHOLD = 0.5f

    // ==================== ADDITIONAL GPS ACCURACY CONSTANTS ====================

    /** GPS accuracy threshold for traffic conditions (in meters) */
    const val TRAFFIC_GPS_ACCURACY_THRESHOLD = 25f

    /** GPS accuracy threshold for normal conditions (in meters) */
    const val NORMAL_GPS_ACCURACY_THRESHOLD = 15f

    /** Adaptation factor for traffic mode */
    const val GPS_ACCURACY_ADAPTATION_FACTOR = 1.5f

    /** Smoothing factor for accuracy transitions */
    const val GPS_ACCURACY_SMOOTHING_FACTOR = 0.8f

    /** Time for smooth accuracy transitions (in milliseconds) */
    const val GPS_ACCURACY_TRANSITION_TIME = 10000L // 10 seconds

    /** Hysteresis to prevent rapid accuracy switching (in meters) */
    const val GPS_ACCURACY_HYSTERESIS = 2f

    /** Enable/disable adaptive accuracy */
    const val GPS_ACCURACY_ADAPTATION_ENABLED = true

    // ==================== UPDATE FREQUENCY CONSTANTS ====================

    /** Update frequency for traffic conditions (in milliseconds) */
    const val TRAFFIC_UPDATE_FREQUENCY = 2000L // 2 seconds

    /** Update frequency for normal conditions (in milliseconds) */
    const val NORMAL_UPDATE_FREQUENCY = 5000L // 5 seconds

    /** Minimum time between updates (in milliseconds) */
    const val MIN_UPDATE_INTERVAL = 1000L // 1 second

    /** Enable/disable frequency adaptation */
    const val UPDATE_FREQUENCY_ADAPTATION_ENABLED = true

    /** Factor to reduce frequency for poor GPS quality */
    const val GPS_QUALITY_UPDATE_FACTOR = 0.8f

    /** Enable speed-based frequency adjustment */
    const val SPEED_BASED_FREQUENCY_ENABLED = true

    /** Minimum update frequency (in milliseconds) */
    const val MIN_UPDATE_FREQUENCY = 1000L // 1 second

    /** Maximum update frequency (in milliseconds) */
    const val MAX_UPDATE_FREQUENCY = 10000L // 10 seconds

    // ==================== ADDITIONAL TRAFFIC STATE CONSTANTS ====================

    /** Confidence threshold for traffic state change */
    const val TRAFFIC_STATE_TRANSITION_THRESHOLD = 0.7f

    /** How long to maintain traffic state (in milliseconds) */
    const val TRAFFIC_STATE_PERSISTENCE_TIME = 60000L // 1 minute

    /** Hysteresis to prevent rapid state switching */
    const val TRAFFIC_STATE_HYSTERESIS = 0.2f

    // State transition speed thresholds (mph)
    const val FLOWING_SPEED_THRESHOLD = 25f
    const val SLOW_MOVING_SPEED_THRESHOLD = 15f
    const val HEAVY_TRAFFIC_SPEED_THRESHOLD = 5f
    const val STOPPED_SPEED_THRESHOLD = 2f

    // State transition confidence thresholds
    const val MIN_STATE_CONFIDENCE = 0.6f

    // ==================== ANALYTICS CONSTANTS ====================

    /** Sample rate for analytics (percentage) */
    const val ANALYTICS_SAMPLE_RATE = 0.1f // 10%

    /** Number of locations per analytics batch */
    const val ANALYTICS_BATCH_SIZE = 100

    /** Analytics reporting interval (in milliseconds) */
    const val ANALYTICS_REPORTING_INTERVAL = 300000L // 5 minutes

    /** Enable/disable analytics collection */
    const val ANALYTICS_ENABLED = true

    /** Enable/disable traffic-specific analytics */
    const val TRAFFIC_ANALYTICS_ENABLED = true

    /** Minimum duration for traffic mode session (in milliseconds) */
    const val TRAFFIC_MODE_SESSION_MIN_DURATION = 30000L // 30 seconds

    /** Log interval for accumulated distance (in milliseconds) */
    const val TRAFFIC_DISTANCE_ACCUMULATION_LOG_INTERVAL = 60000L // 1 minute

    // ==================== CONVERSION CONSTANTS ====================

    /** Earth radius in meters */
    const val EARTH_RADIUS_METERS = 6371000.0

    // ==================== ERROR HANDLING CONSTANTS ====================

    /** Maximum consecutive errors before service failure */
    const val MAX_CONSECUTIVE_ERRORS = 10

    /** Error recovery interval (in milliseconds) */
    const val ERROR_RECOVERY_INTERVAL = 60000L // 1 minute

    /** Error recovery delay (in milliseconds) */
    const val ERROR_RECOVERY_DELAY = 5000L // 5 seconds

    /** Monitoring interval (in milliseconds) */
    const val MONITORING_INTERVAL = 10000L // 10 seconds

    // ==================== BATTERY AND MEMORY CONSTANTS ====================

    /** Battery warning threshold (percentage) */
    const val BATTERY_WARNING_THRESHOLD = 20

    /** Memory warning threshold (in MB) */
    const val MEMORY_WARNING_THRESHOLD = 500L

    // ===== GPS DATA FLOW CONSTANTS =====

    /** GPS data batch size for processing */
    const val GPS_BATCH_SIZE = 5

    /** Maximum time to wait for GPS batch completion (in milliseconds) */
    const val GPS_BATCH_TIMEOUT_MS = 1000L

    /** Minimum distance between GPS points in meters */
    const val GPS_MIN_DISTANCE_METERS = 10.0

    /** Maximum realistic speed in MPH - DEPRECATED: Use VEHICLE_MAX_SPEED_MPH instead */
    @Deprecated("Use VEHICLE_MAX_SPEED_MPH for consistency", ReplaceWith("VEHICLE_MAX_SPEED_MPH"))
    const val GPS_MAX_SPEED_MPH = 85.0 // Standardized to match VEHICLE_MAX_SPEED_MPH

    /** Minimum GPS accuracy in meters (aligned with VEHICLE_MIN_ACCURACY) */
    const val GPS_MIN_ACCURACY_METERS = 20.0 // Matches VEHICLE_MIN_ACCURACY

    /** Maximum GPS accuracy to accept in meters (aligned with MAX_ACCURACY) */
    const val GPS_MAX_ACCURACY_METERS = 50.0 // Standardized to match MAX_ACCURACY for consistency

    /** Minimum time between GPS updates in milliseconds */
    const val GPS_MIN_UPDATE_INTERVAL_MS = 500L

    /** Maximum time between GPS updates in milliseconds */
    const val GPS_MAX_UPDATE_INTERVAL_MS = 5000L

    /** Whether adaptive GPS rate limiting is enabled */
    const val GPS_ADAPTIVE_RATE_ENABLED = true

    // ===== BACKGROUND SYNC CONSTANTS =====

    /** Cache cleanup interval in milliseconds (5 minutes) */
    const val SYNC_CACHE_CLEANUP_INTERVAL_MS = 5 * 60 * 1000L

    /** State synchronization interval in milliseconds (30 seconds) */
    const val SYNC_STATE_INTERVAL_MS = 30 * 1000L

    /** GPS synchronization interval in milliseconds (10 seconds) */
    const val SYNC_GPS_INTERVAL_MS = 10 * 1000L

    /** Data integrity check interval in milliseconds (10 minutes) */
    const val SYNC_DATA_INTEGRITY_INTERVAL_MS = 10 * 60 * 1000L

    /** Background sync service actions */
    const val SYNC_ACTION_START = "com.example.outofroutebuddy.START_SYNC"
    const val SYNC_ACTION_STOP = "com.example.outofroutebuddy.STOP_SYNC"
    const val SYNC_ACTION_FORCE = "com.example.outofroutebuddy.FORCE_SYNC"

    // ===== OFFLINE SERVICE CONSTANTS =====

    /** Simple offline service preferences name */
    const val OFFLINE_SIMPLE_PREFS_NAME = "SimpleOfflinePrefs"

    /** Standalone offline service preferences name */
    const val OFFLINE_STANDALONE_PREFS_NAME = "StandaloneOfflinePrefs"

    /** Offline trips storage key */
    const val OFFLINE_KEY_TRIPS = "offline_trips"

    /** Offline analytics storage key */
    const val OFFLINE_KEY_ANALYTICS = "offline_analytics"

    /** Last sync time storage key */
    const val OFFLINE_KEY_LAST_SYNC = "last_sync"

    /** Network status storage key */
    const val OFFLINE_KEY_NETWORK_STATUS = "network_status"

    /** Network check timeout in milliseconds */
    const val OFFLINE_NETWORK_TIMEOUT_MS = 3000L

    // ===== TEST CONSTANTS =====

    /** Test delay intervals in milliseconds */
    const val TEST_SHORT_DELAY = 1000L
    const val TEST_MEDIUM_DELAY = 3000L
    const val TEST_LONG_DELAY = 5000L

    /** Test accuracy values in meters */
    const val TEST_GOOD_ACCURACY = 15f
    const val TEST_POOR_ACCURACY = 30f
    const val TEST_CRITICAL_ACCURACY = 60f

    /** Test speed values in MPH */
    const val TEST_NORMAL_SPEED_MPH = 25f
    const val TEST_HIGH_SPEED_MPH = 50f
    const val TEST_UNREALISTIC_SPEED_MPH = 100f
    const val TEST_TRAFFIC_SPEED_MPH = 3f
} 

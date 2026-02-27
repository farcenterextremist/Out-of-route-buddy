package com.example.outofroutebuddy.core.config

import android.os.Build

/**
 * ✅ CENTRALIZED BUILD CONFIGURATION
 *
 * This file consolidates all build-related constants and configuration values
 * to provide a single source of truth for build settings.
 *
 * BENEFITS:
 * - Single source of truth for all build constants
 * - Easy to update build settings across the app
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
object BuildConfig {
    // ==================== SDK VERSION CONSTANTS ====================

    /** Compile SDK version */
    const val COMPILE_SDK = 34

    /** Minimum supported SDK version */
    const val MIN_SDK = 24

    /** Target SDK version */
    const val TARGET_SDK = 34

    /** Minimum supported API level */
    const val MIN_API_LEVEL = Build.VERSION_CODES.N

    /** Target API level */
    const val TARGET_API_LEVEL = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    /** Build tools version */
    const val BUILD_TOOLS_VERSION = "34.0.0"

    // ==================== APPLICATION CONSTANTS ====================

    /** Application package name */
    const val PACKAGE_NAME = "com.example.outofroutebuddy"

    /** Application version name */
    const val VERSION_NAME = "1.0.2"

    /** Application version code */
    const val VERSION_CODE = 2

    /** Application name */
    const val APP_NAME = "OutOfRouteBuddy"

    // ==================== DATABASE CONSTANTS ====================

    /** Default database name */
    const val DATABASE_NAME = "outofroute_buddy.db"

    /** Database version */
    const val DATABASE_VERSION = 1

    /** Default shared preferences name */
    const val PREFERENCES_NAME = "outofroute_buddy_prefs"

    // ==================== SERVICE CONSTANTS ====================

    /** Notification channel ID for trip tracking */
    const val NOTIFICATION_CHANNEL_ID = "trip_tracking_channel"

    /** Notification ID for trip tracking service */
    const val NOTIFICATION_ID = 1001

    /** Service action to start trip tracking */
    const val ACTION_START_TRIP = "START_TRIP"

    /** Service action to end trip tracking */
    const val ACTION_END_TRIP = "END_TRIP"

    // ==================== UI CONSTANTS ====================

    /** Default animation duration (in milliseconds) */
    const val DEFAULT_ANIMATION_DURATION = 300L

    /** Toast duration for short messages (in milliseconds) */
    const val TOAST_DURATION_SHORT = 2000L

    /** Toast duration for long messages (in milliseconds) */
    const val TOAST_DURATION_LONG = 3500L

    // ==================== NETWORK CONSTANTS ====================

    /** Default network timeout (in milliseconds) */
    const val DEFAULT_NETWORK_TIMEOUT = 30000L

    /** Default retry attempts for network operations */
    const val DEFAULT_RETRY_ATTEMPTS = 3

    // ==================== DEBUG CONSTANTS ====================

    /** Enable debug logging */
    const val DEBUG_MODE = true

    /** Enable performance monitoring */
    const val PERFORMANCE_MONITORING = true

    /** Enable crash reporting */
    const val CRASH_REPORTING_ENABLED = true

    /** Enable analytics collection */
    const val ANALYTICS_ENABLED = true

    // ==================== FEATURE FLAGS ====================

    /** Enable advanced GPS features */
    const val ADVANCED_GPS_ENABLED = true

    /** Enable traffic detection */
    const val TRAFFIC_DETECTION_ENABLED = true

    /** Enable offline mode */
    const val OFFLINE_MODE_ENABLED = true

    /** Enable background sync */
    const val BACKGROUND_SYNC_ENABLED = true

    // ==================== PERFORMANCE CONSTANTS ====================

    /** Maximum memory usage threshold (in MB) */
    const val MAX_MEMORY_USAGE_MB = 512L

    /** CPU usage warning threshold (percentage) */
    const val CPU_USAGE_WARNING_THRESHOLD = 80

    /** Battery usage warning threshold (percentage) */
    const val BATTERY_USAGE_WARNING_THRESHOLD = 20

    // ==================== SECURITY CONSTANTS ====================

    /** Enable SSL pinning */
    const val SSL_PINNING_ENABLED = true

    /** Enable certificate transparency */
    const val CERTIFICATE_TRANSPARENCY_ENABLED = true

    /** Enable root detection */
    const val ROOT_DETECTION_ENABLED = true

    // ==================== TESTING CONSTANTS ====================

    /** Enable test mode */
    const val TEST_MODE_ENABLED = false

    /** Test timeout for UI tests (in milliseconds) */
    const val UI_TEST_TIMEOUT = 10000L

    /** Test timeout for unit tests (in milliseconds) */
    const val UNIT_TEST_TIMEOUT = 5000L

    /** Test timeout for integration tests (in milliseconds) */
    const val INTEGRATION_TEST_TIMEOUT = 30000L

    // ===== BUILD FEATURES =====

    /** Whether BuildConfig generation is enabled */
    const val BUILD_CONFIG_ENABLED = true

    /** Whether view binding is enabled */
    const val VIEW_BINDING_ENABLED = true

    /** Whether data binding is enabled */
    const val DATA_BINDING_ENABLED = false

    // ===== COMPILATION SETTINGS =====

    /** Whether R8 optimization is enabled */
    const val R8_ENABLED = true

    /** Whether minification is enabled */
    const val MINIFY_ENABLED = false

    /** Whether ProGuard rules are applied */
    const val PROGUARD_ENABLED = false

    // ===== TEST CONFIGURATION =====

    /** Whether unit tests are enabled */
    const val UNIT_TESTS_ENABLED = true

    /** Whether instrumentation tests are enabled */
    const val INSTRUMENTATION_TESTS_ENABLED = true

    /** Test runner class */
    const val TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

    // ===== PERFORMANCE SETTINGS =====

    /** Whether build caching is enabled */
    const val BUILD_CACHING_ENABLED = true

    /** Whether parallel execution is enabled */
    const val PARALLEL_EXECUTION_ENABLED = true

    /** Whether configuration on demand is enabled */
    const val CONFIGURATION_ON_DEMAND_ENABLED = true

    // ===== CODE QUALITY =====

    /** Whether Detekt static analysis is enabled */
    const val DETEKT_ENABLED = false

    /** Whether Ktlint formatting is enabled */
    const val KTLINT_ENABLED = false

    /** Whether SonarQube analysis is enabled */
    const val SONARQUBE_ENABLED = false
} 

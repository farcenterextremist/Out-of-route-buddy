package com.example.outofroutebuddy

import android.content.Context
import com.example.outofroutebuddy.core.config.ValidationConfig

/**
 * Test configuration and utilities for automated testing
 *
 * Provides common test setup, mock data, and utility functions
 * for both unit tests and instrumentation tests.
 */
object TestConfig {
    /**
     * Get the test application context
     * Note: This is only available in instrumentation tests
     */
    fun getTestContext(): Context? {
        return try {
            // This will only work in instrumentation tests
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            val instrumentationClass = Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            val getInstrumentationMethod = instrumentationClass.getMethod("getInstrumentation")
            val instrumentation = getInstrumentationMethod.invoke(null)
            val getTargetContextMethod = instrumentation.javaClass.getMethod("getTargetContext")
            getTargetContextMethod.invoke(instrumentation) as Context
        } catch (e: Exception) {
            // In unit tests, return null
            null
        }
    }

    /**
     * Sample test data for accessibility
     */
    object AccessibilityTestData {
        val EXPECTED_CONTENT_DESCRIPTIONS =
            mapOf(
                "loaded_miles_input" to "Input field for loaded miles. Enter the number of miles you are contracted to drive.",
                "bounce_miles_input" to "Input field for bounce miles. Enter the number of miles you drive to return to your starting point.",
                "start_trip_button" to "Button to start or end your trip tracking. Tap to begin GPS tracking or end current trip.",
                "statistics_button" to "Button to expand or collapse trip statistics. Shows weekly, monthly, and yearly trip data.",
                "settings_button" to "Button to access app settings. Configure app preferences and view help information.",
                "total_miles_output" to "Total miles driven during the current trip",
                "oor_miles_output" to "Out of route miles calculated for the current trip",
                "oor_percentage_output" to "Percentage of miles that are out of route for the current trip",
                "progress_bar" to "Progress indicator showing trip tracking status",
            )

        val INTERACTIVE_ELEMENTS =
            listOf(
                "loaded_miles_input",
                "bounce_miles_input",
                "start_trip_button",
                "statistics_button",
                "settings_button",
                "total_miles_output",
                "oor_miles_output",
                "oor_percentage_output",
            )
    }

    /**
     * Test timeouts and delays - now using centralized config
     */
    object TestTimeouts {
        const val SHORT_DELAY = ValidationConfig.TEST_SHORT_DELAY
        const val MEDIUM_DELAY = ValidationConfig.TEST_MEDIUM_DELAY
        const val LONG_DELAY = ValidationConfig.TEST_LONG_DELAY
    }

    /**
     * Test assertions and validations
     */
    object TestAssertions {
        fun isValidMilesValue(value: Double): Boolean {
            return value >= 0.0 && value <= 10000.0 // Reasonable range for miles
        }

        fun isValidPercentageValue(value: Double): Boolean {
            return value >= 0.0 && value <= 100.0
        }

        fun isValidContentDescription(description: String?): Boolean {
            return !description.isNullOrBlank() && description.length > 10
        }
    }
} 

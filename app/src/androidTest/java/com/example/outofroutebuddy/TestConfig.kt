package com.example.outofroutebuddy

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

object TestConfig {
    fun getTestContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    object TestAssertions {
        fun isValidMilesValue(value: Double?): Boolean {
            if (value == null) return false
            return value >= 0.0 && value <= 10_000.0
        }

        fun isValidPercentageValue(value: Double?): Boolean {
            if (value == null) return false
            return value in 0.0..100.0
        }

        fun isValidContentDescription(description: String?): Boolean {
            return !description.isNullOrBlank() && description.length >= 10
        }
    }

    object TestTimeouts {
        const val SHORT_DELAY: Long = 250
        const val MEDIUM_DELAY: Long = 750
        const val LONG_DELAY: Long = 1500
    }

    object AccessibilityTestData {
        // Map of view identifiers to expected content descriptions
        val EXPECTED_CONTENT_DESCRIPTIONS: Map<String, String> = mapOf(
            "submit_button" to "Submit trip",
            "settings_button" to "Open settings",
            "trip_input_loaded_miles" to "Loaded miles input",
            "trip_input_bounce_miles" to "Bounce miles input",
            "trip_input_actual_miles" to "Actual miles input"
        )

        // Set of interactive elements we expect to validate
        val INTERACTIVE_ELEMENTS: Set<String> = setOf(
            "submit_button",
            "settings_button"
        )
    }
}



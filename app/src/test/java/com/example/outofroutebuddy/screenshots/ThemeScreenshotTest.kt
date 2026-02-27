package com.example.outofroutebuddy.screenshots

import org.junit.Ignore
import org.junit.Test

/**
 * Theme Screenshot Tests
 *
 * Visual regression testing for theme variants.
 *
 * DEFERRED: Screenshot tests require Paparazzi plugin setup.
 * See docs/qa/TEST_STRATEGY.md for details.
 *
 * To enable: Add paparazzi plugin to app/build.gradle.kts, then:
 * - Run: ./gradlew recordPaparazziDebug (to capture baseline)
 * - Run: ./gradlew verifyPaparazziDebug (to verify no changes)
 */
class ThemeScreenshotTest {

    @Test
    @Ignore("Screenshot tests deferred until Paparazzi is configured. See docs/qa/TEST_STRATEGY.md")
    fun screenshotTestPlaceholder() {
        // Placeholder until Paparazzi is configured
        assert(true)
    }
}


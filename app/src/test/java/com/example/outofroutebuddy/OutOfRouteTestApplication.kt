package com.example.outofroutebuddy

/**
 * Test Application that overrides [isHealthy] to return true so ViewModel trip flows
 * (calculateTrip, continueRecoveredTrip) run in Robolectric and unit tests.
 * Used with @Config(application = OutOfRouteTestApplication::class).
 */
class OutOfRouteTestApplication : OutOfRouteApplication() {
    override fun isHealthy(): Boolean = true
}

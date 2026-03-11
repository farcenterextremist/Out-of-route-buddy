package com.example.outofroutebuddy

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Unit tests for [OutOfRouteApplication]: health check and recovery state (Phase 1 coverage).
 * Uses HiltTestApplication so HiltAndroidRule runs; companion clearRecoveredState tested directly.
 * isHealthy/getDatabaseError require real OutOfRouteApplication instance (not provided by HiltTestApplication);
 * covered in instrumented ApplicationInitializationTest or manual run.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(sdk = [34], application = HiltTestApplication::class)
class OutOfRouteApplicationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun clearRecoveredState_clearsStaticState() {
        OutOfRouteApplication.clearRecoveredState()
        assertThat(OutOfRouteApplication.recoveredTripState).isNull()
    }

    @Test
    @Ignore("Requires OutOfRouteApplication; HiltTestApplication is used for Hilt rule. Covered in instrumented ApplicationInitializationTest.")
    fun application_isHealthy_afterInit_returnsTrue() {
        val app = ApplicationProvider.getApplicationContext<OutOfRouteApplication>()
        val healthy = app.isHealthy()
        assertThat(healthy).isTrue()
    }

    @Test
    @Ignore("Requires OutOfRouteApplication; HiltTestApplication is used for Hilt rule. Covered in instrumented ApplicationInitializationTest.")
    fun application_getDatabaseError_beforeAnyFailure_returnsNull() {
        val app = ApplicationProvider.getApplicationContext<OutOfRouteApplication>()
        val error = app.getDatabaseError()
        assertThat(error).isNull()
    }
}

package com.example.outofroutebuddy.ui

import android.app.Activity
import android.app.Application
import android.Manifest
import android.os.Looper
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.PreferencesManager
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

/**
 * MainActivity shows permission + period onboarding dialogs when prefs/permissions are incomplete;
 * Trip start may show background-tracking reliability warnings. Helpers stabilize Robolectric.
 */
object TripInputRobolectricHelpers {

    fun prepareApplicationForMainActivityTripFlow(preferencesManager: PreferencesManager) {
        preferencesManager.setHasSeenPeriodOnboarding(true)
        val app = RuntimeEnvironment.getApplication() as Application
        Shadows.shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    fun dismissBackgroundTrackingWarningIfShowing(activity: Activity) {
        val continueExact = activity.getString(R.string.background_tracking_warning_continue).lowercase()
        var dismissed = false
        repeat(25) {
            if (dismissed) return@repeat
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val dialog = ShadowAlertDialog.getLatestAlertDialog() ?: return@repeat
            val pos = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE) ?: return@repeat
            val label = pos.text?.toString()?.lowercase().orEmpty()
            if (label == continueExact || label.contains("anyway")) {
                pos.performClick()
                dismissed = true
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }
        repeat(15) { Shadows.shadowOf(Looper.getMainLooper()).idle() }
    }
}

package com.example.outofroutebuddy.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityDeviceSmokeTest {

    @Test
    fun launches_andDismissesStartupPopup() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(500)
        try {
            onView(withText("Continue trip")).inRoot(isDialog()).perform(click())
        } catch (_: Throwable) {
            try {
                onView(withText("Start new trip")).inRoot(isDialog()).perform(click())
            } catch (_: Throwable) {
                // no dialog shown; fine for smoke
            }
        }
        Thread.sleep(300)
    }
}



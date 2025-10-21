package com.example.outofroutebuddy

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testMainActivityLaunches() {
        // Simple test to verify the activity launches
        // This should always pass if the app can be launched
        assert(true)
    }
} 

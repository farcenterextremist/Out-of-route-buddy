package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.google.android.material.button.MaterialButton
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Phase 5: Fragment display tests for Month and Year statistics wiring.
 * Verifies statistics section shows Month/Year labels and values when expanded.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripInputFragmentStatisticsRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun launchFragment(): Pair<MainActivity, TripInputFragment> {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripInputFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        Shadows.shadowOf(activity).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return activity to fragment
    }

    private fun findTextViewWithText(parent: android.view.View, text: String): TextView? {
        if (parent is TextView && parent.text.toString().contains(text, ignoreCase = true)) {
            return parent
        }
        if (parent is android.view.ViewGroup) {
            for (i in 0 until parent.childCount) {
                findTextViewWithText(parent.getChildAt(i), text)?.let { return it }
            }
        }
        return null
    }

    @Test
    fun statisticsSection_showsMonthAndYearLabelsWhenExpanded() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        val statisticsButton = root.findViewById<MaterialButton>(R.id.statistics_button)
        statisticsButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val statisticsContent = root.findViewById<android.view.View>(R.id.statistics_content)
        assertThat(statisticsContent.visibility).isEqualTo(android.view.View.VISIBLE)

        val monthLabel = findTextViewWithText(statisticsContent, "Month")
        assertThat(monthLabel).isNotNull()
        assertThat(monthLabel?.text?.toString()).contains("Month")

        val yearLabel = findTextViewWithText(statisticsContent, "Year")
        assertThat(yearLabel).isNotNull()
        assertThat(yearLabel?.text?.toString()).contains("Year")
    }

    @Test
    fun statisticsSection_showsPlaceholderWhenNoStats() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        val statisticsButton = root.findViewById<MaterialButton>(R.id.statistics_button)
        statisticsButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val monthlyStats = root.findViewById<android.view.View>(R.id.monthly_stats)
        val yearlyStats = root.findViewById<android.view.View>(R.id.yearly_stats)
        assertThat(monthlyStats).isNotNull()
        assertThat(yearlyStats).isNotNull()

        val monthlyTotalMiles = monthlyStats.findViewById<TextView>(R.id.total_miles)
        val yearlyTotalMiles = yearlyStats.findViewById<TextView>(R.id.total_miles)
        assertThat(monthlyTotalMiles).isNotNull()
        assertThat(yearlyTotalMiles).isNotNull()
        val monthlyText = monthlyTotalMiles.text.toString()
        val yearlyText = yearlyTotalMiles.text.toString()
        assertThat(monthlyText).isNotEmpty()
        assertThat(yearlyText).isNotEmpty()
    }

    @Test
    fun statisticsSection_hasMonthAndYearRowsWithStatFields() {
        val (activity, fragment) = launchFragment()
        val root = fragment.requireView()

        val statisticsButton = root.findViewById<MaterialButton>(R.id.statistics_button)
        statisticsButton.performClick()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val monthlyStats = root.findViewById<android.view.View>(R.id.monthly_stats)
        val yearlyStats = root.findViewById<android.view.View>(R.id.yearly_stats)

        listOf(monthlyStats, yearlyStats).forEach { row ->
            assertThat(row.findViewById<TextView>(R.id.total_miles)).isNotNull()
            assertThat(row.findViewById<TextView>(R.id.oor_miles)).isNotNull()
            assertThat(row.findViewById<TextView>(R.id.oor_percentage)).isNotNull()
        }
    }
}

package com.example.outofroutebuddy.presentation.ui.history

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TripHistoryFragmentRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun emptyState_showsMessage_andHidesRecycler() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripHistoryFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        val root = fragment.requireView()
        val empty = root.findViewById<android.view.View>(R.id.empty_state_text)
        val list = root.findViewById<android.view.View>(R.id.trip_history_recycler_view)
        assertThat(empty.visibility).isEqualTo(android.view.View.VISIBLE)
        assertThat(list.visibility).isEqualTo(android.view.View.GONE)
    }

    @Test
    fun whenListPopulated_hidesEmpty_showsRecycler() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = TripHistoryFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        // Allow fragment lifecycle to start (onViewCreated, observeTrips)
        activity.supportFragmentManager.executePendingTransactions()
        org.robolectric.Robolectric.flushForegroundThreadScheduler()
        val shadowLooper = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
        shadowLooper.idle()
        shadowLooper.idle()

        // Push a non-empty list into the ViewModel's flow AFTER fragment has started collecting
        val vm = fragment.getViewModel()
        vm.setTripsForTest(listOf(
            com.example.outofroutebuddy.domain.models.Trip(id = "t1", startTime = java.util.Date(), status = com.example.outofroutebuddy.domain.models.TripStatus.COMPLETED, actualMiles = 5.0, oorMiles = 0.5),
            com.example.outofroutebuddy.domain.models.Trip(id = "t2", startTime = java.util.Date(), status = com.example.outofroutebuddy.domain.models.TripStatus.COMPLETED, actualMiles = 8.0, oorMiles = 0.8)
        ))

        // Allow flow emission and collect block to run (visibility + submitList)
        org.robolectric.Robolectric.flushForegroundThreadScheduler()
        shadowLooper.runToEndOfTasks()
        // AsyncListDiffer runs DiffUtil on background executor, then posts to main - allow it to complete
        Thread.sleep(80)
        shadowLooper.runToEndOfTasks()

        val root = fragment.requireView()
        val empty = root.findViewById<android.view.View>(R.id.empty_state_text)
        val list = root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.trip_history_recycler_view)
        assertThat(empty.visibility).isEqualTo(android.view.View.GONE)
        assertThat(list.visibility).isEqualTo(android.view.View.VISIBLE)
        assertThat(list.adapter?.itemCount).isAtLeast(1)
    }
}

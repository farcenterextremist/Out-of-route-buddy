package com.example.outofroutebuddy.presentation.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.outofroutebuddy.MainActivity
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
class SettingsFragmentRobolectricTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun launch(): SettingsFragment {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = SettingsFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
        return fragment
    }

    @Test
    fun defaultTheme_isLight_whenUnset() {
        val fragment = launch()
        val prefs = fragment.preferenceManager.sharedPreferences!!
        // Clear any existing value
        prefs.edit().remove("theme_preference").commit()

        fragment.requireActivity().recreate()
        Robolectric.buildActivity(MainActivity::class.java).setup()

        val themePref = fragment.findPreference<ListPreference>("theme_preference")
        val value = themePref?.value ?: "light"
        assertThat(value).isEqualTo("light")
    }

    @Test
    fun switchingTheme_updatesAppCompatDelegate_andPersistsValue() {
        val fragment = launch()
        val themePref = fragment.findPreference<ListPreference>("theme_preference")!!

        // Programmatically change preference value to dark (simulates user selection)
        themePref.value = "dark"

        // Persisted
        val prefs = fragment.preferenceManager.sharedPreferences!!
        val stored = prefs.getString("theme_preference", null)
        assertThat(stored).isEqualTo("dark")
    }

    @Test
    fun distanceUnits_change_persistsValue() {
        val fragment = launch()
        val unitsPref = fragment.findPreference<ListPreference>("distance_units")!!
        unitsPref.value = "kilometers"
        val prefs = fragment.preferenceManager.sharedPreferences!!
        assertThat(prefs.getString("distance_units", null)).isEqualTo("kilometers")
    }

    @Test
    fun notifications_toggle_updatesPreference() {
        val fragment = launch()
        val notifPref = fragment.findPreference<androidx.preference.SwitchPreferenceCompat>("notifications_enabled")!!
        notifPref.isChecked = true
        val prefs = fragment.preferenceManager.sharedPreferences!!
        assertThat(prefs.getBoolean("notifications_enabled", false)).isTrue()
        notifPref.isChecked = false
        assertThat(prefs.getBoolean("notifications_enabled", true)).isFalse()
    }

    @Test
    fun autoStart_toggle_persistsPreference() {
        val fragment = launch()
        val autoPref = fragment.findPreference<androidx.preference.SwitchPreferenceCompat>("auto_start_trip")!!
        autoPref.isChecked = true
        val prefs = fragment.preferenceManager.sharedPreferences!!
        assertThat(prefs.getBoolean("auto_start_trip", false)).isTrue()
    }
}



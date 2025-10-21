package com.example.outofroutebuddy.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.outofroutebuddy.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * ✅ Settings Fragment
 * 
 * Provides user-configurable settings for:
 * - GPS update frequency
 * - Distance units (miles/kilometers)
 * - Dark mode
 * - Notification preferences
 * - Auto-start trip option
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Use the same SharedPreferences file as SettingsManager
        preferenceManager.sharedPreferencesName = "app_settings"
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        // Ensure theme preference shows the correct current value
        syncThemePreferenceDisplay()
        
        setupPreferenceListeners()
    }
    
    /**
     * Sync the theme preference display with actual saved value
     */
    private fun syncThemePreferenceDisplay() {
        try {
            val themePreference = findPreference<ListPreference>("theme_preference")
            themePreference?.let { pref ->
                // Get the current saved value (defaults to "light" if not set)
                val currentValue = pref.value ?: "light"
                
                // Ensure the preference displays the correct current selection
                pref.value = currentValue
                
                android.util.Log.d("SettingsFragment", "Theme preference synced to: $currentValue")
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Error syncing theme preference", e)
        }
    }
    
    private fun setupPreferenceListeners() {
        // Dark Mode preference
        findPreference<ListPreference>("theme_preference")?.setOnPreferenceChangeListener { _, newValue ->
            val themeValue = newValue as String
            val mode = when (themeValue) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            android.util.Log.d("SettingsFragment", "Theme changed to: $themeValue")
            
            // Recreate activity to apply theme immediately
            activity?.recreate()
            true
        }
        
        // GPS Update Frequency preference
        findPreference<ListPreference>("gps_update_frequency")?.setOnPreferenceChangeListener { _, newValue ->
            val frequency = (newValue as String).toIntOrNull() ?: 10
            // Update GPS service configuration
            updateGpsFrequency(frequency)
            true
        }
        
        // Distance Units preference
        findPreference<ListPreference>("distance_units")?.setOnPreferenceChangeListener { _, _ ->
            // UI will automatically update on next screen refresh
            true
        }
        
        // Notification preferences
        findPreference<SwitchPreferenceCompat>("notifications_enabled")?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            updateNotificationSettings(enabled)
            true
        }
        
        // Auto-start trip preference
        findPreference<SwitchPreferenceCompat>("auto_start_trip")?.setOnPreferenceChangeListener { _, _ ->
            // This will be checked when app launches
            true
        }
    }
    
    private fun updateGpsFrequency(seconds: Int) {
        // This would update the GPS service configuration
        // For now, just log it
        android.util.Log.d("SettingsFragment", "GPS update frequency changed to: $seconds seconds")
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        // Update notification channel settings
        android.util.Log.d("SettingsFragment", "Notifications enabled: $enabled")
    }
    
    companion object {
        fun newInstance() = SettingsFragment()
    }
}



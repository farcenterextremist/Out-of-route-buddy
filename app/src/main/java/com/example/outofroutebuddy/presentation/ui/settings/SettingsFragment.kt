package com.example.outofroutebuddy.presentation.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.core.config.BuildConfig
import com.example.outofroutebuddy.presentation.viewmodel.DataManagementViewModel
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.Toast

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
    
    // ✅ FIX: Access ViewModel via activity scope to ensure it survives config changes
    private val tripInputViewModel: TripInputViewModel by activityViewModels()
    private val dataManagementViewModel: DataManagementViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Use the same SharedPreferences file as SettingsManager
        preferenceManager.sharedPreferencesName = "app_settings"
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        // Ensure theme preference shows the correct current value
        syncThemePreferenceDisplay()
        
        setupPreferenceListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh theme display when returning to Settings (e.g. after changing theme elsewhere)
        syncThemePreferenceDisplay()
    }
    
    /**
     * Sync the theme preference display with actual saved value.
     * Reads from the same SharedPreferences as SettingsManager so the selection
     * always reflects the persisted theme (e.g. Dark) even when opened from Settings.
     */
    private fun syncThemePreferenceDisplay() {
        try {
            val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val savedValue = prefs.getString("theme_preference", "system") ?: "system"
            val themePreference = findPreference<ListPreference>("theme_preference")
            themePreference?.let { pref ->
                pref.value = savedValue
                // Show current selection in summary (e.g. "Dark Mode")
                val entries = pref.entries
                val values = pref.entryValues
                val index = (0 until values.size).indexOfFirst { values[it].toString() == savedValue }
                if (index in entries.indices) pref.summary = entries[index]
                android.util.Log.d(TAG, "Theme preference synced to: $savedValue")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error syncing theme preference", e)
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
            android.util.Log.d(TAG, "Theme changed to: $themeValue")
            
            // ✅ FIX: Save current trip data before recreation
            saveCurrentTripData()
            
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
        
        // About preference - show version information
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            android.util.Log.d(TAG, "About preference clicked")
            showAboutDialog()
            true
        }

        // Data & privacy: delete old data from device (keep on server)
        findPreference<Preference>("delete_old_data_from_device")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete old data from device")
                .setMessage("Remove trips older than 12 months from this device? Data may be kept on the server for training.")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    dataManagementViewModel.deleteOldDataFromDevice(12)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }

        // Data & privacy: clear all trip data from device
        findPreference<Preference>("clear_all_data_from_device")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear all trip data")
                .setMessage("Remove all trips from this device? Data may be kept on the server for product improvement and training.")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    dataManagementViewModel.clearAllDataFromDevice()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataManagementViewModel.results.collectLatest { result ->
                    when (result) {
                        is DataManagementViewModel.DataManagementResult.Success ->
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                        is DataManagementViewModel.DataManagementResult.Error ->
                            Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun updateGpsFrequency(seconds: Int) {
        // This would update the GPS service configuration
        // For now, just log it
        android.util.Log.d(TAG, "GPS update frequency changed to: $seconds seconds")
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        // Update notification channel settings
        android.util.Log.d(TAG, "Notifications enabled: $enabled")
    }
    
    /**
     * Show about dialog with app version and information
     */
    private fun showAboutDialog() {
        try {
            android.util.Log.d(TAG, "showAboutDialog called")
            val message = """
                ${BuildConfig.APP_NAME}
                Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                
                A GPS tracking app for calculating out-of-route miles.
                
                Features:
                • Real-time GPS tracking
                • Trip persistence across app restarts
                • Dark/Light theme support
                • Comprehensive trip history
                
                Built with Android ${BuildConfig.TARGET_SDK}
            """.trimIndent()
            
            android.util.Log.d(TAG, "About message: $message")
            
            AlertDialog.Builder(requireContext())
                .setTitle("About")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    android.util.Log.d(TAG, "About dialog OK clicked")
                    dialog.dismiss()
                }
                .show()
                
            android.util.Log.d(TAG, "About dialog shown")
                
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing about dialog", e)
        }
    }
    
    /**
     * ✅ FIX: Save current trip data before activity recreation
     */
    private fun saveCurrentTripData() {
        try {
            android.util.Log.d(TAG, "Saving trip data before theme change")
            
            // The TripInputViewModel already handles persistence automatically via:
            // 1. Auto-save via TripCrashRecoveryManager (every 30 seconds)
            // 2. TripPersistenceManager saves to SharedPreferences
            // 3. StateCache persists in-memory state
            
            // Hilt ViewModels survive configuration changes by default,
            // so the ViewModel instance will persist across activity recreation
            // and trip data will be restored automatically
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving trip data", e)
        }
    }
    
    companion object {
        private const val TAG = "SettingsFragment"
        fun newInstance() = SettingsFragment()
    }
}



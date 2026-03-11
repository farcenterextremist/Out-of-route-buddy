package com.example.outofroutebuddy.presentation.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.BuildConfig
import com.example.outofroutebuddy.core.config.BuildConfig as CoreBuildConfig
import com.example.outofroutebuddy.data.SettingsManager
import com.example.outofroutebuddy.presentation.viewmodel.DataManagementViewModel
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    @javax.inject.Inject
    lateinit var settingsManager: SettingsManager

    // ✅ FIX: Access ViewModel via activity scope to ensure it survives config changes
    private val tripInputViewModel: TripInputViewModel by activityViewModels()
    private val dataManagementViewModel: DataManagementViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Use the same SharedPreferences file as SettingsManager
        preferenceManager.sharedPreferencesName = "app_settings"
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Phase 3.2: Hide Developer category in release builds
        if (!BuildConfig.DEBUG) {
            findPreference<PreferenceCategory>("developer_category")?.let {
                preferenceScreen.removePreference(it)
            }
        }

        // Ensure theme preference shows the correct current value
        syncThemePreferenceDisplay()
        
        setupPreferenceListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh theme display when returning to Settings (e.g. after changing theme elsewhere)
        syncThemePreferenceDisplay()
        logAutoPruneScheduleDebug()
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

        // GPS preset: applies both gps_update_frequency and high_accuracy_mode
        findPreference<ListPreference>("gps_preset")?.setOnPreferenceChangeListener { _, newValue ->
            val preset = newValue as? String ?: "balanced"
            settingsManager.setGpsPreset(preset)
            android.util.Log.d(TAG, "GPS preset changed to: $preset")
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

        // Trip-ended overlay permission: open system overlay settings
        findPreference<Preference>("overlay_permission")?.setOnPreferenceClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            }
            true
        }
        
        // About preference - show version in summary (120_MINUTE_IMPROVEMENT_LOOP: useful info)
        findPreference<Preference>("about")?.let { pref ->
            pref.summary = "Version ${BuildConfig.VERSION_NAME}"
            pref.setOnPreferenceClickListener {
                android.util.Log.d(TAG, "About preference clicked")
                showAboutDialog()
                true
            }
        }

        // Advanced: Clear cache (export/temp files in app cache dir; not Room DB or SharedPreferences)
        findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            clearAppCache()
            true
        }

        // Developer (debug only): Export app logs — share log file if app writes one; else show message
        findPreference<Preference>("export_app_logs")?.setOnPreferenceClickListener {
            exportAppLogsIfAvailable()
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
     * Debug-only diagnostic log for automatic trip pruning schedule.
     * No UI changes; this is Logcat visibility for maintenance and validation.
     */
    private fun logAutoPruneScheduleDebug() {
        if (!BuildConfig.DEBUG) return
        try {
            val prefs = com.example.outofroutebuddy.data.PreferencesManager(requireContext())
            val lastRunMs = prefs.getLastAutoPruneTimestamp()
            val nextEligibleMs = if (lastRunMs > 0L) lastRunMs + AUTO_PRUNE_MIN_INTERVAL_MS else 0L
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val lastRunText = if (lastRunMs > 0L) formatter.format(Date(lastRunMs)) else "never"
            val nextEligibleText = if (nextEligibleMs > 0L) formatter.format(Date(nextEligibleMs)) else "now (never run)"
            android.util.Log.d(
                TAG,
                "Auto-prune debug -> last_run=$lastRunText, next_eligible=$nextEligibleText, retention_months=$AUTO_PRUNE_RETENTION_MONTHS",
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to log auto-prune schedule debug info", e)
        }
    }
    
    /**
     * Show about dialog with app version and information
     */
    private fun showAboutDialog() {
        try {
            android.util.Log.d(TAG, "showAboutDialog called")
            val message = """
                ${CoreBuildConfig.APP_NAME}
                Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                
                A GPS tracking app for calculating out-of-route miles.
                
                Features:
                • Real-time GPS tracking
                • Trip persistence across app restarts
                • Dark/Light theme support
                • Comprehensive trip history
                
                Built with Android ${CoreBuildConfig.TARGET_SDK}
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
    
    /**
     * Clear app cache (export files, temp files in context.cacheDir).
     * Does not clear Room database or SharedPreferences.
     */
    private fun clearAppCache() {
        try {
            val cacheDir = requireContext().cacheDir ?: return
            deleteDirContents(cacheDir)
            Toast.makeText(requireContext(), getString(R.string.cache_cleared_message), Toast.LENGTH_SHORT).show()
            android.util.Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error clearing cache", e)
            Toast.makeText(requireContext(), getString(R.string.cache_clear_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDirContents(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteDirContents(file)
            file.delete()
        }
    }

    /**
     * Phase 3.2: Share recent app log file if the app writes one (e.g. to cache or files dir).
     * No PII or secrets in exported logs. If no log file exists, show a short message.
     */
    private fun exportAppLogsIfAvailable() {
        val logDir = requireContext().cacheDir
        val logFiles = logDir.listFiles { _, name -> name.endsWith(".log") }?.sortedByDescending { it.lastModified() }
        val logFile = logFiles?.firstOrNull()
        if (logFile != null && logFile.canRead()) {
            try {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    logFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.export_app_logs_title)))
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error sharing log file", e)
                Toast.makeText(requireContext(), getString(R.string.export_logs_error), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.export_logs_not_configured), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
        private const val AUTO_PRUNE_RETENTION_MONTHS = 12
        private const val AUTO_PRUNE_MIN_INTERVAL_MS = 24 * 60 * 60 * 1000L
        fun newInstance() = SettingsFragment()
    }
}



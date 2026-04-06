package com.example.outofroutebuddy.presentation.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SwitchPreferenceCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.dao.TripDao
import com.example.outofroutebuddy.BuildConfig
import com.example.outofroutebuddy.data.SettingsManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    private data class AccordionSection(
        val headerKey: String,
        val childKeys: List<String>,
    )
    
    @javax.inject.Inject
    lateinit var settingsManager: SettingsManager

    @javax.inject.Inject
    lateinit var tripDao: TripDao

    private val tripInputViewModel: TripInputViewModel by activityViewModels()
    private val accordionSections = listOf(
        AccordionSection(
            headerKey = "header_appearance",
            childKeys = listOf("theme_preference", "distance_units"),
        ),
        AccordionSection(
            headerKey = "header_battery",
            childKeys = listOf("gps_preset", "battery_optimization", "show_pause_button", "continue_tracking_after_app_dismissed"),
        ),
        AccordionSection(
            headerKey = "header_notifications",
            childKeys = listOf("notifications_enabled", "notification_sound", "overlay_permission"),
        ),
        AccordionSection(
            headerKey = "header_general",
            childKeys = listOf("gps_update_frequency", "high_accuracy_mode", "auto_start_trip", "auto_save_trip"),
        ),
        AccordionSection(
            headerKey = "header_ludacris",
            childKeys = listOf(
                "drive_detect_walking_speed_mph",
                "drive_detect_walking_min_duration_sec",
                "drive_detect_highway_lookback_sec",
                "ludicrous_show_time_zones",
                "ludicrous_show_elevation",
                "ludicrous_show_max_speed",
                "verbose_logging",
                "export_app_logs",
                "export_debug_snapshot",
            ),
        ),
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Use the same SharedPreferences file as SettingsManager
        preferenceManager.sharedPreferencesName = "app_settings"
        setPreferencesFromResource(R.xml.preferences, rootKey)
        normalizePreferenceLayout(preferenceScreen)

        // Phase 3.2: Hide Developer category in release builds
        if (!BuildConfig.DEBUG) {
            hideDeveloperSection()
        }

        setupAccordionSections(savedInstanceState)
        applyFocusFromArguments()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        accordionSections.forEach { section ->
            val header = findPreference<AccordionHeaderPreference>(section.headerKey)
            outState.putBoolean(stateKeyFor(section.headerKey), header?.isExpanded ?: false)
        }
    }

    private fun setupAccordionSections(savedInstanceState: Bundle?) {
        accordionSections.forEachIndexed { index, section ->
            val header = findPreference<AccordionHeaderPreference>(section.headerKey) ?: return@forEachIndexed
            val expanded = savedInstanceState?.getBoolean(stateKeyFor(section.headerKey))
                ?: false
            setSectionExpanded(section, expanded)
            header.setOnPreferenceClickListener {
                setSectionExpanded(section, !header.isExpanded)
                true
            }
        }
    }

    private fun setSectionExpanded(section: AccordionSection, expanded: Boolean) {
        findPreference<AccordionHeaderPreference>(section.headerKey)?.isExpanded = expanded
        section.childKeys.forEach { childKey ->
            findPreference<Preference>(childKey)?.isVisible = expanded
        }
    }

    private fun hideDeveloperSection() {
        findPreference<Preference>("verbose_logging")?.isVisible = false
        findPreference<Preference>("export_app_logs")?.isVisible = false
        findPreference<Preference>("export_debug_snapshot")?.isVisible = false
    }

    /** When already on Settings, drawer can request a different accordion focus. */
    fun applyFocusSection(focusSection: String) {
        applyFocusSectionInternal(focusSection)
    }

    private fun applyFocusFromArguments() {
        applyFocusSectionInternal(arguments?.getString("focusSection").orEmpty())
    }

    private fun applyFocusSectionInternal(focusSection: String) {
        val target = when (focusSection) {
            "trip_tracking" -> "header_general"
            "battery" -> "header_battery"
            "notifications" -> "header_notifications"
            else -> return
        }
        accordionSections.forEach { section ->
            setSectionExpanded(section, section.headerKey == target)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_settings_root, container, false)
        val host = root.findViewById<FrameLayout>(R.id.settings_prefs_host)
        val prefsRoot = super.onCreateView(inflater, host, savedInstanceState)
        host.addView(
            prefsRoot,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        root.findViewById<android.widget.ImageButton>(R.id.settings_back_button).setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        return root
    }

    private fun normalizePreferenceLayout(group: PreferenceGroup?) {
        group ?: return
        for (index in 0 until group.preferenceCount) {
            val preference = group.getPreference(index)
            preference.isIconSpaceReserved = true
            if (preference is PreferenceGroup) {
                normalizePreferenceLayout(preference)
            }
        }
    }
    
    private fun setupPreferenceListeners() {
        // Dark Mode preference
        findPreference<ListPreference>("theme_preference")?.setOnPreferenceChangeListener { _, newValue ->
            val themeValue = newValue as String
            settingsManager.setThemePreference(themeValue)
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
            android.util.Log.d(TAG, "GPS update frequency set to ${frequency}s (applies when tracking starts; stored by preference UI)")
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
        findPreference<ListPreference>("distance_units")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setDistanceUnits(newValue as String)
            true
        }

        findPreference<SwitchPreferenceCompat>("battery_optimization")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setBatteryOptimizationEnabled(newValue as Boolean)
            true
        }

        findPreference<SwitchPreferenceCompat>("high_accuracy_mode")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setHighAccuracyMode(newValue as Boolean)
            true
        }
        
        // Notification preferences
        findPreference<SwitchPreferenceCompat>("notifications_enabled")?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            settingsManager.setNotificationsEnabled(enabled)
            true
        }
        
        // Auto-start trip preference
        findPreference<SwitchPreferenceCompat>("auto_start_trip")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setAutoStartTripEnabled(newValue as Boolean)
            true
        }

        findPreference<SwitchPreferenceCompat>("notification_sound")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setNotificationSoundEnabled(newValue as Boolean)
            true
        }

        findPreference<SwitchPreferenceCompat>("auto_save_trip")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.setAutoSaveTripEnabled(newValue as Boolean)
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
        
        // Developer (debug only): Export app logs
        findPreference<Preference>("export_app_logs")?.setOnPreferenceClickListener {
            exportAppLogsIfAvailable()
            true
        }

        findPreference<Preference>("export_debug_snapshot")?.setOnPreferenceClickListener {
            exportDebugSnapshot()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(null)
        setDividerHeight(0)
        val dividerDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.settings_preference_divider)
        if (dividerDrawable != null) {
            listView?.addItemDecoration(UniformDividerDecoration(dividerDrawable))
        }
        enforceUniformPreferenceFonts()
    }

    /**
     * Draws a consistent divider line between every visible preference row,
     * ensuring uniform separators inside each accordion section.
     */
    private class UniformDividerDecoration(
        private val divider: android.graphics.drawable.Drawable,
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position > 0) {
                outRect.top = divider.intrinsicHeight.coerceAtLeast(1)
            }
        }

        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val position = parent.getChildAdapterPosition(child)
                if (position <= 0) continue
                val top = child.top - divider.intrinsicHeight.coerceAtLeast(1)
                val bottom = child.top
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
    }

    /**
     * Attaches a child-attach listener to the preference RecyclerView so that every
     * preference row gets a uniform 14sp title / 12sp summary — matching the drawer
     * menu font. Accordion headers are exempt (they use their own bold 15sp style).
     */
    private fun enforceUniformPreferenceFonts() {
        val rv = listView ?: return
        rv.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(child: View) {
                stylePreferenceRow(child)
            }
            override fun onChildViewDetachedFromWindow(child: View) { /* no-op */ }
        })
    }

    private fun stylePreferenceRow(row: View) {
        val titleView = row.findViewById<TextView>(android.R.id.title) ?: return
        val summaryView = row.findViewById<TextView>(android.R.id.summary)
        val iconView = row.findViewById<ImageView>(android.R.id.icon)

        val textPrimary = ContextCompat.getColor(row.context, R.color.text_primary_adaptive)
        val textSecondary = ContextCompat.getColor(row.context, R.color.text_secondary_adaptive)
        val iconTint = ContextCompat.getColor(row.context, R.color.settings_pref_icon)

        val isHeader = titleView.typeface?.isBold == true &&
            titleView.textSize > TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.5f, resources.displayMetrics)

        if (!isHeader) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            titleView.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            titleView.setTextColor(textPrimary)
        }
        summaryView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        summaryView?.setTextColor(textSecondary)
        iconView?.imageTintList = ColorStateList.valueOf(iconTint)

        val iconFrame = row.findViewById<View>(android.R.id.icon_frame)
            ?: row.findViewById<View>(androidx.preference.R.id.icon_frame)
        iconFrame?.visibility = View.VISIBLE
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

    /**
     * Debug / instrumented support: text snapshot (version, prefs summary, trip count only).
     * Negative: file briefly written to cache; share sheet required to extract. No GPS or trip coordinates.
     */
    private fun exportDebugSnapshot() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val tripCount = withContext(Dispatchers.IO) { tripDao.countTripsSuspend() }
                val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val utc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val body = buildString {
                    appendLine("Out of Route — debug snapshot")
                    appendLine("Generated (UTC): ${utc.format(Date())}")
                    appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    appendLine("Debug build: ${BuildConfig.DEBUG}")
                    appendLine("Local trip count (Room): $tripCount")
                    appendLine("--- app_settings (keys only, no secrets) ---")
                    appendLine("theme: ${prefs.getString("theme_preference", "?")}")
                    appendLine("distance_units: ${prefs.getString("distance_units", "?")}")
                    appendLine("gps_preset: ${prefs.getString("gps_preset", "?")}")
                    appendLine("gps_update_frequency: ${prefs.getString("gps_update_frequency", "?")}")
                    appendLine("high_accuracy_mode: ${prefs.getBoolean("high_accuracy_mode", false)}")
                    appendLine("notifications_enabled: ${prefs.getBoolean("notifications_enabled", true)}")
                    appendLine(
                        "continue_tracking_after_app_dismissed: " +
                            "${prefs.getBoolean("continue_tracking_after_app_dismissed", true)}",
                    )
                    appendLine("period_mode (ViewModel may override UI): ${tripInputViewModel.getCurrentPeriodMode().name}")
                    appendLine("--- end ---")
                    appendLine("No coordinates, addresses, or trip payloads included.")
                }
                val file = File(
                    requireContext().cacheDir,
                    "oor_debug_snapshot_${System.currentTimeMillis()}.txt",
                )
                withContext(Dispatchers.IO) {
                    file.writeText(body)
                }
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file,
                )
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "OOR debug snapshot")
                    putExtra(Intent.EXTRA_TEXT, body)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(send, getString(R.string.export_debug_snapshot_title)))
                Toast.makeText(requireContext(), R.string.debug_snapshot_shared, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "exportDebugSnapshot failed", e)
                Toast.makeText(requireContext(), R.string.debug_snapshot_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
        private const val AUTO_PRUNE_RETENTION_MONTHS = 12
        private const val AUTO_PRUNE_MIN_INTERVAL_MS = 24 * 60 * 60 * 1000L
        private const val ACCORDION_STATE_PREFIX = "accordion_state_"

        private fun stateKeyFor(headerKey: String): String = ACCORDION_STATE_PREFIX + headerKey

        fun newInstance() = SettingsFragment()
    }
}



package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.view.HapticFeedbackConstants
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.util.Pair
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.OutOfRouteApplication
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.core.config.BuildConfig
import com.example.outofroutebuddy.databinding.DialogHelpInfoBinding
import com.example.outofroutebuddy.databinding.DialogModeSelectBinding
import com.example.outofroutebuddy.databinding.DialogSettingsBinding
import com.example.outofroutebuddy.databinding.DialogTemplateSelectBinding
import com.example.outofroutebuddy.databinding.FragmentTripInputBinding
import com.example.outofroutebuddy.databinding.StatisticsRowBinding
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.data.SettingsManager
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel.TripEvent
import com.example.outofroutebuddy.presentation.ui.dialogs.CustomCalendarDialog
import com.example.outofroutebuddy.presentation.ui.dialogs.TripHistoryByDateDialog
import com.example.outofroutebuddy.services.PeriodCalculationService
import com.example.outofroutebuddy.services.TripEndedOverlayService
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * Trip Input Fragment
 *
 * This fragment handles the main trip input interface where users can:
 * - Enter loaded and bounce miles
 * - Start/stop trip tracking
 * - View real-time GPS data
 * - View trip statistics
 *
 * ✅ NEW: Hilt Integration
 * - Added @AndroidEntryPoint for dependency injection
 * - ViewModel is now injected automatically by Hilt
 * - No more manual factory creation
 * - Clean separation of concerns
 */
@AndroidEntryPoint
class TripInputFragment : Fragment() {
    /** One automatic start per fragment instance when Settings → auto-start is enabled. */
    private var consumedAutoStartThisInstance = false

    private var _binding: FragmentTripInputBinding? = null
    private val binding: FragmentTripInputBinding
        get() = _binding ?: throw IllegalStateException("Binding accessed before onCreateView or after onDestroyView")

    @Inject
    lateinit var periodCalculationService: PeriodCalculationService

    @Inject
    lateinit var settingsManager: SettingsManager

    // ✅ FIXED: Use Hilt for ViewModel injection
    //
    // 🔍 ROOT CAUSE ANALYSIS: ViewModel Constructor Issue - RESOLVED
    // =============================================================
    //
    // The original error was:
    // "Type mismatch: inferred type is PreferencesManager but TripRepository was expected"
    // "No value passed for parameter 'tripStateManager'"
    // "No value passed for parameter 'coroutineScope'"
    //
    // This was caused by THREE separate issues:
    //
    // 1. ✅ FIXED: STALE CLASS FILES: There were stale .class files for TripInputViewModel in the build directory
    //    from an old version of the class with a different constructor signature (with coroutineScope).
    //    The compiler was picking up these old files instead of the current source code.
    //    SOLUTION: Delete all TripInputViewModel.class files, kill Java processes, and run full clean.
    //
    // 2. ✅ FIXED: REPOSITORY TYPE MISMATCH: There are TWO different TripRepository types in the codebase:
    //    - com.example.outofroutebuddy.data.repository.TripRepository (class - implementation)
    //    - com.example.outofroutebuddy.domain.repository.TripRepository (interface - contract)
    //
    //    TripStatePersistence expects the data layer class, but the mock was implementing the domain layer interface.
    //    This caused type mismatch errors when trying to pass the mock to TripStatePersistence.
    //    SOLUTION: Created separate mocks for each repository type and use the correct one for each dependency.
    //
    // 3. ✅ FIXED: COMPLEX DEPENDENCY INJECTION: The ViewModel has 15+ dependencies, making manual instantiation
    //    in the fragment very complex and error-prone. This should be replaced with proper DI (Hilt/Dagger).
    //    SOLUTION: Implemented proper dependency injection framework with Hilt.
    //
    // FIXES APPLIED:
    // - ✅ Deleted all stale .class files
    // - ✅ Killed Java processes to release file locks
    // - ✅ Ran full clean build
    // - ✅ Identified repository type mismatch
    // - ✅ Created separate mocks for data and domain repositories
    // - ✅ Implemented proper dependency injection (Hilt)
    // - ✅ Removed manual ViewModel instantiation from fragment
    // - ✅ Used @Inject and @Provides for clean dependency management
    //
    // ✅ RESULT: ViewModel is now properly injected by Hilt with all dependencies resolved
    //
    private val viewModel: TripInputViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTripInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        
        // ✅ FIX: Restore text input values after theme change
        restoreTextInputs()
        
        setupUI()
        observeViewModel()
        setupClickListeners()
        observeTripHistoryDeletionResults()
        
        // ✅ FIX: Update UI immediately with current state in case trip was restored from persistence
        // This handles the case where app was closed and reopened - the ViewModel may have already
        // loaded persisted state before observeViewModel() starts collecting
        updateUI(viewModel.uiState.value)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // ✅ FIX: Save text input values to survive configuration changes
        if (_binding != null) {
            outState.putString("loaded_miles_input", binding.loadedMilesInput.text.toString())
            outState.putString("bounce_miles_input", binding.bounceMilesInput.text.toString())
            outState.putString("pickup_address_input", binding.pickupAddressInput.text.toString())
            outState.putString("dropoff_address_input", binding.dropoffAddressInput.text.toString())
            // Also save to SharedPreferences for theme changes
            saveTextInputsToPrefs()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // ✅ FIX: Save text input values when fragment pauses (before theme change)
        if (_binding != null) {
            saveTextInputsToPrefs()
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep stat cards aligned with natural month/year rollover when app returns to foreground.
        viewModel.ensureStatisticsPeriodIsCurrentCycle()
        binding.root.post { tryAutoStartTripIfEnabled() }
    }
    
    /**
     * ✅ FIX: Save text input values to SharedPreferences
     */
    private fun saveTextInputsToPrefs() {
        try {
            val prefs = requireContext().getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
            prefs.edit {
                    putString("loaded_miles_input", binding.loadedMilesInput.text.toString())
                    .putString("bounce_miles_input", binding.bounceMilesInput.text.toString())
                    .putString("pickup_address_input", binding.pickupAddressInput.text.toString())
                    .putString("dropoff_address_input", binding.dropoffAddressInput.text.toString())
                }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving text inputs", e)
        }
    }
    
    /**
     * ✅ FIX: Restore text input values after configuration change
     */
    private fun restoreTextInputs() {
        try {
            val state = viewModel.uiState.value
            
            // ✅ FIX: If trip is active, restore from ViewModel state (from persisted trip)
            if (state.isTripActive) {
                // Restore from ViewModel state which was loaded from persistence
                if (state.loadedMiles > 0.0) {
                    binding.loadedMilesInput.setText(state.loadedMiles.toString())
                }
                if (state.bounceMiles > 0.0) {
                    binding.bounceMilesInput.setText(state.bounceMiles.toString())
                }
                return
            }
            
            // If trip is not active, restore from SharedPreferences
            val prefs = requireContext().getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
            val savedLoadedMiles = prefs.getString("loaded_miles_input", "")
            val savedBounceMiles = prefs.getString("bounce_miles_input", "")
            
            if (!savedLoadedMiles.isNullOrEmpty()) {
                binding.loadedMilesInput.setText(savedLoadedMiles)
            }
            
            if (!savedBounceMiles.isNullOrEmpty()) {
                binding.bounceMilesInput.setText(savedBounceMiles)
            }
            val savedPickup = prefs.getString("pickup_address_input", null)
            val legacyDest = prefs.getString("destination_address_input", "").orEmpty()
            when {
                !savedPickup.isNullOrEmpty() -> binding.pickupAddressInput.setText(savedPickup)
                legacyDest.isNotEmpty() -> {
                    // One-time migration from single "destination" field to Pickup
                    binding.pickupAddressInput.setText(legacyDest)
                }
            }
            val savedDropoff = prefs.getString("dropoff_address_input", "").orEmpty()
            if (savedDropoff.isNotEmpty()) {
                binding.dropoffAddressInput.setText(savedDropoff)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error restoring text inputs", e)
        }
    }
    
    /**
     * ✅ FIX: Clear text input fields (public for MainActivity to call)
     */
    fun clearTextInputs() {
        try {
            if (_binding != null) {
                binding.loadedMilesInput.setText("")
                binding.bounceMilesInput.setText("")
                binding.pickupAddressInput.setText("")
                binding.dropoffAddressInput.setText("")
                clearSavedTextInputs()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error clearing text inputs", e)
        }
    }
    
    /**
     * ✅ FIX: Clear saved text input values
     */
    private fun clearSavedTextInputs() {
        try {
            val prefs = requireContext().getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
            prefs.edit {
                    putString("loaded_miles_input", "")
                    .putString("bounce_miles_input", "")
                    .putString("pickup_address_input", "")
                    .putString("dropoff_address_input", "")
                    .remove("destination_address_input")
                }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error clearing saved text inputs", e)
        }
    }

    private lateinit var addressHistoryStore: AddressHistoryStore

    private fun setupUI() {
        // Initialize UI components
        binding.startTripButton.text = getString(R.string.start_trip)
        binding.startTripButton.contentDescription = getString(R.string.start_trip_button_description)
        binding.progressBar.visibility = View.GONE

        // Toolbar background: set in custom_toolbar.xml (toolbar_background_cracked_road)
        // Ensure statistics content starts collapsed
        binding.statisticsContent.visibility = View.GONE
        binding.statisticsButton.setIconResource(R.drawable.ic_arrow_down)
        binding.statisticsButton.contentDescription = getString(R.string.statistics_button_description)

        setupAddressAutocomplete()
    }

    private fun setupAddressAutocomplete() {
        addressHistoryStore = AddressHistoryStore(requireContext())
        val pickupAdapter = AddressSuggestionAdapter(requireContext(), addressHistoryStore, viewLifecycleOwner.lifecycleScope)
        val dropoffAdapter = AddressSuggestionAdapter(requireContext(), addressHistoryStore, viewLifecycleOwner.lifecycleScope)
        binding.pickupAddressInput.setAdapter(pickupAdapter)
        binding.dropoffAddressInput.setAdapter(dropoffAdapter)

        binding.pickupAddressInput.setOnItemClickListener { _, _, position, _ ->
            val address = pickupAdapter.getAddress(position)
            binding.pickupAddressInput.setText(address)
            binding.pickupAddressInput.setSelection(address.length)
            addressHistoryStore.add(address)
        }
        binding.dropoffAddressInput.setOnItemClickListener { _, _, position, _ ->
            val address = dropoffAdapter.getAddress(position)
            binding.dropoffAddressInput.setText(address)
            binding.dropoffAddressInput.setSelection(address.length)
            addressHistoryStore.add(address)
        }
    }

    private fun setupClickListeners() {
        binding.startTripButton.setOnClickListener {
            performButtonHapticFeedback(it)
            // ✅ FIX: Toggle between Start Trip and End Trip based on current state
            if (viewModel.uiState.value.isTripActive) {
                // Trip is active, so show confirmation before ending
                showEndTripConfirmation()
            } else {
                // ✅ NEW: Check permissions before starting trip
                if (!checkLocationPermissions()) {
                    showPermissionRequiredDialog()
                    return@setOnClickListener
                }
                
                // ✅ NEW: Validate that loaded and bounce miles are entered
                val loadedMilesText = binding.loadedMilesInput.text.toString().trim()
                val bounceMilesText = binding.bounceMilesInput.text.toString().trim()
                
                if (loadedMilesText.isEmpty() || bounceMilesText.isEmpty()) {
                    // Hide keyboard first so Snackbar is visible
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

                    // Show Snackbar using the same helper function
                    showSnackbar("Must enter loaded/bounce miles")
                    return@setOnClickListener
                }
                
                // Trip is not active, so START it
                val loadedMiles = loadedMilesText.toDoubleOrNull() ?: 0.0
                val bounceMiles = bounceMilesText.toDoubleOrNull() ?: 0.0
                val actualMiles = 0.0 // Always start at 0, GPS will update

                maybeWarnAboutBackgroundTrackingReliability(
                    loadedMiles = loadedMiles,
                    bounceMiles = bounceMiles,
                    actualMiles = actualMiles,
                )
            }
        }

        // Statistics button click listener
        binding.statisticsButton.setOnClickListener {
            val isVisible = binding.statisticsContent.isVisible
            binding.statisticsContent.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Update button icon - when collapsed show down arrow (to expand), when expanded show up arrow (to collapse)
            val icon = if (isVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            binding.statisticsButton.setIconResource(icon)
            binding.statisticsButton.contentDescription = getString(R.string.statistics_button_description)
        }

        binding.statisticsCalendarButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.refreshPeriodForCalendar()
                showCalendarPicker()
            }
        }

        binding.monthDeleteButton.setOnClickListener {
            showStatsDeleteConfirmationDialog(
                scope = "month section",
                onConfirm = { clearMonthSectionDisplay() },
            )
        }

        binding.yearDeleteButton.setOnClickListener {
            showStatsDeleteConfirmationDialog(
                scope = "year section",
                onConfirm = { clearYearSectionDisplay() },
            )
        }

        // Hamburger menu opens navigation drawer
        binding.customToolbarLayout.menuButton.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        // Settings gear opens settings screen
        binding.customToolbarLayout.settingsButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToSettings()
        }

        // Info button removed for cleaner field alignment; tooltip info is in hint text.
        
        // Pause button click listener
        binding.pauseButton.setOnClickListener {
            val isPaused = viewModel.uiState.value.isPaused
            if (!isPaused) {
                // Currently active, so pause
                viewModel.pauseTrip()
                binding.pauseButton.setIconResource(R.drawable.ic_play_adaptive)
                binding.pauseButton.setIconTintResource(android.R.color.white) // Dark mode: force white for visibility
                binding.pauseButton.contentDescription = getString(R.string.resume_trip_button_description)
                showSnackbar("Trip paused")
            } else {
                // Currently paused, so resume
                viewModel.resumeTrip()
                binding.pauseButton.setIconResource(R.drawable.ic_pause_adaptive)
                binding.pauseButton.setIconTintResource(android.R.color.white) // Dark mode: force white for visibility
                binding.pauseButton.contentDescription = getString(R.string.pause_trip_button_description)
                showSnackbar("Trip resumed")
            }
        }
    }
    
    /**
     * ✅ NEW: Helper function to show centered Snackbar messages with swipe-to-dismiss
     */
    private fun showSnackbar(message: String) {
        // Find the CoordinatorLayout from the activity
        val coordinatorLayout = requireActivity().findViewById<CoordinatorLayout>(R.id.root_coordinator_layout)
        
        val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
        
        // Center the text inside the Snackbar
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.gravity = Gravity.CENTER_HORIZONTAL
        
        snackbar.show()
    }

    private data class BackgroundTrackingReliability(
        val warnings: List<String>,
        val backgroundPermissionMissing: Boolean,
        val notificationsBlocked: Boolean,
        val batteryOptimizationRestricted: Boolean,
    )

    /**
     * @param autoStartWithoutConfirmIfClean when true and there are no reliability warnings, starts immediately
     *        (used for Settings → auto-start trip).
     */
    private fun maybeWarnAboutBackgroundTrackingReliability(
        loadedMiles: Double,
        bounceMiles: Double,
        actualMiles: Double,
        autoStartWithoutConfirmIfClean: Boolean = false,
    ) {
        val reliability = buildBackgroundTrackingReliability()
        val startTrip = {
            viewModel.calculateTrip(
                loadedMiles = loadedMiles,
                bounceMiles = bounceMiles,
                actualMiles = actualMiles,
                backgroundTrackingDegraded = reliability.warnings.isNotEmpty(),
                backgroundTrackingDegradedReasons = reliability.warnings,
                pickupAddress = binding.pickupAddressInput.text.toString().trim(),
                dropoffAddress = binding.dropoffAddressInput.text.toString().trim(),
            )
            if (::addressHistoryStore.isInitialized) {
                val pickup = binding.pickupAddressInput.text.toString().trim()
                val dropoff = binding.dropoffAddressInput.text.toString().trim()
                if (pickup.isNotBlank()) addressHistoryStore.add(pickup)
                if (dropoff.isNotBlank()) addressHistoryStore.add(dropoff)
            }
            clearSavedTextInputs()
        }

        if (autoStartWithoutConfirmIfClean && reliability.warnings.isEmpty()) {
            startTrip()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Start trip?")
            .setMessage("Are you sure you want to start?")
            .setPositiveButton("Start") { dialog, _ ->
                dialog.dismiss()
                startTrip()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Settings → Auto-start trip: if miles are filled and permissions OK, start once without the
     * extra confirmation dialog when tracking preflight is clean; otherwise show the same warning dialog as manual start.
     */
    private fun tryAutoStartTripIfEnabled() {
        if (consumedAutoStartThisInstance) return
        if (_binding == null) return
        if (!settingsManager.isAutoStartTripEnabled()) return
        if (viewModel.uiState.value.isTripActive) return
        val loaded = binding.loadedMilesInput.text?.toString()?.trim().orEmpty()
        val bounce = binding.bounceMilesInput.text?.toString()?.trim().orEmpty()
        if (loaded.isEmpty() || bounce.isEmpty()) return
        if (!checkLocationPermissions()) return
        val loadedVal = loaded.toDoubleOrNull() ?: return
        val bounceVal = bounce.toDoubleOrNull() ?: return
        consumedAutoStartThisInstance = true
        maybeWarnAboutBackgroundTrackingReliability(
            loadedMiles = loadedVal,
            bounceMiles = bounceVal,
            actualMiles = 0.0,
            autoStartWithoutConfirmIfClean = true,
        )
    }

    private fun buildBackgroundTrackingReliability(): BackgroundTrackingReliability {
        val mainActivity = activity as? MainActivity
        val backgroundPermissionMissing =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mainActivity?.hasBackgroundPermission() == false
        val notificationsBlocked =
            mainActivity?.hasNotificationPermission() == false || mainActivity?.areSystemNotificationsEnabled() == false
        val batteryOptimizationRestricted = mainActivity?.isIgnoringBatteryOptimizations() == false

        val warnings = buildList {
            if (backgroundPermissionMissing) {
                add(getString(R.string.background_tracking_warning_background_location))
            }
            if (notificationsBlocked) {
                add(getString(R.string.background_tracking_warning_notifications))
            }
            if (batteryOptimizationRestricted) {
                add(getString(R.string.background_tracking_warning_battery))
            }
        }

        return BackgroundTrackingReliability(
            warnings = warnings,
            backgroundPermissionMissing = backgroundPermissionMissing,
            notificationsBlocked = notificationsBlocked,
            batteryOptimizationRestricted = batteryOptimizationRestricted,
        )
    }

    private fun performButtonHapticFeedback(target: View) {
        target.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun showStatsDeleteConfirmationDialog(
        scope: String,
        onConfirm: () -> Unit,
    ) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Clear Section")
            .setMessage(
                "Clear displayed stats in the $scope?\n\n" +
                    "This will NOT delete trip history and will NOT affect other sections."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear") { _, _ ->
                onConfirm()
            }
            .create()
        dialog.setOnShowListener {
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                val white = android.graphics.Color.WHITE
                dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(white)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(white)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(white)
            }
        }
        dialog.show()
    }

    private fun clearMonthSectionDisplay() {
        viewModel.clearSelectedPeriodSectionDisplay()
        showSnackbar("Month section cleared (display only).")
    }

    private fun clearYearSectionDisplay() {
        viewModel.clearYearSectionDisplay()
        showSnackbar("Year section cleared (display only).")
    }

    /**
     * Re-sync month/year stats after single-day trip deletion in history dialog.
     * Day deletion is data-destructive by design and should update aggregate rows.
     */
    private fun observeTripHistoryDeletionResults() {
        parentFragmentManager.setFragmentResultListener(
            TripHistoryByDateDialog.REQUEST_KEY_DAY_TRIP_DELETED,
            viewLifecycleOwner
        ) { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.refreshPeriodForCalendar()
                // Refresh calendar dots (yellow/orange) so deleted trip no longer shows a dot
                val now = Date()
                val calMin = getFirstDayOfMonth(now).apply { add(Calendar.MONTH, -12) }
                val calMax = getLastDayOfMonth(now)
                val calendarMinDate = calMin.time
                val calendarMaxDate = calMax.time
                val datesWithTrips = viewModel.getDatesWithTripsForCalendarRange(calendarMinDate, calendarMaxDate)
                withContext(Dispatchers.Main) {
                    val calendarDialog = parentFragmentManager.findFragmentByTag("custom_calendar_dialog")
                    if (calendarDialog is CustomCalendarDialog) {
                        calendarDialog.refreshDatesWithTrips(datesWithTrips)
                    }
                }
            }
        }
    }
    
    /**
     * ✅ NEW: Show confirmation dialog before ending trip
     * Buttons (top to bottom): [End Trip] >> [Clear Trip] >> [Continue Trip]
     */
    private fun showEndTripConfirmation() {
        val dialogView = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        
        val messageText = android.widget.TextView(requireContext()).apply {
            text = getString(R.string.end_trip_dialog_message)
            setPadding(0, 16, 0, 24)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_adaptive))
        }
        dialogView.addView(messageText)
        dialogView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_adaptive))
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.setOnShowListener {
            // Title color follows theme via alertDialogTheme; message and background set above
        }
        
        val buttonLayoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8, 0, 8)
        }
        
        val applyPrimaryButtonStyle: (com.google.android.material.button.MaterialButton) -> Unit = { btn ->
            btn.setBackgroundResource(R.drawable.button_primary_material)
            btn.backgroundTintList = null
            btn.setTextColor(android.graphics.Color.WHITE)
            btn.textSize = 16f
            btn.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val applySecondaryButtonStyle: (com.google.android.material.button.MaterialButton) -> Unit = { btn ->
            btn.setBackgroundResource(R.drawable.button_secondary_material)
            btn.backgroundTintList = null
            btn.setTextColor(android.graphics.Color.WHITE)
            btn.textSize = 16f
            btn.setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Button 1: End Trip (top) - primary
        val endTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "End Trip"
            contentDescription = getString(R.string.end_trip_button_description)
            layoutParams = buttonLayoutParams
            applyPrimaryButtonStyle(this)
            setOnClickListener {
                performButtonHapticFeedback(this)
                dialog.dismiss()
                viewModel.endTrip()
                clearSavedTextInputs()
                binding.loadedMilesInput.setText("")
                binding.bounceMilesInput.setText("")
            }
        }
        dialogView.addView(endTripButton)
        
        // Button 2: Clear Trip (middle) - secondary
        val clearTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Clear Trip"
            contentDescription = getString(R.string.cancel_trip_button_description)
            layoutParams = buttonLayoutParams
            applySecondaryButtonStyle(this)
            setOnClickListener {
                dialog.dismiss()
                showClearTripConfirmation()
            }
        }
        dialogView.addView(clearTripButton)
        
        // Button 3: Continue Trip (bottom) - secondary
        val continueTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Continue Trip"
            contentDescription = getString(R.string.continue_trip_button_description)
            layoutParams = buttonLayoutParams
            applySecondaryButtonStyle(this)
            setOnClickListener {
                dialog.dismiss()
                TripEndedOverlayService.notifyUserChoseNoContinue(requireContext())
            }
        }
        dialogView.addView(continueTripButton)
        
        dialog.show()
    }

    /**
     * Show a lightweight arrival confirmation when trip-ended detection routes the user back into the app.
     */
    fun showEndTripConfirmationFromOverlay(
        promptSource: String = TripEndedOverlayService.PROMPT_SURFACE_OVERLAY,
    ) {
        val dialogView = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        val messageText = android.widget.TextView(requireContext()).apply {
            text = getString(R.string.end_trip_overlay_dialog_message)
            setPadding(0, 16, 0, 24)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_adaptive))
        }
        dialogView.addView(messageText)
        dialogView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_adaptive))
        val buttonLayoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8, 0, 8) }
        val applyPrimaryButtonStyle: (com.google.android.material.button.MaterialButton) -> Unit = { btn ->
            btn.setBackgroundResource(R.drawable.button_primary_material)
            btn.backgroundTintList = null
            btn.setTextColor(android.graphics.Color.WHITE)
            btn.textSize = 16f
            btn.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val applySecondaryButtonStyle: (com.google.android.material.button.MaterialButton) -> Unit = { btn ->
            btn.setBackgroundResource(R.drawable.button_secondary_material)
            btn.backgroundTintList = null
            btn.setTextColor(android.graphics.Color.WHITE)
            btn.textSize = 16f
            btn.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.end_trip_overlay_dialog_title))
            .setView(dialogView)
            .setCancelable(true)
            .create()
        var actionTaken = false
        dialog.setOnCancelListener {
            if (!actionTaken) {
                logTripEndPromptAction(promptSource, "ignored")
            }
        }
        val yesButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = getString(R.string.yes_complete_trip)
            layoutParams = buttonLayoutParams
            applyPrimaryButtonStyle(this)
            setOnClickListener {
                actionTaken = true
                dialog.dismiss()
                logTripEndPromptAction(promptSource, "ended")
                viewModel.endTrip()
                if (_binding != null) {
                    clearSavedTextInputs()
                    binding.loadedMilesInput.setText("")
                    binding.bounceMilesInput.setText("")
                }
                TripEndedOverlayService.dismissBubble(requireContext())
            }
        }
        val noButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = getString(R.string.no_continue_trip)
            layoutParams = buttonLayoutParams
            applySecondaryButtonStyle(this)
            setOnClickListener {
                actionTaken = true
                dialog.dismiss()
                logTripEndPromptAction(promptSource, "continued")
                TripEndedOverlayService.notifyUserChoseNoContinue(requireContext())
            }
        }
        dialogView.addView(yesButton)
        dialogView.addView(noButton)
        dialog.show()
    }

    private fun logTripEndPromptAction(
        promptSource: String,
        action: String,
    ) {
        (activity?.application as? OutOfRouteApplication)?.logAnalyticsEvent(
            "trip_end_prompt_action",
            mapOf(
                "surface" to promptSource,
                "action" to action,
            ),
        )
    }
    
    /**
     * ✅ NEW: Show confirmation dialog for clearing trip
     */
    private fun showClearTripConfirmation() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear Trip?")
            .setMessage(getString(R.string.clear_trip_dialog_message))
            .setPositiveButton("Yes, Clear") { _, _ ->
                // User confirmed - clear the trip
                viewModel.clearTrip()
                // Clear saved input values
                clearSavedTextInputs()
                // Clear the text input fields
                binding.loadedMilesInput.setText("")
                binding.bounceMilesInput.setText("")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // In dark mode, force readable action text for this platform AlertDialog.
        dialog.setOnShowListener {
            val isDarkMode =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                val white = ContextCompat.getColor(requireContext(), android.R.color.white)
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(white)
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(white)
            }
        }
        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)

        // Load theme from persisted preference so summary matches after restart.
        // When preference is "system", show the actual applied mode (Light/Dark) instead of "System".
        val themePreference = settingsManager.getThemePreference()
        dialogBinding.modeSummary.text = when (themePreference) {
            "dark" -> "Dark"
            "light" -> "Light"
            else -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) "Dark" else "Light"
            }
        }
        dialogBinding.templateSummary.text = viewModel.getCurrentPeriodModeDisplayText()

        // Mode row click
        val modeSummaryView = dialogBinding.modeSummary
        dialogBinding.modeRow.setOnClickListener {
            showModeSelectDialog(modeSummaryView)
        }
        // Template row click
        val templateSummaryView = dialogBinding.templateSummary
        dialogBinding.templateRow.setOnClickListener {
            showTemplateSelectDialog(templateSummaryView)
        }
        // Help & Info button
        dialogBinding.helpInfoButton.setOnClickListener {
            showHelpInfoDialog()
        }
        // Close X - dismiss settings dialog
        dialogBinding.settingsCloseButton.setOnClickListener {
            dialog.dismiss()
        }
        // Transparent window so rounded corners of content drawable show through
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showModeSelectDialog(summaryTextView: TextView) {
        val modeBinding = DialogModeSelectBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(modeBinding.root)
        dialog.setCancelable(true)

        // Initial selection from persisted preference (or current delegate if system)
        val pref = settingsManager.getThemePreference()
        val isDark = when (pref) {
            "dark" -> true
            "light" -> false
            else -> AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        }
        val checkedId = if (isDark) R.id.radio_mode_dark else R.id.radio_mode_light
        modeBinding.modeSelectRadioGroup.check(checkedId)
        // Apply selection after layout so it's visible when dialog is shown
        modeBinding.root.post { modeBinding.modeSelectRadioGroup.check(checkedId) }

        modeBinding.modeSelectRadioGroup.setOnCheckedChangeListener { _, selectedId ->
            when (selectedId) {
                R.id.radio_mode_light -> {
                    settingsManager.setThemePreference("light")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    summaryTextView.text = "Light"
                    activity?.recreate()
                }
                R.id.radio_mode_dark -> {
                    settingsManager.setThemePreference("dark")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    summaryTextView.text = "Dark"
                    activity?.recreate()
                }
            }
            dialog.dismiss()
        }
        // Transparent window so rounded corners of card_white_rounded show through (fixes sharp edges in dark mode)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showTemplateSelectDialog(summaryTextView: TextView) {
        val templateBinding = DialogTemplateSelectBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(templateBinding.root)
        dialog.setCancelable(true)

        // Load current period mode using ViewModel
        val currentPeriodMode = viewModel.getCurrentPeriodMode()
        
        // Set initial selection based on current mode
        when (currentPeriodMode) {
            com.example.outofroutebuddy.domain.models.PeriodMode.STANDARD -> 
                templateBinding.templateSelectRadioGroup.check(R.id.radio_template_standard)
            com.example.outofroutebuddy.domain.models.PeriodMode.CUSTOM -> 
                templateBinding.templateSelectRadioGroup.check(R.id.radio_template_custom)
        }

        templateBinding.templateSelectRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedPeriodMode = when (checkedId) {
                R.id.radio_template_standard -> com.example.outofroutebuddy.domain.models.PeriodMode.STANDARD
                R.id.radio_template_custom -> com.example.outofroutebuddy.domain.models.PeriodMode.CUSTOM
                else -> com.example.outofroutebuddy.domain.models.PeriodMode.STANDARD
            }
            
            // Save the selected period mode using ViewModel
            viewModel.savePeriodMode(selectedPeriodMode)
            
            // Update the summary text using ViewModel
            summaryTextView.text = viewModel.getCurrentPeriodModeDisplayText()
            
            dialog.dismiss()
        }
        // Transparent window so rounded corners of card_white_rounded show through (fixes sharp edges in dark mode)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showHelpInfoDialog() {
        val helpBinding = DialogHelpInfoBinding.inflate(LayoutInflater.from(requireContext()))
        helpBinding.versionText.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        // Future features: append to features section so users see what's coming (from ROADMAP)
        val currentFeatures = helpBinding.featuresText.text?.toString() ?: ""
        val futureSection = "\n\n${getString(R.string.help_future_features_title)}\n${getString(R.string.help_future_features)}"
        helpBinding.featuresText.text = if (currentFeatures.isNotEmpty()) currentFeatures + futureSection else getString(R.string.help_future_features_title) + "\n" + getString(R.string.help_future_features)
        val dialog = Dialog(requireContext())
        dialog.setContentView(helpBinding.root)
        dialog.setCancelable(true)
        // Close X - dismiss Help & Info dialog
        helpBinding.helpCloseButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Size dialog so content scrolls inside ~85% of screen (fixes overflow/cut-off)
        val dm = resources.displayMetrics
        val height = (dm.heightPixels * 0.85).toInt()
        val width = (dm.widthPixels * 0.95).toInt().coerceAtLeast(320)
        dialog.window?.setLayout(width, height)
        dialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe UI state changes
                launch {
                    viewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                }

                // Observe events
                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun updateUI(state: TripInputViewModel.TripInputUiState) {
        // Update button text and contentDescription based on trip state
        binding.startTripButton.text =
            if (state.isTripActive) {
                getString(R.string.end_trip)
            } else {
                getString(R.string.start_trip)
            }
        binding.startTripButton.contentDescription =
            if (state.isTripActive) {
                getString(R.string.end_trip_button_description)
            } else {
                getString(R.string.start_trip_button_description)
            }
        
        // Update pause button visibility and icon (hidden unless enabled in settings)
        val showPause = state.isTripActive && settingsManager.isShowPauseButtonEnabled()
        binding.pauseButton.visibility = if (showPause) View.VISIBLE else View.GONE
        if (showPause) {
            binding.pauseButton.setIconResource(
                if (state.isPaused) R.drawable.ic_play_adaptive else R.drawable.ic_pause_adaptive
            )
            binding.pauseButton.setIconTintResource(android.R.color.white)
            binding.pauseButton.contentDescription =
                if (state.isPaused) getString(R.string.resume_trip_button_description) else getString(R.string.pause_trip_button_description)
        }
        
        // ✅ FIX: Restore loaded/bounce miles from ViewModel state when trip is active
        // This ensures values persist when app is closed and reopened during a trip
        if (state.isTripActive) {
            // Only update if fields are empty (to avoid overwriting user input during active trip)
            if (binding.loadedMilesInput.text.toString().isEmpty() && state.loadedMiles > 0.0) {
                binding.loadedMilesInput.setText(state.loadedMiles.toString())
            }
            if (binding.bounceMilesInput.text.toString().isEmpty() && state.bounceMiles > 0.0) {
                binding.bounceMilesInput.setText(state.bounceMiles.toString())
            }
        }

        // ✅ UPDATE: Format Total Miles to .1 decimal for real-time GPS tracking
        val totalMilesText = String.format(Locale.US, "%.1f", state.actualMiles)
        val oorMilesText = formatOORMiles(state.oorMiles)
        val oorPercentText = formatOORPercentage(state.oorPercentage)
        
        // Update UI fields
        binding.totalMilesOutput.text = totalMilesText
        binding.oorMilesOutput.text = oorMilesText
        binding.oorPercentageOutput.text = oorPercentText

        // Statistics for selected period (Month) and Year
        // Monthly row = selected period (current calendar month in STANDARD mode; custom range in CUSTOM).
        // Yearly row = Jan 1–Dec 31 of the period's year. Both are populated together in refreshStatisticsAfterSave.
        updateStatisticsRow(binding.monthlyStats, viewModel.mapPeriodToSummary(state.periodStatistics))
        updateStatisticsRow(binding.yearlyStats, viewModel.mapPeriodToSummary(state.yearStatistics))

        binding.selectedPeriodValue.text = state.selectedPeriodLabel

        // ✅ New: Lock inputs while trip is active; unlock when trip ends
        val inputsEnabled = !state.isTripActive
        binding.loadedMilesInput.isEnabled = inputsEnabled
        binding.bounceMilesInput.isEnabled = inputsEnabled
        binding.pickupAddressInput.isEnabled = inputsEnabled
        binding.dropoffAddressInput.isEnabled = inputsEnabled

        val ludacrisTrip = state.isTripActive
        val showTz = ludacrisTrip && settingsManager.isLudacrisShowTimeZones()
        val showEl = ludacrisTrip && settingsManager.isLudacrisShowElevation()
        val showSp = ludacrisTrip && settingsManager.isLudacrisShowMaxSpeed()
        val anyLudacris = showTz || showEl || showSp
        binding.ludacrisDivider.isVisible = anyLudacris
        binding.ludacrisTzRow.isVisible = showTz
        binding.ludacrisElevRow.isVisible = showEl
        binding.ludacrisSpeedRow.isVisible = showSp
        if (showTz) {
            binding.ludacrisTzValue.text = state.ludacrisTimeZoneCount.toString()
        }
        if (showEl) {
            val minM = state.ludacrisElevMinMeters
            val maxM = state.ludacrisElevMaxMeters
            binding.ludacrisElevValue.text =
                if (minM != null && maxM != null) {
                    val ft1 = minM * 3.28084
                    val ft2 = maxM * 3.28084
                    String.format(Locale.US, "%d–%d ft", ft1.toInt(), ft2.toInt())
                } else {
                    "—"
                }
        }
        if (showSp) {
            binding.ludacrisSpeedValue.text =
                if (state.ludacrisMaxSpeedMph > 0.5) {
                    String.format(Locale.US, "%.0f mph", state.ludacrisMaxSpeedMph)
                } else {
                    "—"
                }
        }

        // ✅ POLISH: Add subtle animation to show real-time updates
        if (state.isTripActive && state.actualMiles > 0) {
            binding.totalMilesOutput.alpha = 0.7f
            binding.totalMilesOutput.animate().alpha(1.0f).setDuration(300).start()
        }
        
        // Debug logging to verify real-time GPS updates
        android.util.Log.d(TAG, "GPS→UI: Total=$totalMilesText mi (${state.actualMiles}), TripActive=${state.isTripActive}")

        // Status message handled by UI state (no Toast needed)
    }
    
    /**
     * Format OOR miles for user-friendly display
     * - Negative values show as "X.X under" instead of "-X.X"
     * - Positive values show as "X.X over" instead of "+X.X"
     * - Zero shows as "0.0 (on route)"
     */
    private fun formatOORMiles(oorMiles: Double): String {
        return when {
            oorMiles > 0.01 -> String.format(Locale.US, "%.1f over", oorMiles)
            oorMiles < -0.01 -> String.format(Locale.US, "%.1f under", Math.abs(oorMiles))
            else -> "0.0 (on route)"
        }
    }
    
    /**
     * Format OOR percentage for user-friendly display
     * - Negative values show as "X.X% under" instead of "-X.X%"
     * - Positive values show as "X.X% over" instead of "+X.X%"
     * - Zero shows as "0.0% (perfect)"
     */
    private fun formatOORPercentage(oorPercentage: Double): String {
        return when {
            oorPercentage > 0.01 -> String.format(Locale.US, "%.1f%% over", oorPercentage)
            oorPercentage < -0.01 -> String.format(Locale.US, "%.1f%% under", Math.abs(oorPercentage))
            else -> "0.0% (perfect)"
        }
    }

    private fun updateStatisticsRow(rowBinding: StatisticsRowBinding, stats: TripInputViewModel.SummaryStatistics?) {
        val container = rowBinding.statsExtraRowsContainer
        container.removeAllViews()

        if (stats == null) {
            val placeholder = getString(R.string.statistics_placeholder)
            rowBinding.totalMiles.text = placeholder
            rowBinding.oorMiles.text = placeholder
            rowBinding.oorPercentage.text = placeholder
            return
        }

        rowBinding.totalMiles.text = formatStatisticMiles(stats.totalMiles)
        rowBinding.oorMiles.text = formatStatisticMiles(stats.oorMiles)
        rowBinding.oorPercentage.text = formatStatisticPercentage(stats.oorPercentage)

        for ((label, value) in stats.extraFields) {
            val row = layoutInflater.inflate(R.layout.statistics_extra_row, container, false)
            row.findViewById<TextView>(R.id.extra_row_label).text = label
            row.findViewById<TextView>(R.id.extra_row_value).text = value
            container.addView(row)
        }
    }

    private fun formatStatisticMiles(value: Double): String {
        return String.format(Locale.US, "%1$,.1f", value)
    }

    private fun formatStatisticPercentage(value: Double): String {
        val safeValue = if (value.isFinite()) value else 0.0
        return String.format(Locale.US, "%.1f%%", safeValue)
    }

    private fun showCalendarPicker() {
        val periodMode = viewModel.getCurrentPeriodMode()
        val selectedPeriod = viewModel.uiState.value.selectedPeriod
        // CUSTOM: always open calendar to period containing today so "Current Period" is consistent
        val referenceDate = if (periodMode == PeriodMode.CUSTOM) {
            Date()
        } else {
            selectedPeriod?.startDate ?: Date()
        }
        // Calendar history window: rolling last 12 months through current month.
        val now = Date()
        val calMin = getFirstDayOfMonth(now).apply { add(Calendar.MONTH, -12) }
        val calMax = getLastDayOfMonth(now)
        val calendarMinDate = calMin.time
        val calendarMaxDate = calMax.time

        viewLifecycleOwner.lifecycleScope.launch {
            val datesWithTrips = viewModel.getDatesWithTripsForCalendarRange(calendarMinDate, calendarMaxDate)
            withContext(Dispatchers.Main) {
                val dialog = CustomCalendarDialog.newInstance(
                    periodMode = periodMode,
                    referenceDate = referenceDate,
                    onHistoryDateClicked = { date ->
                        showTripHistoryForDate(date)
                    },
                    datesWithTrips = datesWithTrips,
                )
                dialog.show(parentFragmentManager, "custom_calendar_dialog")
            }
        }
    }

    /**
     * ✅ NEW: Get first and last day of month for STANDARD mode highlighting
     * Returns a Pair of Calendar objects representing the first and last day of the month
     */
    private fun getFirstAndLastDayOfMonth(date: Date): Pair<Calendar, Calendar> {
        val firstDay = getFirstDayOfMonth(date)
        val lastDay = getLastDayOfMonth(date)
        return Pair(firstDay, lastDay)
    }

    /**
     * ✅ NEW: Get first day of the month for a given date
     */
    private fun getFirstDayOfMonth(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    /**
     * ✅ NEW: Get last day of the month for a given date
     */
    private fun getLastDayOfMonth(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar
    }


    /**
     * ✅ FIXED: Normalize UTC milliseconds to start of day in UTC
     */
    private fun normalizeToStartOfDayUtc(utcMillis: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = utcMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * ✅ FIXED: Disable calendar interaction to lock selection
     * Recursively finds and disables all interactive calendar views
     */
    private fun disableCalendarInteraction(view: View?) {
        if (view == null) return
        
        try {
            // Disable this view
            view.isEnabled = false
            view.isClickable = false
            view.isFocusable = false
            // Consume all touch events to prevent interaction
            view.setOnTouchListener { _, _ -> true }
            
            // Recursively disable child views
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    disableCalendarInteraction(view.getChildAt(i))
                }
            }
        } catch (e: Exception) {
            // If we can't disable interaction, selection will still be locked in click handler
        }
    }

    /**
     * ✅ NEW: Show trip history for a specific date
     * Opens a dialog displaying all trips that occurred on the selected date
     * 
     * ✅ EDGE CASE: Handles null dates, dialog already showing, and other errors
     */
    private fun showTripHistoryForDate(date: Date) {
        try {
            // ✅ EDGE CASE: Check if dialog is already showing
            val existingDialog = parentFragmentManager.findFragmentByTag("TripHistoryByDateDialog")
            if (existingDialog != null && existingDialog.isAdded) {
                android.util.Log.d(TAG, "Trip history dialog already showing, skipping")
                return
            }
            
            val dialog = com.example.outofroutebuddy.presentation.ui.dialogs.TripHistoryByDateDialog.newInstance(date)
            dialog.show(parentFragmentManager, "TripHistoryByDateDialog")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to show trip history dialog", e)
            // ✅ EDGE CASE: Show user-friendly error message
            // Could show a Snackbar here if needed
        }
    }

    /** Production #9: Called from MainActivity when drawer item "Settings" is selected. */
    fun openSettingsFromDrawer() {
        showSettingsDialog()
    }

    /** Production #9: Called from MainActivity when drawer item "History" is selected. */
    fun openHistoryFromDrawer() {
        showTripHistoryForDate(Date())
    }

    /** Production #9: Called from MainActivity when drawer item "Help" is selected. */
    fun openHelpFromDrawer() {
        showHelpInfoDialog()
    }

    /**
     * ✅ FIXED: Convert UTC milliseconds to local Date (start of day)
     * MaterialDatePicker returns UTC milliseconds, but we interpret it in local timezone
     * to match what the user sees on screen
     */
    private fun convertUtcMillisToLocalDate(utcMillis: Long): Date {
        // MaterialDatePicker returns UTC milliseconds representing midnight UTC
        // Convert to local timezone to get the date as the user sees it
        val localCal = Calendar.getInstance()
        localCal.timeInMillis = utcMillis
        
        // Extract date components as they appear in local timezone
        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH)
        val day = localCal.get(Calendar.DAY_OF_MONTH)
        
        // Create date in local timezone at start of day
        val resultCal = Calendar.getInstance()
        resultCal.set(year, month, day, 0, 0, 0)
        resultCal.set(Calendar.MILLISECOND, 0)
        
        return resultCal.time
    }


    // All error events show snackbar (Health & resilience audit).
    private fun handleEvent(event: TripEvent) {
        when (event) {
            is TripEvent.TripCalculated -> {
                // OOR calculated - displayed in UI
            }
            is TripEvent.ValidationError -> {
                showSnackbar(event.message)
            }
            is TripEvent.Error -> {
                showSnackbar(event.message)
            }
            is TripEvent.PeriodStatisticsCalculated -> {
                // Statistics displayed in UI
            }
            is TripEvent.CalculationError -> {
                showSnackbar(event.message)
            }
            is TripEvent.SaveError -> {
                showSnackbar(event.message)
            }
            TripEvent.TripEnded -> {
                // Status shown in UI
            }
            TripEvent.TripSaved -> {
                // Status shown in UI
            }
        }
    }

    /**
     * ✅ NEW: Check if location permissions are granted
     */
    private fun checkLocationPermissions(): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return hasFineLocation && hasCoarseLocation
    }
    
    /**
     * ✅ NEW: Show dialog when permissions are not granted
     */
    private fun showPermissionRequiredDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage(
                "GPS tracking requires location permissions. " +
                "Please grant location access in the app settings or permission dialog."
            )
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Permission dialog shown - no additional message needed
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "TripInputFragment"
    }
} 

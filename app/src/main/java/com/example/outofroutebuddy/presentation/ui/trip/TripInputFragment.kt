package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.outofroutebuddy.MainActivity
import com.example.outofroutebuddy.R
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
import com.example.outofroutebuddy.services.PeriodCalculationService
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    private val viewModel: TripInputViewModel by viewModels()

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
    
    /**
     * ✅ FIX: Save text input values to SharedPreferences
     */
    private fun saveTextInputsToPrefs() {
        try {
            val prefs = requireContext().getSharedPreferences("trip_input_state", Context.MODE_PRIVATE)
            prefs.edit {
                    putString("loaded_miles_input", binding.loadedMilesInput.text.toString())
                    .putString("bounce_miles_input", binding.bounceMilesInput.text.toString())
                }
        } catch (e: Exception) {
            android.util.Log.e("TripInputFragment", "Error saving text inputs", e)
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
        } catch (e: Exception) {
            android.util.Log.e("TripInputFragment", "Error restoring text inputs", e)
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
                clearSavedTextInputs()
            }
        } catch (e: Exception) {
            android.util.Log.e("TripInputFragment", "Error clearing text inputs", e)
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
                }
        } catch (e: Exception) {
            android.util.Log.e("TripInputFragment", "Error clearing saved text inputs", e)
        }
    }

    private fun setupUI() {
        // Initialize UI components
        binding.startTripButton.text = getString(R.string.start_trip)
        binding.progressBar.visibility = View.GONE

        // Toolbar background: set in custom_toolbar.xml (toolbar_background_cracked_road)
        // Ensure statistics content starts collapsed
        binding.statisticsContent.visibility = View.GONE
        binding.statisticsButton.setIconResource(R.drawable.ic_arrow_down)
    }

    private fun setupClickListeners() {
        binding.startTripButton.setOnClickListener {
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
                
                viewModel.calculateTrip(loadedMiles, bounceMiles, actualMiles)
                // Clear saved input values after trip starts
                clearSavedTextInputs()
            }
        }

        // Statistics button click listener
        binding.statisticsButton.setOnClickListener {
            val isVisible = binding.statisticsContent.isVisible
            binding.statisticsContent.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Update button icon - when collapsed show down arrow (to expand), when expanded show up arrow (to collapse)
            val icon = if (isVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            binding.statisticsButton.setIconResource(icon)
        }

        binding.statisticsCalendarButton.setOnClickListener {
            showCalendarPicker()
        }

        // Settings button click listener
        binding.customToolbarLayout.settingsButton.setOnClickListener {
            showSettingsDialog()
        }
        
        // Pause button click listener
        binding.pauseButton.setOnClickListener {
            val isPaused = viewModel.uiState.value.isPaused
            if (!isPaused) {
                // Currently active, so pause
                viewModel.pauseTrip()
                binding.pauseButton.setIconResource(R.drawable.ic_play)
                binding.pauseButton.contentDescription = "Resume trip tracking"
                showSnackbar("Trip paused")
            } else {
                // Currently paused, so resume
                viewModel.resumeTrip()
                binding.pauseButton.setIconResource(R.drawable.ic_pause)
                binding.pauseButton.contentDescription = "Pause trip tracking"
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
        }
        dialogView.addView(messageText)
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("End Trip?")
            .setView(dialogView)
            .create()
        
        val buttonLayoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8, 0, 8)
        }
        
        // Button 1: End Trip (top)
        val endTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "End Trip"
            layoutParams = buttonLayoutParams
            setOnClickListener {
                dialog.dismiss()
                viewModel.endTrip()
                clearSavedTextInputs()
                binding.loadedMilesInput.setText("")
                binding.bounceMilesInput.setText("")
            }
        }
        dialogView.addView(endTripButton)
        
        // Button 2: Clear Trip (middle)
        val clearTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Clear Trip"
            layoutParams = buttonLayoutParams
            setOnClickListener {
                dialog.dismiss()
                showClearTripConfirmation()
            }
        }
        dialogView.addView(clearTripButton)
        
        // Button 3: Continue Trip (bottom)
        val continueTripButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Continue Trip"
            layoutParams = buttonLayoutParams
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.addView(continueTripButton)
        
        dialog.show()
    }
    
    /**
     * ✅ NEW: Show confirmation dialog for clearing trip
     */
    private fun showClearTripConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
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
            .show()
    }

    /**
     * ✅ SIMPLIFIED: Show dialog for trip calculation
     */
    private fun showTripCalculationDialog() {
        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Calculate Trip")
                .setMessage("Calculate OOR miles for this trip?")
                .setPositiveButton("Calculate") { _, _ ->
                    val loadedMiles = binding.loadedMilesInput.text.toString().toDoubleOrNull() ?: 0.0
                    val bounceMiles = binding.bounceMilesInput.text.toString().toDoubleOrNull() ?: 0.0
                    val actualMiles = viewModel.uiState.value.actualMiles // Use current GPS distance
                    
                    viewModel.calculateTrip(loadedMiles, bounceMiles, actualMiles)
                }
                .setNegativeButton("Cancel", null)
                .create()

        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)

        // Load theme from persisted preference so summary matches after restart
        val themePreference = settingsManager.getThemePreference()
        dialogBinding.modeSummary.text = when (themePreference) {
            "dark" -> "Dark"
            "light" -> "Light"
            else -> "System"
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

        modeBinding.modeSelectRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
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
        dialog.show()
    }

    private fun showHelpInfoDialog() {
        val helpBinding = DialogHelpInfoBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(helpBinding.root)
        dialog.setCancelable(true)
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
        // Update button text based on trip state
        binding.startTripButton.text =
            if (state.isTripActive) {
                getString(R.string.end_trip)
            } else {
                getString(R.string.start_trip)
            }
        
        // Update pause button visibility and icon
        binding.pauseButton.visibility = if (state.isTripActive) View.VISIBLE else View.GONE
        if (state.isTripActive) {
            binding.pauseButton.setIconResource(
                if (state.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            )
            binding.pauseButton.contentDescription = 
                if (state.isPaused) "Resume trip tracking" else "Pause trip tracking"
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

        // Monthly statistics only (weekly/yearly sections removed)
        updateStatisticsRow(binding.monthlyStats, state.monthlyStatistics)

        binding.selectedPeriodValue.text = state.selectedPeriodLabel

        // Calendar/current period: show days with saved trips (clickable)
        val datesWithTrips = state.datesWithTripsInPeriod
        if (datesWithTrips.isEmpty()) {
            binding.daysWithTripsLabel.visibility = View.GONE
            binding.daysWithTripsContainerWrapper.visibility = View.GONE
            binding.daysWithTripsContainer.removeAllViews()
        } else {
            binding.daysWithTripsLabel.visibility = View.VISIBLE
            binding.daysWithTripsContainerWrapper.visibility = View.VISIBLE
            binding.daysWithTripsContainer.removeAllViews()
            val dateFormat = java.text.SimpleDateFormat("MMM d", Locale.getDefault())
            val dp8 = (8 * resources.displayMetrics.density).toInt()
            val dp12 = (12 * resources.displayMetrics.density).toInt()
            for (date in datesWithTrips) {
                val chip = com.google.android.material.button.MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = dateFormat.format(date)
                    setOnClickListener { showTripHistoryForDate(date) }
                    setPadding(dp12, dp8, dp12, dp8)
                    minimumWidth = 0
                    minWidth = 0
                }
                val params = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp8 }
                binding.daysWithTripsContainer.addView(chip, params)
            }
        }
        
        // ✅ New: Lock inputs while trip is active; unlock when trip ends
        val inputsEnabled = !state.isTripActive
        binding.loadedMilesInput.isEnabled = inputsEnabled
        binding.bounceMilesInput.isEnabled = inputsEnabled

        // ✅ POLISH: Add subtle animation to show real-time updates
        if (state.isTripActive && state.actualMiles > 0) {
            binding.totalMilesOutput.alpha = 0.7f
            binding.totalMilesOutput.animate().alpha(1.0f).setDuration(300).start()
        }
        
        // Debug logging to verify real-time GPS updates
        android.util.Log.d("TripInputFragment", "GPS→UI: Total=$totalMilesText mi (${state.actualMiles}), TripActive=${state.isTripActive}")

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
        val referenceDate = selectedPeriod?.startDate ?: Date()
        
        // ✅ NEW: Use CustomCalendarDialog; pass dates with saved trips so calendar can mark them
        val datesWithTrips = viewModel.uiState.value.datesWithTripsInPeriod
        val dialog = CustomCalendarDialog.newInstance(
            periodMode = periodMode,
            referenceDate = referenceDate,
            onPeriodConfirmed = { startDate, endDate ->
                viewModel.onCalendarPeriodSelected(periodMode, startDate, endDate)
            },
            onHistoryDateClicked = { date ->
                showTripHistoryForDate(date)
            },
            datesWithTrips = datesWithTrips
        )
        dialog.show(parentFragmentManager, "custom_calendar_dialog")
    }

    private fun showSingleDatePicker(selectedPeriod: TripInputViewModel.SelectedPeriod?) {
        val initialSelection = selectedPeriod?.startDate?.time ?: MaterialDatePicker.todayInUtcMilliseconds()
        val initialDate = Date(initialSelection)
        
        // ✅ FIXED: Calculate first and last day of month for STANDARD mode highlighting
        val firstDayOfMonth = getFirstDayOfMonth(initialDate)
        val lastDayOfMonth = getLastDayOfMonth(initialDate)
        
        // ✅ FIXED: Use Calendar.timeInMillis directly - it already returns UTC milliseconds
        val firstDayUtcMillis = firstDayOfMonth.timeInMillis
        val lastDayUtcMillis = lastDayOfMonth.timeInMillis
        
        // ✅ FIXED: For STANDARD mode, use range picker to show both first and last day highlighted with circles
        // Pre-selecting the range will show both boundary dates with circular highlighting
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.statistics_select_day))
        
        // ✅ NEW: Create validator that allows all dates to be clickable (for history viewing)
        // But we'll intercept selection changes to handle history clicks vs boundary selection
        val allowAllDatesValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                // Allow all dates in the month range - we'll handle boundary logic in selection listener
                val dateAtStartOfDay = normalizeToStartOfDayUtc(date)
                return dateAtStartOfDay >= firstDayUtcMillis && dateAtStartOfDay <= lastDayUtcMillis
            }
            override fun describeContents(): Int = 0
            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
        }
        
        // ✅ FIXED: Create calendar constraints - allow all dates to be clickable (no grayed out dates)
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(firstDayUtcMillis)
            .setEnd(lastDayUtcMillis)
            .setValidator(allowAllDatesValidator) // Allow all dates for clicking
        
        builder.setCalendarConstraints(constraintsBuilder.build())
        
        // ✅ FIXED: Pre-select first to last day range to highlight both boundaries with circles
        builder.setSelection(Pair(firstDayUtcMillis, lastDayUtcMillis))
        
        val picker = builder.build()


        picker.addOnPositiveButtonClickListener { range ->
            val startMillis = range.first
            val endMillis = range.second
            if (startMillis != null && endMillis != null) {
                // ✅ FIXED: Selection is locked to boundaries, so always use first and last day
                val startDate = convertUtcMillisToLocalDate(firstDayUtcMillis)
                val endDate = convertUtcMillisToLocalDate(lastDayUtcMillis)
                // For STANDARD mode, use first day as the selected date
                viewModel.onCalendarPeriodSelected(PeriodMode.STANDARD, startDate, startDate)
            }
        }

        picker.show(parentFragmentManager, "trip_period_day_picker")
    }

    private fun showRangeDatePicker(selectedPeriod: TripInputViewModel.SelectedPeriod?) {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("${getString(R.string.statistics_select_range)}\n${getString(R.string.calendar_click_hint)}")

        // ✅ FIXED: Calculate custom period boundaries for highlighting
        val referenceDate = selectedPeriod?.startDate ?: Date()
        val customPeriodStart = periodCalculationService.calculateCustomPeriodStart(referenceDate)
        val customPeriodEnd = periodCalculationService.calculateCustomPeriodEnd(referenceDate)
        
        // ✅ FIXED: Normalize times to start of day in local timezone
        customPeriodStart.set(Calendar.HOUR_OF_DAY, 0)
        customPeriodStart.set(Calendar.MINUTE, 0)
        customPeriodStart.set(Calendar.SECOND, 0)
        customPeriodStart.set(Calendar.MILLISECOND, 0)
        
        customPeriodEnd.set(Calendar.HOUR_OF_DAY, 0)
        customPeriodEnd.set(Calendar.MINUTE, 0)
        customPeriodEnd.set(Calendar.SECOND, 0)
        customPeriodEnd.set(Calendar.MILLISECOND, 0)
        
        // ✅ FIXED: Use Calendar.timeInMillis directly - it already returns UTC milliseconds
        val periodStartUtcMillis = customPeriodStart.timeInMillis
        val periodEndUtcMillis = customPeriodEnd.timeInMillis
        
        // ✅ FIXED: Create validators to ONLY allow period start and end date selection (locked/unmoveable)
        // This locks the selection but will gray out other dates (required for locking)
        val periodStartValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                val dateAtStartOfDay = normalizeToStartOfDayUtc(date)
                return dateAtStartOfDay == periodStartUtcMillis
            }
            override fun describeContents(): Int = 0
            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
        }
        
        val periodEndValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                val dateAtStartOfDay = normalizeToStartOfDayUtc(date)
                return dateAtStartOfDay == periodEndUtcMillis
            }
            override fun describeContents(): Int = 0
            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
        }
        
        // Combine validators - only period start OR end can be selected
        val validators = listOf(periodStartValidator, periodEndValidator)
        val combinedValidator = CompositeDateValidator.anyOf(validators)
        
        // ✅ FIXED: Create calendar constraints with validators to lock selection to period boundaries only
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(periodStartUtcMillis)
            .setEnd(periodEndUtcMillis)
            .setValidator(combinedValidator) // Only allow period start or end date selection (locked)
        
        builder.setCalendarConstraints(constraintsBuilder.build())

        // ✅ FIXED: Pre-select the period boundaries to highlight them with circles (green for start, red for end)
        builder.setSelection(Pair(periodStartUtcMillis, periodEndUtcMillis))

        val picker = builder.build()

        // ✅ NEW: Intercept selection changes to handle history clicks vs boundary selection
        var lastSelection: Pair<Long, Long>? = null
        
        // Monitor selection changes after picker is shown
        picker.view?.postDelayed({
            try {
                // Use reflection to access the selection field and monitor changes
                val selectionField = picker.javaClass.getDeclaredField("selection")
                selectionField.isAccessible = true
                
                // Check selection periodically to detect changes
                val selectionChecker = object : Runnable {
                    override fun run() {
                        try {
                            val currentSelection = selectionField.get(picker) as? Pair<Long, Long>
                            if (currentSelection != null && currentSelection != lastSelection) {
                                val startMillis = currentSelection.first
                                val endMillis = currentSelection.second
                                
                                if (startMillis != null && endMillis != null) {
                                    val startAtStartOfDay = normalizeToStartOfDayUtc(startMillis)
                                    val endAtStartOfDay = normalizeToStartOfDayUtc(endMillis)
                                    
                                    // Check if clicked date is a boundary date
                                    val isPeriodStart = startAtStartOfDay == periodStartUtcMillis
                                    val isPeriodEnd = endAtStartOfDay == periodEndUtcMillis
                                    
                                    if (!isPeriodStart && !isPeriodEnd) {
                                        // User clicked a non-boundary date - open history
                                        val clickedDate = convertUtcMillisToLocalDate(startMillis)
                                        showTripHistoryForDate(clickedDate)
                                        
                                        // Reset selection to boundaries
                                        selectionField.set(picker, Pair(periodStartUtcMillis, periodEndUtcMillis))
                                    }
                                    
                                    lastSelection = currentSelection
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore reflection errors
                        }
                        
                        // Continue checking if picker is still showing
                        if (picker.isAdded && picker.dialog?.isShowing == true) {
                            picker.view?.postDelayed(this, 100)
                        }
                    }
                }
                picker.view?.post(selectionChecker)
            } catch (e: Exception) {
                // If reflection fails, fall back to standard behavior
            }
        }, 200)

        picker.addOnPositiveButtonClickListener { range ->
            val startMillis = range.first
            val endMillis = range.second
            if (startMillis != null && endMillis != null) {
                // ✅ FIXED: Selection is locked to boundaries, so always use period start and end
                val startDate = convertUtcMillisToLocalDate(periodStartUtcMillis)
                val endDate = convertUtcMillisToLocalDate(periodEndUtcMillis)
                viewModel.onCalendarPeriodSelected(PeriodMode.CUSTOM, startDate, endDate)
            }
        }

        picker.show(parentFragmentManager, "trip_period_range_picker")
    }

    private fun startOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun endOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
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
     * ✅ NEW: Show a date picker for selecting which date to view history
     * This allows users to view history for any date while keeping the period selection locked
     */
    private fun showHistoryDatePickerForCurrentPeriod() {
        val periodMode = viewModel.getCurrentPeriodMode()
        val selectedPeriod = viewModel.uiState.value.selectedPeriod
        val referenceDate = selectedPeriod?.startDate ?: Date()
        
        val (startUtcMillis, endUtcMillis) = when (periodMode) {
            PeriodMode.STANDARD -> {
                val initialDate = referenceDate
                val firstDayOfMonth = getFirstDayOfMonth(initialDate)
                val lastDayOfMonth = getLastDayOfMonth(initialDate)
                firstDayOfMonth.timeInMillis to lastDayOfMonth.timeInMillis
            }
            PeriodMode.CUSTOM -> {
                val customPeriodStart = periodCalculationService.calculateCustomPeriodStart(referenceDate)
                val customPeriodEnd = periodCalculationService.calculateCustomPeriodEnd(referenceDate)
                customPeriodStart.set(Calendar.HOUR_OF_DAY, 0)
                customPeriodStart.set(Calendar.MINUTE, 0)
                customPeriodStart.set(Calendar.SECOND, 0)
                customPeriodStart.set(Calendar.MILLISECOND, 0)
                customPeriodEnd.set(Calendar.HOUR_OF_DAY, 0)
                customPeriodEnd.set(Calendar.MINUTE, 0)
                customPeriodEnd.set(Calendar.SECOND, 0)
                customPeriodEnd.set(Calendar.MILLISECOND, 0)
                customPeriodStart.timeInMillis to customPeriodEnd.timeInMillis
            }
        }
        
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.history_date_picker_title))
        
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(startUtcMillis)
            .setEnd(endUtcMillis)
        
        builder.setCalendarConstraints(constraintsBuilder.build())
        
        val picker = builder.build()
        
        picker.addOnPositiveButtonClickListener { selectedDate ->
            if (selectedDate != null) {
                val date = convertUtcMillisToLocalDate(selectedDate)
                showTripHistoryForDate(date)
            }
        }
        
        picker.show(parentFragmentManager, "history_date_picker")
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
                android.util.Log.d("TripInputFragment", "Trip history dialog already showing, skipping")
                return
            }
            
            val dialog = com.example.outofroutebuddy.presentation.ui.dialogs.TripHistoryByDateDialog.newInstance(date)
            dialog.show(parentFragmentManager, "TripHistoryByDateDialog")
        } catch (e: Exception) {
            android.util.Log.e("TripInputFragment", "Failed to show trip history dialog", e)
            // ✅ EDGE CASE: Show user-friendly error message
            // Could show a Snackbar here if needed
        }
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


    private fun handleEvent(event: TripEvent) {
        // Events are handled by UI state updates
        // Toast messages removed - will add Snackbar later if needed
        when (event) {
            is TripEvent.TripCalculated -> {
                // OOR calculated - displayed in UI
            }
            is TripEvent.ValidationError -> {
                // Error shown in UI state
            }
            is TripEvent.Error -> {
                // Error shown in UI state
            }
            is TripEvent.PeriodStatisticsCalculated -> {
                // Statistics displayed in UI
            }
            is TripEvent.CalculationError -> {
                // Error shown in UI state
            }
            is TripEvent.SaveError -> {
                // Error shown in UI state
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
} 

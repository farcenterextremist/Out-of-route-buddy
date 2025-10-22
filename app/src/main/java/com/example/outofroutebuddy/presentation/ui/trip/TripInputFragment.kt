package com.example.outofroutebuddy.presentation.ui.trip

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel
import com.example.outofroutebuddy.presentation.viewmodel.TripInputViewModel.TripEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

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
        setupUI()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupUI() {
        // Initialize UI components
        binding.startTripButton.text = getString(R.string.start_trip)
        binding.progressBar.visibility = View.GONE

        // Ensure statistics content starts collapsed
        binding.statisticsContent.visibility = View.GONE
        binding.statisticsButton.setIconResource(R.drawable.ic_arrow_down)
    }

    private fun setupClickListeners() {
        binding.startTripButton.setOnClickListener {
            // ✅ FIX: Toggle between Start Trip and End Trip based on current state
            if (viewModel.uiState.value.isTripActive) {
                // Trip is active, so END it
                viewModel.endTrip()
            } else {
                // ✅ NEW: Check permissions before starting trip
                if (!checkLocationPermissions()) {
                    showPermissionRequiredDialog()
                    return@setOnClickListener
                }
                
                // Trip is not active, so START it
                val loadedMiles = binding.loadedMilesInput.text.toString().toDoubleOrNull() ?: 0.0
                val bounceMiles = binding.bounceMilesInput.text.toString().toDoubleOrNull() ?: 0.0
                val actualMiles = 0.0 // Always start at 0, GPS will update
                
                viewModel.calculateTrip(loadedMiles, bounceMiles, actualMiles)
            }
        }

        // Statistics button click listener
        binding.statisticsButton.setOnClickListener {
            val isVisible = binding.statisticsContent.visibility == View.VISIBLE
            binding.statisticsContent.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Update button icon - when collapsed show down arrow (to expand), when expanded show up arrow (to collapse)
            val icon = if (isVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            binding.statisticsButton.setIconResource(icon)
        }

        // Settings button click listener
        binding.customToolbarLayout.settingsButton.setOnClickListener {
            showSettingsDialog()
        }
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

        // Load preferences using ViewModel
        val isDarkMode =
            when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> true
                AppCompatDelegate.MODE_NIGHT_NO -> false
                else -> false
            }

        // Set initial summary text
        dialogBinding.modeSummary.text = if (isDarkMode) "Dark" else "Light"
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
        dialog.show()
    }

    private fun showModeSelectDialog(summaryTextView: TextView) {
        val modeBinding = DialogModeSelectBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(modeBinding.root)
        dialog.setCancelable(true)

        // Set initial selection
        val isDarkMode =
            when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> true
                AppCompatDelegate.MODE_NIGHT_NO -> false
                else -> false
            }
        modeBinding.modeSelectRadioGroup.check(if (isDarkMode) R.id.radio_mode_dark else R.id.radio_mode_light)

        modeBinding.modeSelectRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_mode_light -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    summaryTextView.text = "Light"
                }
                R.id.radio_mode_dark -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    summaryTextView.text = "Dark"
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

        // ✅ UPDATE: Format Total Miles to .1 decimal for real-time GPS tracking
        val totalMilesText = String.format(Locale.US, "%.1f", state.actualMiles)
        val oorMilesText = formatOORMiles(state.oorMiles)
        val oorPercentText = formatOORPercentage(state.oorPercentage)
        
        // Update UI fields
        binding.totalMilesOutput.text = totalMilesText
        binding.oorMilesOutput.text = oorMilesText
        binding.oorPercentageOutput.text = oorPercentText
        
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

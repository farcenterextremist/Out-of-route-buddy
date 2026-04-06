package com.example.outofroutebuddy.presentation.ui.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.databinding.DialogTripHistoryByDateBinding
import com.google.android.material.snackbar.Snackbar
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.presentation.ui.history.TripHistoryStatCardAdapter
import com.example.outofroutebuddy.presentation.ui.history.TripHistoryByDateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ NEW: Trip History By Date Dialog
 *
 * Displays trip history for a specific date selected from the calendar.
 * Data flow: Calendar date click → onHistoryDateClicked → showTripHistoryForDate(date)
 * → TripHistoryByDateDialog.newInstance(selectedDate) → viewModel.loadTripsForDate(selectedDate)
 * → repository.getTripsOverlappingDay(startOfDay, endOfDay) → miles scaled to that day’s time overlap → adapter.submitList(trips).
 * Midnight-spanning trips appear on both days; each day shows the time-proportional share of miles.
 */
@AndroidEntryPoint
class TripHistoryByDateDialog : DialogFragment() {
    
    companion object {
        private const val TAG = "TripHistoryByDateDialog"
        private const val ARG_SELECTED_DATE = "selected_date"
        /** Request key used when a trip was deleted so the calendar can refresh its dots. */
        const val REQUEST_KEY_DAY_TRIP_DELETED = "trip_history_day_trip_deleted"
        
        fun newInstance(selectedDate: Date): TripHistoryByDateDialog {
            val dialog = TripHistoryByDateDialog()
            val args = Bundle()
            args.putSerializable(ARG_SELECTED_DATE, selectedDate)
            dialog.arguments = args
            return dialog
        }
    }
    
    private var _binding: DialogTripHistoryByDateBinding? = null
    private val binding: DialogTripHistoryByDateBinding
        get() = _binding ?: throw IllegalStateException("Binding accessed before onCreateView or after onDestroyView")
    
    private val viewModel: TripHistoryByDateViewModel by viewModels()
    private lateinit var adapter: TripHistoryStatCardAdapter
    private lateinit var selectedDate: Date
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            selectedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getSerializable(ARG_SELECTED_DATE, java.util.Date::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                args.getSerializable(ARG_SELECTED_DATE) as java.util.Date
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTripHistoryByDateBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupRecyclerView()
        observeTrips()
        observeDeleteError()
        observeDeleteSuccess()
        observeLoadError()

        // Load trips for the selected date
        viewModel.loadTripsForDate(selectedDate)
    }

    override fun onResume() {
        super.onResume()
        // Refresh when dialog becomes visible (e.g. after saving a trip elsewhere)
        viewModel.loadTripsForDate(selectedDate)
    }

    private val titleDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)

    private fun setupViews() {
        updateDateTitle()

        binding.prevDayButton.setOnClickListener { navigateToDay(-1) }
        binding.nextDayButton.setOnClickListener { navigateToDay(+1) }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateDateTitle() {
        try {
            binding.dateTitleText.text = titleDateFormat.format(selectedDate)
        } catch (e: Exception) {
            binding.dateTitleText.text = "Selected date"
            android.util.Log.e(TAG, "Error formatting date", e)
        }
    }

    private fun navigateToDay(offset: Int) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        cal.add(Calendar.DAY_OF_MONTH, offset)
        selectedDate = cal.time
        updateDateTitle()
        adapter.viewingDate = selectedDate
        viewModel.loadTripsForDate(selectedDate)
    }
    
    private fun setupRecyclerView() {
        adapter = TripHistoryStatCardAdapter(
            onDeleteClick = { trip -> showDeleteConfirmation(trip) },
            onTripClick = { trip ->
                dismiss()
                val navController = (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.navController
                navController?.navigate(
                    R.id.tripDetailsFragment,
                    Bundle().apply { putString("tripId", trip.id) }
                )
            },
            viewingDate = selectedDate,
        )
        
        binding.tripListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripHistoryByDateDialog.adapter
        }
    }

    
    private fun observeDeleteError() {
        lifecycleScope.launch {
            viewModel.deleteError.collect { message ->
                view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
            }
        }
    }

    private fun observeDeleteSuccess() {
        lifecycleScope.launch {
            viewModel.deleteSuccess.collect { message ->
                parentFragmentManager.setFragmentResult(REQUEST_KEY_DAY_TRIP_DELETED, Bundle())
                view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
            }
        }
    }

    private fun showDeleteConfirmation(trip: Trip) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete? Trip will be lost forever.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTrip(trip)
            }
            .create()
        dialog.setOnShowListener {
            val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                val white = android.graphics.Color.WHITE
                dialog.findViewById<android.widget.TextView>(android.R.id.message)?.setTextColor(white)
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(white)
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(white)
            }
        }
        dialog.show()
    }

    /** Show snackbar when repository load fails (D1 wiring). */
    private fun observeLoadError() {
        lifecycleScope.launch {
            viewModel.loadError.collect { message ->
                view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
            }
        }
    }

    private fun observeTrips() {
        lifecycleScope.launch {
            viewModel.trips.collect { trips ->
                adapter.submitList(trips)

                // ✅ EDGE CASE: Update empty state with proper message
                if (trips.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.tripListRecyclerView.visibility = View.GONE
                    // Update empty state message
                    try {
                        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                        binding.emptyStateText.text = "No trips found for ${dateFormat.format(selectedDate)}."
                    } catch (e: Exception) {
                        binding.emptyStateText.text = "No trips found for this date."
                    }
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.tripListRecyclerView.visibility = View.VISIBLE
                }
                
                // ✅ EDGE CASE: Update trip count with proper pluralization
                binding.tripCountText.text = "${trips.size} trip${if (trips.size != 1) "s" else ""}"
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            (resources.displayMetrics.heightPixels * 0.8f).toInt()
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.outofroutebuddy.presentation.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.databinding.DialogTripHistoryByDateBinding
import com.google.android.material.snackbar.Snackbar
import com.example.outofroutebuddy.presentation.ui.history.TripHistoryAdapter
import com.example.outofroutebuddy.presentation.ui.history.TripHistoryByDateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ NEW: Trip History By Date Dialog
 *
 * Displays trip history for a specific date selected from the calendar.
 * Shows all trips that occurred on that date in a scrollable list.
 */
@AndroidEntryPoint
class TripHistoryByDateDialog : DialogFragment() {
    
    companion object {
        private const val TAG = "TripHistoryByDateDialog"
        private const val ARG_SELECTED_DATE = "selected_date"
        
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
    private lateinit var adapter: TripHistoryAdapter
    private lateinit var selectedDate: Date
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            selectedDate = args.getSerializable(ARG_SELECTED_DATE) as Date
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

        // Load trips for the selected date
        viewModel.loadTripsForDate(selectedDate)
    }

    override fun onResume() {
        super.onResume()
        // Refresh when dialog becomes visible (e.g. after saving a trip elsewhere)
        viewModel.loadTripsForDate(selectedDate)
    }

    private fun setupViews() {
        // ✅ EDGE CASE: Format and display the selected date with proper error handling
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            binding.dateTitleText.text = "Trips for ${dateFormat.format(selectedDate)}"
        } catch (e: Exception) {
            // ✅ EDGE CASE: If date formatting fails, show fallback
            binding.dateTitleText.text = "Trips for selected date"
            android.util.Log.e(TAG, "Error formatting date", e)
        }
        
        // Close button
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = TripHistoryAdapter(
            onTripClick = { trip ->
                dismiss()
                try {
                    val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    navController.navigate(R.id.tripDetailsFragment, bundleOf("tripId" to trip.id))
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to navigate to trip details", e)
                }
            },
            onDeleteClick = { trip ->
                viewModel.deleteTrip(trip)
            }
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
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
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

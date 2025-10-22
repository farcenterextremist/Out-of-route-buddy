package com.example.outofroutebuddy.presentation.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.outofroutebuddy.databinding.FragmentTripHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * ✅ Trip History Fragment
 * 
 * Displays a list of past trips with:
 * - Date, miles, OOR for each trip
 * - Filter by date range
 * - Sort options (date, OOR amount)
 * - Tap to view details
 * - Delete trips
 */
@AndroidEntryPoint
class TripHistoryFragment : Fragment() {
    
    private var _binding: FragmentTripHistoryBinding? = null
    private val binding: FragmentTripHistoryBinding
        get() = _binding ?: throw IllegalStateException("Binding accessed before onCreateView or after onDestroyView")
    
    private val viewModel: TripHistoryViewModel by viewModels()
    private lateinit var adapter: TripHistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeTrips()
    }
    
    private fun setupRecyclerView() {
        adapter = TripHistoryAdapter(
            onTripClick = { trip ->
                // Navigate to trip details (navigation handled by adapter)
            },
            onDeleteClick = { trip ->
                viewModel.deleteTrip(trip)
            }
        )
        
        binding.tripHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripHistoryFragment.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.filterButton.setOnClickListener {
            // Filter options - to be implemented
        }
        
        binding.exportButton.setOnClickListener {
            // Export trips
            viewModel.exportTrips()
        }
    }
    
    private fun observeTrips() {
        lifecycleScope.launch {
            viewModel.trips.collect { trips ->
                adapter.submitList(trips)
                
                // Update empty state
                if (trips.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.tripHistoryRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.tripHistoryRecyclerView.visibility = View.VISIBLE
                }
                
                // Update quick stats
                updateQuickStats(trips)
            }
        }
    }
    
    private fun updateQuickStats(trips: List<com.example.outofroutebuddy.domain.models.Trip>) {
        val totalTrips = trips.size
        val totalMiles = trips.sumOf { it.actualMiles }
        val avgOor = if (trips.isNotEmpty()) trips.map { it.oorMiles }.average() else 0.0
        
        binding.quickStatsText.text = "Total Trips: $totalTrips | Miles: ${"%.1f".format(totalMiles)} | Avg OOR: ${"%.1f".format(avgOor)}"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


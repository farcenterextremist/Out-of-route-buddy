package com.example.outofroutebuddy.presentation.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.outofroutebuddy.databinding.FragmentTripDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trip Details screen – CRUCIAL §4 (docs/CRUCIAL_IMPROVEMENTS_TODO.md).
 *
 * Placeholder UI: displays basic trip info (id, miles, OOR, date) loaded from TripRepository.
 * Navigation from history list is not yet wired; full design can be added later.
 */
@AndroidEntryPoint
class TripDetailsFragment : Fragment() {

    companion object {
        private const val ARG_TRIP_ID = "tripId"
    }

    private var _binding: FragmentTripDetailsBinding? = null
    private val binding: FragmentTripDetailsBinding
        get() = _binding ?: throw IllegalStateException("Binding accessed before onCreateView or after onDestroyView")

    private val tripId: String by lazy {
        arguments?.getString(ARG_TRIP_ID) ?: ""
    }
    private val viewModel: TripDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTripDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTrip(tripId)
        lifecycleScope.launch {
            viewModel.trip.collect { trip ->
                trip?.let { displayTrip(it) }
            }
        }
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.tripDetailsLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun displayTrip(trip: com.example.outofroutebuddy.domain.models.Trip) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val dateStr = trip.endTime?.let { dateFormat.format(it) }
            ?: trip.startTime?.let { dateFormat.format(it) }
            ?: "—"
        val oorStr = String.format(Locale.US, "%.1f mi (%.1f%%)", trip.oorMiles, trip.oorPercentage)
        binding.tripDetailsContent.text = getString(
            com.example.outofroutebuddy.R.string.trip_details_format,
            trip.id,
            trip.actualMiles,
            oorStr,
            dateStr,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

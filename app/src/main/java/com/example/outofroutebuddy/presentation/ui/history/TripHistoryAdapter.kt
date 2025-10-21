package com.example.outofroutebuddy.presentation.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.outofroutebuddy.databinding.ItemTripHistoryBinding
import com.example.outofroutebuddy.domain.models.Trip
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Trip History Adapter
 * 
 * RecyclerView adapter for displaying trip list
 */
class TripHistoryAdapter(
    private val onTripClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : ListAdapter<Trip, TripHistoryAdapter.TripViewHolder>(TripDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TripViewHolder(
        private val binding: ItemTripHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(trip: Trip) {
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            binding.tripDate.text = dateFormat.format(trip.endTime ?: trip.startTime ?: Date())
            
            // Display trip data
            binding.tripMiles.text = String.format(Locale.US, "%.1f mi", trip.actualMiles)
            binding.tripOor.text = formatOORDisplay(trip.oorMiles, trip.oorPercentage)
            
            // Click listeners
            binding.root.setOnClickListener {
                onTripClick(trip)
            }
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(trip)
            }
        }
        
        private fun formatOORDisplay(oorMiles: Double, oorPercentage: Double): String {
            return when {
                oorMiles > 0.01 -> String.format(Locale.US, "%.1f over (%.1f%%)", oorMiles, oorPercentage)
                oorMiles < -0.01 -> String.format(Locale.US, "%.1f under (%.1f%%)", Math.abs(oorMiles), Math.abs(oorPercentage))
                else -> "On route (0.0%)"
            }
        }
    }
    
    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}


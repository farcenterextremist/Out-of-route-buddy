package com.example.outofroutebuddy.presentation.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * ✅ NEW: Trip History By Date ViewModel
 * 
 * Manages trip history data for a specific date.
 * Filters trips to show only those that occurred on the selected date.
 */
@HiltViewModel
class TripHistoryByDateViewModel @Inject constructor(
    application: Application,
    private val repository: TripRepository
) : AndroidViewModel(application) {
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentDate: Date? = null
    
    /**
     * Load trips for a specific date
     * Filters trips to show only those that occurred on the given date
     */
    fun loadTripsForDate(date: Date) {
        currentDate = date
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Calculate start and end of the selected date
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.time
                
                // ✅ EDGE CASE: Handle timezone - ensure we're comparing dates in the same timezone
                // Get trips for the date range - get first emission from Flow
                val tripList = try {
                    repository.getTripsByDateRange(startOfDay, endOfDay).first()
                } catch (e: Exception) {
                    android.util.Log.e("TripHistoryByDateViewModel", "Error fetching trips from repository", e)
                    emptyList()
                }
                
                // ✅ EDGE CASE: Filter to only trips that occurred on this specific date
                // Handle timezone normalization and null dates
                val filteredTrips = tripList.filter { trip ->
                    val tripDate = trip.endTime ?: trip.startTime
                    if (tripDate != null) {
                        try {
                            // Normalize both dates to start of day in local timezone for comparison
                            val tripCalendar = Calendar.getInstance()
                            tripCalendar.time = tripDate
                            tripCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            tripCalendar.set(Calendar.MINUTE, 0)
                            tripCalendar.set(Calendar.SECOND, 0)
                            tripCalendar.set(Calendar.MILLISECOND, 0)
                            
                            val dateCalendar = Calendar.getInstance()
                            dateCalendar.time = date
                            dateCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            dateCalendar.set(Calendar.MINUTE, 0)
                            dateCalendar.set(Calendar.SECOND, 0)
                            dateCalendar.set(Calendar.MILLISECOND, 0)
                            
                            // Compare normalized dates
                            tripCalendar.timeInMillis == dateCalendar.timeInMillis
                        } catch (e: Exception) {
                            // ✅ EDGE CASE: If date parsing fails, exclude the trip
                            android.util.Log.w("TripHistoryByDateViewModel", "Error comparing trip date", e)
                            false
                        }
                    } else {
                        // ✅ EDGE CASE: Trip has no date - exclude it
                        false
                    }
                }
                
                // ✅ EDGE CASE: Sort by date descending, handle null dates
                _trips.value = filteredTrips.sortedByDescending { trip ->
                    (trip.endTime ?: trip.startTime)?.time ?: 0L
                }
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryByDateViewModel", "Error loading trips for date", e)
                _trips.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(trip)
                android.util.Log.d("TripHistoryByDateViewModel", "Trip deleted: ${trip.id}")
                
                // Reload trips after deletion if we have a current date
                currentDate?.let { date ->
                    loadTripsForDate(date)
                }
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryByDateViewModel", "Error deleting trip", e)
            }
        }
    }
}

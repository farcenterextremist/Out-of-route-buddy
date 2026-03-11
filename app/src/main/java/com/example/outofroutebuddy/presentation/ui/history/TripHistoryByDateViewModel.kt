package com.example.outofroutebuddy.presentation.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

/**
 * ✅ NEW: Trip History By Date ViewModel
 * 
 * Manages trip history data for a specific date.
 * Filters trips to show only those that occurred on the selected date.
 */
@HiltViewModel
class TripHistoryByDateViewModel @Inject constructor(
    application: Application,
    private val repository: TripRepository,
    @Named("repositoryLoadErrors") private val repositoryLoadErrors: kotlinx.coroutines.flow.Flow<String>,
) : AndroidViewModel(application) {
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deleteError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deleteError: SharedFlow<String> = _deleteError.asSharedFlow()
    private val _deleteSuccess = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deleteSuccess: SharedFlow<String> = _deleteSuccess.asSharedFlow()

    /** Repository load failures (e.g. getTripsOverlappingDay). UI should show snackbar. */
    private val _loadError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val loadError: SharedFlow<String> = _loadError.asSharedFlow()

    init {
        viewModelScope.launch {
            repositoryLoadErrors.collect { msg -> _loadError.emit(msg) }
        }
    }

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
                
                // Use overlap query: includes midnight-spanning trips (shown on both days)
                val tripList = try {
                    repository.getTripsOverlappingDay(startOfDay, endOfDay).first()
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error fetching trips from repository", e)
                    emptyList()
                }
                _trips.value = tripList
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error loading trips for date", e)
                _trips.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                val deleted = repository.deleteTrip(trip)
                if (deleted) {
                    AppLogger.d(TAG, "Trip deleted")
                    _deleteSuccess.emit("Trip deleted")
                    currentDate?.let { date ->
                        loadTripsForDate(date)
                    }
                } else {
                    _deleteError.emit("Failed to delete trip")
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error deleting trip", e)
                _deleteError.emit("Failed to delete trip: ${e.message ?: "Unknown error"}")
            }
        }
    }

    companion object {
        private const val TAG = "TripHistoryByDateViewModel"
    }
}

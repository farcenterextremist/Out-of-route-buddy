package com.example.outofroutebuddy.presentation.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.util.TripExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ✅ Trip History ViewModel
 * 
 * Manages trip history data and operations
 */
@HiltViewModel
class TripHistoryViewModel @Inject constructor(
    application: Application,
    private val repository: TripRepository
) : AndroidViewModel(application) {
    
    private val tripExporter = TripExporter(application)
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadTrips()
    }
    
    fun loadTrips() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllTrips().collect { tripList ->
                    _trips.value = tripList.sortedByDescending { (it.endTime ?: it.startTime)?.time ?: 0L }
                }
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryViewModel", "Error loading trips", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(trip)
                android.util.Log.d("TripHistoryViewModel", "Trip deleted: ${trip.id}")
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryViewModel", "Error deleting trip", e)
            }
        }
    }
    
    fun exportTrips() {
        viewModelScope.launch {
            try {
                val allTrips = _trips.value
                if (allTrips.isEmpty()) {
                    android.util.Log.w("TripHistoryViewModel", "No trips to export")
                    return@launch
                }
                
                // Export to CSV
                val csvFile = tripExporter.exportToCSV(allTrips)
                tripExporter.shareFile(csvFile, "text/csv")
                
                android.util.Log.d("TripHistoryViewModel", "Exported ${allTrips.size} trips to CSV")
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryViewModel", "Error exporting trips", e)
            }
        }
    }
    
    fun exportToPDF() {
        viewModelScope.launch {
            try {
                val allTrips = _trips.value
                if (allTrips.isEmpty()) {
                    android.util.Log.w("TripHistoryViewModel", "No trips to export")
                    return@launch
                }
                
                // Export to PDF/Report
                val pdfFile = tripExporter.exportToPDF(allTrips)
                tripExporter.shareFile(pdfFile, "text/plain")
                
                android.util.Log.d("TripHistoryViewModel", "Exported ${allTrips.size} trips to report")
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryViewModel", "Error exporting to PDF", e)
            }
        }
    }
    
    fun filterByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                val allTrips = _trips.value
                val filtered = allTrips.filter {  trip ->
                    val tripTime = (trip.endTime ?: trip.startTime)?.time ?: 0L
                    tripTime in startDate..endDate
                }
                _trips.value = filtered.sortedByDescending { (it.endTime ?: it.startTime)?.time ?: 0L }
            } catch (e: Exception) {
                android.util.Log.e("TripHistoryViewModel", "Error filtering trips", e)
            }
        }
    }
}


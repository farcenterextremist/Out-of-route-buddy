package com.example.outofroutebuddy.presentation.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import com.example.outofroutebuddy.util.TripExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    @Volatile
    private var testModeOverride = false
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deleteError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deleteError: SharedFlow<String> = _deleteError.asSharedFlow()

    init {
        loadTrips()
    }
    
    fun loadTrips() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                var initialLoadDone = false
                repository.getAllTrips().collect { tripList ->
                    if (!testModeOverride) {
                        _trips.value = tripList.sortedByDescending { (it.endTime ?: it.startTime)?.time ?: 0L }
                    }
                    if (!initialLoadDone) {
                        initialLoadDone = true
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading trips", e)
                _isLoading.value = false
            }
        }
    }
    
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                // Blue Team remediation: audit trail for delete (Purple exercise)
                val deleted = repository.deleteTrip(trip)
                android.util.Log.w(TAG_DELETE_AUDIT, "delete_attempted trip_id=${trip.id} result=${deleted}")
                if (deleted) {
                    android.util.Log.d(TAG, "Trip deleted: ${trip.id}")
                } else {
                    _deleteError.emit("Failed to delete trip")
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG_DELETE_AUDIT, "delete_attempted trip_id=${trip.id} result=exception")
                android.util.Log.e(TAG, "Error deleting trip", e)
                _deleteError.emit("Failed to delete trip: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun exportTrips() {
        viewModelScope.launch {
            try {
                val allTrips = _trips.value
                if (allTrips.isEmpty()) {
                    android.util.Log.w(TAG, "No trips to export")
                    return@launch
                }
                // Blue Team remediation: audit trail for export (Purple exercise)
                android.util.Log.w(TAG_EXPORT_AUDIT, "export_requested format=csv trip_count=${allTrips.size}")
                // Export to CSV
                val csvFile = tripExporter.exportToCSV(allTrips)
                tripExporter.shareFile(csvFile, "text/csv")
                android.util.Log.d(TAG, "Exported ${allTrips.size} trips to CSV")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error exporting trips", e)
            }
        }
    }
    
    fun exportToPDF() {
        viewModelScope.launch {
            try {
                val allTrips = _trips.value
                if (allTrips.isEmpty()) {
                    android.util.Log.w(TAG, "No trips to export")
                    return@launch
                }
                // Blue Team remediation: audit trail for export (Purple exercise)
                android.util.Log.w(TAG_EXPORT_AUDIT, "export_requested format=report trip_count=${allTrips.size}")
                // Export to PDF/Report
                val pdfFile = tripExporter.exportToPDF(allTrips)
                tripExporter.shareFile(pdfFile, "text/plain")
                android.util.Log.d(TAG, "Exported ${allTrips.size} trips to report")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error exporting to PDF", e)
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
                android.util.Log.e(TAG, "Error filtering trips", e)
            }
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun setTripsForTest(tripsForTest: List<Trip>) {
        testModeOverride = true
        _trips.value = tripsForTest
    }

    companion object {
        private const val TAG = "TripHistoryViewModel"
        private const val TAG_DELETE_AUDIT = "TripDeleteAudit"
        private const val TAG_EXPORT_AUDIT = "TripExportAudit"
    }
}


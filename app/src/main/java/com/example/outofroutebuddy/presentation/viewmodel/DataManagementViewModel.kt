package com.example.outofroutebuddy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.di.IoDispatcher
import com.example.outofroutebuddy.domain.data.TripArchiveService
import com.example.outofroutebuddy.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for "delete from device (keep on server)" flows. Exports trip data via
 * [TripArchiveService] before deleting locally so the server can retain it for training.
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripArchiveService: TripArchiveService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _results = MutableSharedFlow<DataManagementResult>()
    val results: SharedFlow<DataManagementResult> = _results.asSharedFlow()

    /**
     * Deletes trips older than [olderThanMonths] from the device. Exports them first so the
     * server may retain for training; then deletes locally.
     */
    fun deleteOldDataFromDevice(olderThanMonths: Int) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -olderThanMonths)
                val cutoff = cal.time
                val calEnd = Calendar.getInstance().apply {
                    time = cutoff
                    add(Calendar.DAY_OF_MONTH, -1)
                }
                val endInclusive = calEnd.time
                val trips = try {
                    tripRepository.getTripsByDateRange(Date(0), endInclusive).first()
                } catch (e: Exception) {
                    emptyList()
                }
                val exportResult = tripArchiveService.exportBeforeLocalDelete(trips)
                if (exportResult.isSuccess) {
                    tripRepository.deleteTripsOlderThan(cutoff)
                    _results.emit(DataManagementResult.Success("Old data removed from device. Data may be retained on the server for product improvement and training."))
                } else {
                    _results.emit(DataManagementResult.Error(exportResult.exceptionOrNull()?.message ?: "Export failed"))
                }
            }
        }
    }

    /**
     * Clears all trip data from the device. Exports first so the server may retain for training.
     */
    fun clearAllDataFromDevice() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val trips = try {
                    tripRepository.getAllTrips().first()
                } catch (e: Exception) {
                    emptyList()
                }
                val exportResult = tripArchiveService.exportBeforeLocalDelete(trips)
                if (exportResult.isSuccess) {
                    tripRepository.clearAllTrips()
                    _results.emit(DataManagementResult.Success("All trip data removed from device. Data may be retained on the server for product improvement and training."))
                } else {
                    _results.emit(DataManagementResult.Error(exportResult.exceptionOrNull()?.message ?: "Export failed"))
                }
            }
        }
    }

    sealed class DataManagementResult {
        data class Success(val message: String) : DataManagementResult()
        data class Error(val message: String) : DataManagementResult()
    }
}

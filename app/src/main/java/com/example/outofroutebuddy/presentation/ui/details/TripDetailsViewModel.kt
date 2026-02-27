package com.example.outofroutebuddy.presentation.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.domain.models.Trip
import com.example.outofroutebuddy.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Trip Details screen - loads trip by ID from repository.
 */
@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            tripRepository.getTripById(tripId).collect { t ->
                _trip.value = t
                _isLoading.value = false
            }
        }
    }
}

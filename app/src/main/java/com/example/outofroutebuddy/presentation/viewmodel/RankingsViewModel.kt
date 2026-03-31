package com.example.outofroutebuddy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outofroutebuddy.di.IoDispatcher
import com.example.outofroutebuddy.domain.models.FleetLeaderboard
import com.example.outofroutebuddy.domain.ranking.FleetRankingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Rankings screen.
 * Manages the fleet leaderboard state and handles refresh actions.
 */
@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val fleetRankingsRepository: FleetRankingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RankingsUiState>(RankingsUiState.Loading)
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = RankingsUiState.Loading
            try {
                val leaderboard = withContext(ioDispatcher) {
                    fleetRankingsRepository.getUserRankingQuick()
                }
                _uiState.value = RankingsUiState.Success(leaderboard)
            } catch (e: Exception) {
                _uiState.value = RankingsUiState.Error(
                    e.message ?: "Failed to load rankings",
                )
            }
        }
    }

    fun refreshFleet() {
        viewModelScope.launch {
            _uiState.value = RankingsUiState.Loading
            try {
                withContext(ioDispatcher) {
                    fleetRankingsRepository.refreshFleet()
                }
                loadLeaderboard()
            } catch (e: Exception) {
                _uiState.value = RankingsUiState.Error(
                    e.message ?: "Failed to refresh fleet",
                )
            }
        }
    }
}

sealed interface RankingsUiState {
    data object Loading : RankingsUiState
    data class Success(val leaderboard: FleetLeaderboard) : RankingsUiState
    data class Error(val message: String) : RankingsUiState
}

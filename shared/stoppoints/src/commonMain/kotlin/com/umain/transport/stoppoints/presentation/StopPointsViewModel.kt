package com.umain.transport.stoppoints.presentation

import com.umain.transport.stoppoints.domain.model.StopPoint
import com.umain.transport.stoppoints.domain.repository.StopPointsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StopPointsUiState(
    val isLoading: Boolean = false,
    val stopPoints: List<StopPoint> = emptyList(),
    val error: String? = null
)

class StopPointsViewModel(
    private val stopPointsRepository: StopPointsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(StopPointsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadStopPoints() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
            stopPointsRepository.getAllStopPoints()
                .onSuccess { stopPointsList ->
                    _uiState.update {
                        it.copy(isLoading = false, stopPoints = stopPointsList)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "An unknown error occurred")
                    }
                }
        }
    }
}
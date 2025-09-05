package com.umain.transport.departures.presentation

import com.umain.transport.departures.domain.model.Departure
import com.umain.transport.departures.domain.repository.DeparturesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeparturesUiState(
    val isLoading: Boolean = false,
    val departures: List<Departure> = emptyList(),
    val error: String? = null
)

class DeparturesViewModel(
    private val departuresRepository: DeparturesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(DeparturesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDepartures(siteId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null, departures = emptyList()) }
        coroutineScope.launch {
            departuresRepository.getDeparturesForSite(siteId)
                .onSuccess { departuresList ->
                    _uiState.update {
                        it.copy(isLoading = false, departures = departuresList)
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
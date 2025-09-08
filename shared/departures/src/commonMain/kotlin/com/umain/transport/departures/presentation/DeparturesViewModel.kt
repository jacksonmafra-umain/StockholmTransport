package com.umain.transport.departures.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.departures.domain.model.Departure
import com.umain.transport.departures.domain.repository.DeparturesRepository
import com.umain.transport.lines.presentation.LinesUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
data class DeparturesUiState(
    val isLoading: Boolean = false,
    val departures: List<Departure> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
class DeparturesViewModel(
    private val departuresRepository: DeparturesRepository,
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _uiState = MutableStateFlow(DeparturesUiState())
    val uiState = _uiState.asStateFlow()

    @JsName("subscribeToState")
    fun subscribe(onStateUpdate: (DeparturesUiState) -> Unit) {
        viewModelScope.launch {
            uiState.collect {
                onStateUpdate(it)
            }
        }
    }

    fun loadDepartures(siteId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null, departures = emptyList()) }
        viewModelScope.launch {
            when (val result = departuresRepository.getDeparturesForSite(siteId)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, departures = result.data)
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.error.toUserFriendlyMessage())
                    }
                }
            }
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    private fun NetworkError.toUserFriendlyMessage(): String =
        when (this) {
            NetworkError.NoInternet -> "No internet connection. Please check your network."
            NetworkError.ServerError -> "A server error occurred. Please try again later."
            NetworkError.Timeout -> "The request timed out. Please try again."
            NetworkError.NotFound -> "The requested information could not be found."
            is NetworkError.Unknown -> "An unexpected error occurred: $message"
        }
}

package com.umain.transport.departures.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.presentation.BaseViewModel
import com.umain.transport.departures.domain.model.Departure
import com.umain.transport.departures.domain.repository.DeparturesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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
) : BaseViewModel<DeparturesUiState>() {
    private val _uiState = MutableStateFlow(DeparturesUiState())
    override val uiState = _uiState.asStateFlow()

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

    private fun NetworkError.toUserFriendlyMessage(): String =
        when (this) {
            NetworkError.NoInternet -> "No internet connection. Please check your network."
            NetworkError.NotFound -> "The requested information could not be found."
            NetworkError.ServerError -> "A server error occurred. Please try again later."
            NetworkError.Timeout -> "The request timed out. Please try again."
            is NetworkError.Unknown -> "An unexpected error occurred: $message"
        }
}

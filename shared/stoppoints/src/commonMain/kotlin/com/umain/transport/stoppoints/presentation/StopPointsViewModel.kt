package com.umain.transport.stoppoints.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.presentation.BaseViewModel
import com.umain.transport.stoppoints.domain.model.StopPoint
import com.umain.transport.stoppoints.domain.repository.StopPointsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class StopPointsUiState(
    val isLoading: Boolean = false,
    val stopPoints: List<StopPoint> = emptyList(),
    val error: String? = null,
)

// We ignore the primary constructor because it uses a non-exportable type (StopPointsRepository).
// Koin will still use this constructor on the Kotlin side.
@OptIn(ExperimentalJsExport::class)
@JsExport
class StopPointsViewModel
    @JsExport.Ignore
    constructor(
        private val stopPointsRepository: StopPointsRepository,
    ) : BaseViewModel<StopPointsUiState>() {
        private val _uiState = MutableStateFlow(StopPointsUiState())

        @JsExport.Ignore
        override val uiState = _uiState.asStateFlow()

        fun loadStopPoints() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = stopPointsRepository.getAllStopPoints()) {
                    is DataResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, stopPoints = result.data)
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
                NetworkError.ServerError -> "A server error occurred. Please try again later."
                NetworkError.Timeout -> "The request timed out. Please try again."
                NetworkError.NotFound -> "The requested information could not be found."
                is NetworkError.Unknown -> "An unexpected error occurred: $message"
            }
    }

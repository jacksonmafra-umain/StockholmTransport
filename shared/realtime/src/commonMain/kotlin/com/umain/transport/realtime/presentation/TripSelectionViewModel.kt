package com.umain.transport.realtime.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.presentation.BaseViewModel
import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.domain.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * UI state for the trip-picker screen. Mirrors the LinesUiState pattern:
 * flat record so JS consumers can render off it without knowing anything
 * about Flows or coroutines.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class TripSelectionUiState(
    val isLoading: Boolean = true,
    val activeTrips: List<ActiveTrip> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
class TripSelectionViewModel
    @JsExport.Ignore
    constructor(
        private val tripRepository: TripRepository,
    ) : BaseViewModel<TripSelectionUiState>() {
        private val _uiState = MutableStateFlow(TripSelectionUiState())

        @JsExport.Ignore
        override val uiState = _uiState.asStateFlow()

        /** One-shot pull of the simulator's active-trips list. */
        fun loadActiveTrips() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = tripRepository.getActiveTrips()) {
                    is DataResult.Success -> _uiState.update {
                        it.copy(isLoading = false, activeTrips = result.data, error = null)
                    }
                    is DataResult.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.error.toUserFriendlyMessage())
                    }
                }
            }
        }

        private fun NetworkError.toUserFriendlyMessage(): String = when (this) {
            NetworkError.NoInternet -> "No internet connection. Please check your network."
            NetworkError.Timeout -> "The connection timed out. Please try again."
            NetworkError.NotFound -> "No active trips were found."
            NetworkError.ServerError -> "The realtime simulator is unavailable. Please try again."
            is NetworkError.Unknown -> "Unexpected error: $message"
        }
    }

package com.umain.transport.realtime.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.presentation.BaseViewModel
import com.umain.transport.realtime.domain.model.TripDisplayInfo
import com.umain.transport.realtime.domain.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * UI state for the realtime-trip screen. The shape mirrors
 * LinesUiState — a flat record of (isLoading, payload, error) so JS
 * consumers can `state.error ? showError() : render(state.displayInfo)`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class TripUiState(
    val isLoading: Boolean = true,
    val displayInfo: TripDisplayInfo? = null,
    val error: String? = null,
)

/**
 * ViewModel that renders one live trip. JS consumers go through the
 * BaseViewModel.subscribe(callback) bridge — same contract as
 * LinesViewModel and friends. Kotlin consumers can collect uiState
 * directly.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class TripViewModel
    @JsExport.Ignore
    constructor(
        private val tripRepository: TripRepository,
    ) : BaseViewModel<TripUiState>() {
        private val _uiState = MutableStateFlow(TripUiState())

        @JsExport.Ignore
        override val uiState = _uiState.asStateFlow()

        private var tripJob: Job? = null

        /** Begin streaming WS updates for this trip. Cancels any prior trip. */
        fun startObservingTrip(tripId: String) {
            tripJob?.cancel()
            _uiState.update { it.copy(isLoading = true, error = null) }
            tripJob = tripRepository.getTripUpdates(tripId)
                .onEach { result ->
                    _uiState.update {
                        when (result) {
                            is DataResult.Success -> it.copy(
                                isLoading = false,
                                displayInfo = result.data,
                                error = null,
                            )
                            is DataResult.Error -> it.copy(
                                isLoading = false,
                                error = result.error.toUserFriendlyMessage(),
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }

        override fun onCleared() {
            tripJob?.cancel()
            super.onCleared()
        }

        private fun NetworkError.toUserFriendlyMessage(): String = when (this) {
            NetworkError.NoInternet -> "No internet connection. Please check your network."
            NetworkError.Timeout -> "The connection timed out. Please try again."
            NetworkError.NotFound -> "Trip not found — it may have ended."
            NetworkError.ServerError -> "The realtime simulator is unavailable. Please try again."
            is NetworkError.Unknown -> "Unexpected error: $message"
        }
    }

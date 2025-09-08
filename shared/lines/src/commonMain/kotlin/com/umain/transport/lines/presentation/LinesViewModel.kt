package com.umain.transport.lines.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.presentation.BaseViewModel
import com.umain.transport.lines.domain.model.Line
import com.umain.transport.lines.domain.model.TransportMode
import com.umain.transport.lines.domain.repository.LinesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class LinesUiState(
    val isLoading: Boolean = false,
    val linesByMode: Map<TransportMode, List<Line>> = emptyMap(),
    val error: String? = null,
)

// We ignore the primary constructor because it uses a non-exportable type (LinesRepository).
// Koin will still use this constructor on the Kotlin side.
@OptIn(ExperimentalJsExport::class)
@JsExport
class LinesViewModel
    @JsExport.Ignore
    constructor(
        private val linesRepository: LinesRepository,
    ) : BaseViewModel<LinesUiState>() {
        private val _uiState = MutableStateFlow(LinesUiState())

        @JsExport.Ignore
        override val uiState = _uiState.asStateFlow()

        fun loadLines() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = linesRepository.getAllLines()) {
                    is DataResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, linesByMode = result.data)
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

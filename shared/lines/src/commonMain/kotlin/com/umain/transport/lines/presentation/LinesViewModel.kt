package com.umain.transport.lines.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.lines.domain.model.Line
import com.umain.transport.lines.domain.model.TransportMode
import com.umain.transport.lines.domain.repository.LinesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LinesUiState(
    val isLoading: Boolean = false,
    val linesByMode: Map<TransportMode, List<Line>> = emptyMap(),
    val error: String? = null
)

class LinesViewModel(
    private val linesRepository: LinesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(LinesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadLines() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
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

    private fun NetworkError.toUserFriendlyMessage(): String {
        return when (this) {
            NetworkError.NoInternet -> "No internet connection. Please check your network."
            NetworkError.ServerError -> "A server error occurred. Please try again later."
            NetworkError.Timeout -> "The request timed out. Please try again."
            is NetworkError.Unknown -> "An unexpected error occurred: $message"
        }
    }
}
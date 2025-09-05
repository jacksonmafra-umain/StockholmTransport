package com.umain.transport.lines.presentation

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
            linesRepository.getAllLines()
                .onSuccess { linesMap ->
                    _uiState.update {
                        it.copy(isLoading = false, linesByMode = linesMap)
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
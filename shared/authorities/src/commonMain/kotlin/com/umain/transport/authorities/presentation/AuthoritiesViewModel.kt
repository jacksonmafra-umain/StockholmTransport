package com.umain.transport.authorities.presentation

import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthoritiesUiState(
    val isLoading: Boolean = false,
    val authorities: List<Authority> = emptyList(),
    val error: String? = null
)

class AuthoritiesViewModel(
    private val authoritiesRepository: AuthoritiesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(AuthoritiesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAuthorities() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
            authoritiesRepository.getAllAuthorities()
                .onSuccess { authoritiesList ->
                    _uiState.update {
                        it.copy(isLoading = false, authorities = authoritiesList)
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
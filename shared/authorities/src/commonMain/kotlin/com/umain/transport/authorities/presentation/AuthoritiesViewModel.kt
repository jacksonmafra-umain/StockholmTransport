package com.umain.transport.authorities.presentation

import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthoritiesUiState(
    val isLoading: Boolean = false,
    val authorities: List<Authority> = emptyList(),
    val error: String? = null,
)

class AuthoritiesViewModel(
    private val authoritiesRepository: AuthoritiesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private val _uiState = MutableStateFlow(AuthoritiesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAuthorities() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
            when (val result = authoritiesRepository.getAllAuthorities()) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, authorities = result.data)
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

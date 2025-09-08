package com.umain.transport.authorities.presentation

import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.departures.presentation.DeparturesUiState
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
data class AuthoritiesUiState(
    val isLoading: Boolean = false,
    val authorities: List<Authority> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
class AuthoritiesViewModel(
    private val authoritiesRepository: AuthoritiesRepository,
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _uiState = MutableStateFlow(AuthoritiesUiState())
    val uiState = _uiState.asStateFlow()

    @JsName("subscribeToState")
    fun subscribe(onStateUpdate: (AuthoritiesUiState) -> Unit) {
        viewModelScope.launch {
            uiState.collect {
                onStateUpdate(it)
            }
        }
    }

    fun loadAuthorities() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
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

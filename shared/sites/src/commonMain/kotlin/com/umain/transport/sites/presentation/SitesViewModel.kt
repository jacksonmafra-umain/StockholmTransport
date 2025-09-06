package com.umain.transport.sites.presentation

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.sites.domain.model.Site
import com.umain.transport.sites.domain.repository.SitesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SitesUiState(
    val isLoading: Boolean = false,
    val sites: List<Site> = emptyList(),
    val error: String? = null,
)

class SitesViewModel(
    private val sitesRepository: SitesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSites() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
            when (val result = sitesRepository.getAllSites()) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, sites = result.data)
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

package com.umain.transport.sites.presentation

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
    val error: String? = null
)

class SitesViewModel(
    private val sitesRepository: SitesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSites() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        coroutineScope.launch {
            sitesRepository.getAllSites()
                .onSuccess { sitesList ->
                    _uiState.update {
                        it.copy(isLoading = false, sites = sitesList)
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
package com.urdufonts.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.GlobalSearchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val globalSearchUseCase: GlobalSearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, errorMessage = null) }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    fonts = emptyList(),
                    categories = emptyList(),
                    styles = emptyList()
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // Debounce typing
            _uiState.update { it.copy(isLoading = true) }
            globalSearchUseCase(newQuery)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fonts = result.fonts,
                            categories = result.categories,
                            styles = result.styles,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to perform search"
                        )
                    }
                }
        }
    }

    fun setInitialTab(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onTabSelected(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                query = "",
                isLoading = false,
                fonts = emptyList(),
                categories = emptyList(),
                styles = emptyList(),
                errorMessage = null
            )
        }
    }
}

package com.webscare.urdufonts.ui.style

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.GetStylesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StylesViewModel(
    private val getStylesUseCase: GetStylesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StylesUiState())
    val uiState: StateFlow<StylesUiState> = _uiState.asStateFlow()

    init {
        loadStyles()
    }

    private fun loadStyles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getStylesUseCase()
                .onSuccess { styles ->
                    println("Styles: $styles")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allStyles = styles,
                            styles = styles
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load styles"
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.allStyles
            } else {
                current.allStyles.filter { it.title.contains(query, ignoreCase = true) }
            }
            current.copy(searchQuery = query, styles = filtered)
        }
    }

    fun retry() {
        loadStyles()
    }
}
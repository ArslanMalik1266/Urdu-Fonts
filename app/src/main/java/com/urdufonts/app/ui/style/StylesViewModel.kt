package com.urdufonts.app.ui.style

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.GetStylesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.content.Context
import com.urdufonts.app.ui.util.preloadImageUrls

class StylesViewModel(
    private val getStylesUseCase: GetStylesUseCase,
    private val context: Context
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
                    // Preload top 4 initial visible style thumbnail SVGs atomically
                    val initialUrls = styles.take(4).mapNotNull { it.thumbnailUrl }
                    if (initialUrls.isNotEmpty()) {
                        preloadImageUrls(context, initialUrls)
                    }
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
    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", styles = it.allStyles) }
    }
}
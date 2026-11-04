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
    private val context: Context,
    private val getFontsUseCase: com.urdufonts.app.domain.usecases.GetFontsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StylesUiState())
    val uiState: StateFlow<StylesUiState> = _uiState.asStateFlow()

    // Survives tab switches (the screen's remember{} doesn't), so revisited items skip the fade-in
    val seenItems: MutableSet<Int> = mutableSetOf()

    init {
        loadStyles()
    }

    private fun loadStyles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getStylesUseCase()
                .onSuccess { allStyles ->
                    // Only tiles that lead to at least one free font (premium-only ones would open empty)
                    val styles = keepWithFreeFonts(allStyles) { it.slug }
                    // Preload the first screenful (8) of visible style thumbnail SVGs atomically
                    val initialUrls = styles.take(8).mapNotNull { it.thumbnailUrl }
                    if (initialUrls.isNotEmpty()) {
                        preloadImageUrls(context, initialUrls, com.urdufonts.app.ui.util.CardImageRequests::style)
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
                            errorMessage = com.urdufonts.app.ui.util.UserMessages.forError(e)
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

    private suspend fun <T> keepWithFreeFonts(all: List<T>, slugOf: (T) -> String): List<T> {
        val fonts = runCatching { getFontsUseCase() }.getOrNull() ?: return all // can't tell: show everything
        val usedSlugs = fonts.flatMap { it.styles.orEmpty() }.mapTo(HashSet()) { it.slug }
        return all.filter { slugOf(it) in usedSlugs }
    }
}
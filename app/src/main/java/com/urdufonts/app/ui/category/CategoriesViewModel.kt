package com.urdufonts.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.content.Context
import com.urdufonts.app.ui.util.preloadImageUrls

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val context: Context,
    private val getFontsUseCase: com.urdufonts.app.domain.usecases.GetFontsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getCategoriesUseCase()
                .onSuccess { allCategories ->
                    // Only tiles that lead to at least one free font (premium-only ones would open empty)
                    val categories = keepWithFreeFonts(allCategories) { it.slug }
                    // Preload the first screenful (8) of visible category thumbnail SVGs atomically
                    val initialUrls = categories.take(8).mapNotNull { it.thumbnailUrl }
                    if (initialUrls.isNotEmpty()) {
                        preloadImageUrls(context, initialUrls, com.urdufonts.app.ui.util.CardImageRequests::category)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allCategories = categories,
                            categories = categories
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
                current.allCategories
            } else {
                current.allCategories.filter { it.title.contains(query, ignoreCase = true) }
            }
            current.copy(searchQuery = query, categories = filtered)
        }
    }

    fun retry() {
        loadCategories()
    }
    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", categories = it.allCategories) }
    }

    private suspend fun <T> keepWithFreeFonts(all: List<T>, slugOf: (T) -> String): List<T> {
        val fonts = runCatching { getFontsUseCase() }.getOrNull() ?: return all // can't tell: show everything
        val usedSlugs = fonts.flatMap { it.categories.orEmpty() }.mapTo(HashSet()) { it.slug }
        return all.filter { slugOf(it) in usedSlugs }
    }
}
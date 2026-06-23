package com.webscare.urdufonts.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.usecases.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
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
                .onSuccess { categories ->
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
                            errorMessage = e.message ?: "Failed to load categories"
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
}
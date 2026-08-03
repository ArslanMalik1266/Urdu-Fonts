package com.urdufonts.app.ui.category

import com.urdufonts.app.domain.models.CategoryItem

data class CategoriesUiState(
    val searchQuery: String = "",
    val allCategories: List<CategoryItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
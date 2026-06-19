package com.webscare.urdufonts.ui.category

import com.webscare.urdufonts.domain.models.CategoryItem

data class CategoriesUiState(
    val searchQuery: String = "",
    val allCategories: List<CategoryItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
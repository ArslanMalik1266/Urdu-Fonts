package com.urdufonts.app.ui.style

import com.urdufonts.app.domain.models.StyleItem

data class StylesUiState(
    val searchQuery: String = "",
    val allStyles: List<StyleItem> = emptyList(),
    val styles: List<StyleItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
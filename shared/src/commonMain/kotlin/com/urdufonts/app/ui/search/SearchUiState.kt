package com.urdufonts.app.ui.search

import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.domain.models.FontItem
import com.urdufonts.app.domain.models.StyleItem

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.FONTS,
    val isLoading: Boolean = false,
    val fonts: List<FontItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val styles: List<StyleItem> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && query.isNotBlank() && when (selectedTab) {
            SearchTab.FONTS -> fonts.isEmpty()
            SearchTab.CATEGORIES -> categories.isEmpty()
            SearchTab.STYLES -> styles.isEmpty()
        }

    val totalCount: Int
        get() = fonts.size + categories.size + styles.size
}

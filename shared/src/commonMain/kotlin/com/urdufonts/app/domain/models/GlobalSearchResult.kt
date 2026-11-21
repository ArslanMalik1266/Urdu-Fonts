package com.urdufonts.app.domain.models

data class GlobalSearchResult(
    val fonts: List<FontItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val styles: List<StyleItem> = emptyList()
) {
    val isEmpty: Boolean
        get() = fonts.isEmpty() && categories.isEmpty() && styles.isEmpty()

    val totalCount: Int
        get() = fonts.size + categories.size + styles.size
}

package com.urdufonts.app.ui.search

enum class SearchTab(val label: String) {
    FONTS("Fonts"),
    CATEGORIES("Categories"),
    STYLES("Styles");

    companion object {
        fun fromString(value: String?): SearchTab {
            return when (value?.lowercase()) {
                "categories", "category" -> CATEGORIES
                "styles", "style" -> STYLES
                else -> FONTS
            }
        }
    }
}

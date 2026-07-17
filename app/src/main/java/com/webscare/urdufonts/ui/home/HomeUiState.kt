package com.webscare.urdufonts.ui.home

import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.domain.models.FontItem

data class HomeUiState(
    val allFonts: List<FontItem> = emptyList(),
    val fonts: List<FontItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // ── Filter sheet ───────────────────────────────────────────────────────
    val isFilterSheetVisible: Boolean = false,
    val expandedFilterSection: FilterSection = FilterSection.CATEGORIES,
    val isCategoriesGridExpanded: Boolean = false,
    val isStylesGridExpanded: Boolean = false,

    // ── Dynamic filter options derived from API data ───────────────────────
    val availableCategories: List<FontClassifier> = emptyList(),
    val availableStyles: List<FontClassifier> = emptyList(),

    // ── Selected filter slugs ──────────────────────────────────────────────
    val selectedCategories: Set<String> = emptySet(),
    val selectedStyles: Set<String> = emptySet(),
    val appliedCategories: Set<String> = emptySet(),
    val appliedStyles: Set<String> = emptySet(),
) {
    val hasActiveFilters: Boolean
        get() = appliedCategories.isNotEmpty() || appliedStyles.isNotEmpty()

    val totalSelectedFilters: Int
        get() = selectedCategories.size + selectedStyles.size
}
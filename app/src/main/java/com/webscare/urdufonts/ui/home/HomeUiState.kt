package com.webscare.urdufonts.ui.home

import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.models.FontListItem

data class HomeUiState(
    val allFonts: List<FontListItem> = emptyList(),
    val fonts: List<FontListItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // ── Filter sheet ───────────────────────────────────────────────────────
    val isFilterSheetVisible: Boolean = false,
    val expandedFilterSection: FilterSection = FilterSection.NONE,

    // ── Dynamic filter options derived from API data ───────────────────────
    val availableCategories: List<FontClassifier> = emptyList(),
    val availableStyles: List<FontClassifier> = emptyList(),

    // ── Selected filter slugs ──────────────────────────────────────────────
    val selectedCategories: Set<String> = emptySet(),
    val selectedStyles: Set<String> = emptySet(),
) {
    val hasActiveFilters: Boolean
        get() = selectedCategories.isNotEmpty() || selectedStyles.isNotEmpty()

    val totalSelectedFilters: Int
        get() = selectedCategories.size + selectedStyles.size
}
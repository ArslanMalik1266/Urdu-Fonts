package com.webscare.urdufonts.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.models.FontListItem
import com.webscare.urdufonts.domain.usecases.BuildFontListUseCase
import com.webscare.urdufonts.domain.usecases.GetBannersUseCase
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import com.webscare.urdufonts.ui.home.drawer.DrawerMenuItem
import com.webscare.urdufonts.ui.home.drawer.DrawerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getFontsUseCase: GetFontsUseCase,
    private val buildFontListUseCase: BuildFontListUseCase,
    private val getBannersUseCase: GetBannersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _drawerUiState = MutableStateFlow(DrawerUiState())
    val drawerUiState: StateFlow<DrawerUiState> = _drawerUiState.asStateFlow()

    private var allFonts: List<FontItem> = emptyList()

    init {
        loadFonts()
    }


    private fun loadFonts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                allFonts = getFontsUseCase()
                val banners = getBannersUseCase()
                val processedList = buildFontListUseCase(allFonts, banners)
                val uniqueCategories = allFonts
                    .flatMap { it.categories.orEmpty() }
                    .distinctBy { it.slug }

                val uniqueStyles = allFonts
                    .flatMap { it.styles.orEmpty() }
                    .distinctBy { it.slug }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allFonts = processedList,
                        fonts = processedList,
                        availableCategories = uniqueCategories,
                        availableStyles = uniqueStyles
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("=== CRASH DETAIL: ${e::class.java.name}: ${e.message} ===")
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong")
                }
            }
        }
    }

    fun retry() = loadFonts()


    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                fonts = applyFilters(current.allFonts, query, current.selectedCategories, current.selectedStyles)
            )
        }
    }

    fun clearSearch() {
        _uiState.update { current ->
            current.copy(
                searchQuery = "",
                fonts = applyFilters(current.allFonts, "", current.selectedCategories, current.selectedStyles)
            )
        }
    }


    fun showFilterSheet() {
        _uiState.update { it.copy(isFilterSheetVisible = true) }
    }

    fun hideFilterSheet() {
        _uiState.update {
            it.copy(isFilterSheetVisible = false, expandedFilterSection = FilterSection.NONE)
        }
    }


    fun toggleFilterSection(section: FilterSection) {
        _uiState.update { current ->
            val next = if (current.expandedFilterSection == section) FilterSection.NONE else section
            current.copy(expandedFilterSection = next)
        }
    }


    fun toggleCategory(slug: String) {
        _uiState.update { current ->
            current.copy(selectedCategories = current.selectedCategories.toggle(slug))
        }
    }

    fun toggleStyle(slug: String) {
        _uiState.update { current ->
            current.copy(selectedStyles = current.selectedStyles.toggle(slug))
        }
    }


    fun clearAllFilters() {
        _uiState.update { current ->
            current.copy(
                selectedCategories = emptySet(),
                selectedStyles = emptySet(),
                appliedCategories = emptySet(),   // ✅ Applied bhi reset
                appliedStyles = emptySet(),        // ✅
                expandedFilterSection = FilterSection.NONE,
                fonts = applyFilters(current.allFonts, current.searchQuery, emptySet(), emptySet())
            )
        }
    }

    fun applyFiltersAndClose() {
        _uiState.update { current ->
            current.copy(
                isFilterSheetVisible = false,
                expandedFilterSection = FilterSection.NONE,
                // ✅ Applied mein save karo — yahan se hasActiveFilters true hoga
                appliedCategories = current.selectedCategories,
                appliedStyles = current.selectedStyles,
                fonts = applyFilters(
                    current.allFonts,
                    current.searchQuery,
                    current.selectedCategories,
                    current.selectedStyles
                )
            )
        }
    }


    fun onDrawerMenuItemSelected(item: DrawerMenuItem) {
        _drawerUiState.update { it.copy(selectedItem = item) }
    }


    private fun applyFilters(
        source: List<FontListItem>,
        query: String,
        categories: Set<String>,
        styles: Set<String>,
    ): List<FontListItem> {
        val hasFilters = categories.isNotEmpty() || styles.isNotEmpty()

        // 1. Extract only fonts and filter them
        val filteredFonts = source
            .filterIsInstance<FontListItem.Font>()
            .filter { item ->
                val font = item.fontItem
                val matchesQuery = query.isBlank() ||
                        font.name.contains(query, ignoreCase = true)
                val matchesCategory = categories.isEmpty() ||
                        font.categories.orEmpty().any { it.slug in categories }
                val matchesStyle = styles.isEmpty() ||
                        font.styles.orEmpty().any { it.slug in styles }
                matchesQuery && matchesCategory && matchesStyle
            }

        // 2. Re-insert banners every 10 items on the filtered result
        if (hasFilters || query.isNotBlank()) {
            return buildFontListUseCase(filteredFonts.map { it.fontItem }, getBannersUseCase())
        }

        return source.filter { item ->
            when (item) {
                is FontListItem.Banner -> !hasFilters
                is FontListItem.Font -> true
            }
        }
    }

    private fun Set<String>.toggle(id: String): Set<String> =
        if (id in this) this - id else this + id
}
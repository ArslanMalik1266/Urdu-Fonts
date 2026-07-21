package com.webscare.urdufonts.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.domain.models.FontItem
import com.webscare.urdufonts.domain.usecases.GetBannersUseCase
import com.webscare.urdufonts.domain.usecases.GetFontsUseCase
import com.webscare.urdufonts.domain.usecases.GetUserSessionUseCase
import com.webscare.urdufonts.ui.home.drawer.DrawerMenuItem
import com.webscare.urdufonts.ui.home.drawer.DrawerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val getFontsUseCase: GetFontsUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _drawerUiState = MutableStateFlow(DrawerUiState())
    val drawerUiState: StateFlow<DrawerUiState> = _drawerUiState.asStateFlow()

    private var allFonts: List<FontItem> = emptyList()

    init {
        loadFonts()
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            getUserSessionUseCase().collect { session ->
                _drawerUiState.update {
                    it.copy(
                        isLoggedIn = session != null,
                        userName = session?.name,
                        userSubtitle = session?.email ?: "Access premium Urdu fonts",
                        profileImageUrl = session?.avatar
                    )
                }
            }
        }
    }


    private fun loadFonts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                allFonts = getFontsUseCase()

                val uniqueData = withContext(Dispatchers.Default) {
                    val categories = allFonts
                        .flatMap { it.categories.orEmpty() }
                        .distinctBy { it.slug }

                    val styles = allFonts
                        .flatMap { it.styles.orEmpty() }
                        .distinctBy { it.slug }

                    Pair(categories, styles)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allFonts = allFonts,
                        fonts = allFonts,
                        availableCategories = uniqueData.first,
                        availableStyles = uniqueData.second
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        _uiState.update {
            it.copy(
                isFilterSheetVisible = true,
                expandedFilterSection = FilterSection.CATEGORIES // By default, open Categories section
            )
        }
    }

    fun hideFilterSheet() {
        _uiState.update {
            it.copy(
                isFilterSheetVisible = false,
                expandedFilterSection = FilterSection.NONE,
                isCategoriesGridExpanded = false, // Reset grid state
                isStylesGridExpanded = false      // Reset grid state
            )
        }
    }


    fun toggleFilterSection(section: FilterSection) {
        _uiState.update { current ->
            val next = if (current.expandedFilterSection == section) FilterSection.NONE else section
            current.copy(expandedFilterSection = next)
        }
    }
    fun toggleCategoriesGrid() {
        _uiState.update { it.copy(isCategoriesGridExpanded = !it.isCategoriesGridExpanded) }
    }
    fun toggleStylesGrid() {
        _uiState.update { it.copy(isStylesGridExpanded = !it.isStylesGridExpanded) }
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
                appliedCategories = emptySet(),
                appliedStyles = emptySet(),
                expandedFilterSection = FilterSection.NONE,
                isCategoriesGridExpanded = false, // Collapse categories grid
                isStylesGridExpanded = false,     // Collapse styles grid
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
        source: List<FontItem>,
        query: String,
        categories: Set<String>,
        styles: Set<String>,
    ): List<FontItem> {
        return source.filter { font ->
            val matchesQuery = query.isBlank() ||
                    font.name.contains(query, ignoreCase = true)
            val matchesCategory = categories.isEmpty() ||
                    font.categories.orEmpty().any { it.slug in categories }
            val matchesStyle = styles.isEmpty() ||
                    font.styles.orEmpty().any { it.slug in styles }
            matchesQuery && matchesCategory && matchesStyle
        }
    }
    private fun Set<String>.toggle(id: String): Set<String> =
        if (id in this) this - id else this + id
}

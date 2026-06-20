package com.webscare.urdufonts.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webscare.urdufonts.domain.models.FontItem
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fonts = buildFontListUseCase(allFonts, banners)
                    )
                }
            } catch (e: Exception) {
                // 🔍 DEBUG — print full stack trace
                e.printStackTrace()
                println("=== CRASH DETAIL: ${e::class.java.name}: ${e.message} ===")
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong")
                }
            }
        }
    }

    fun retry() {
        loadFonts()
    }
    fun onDrawerMenuItemSelected(item: DrawerMenuItem) {
        _drawerUiState.update { it.copy(selectedItem = item) }
    }
}
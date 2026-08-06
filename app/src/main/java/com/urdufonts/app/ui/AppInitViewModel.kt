package com.urdufonts.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.GetCategoriesUseCase
import com.urdufonts.app.domain.usecases.GetFontsUseCase
import com.urdufonts.app.domain.usecases.GetStylesUseCase
import com.urdufonts.app.domain.usecases.CheckUserStatusUseCase
import com.urdufonts.app.domain.usecases.RestoreSubscriptionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppInitViewModel(
    private val getFontsUseCase: GetFontsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getStylesUseCase: GetStylesUseCase,
    private val checkUserStatusUseCase: CheckUserStatusUseCase,
    private val restoreSubscriptionUseCase: RestoreSubscriptionUseCase
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        preloadAll()
    }

    private fun preloadAll() {
        viewModelScope.launch {
            // All run in parallel
            val fontsDeferred       = async { runCatching { getFontsUseCase() } }
            val categoriesDeferred  = async { getCategoriesUseCase() }
            val stylesDeferred      = async { getStylesUseCase() }
            val checkUserDeferred   = async { runCatching { checkUserStatusUseCase() } }
            val restoreSubDeferred  = async { runCatching { restoreSubscriptionUseCase() } }

            fontsDeferred.await()
            categoriesDeferred.await()
            stylesDeferred.await()
            checkUserDeferred.await()
            restoreSubDeferred.await()

            // Data is now cached in Room — all screens will load instantly
            _isReady.value = true
        }
    }
}
package com.urdufonts.app.ui.subscription

import androidx.lifecycle.ViewModel
import com.urdufonts.app.domain.usecases.GetSubscriptionOptionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SubscriptionViewModel(
    private val getSubscriptionOptionsUseCase: GetSubscriptionOptionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val options = getSubscriptionOptionsUseCase.getOptions()
        val features = getSubscriptionOptionsUseCase.getFeatures()
        val defaultSelectedId = options.find { it.isMostPopular }?.id ?: options.firstOrNull()?.id ?: ""

        _uiState.update {
            it.copy(
                options = options,
                selectedOptionId = defaultSelectedId,
                features = features
            )
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }
}

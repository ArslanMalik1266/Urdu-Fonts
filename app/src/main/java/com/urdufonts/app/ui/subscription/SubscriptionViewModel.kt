package com.urdufonts.app.ui.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.CheckSubscriptionStatusUseCase
import com.urdufonts.app.domain.usecases.GetSubscriptionOptionsUseCase
import com.urdufonts.app.domain.usecases.PurchaseSubscriptionUseCase
import com.urdufonts.app.domain.usecases.RestoreSubscriptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getSubscriptionOptionsUseCase: GetSubscriptionOptionsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restoreSubscriptionUseCase: RestoreSubscriptionUseCase,
    private val checkSubscriptionStatusUseCase: CheckSubscriptionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeSubscriptionStatus()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val options = getSubscriptionOptionsUseCase.getOptions()
            val features = getSubscriptionOptionsUseCase.getFeatures()
            val defaultSelectedId = options.find { it.isMostPopular }?.id ?: options.firstOrNull()?.id ?: "six_months"

            _uiState.update {
                it.copy(
                    options = options,
                    selectedOptionId = defaultSelectedId,
                    features = features,
                    isLoading = false
                )
            }
        }
    }

    fun retryLoadData() = loadData()

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            checkSubscriptionStatusUseCase().collect { isPro ->
                _uiState.update { it.copy(isProUser = isPro) }
            }
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun purchase(activity: Activity) {
        val selectedOption = _uiState.value.options.find { it.id == _uiState.value.selectedOptionId }
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, userMessage = null) }
            val result = purchaseSubscriptionUseCase(activity, selectedOption)
            result.onSuccess {
                _uiState.update { it.copy(isPurchasing = false) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        userMessage = exception.message ?: "Purchase failed"
                    )
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }
            val result = restoreSubscriptionUseCase()
            result.onSuccess { restored ->
                val message = if (restored) "Subscription restored successfully!" else "No active subscription found."
                _uiState.update { it.copy(isLoading = false, userMessage = message) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = exception.message ?: "Failed to restore purchases"
                    )
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

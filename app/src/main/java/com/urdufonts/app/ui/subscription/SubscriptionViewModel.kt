package com.urdufonts.app.ui.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.usecases.CheckSubscriptionStatusUseCase
import com.urdufonts.app.domain.usecases.GetActivePlanTypeUseCase
import com.urdufonts.app.domain.usecases.GetSubscriptionOptionsUseCase
import com.urdufonts.app.domain.usecases.GetUserSessionUseCase
import com.urdufonts.app.domain.usecases.PurchaseSubscriptionUseCase
import com.urdufonts.app.domain.usecases.RestoreSubscriptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getSubscriptionOptionsUseCase: GetSubscriptionOptionsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restoreSubscriptionUseCase: RestoreSubscriptionUseCase,
    private val checkSubscriptionStatusUseCase: CheckSubscriptionStatusUseCase,
    private val getActivePlanTypeUseCase: GetActivePlanTypeUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private var userSelectedOptionId: String? = null

    init {
        loadData()
        observeSubscriptionStatus()
        observeActivePlan()
    }

    fun onScreenOpened() {
        userSelectedOptionId = null
        _uiState.update { resolveSelection(it) }
    }

    private fun resolveSelectedOptionId(
        options: List<SubscriptionOption>,
        activePlanType: SubscriptionType?,
        userSelectedId: String?
    ): String {
        // 1. Honour the user's explicit tap if that option still exists
        if (userSelectedId != null && options.any { it.id == userSelectedId }) {
            return userSelectedId
        }

        // 2. Default to the currently subscribed plan
        if (activePlanType != null) {
            val activeOptionId = when (activePlanType) {
                SubscriptionType.MONTHLY    -> "monthly-plan"
                SubscriptionType.SIX_MONTHS -> "6-month-plan"
                SubscriptionType.LIFETIME   -> "urdu_fonts_lifetime"
            }
            if (options.any { it.id == activeOptionId }) {
                return activeOptionId
            }
        }

        // 3. Fall back to most popular, then first, then hardcoded default
        return options.find { it.isMostPopular }?.id
            ?: options.firstOrNull()?.id
            ?: "6-month-plan"
    }

    private fun resolveSelection(state: SubscriptionUiState): SubscriptionUiState {
        val resolvedId = resolveSelectedOptionId(
            options        = state.options,
            activePlanType = state.activePlanType,
            userSelectedId = userSelectedOptionId
        )
        return state.copy(selectedOptionId = resolvedId)
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val options = getSubscriptionOptionsUseCase.getOptions()
            val features = getSubscriptionOptionsUseCase.getFeatures()
            _uiState.update { state ->
                resolveSelection(
                    state.copy(options = options, features = features, isLoading = false)
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

    private fun observeActivePlan() {
        viewModelScope.launch {
            getActivePlanTypeUseCase().collect { activeType ->
                _uiState.update { state ->
                    if (state.activePlanType != activeType && activeType != null) {
                        userSelectedOptionId = null
                    }
                    resolveSelection(state.copy(activePlanType = activeType))
                }
            }
        }
    }

    fun selectOption(optionId: String) {
        userSelectedOptionId = optionId
        _uiState.update { resolveSelection(it) }
    }

    fun getActionButtonText(state: SubscriptionUiState): String = state.actionButtonText

    fun purchase(activity: Activity, onRequireLogin: () -> Unit) {
        val currentState = _uiState.value
        val selectedOption = currentState.options.find { it.id == currentState.selectedOptionId }
        if (selectedOption == null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, userMessage = null) }
            val result = purchaseSubscriptionUseCase(activity, selectedOption)
            result.onSuccess {
                _uiState.update { it.copy(isPurchasing = false) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        userMessage  = exception.message ?: "Purchase failed"
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
                        isLoading   = false,
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

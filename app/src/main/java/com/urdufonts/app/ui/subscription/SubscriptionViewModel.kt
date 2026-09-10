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

import com.urdufonts.app.domain.usecases.GetUserSessionUseCase
import kotlinx.coroutines.flow.firstOrNull

import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.repo.EntitlementRepository

class SubscriptionViewModel(
    private val getSubscriptionOptionsUseCase: GetSubscriptionOptionsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restoreSubscriptionUseCase: RestoreSubscriptionUseCase,
    private val checkSubscriptionStatusUseCase: CheckSubscriptionStatusUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeSubscriptionStatus()
        observeEntitlements()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val options = getSubscriptionOptionsUseCase.getOptions()
            val features = getSubscriptionOptionsUseCase.getFeatures()
            val defaultSelectedId = options.find { it.isMostPopular }?.id ?: options.firstOrNull()?.id ?: "6-month-plan"

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

    private fun observeEntitlements() {
        viewModelScope.launch {
            entitlementRepository.entitlementsFlow.collect { entitlements ->
                val activeType = entitlements.activeSubscriptionType
                val preSelectedOptionId = when (activeType) {
                    SubscriptionType.MONTHLY -> "monthly-plan"
                    SubscriptionType.SIX_MONTHS -> "6-month-plan"
                    SubscriptionType.LIFETIME -> "urdu_fonts_lifetime"
                    null -> "6-month-plan"
                }
                _uiState.update { state ->
                    val isPlanTypeChanged = state.activePlanType != activeType
                    val isSelectionEmpty = state.selectedOptionId.isBlank()
                    val newSelection = if (isPlanTypeChanged || isSelectionEmpty) preSelectedOptionId else state.selectedOptionId
                    state.copy(
                        activePlanType = activeType,
                        selectedOptionId = newSelection,
                        pendingChange = entitlements.pendingChange
                    )
                }
            }
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun getActionButtonText(state: SubscriptionUiState): String {
        if (state.isPurchasing) return "Processing..."

        val selectedId = state.selectedOptionId
        val activeType = state.activePlanType

        val selectedType = when (selectedId) {
            "monthly-plan" -> SubscriptionType.MONTHLY
            "6-month-plan" -> SubscriptionType.SIX_MONTHS
            "urdu_fonts_lifetime", "lifetime-purchase" -> SubscriptionType.LIFETIME
            else -> null
        }

        if (activeType != null && activeType == selectedType) {
            return "Manage Subscription"
        }

        if (activeType == SubscriptionType.LIFETIME) {
            return "Lifetime Access Active"
        }

        if (activeType == SubscriptionType.MONTHLY && selectedType == SubscriptionType.SIX_MONTHS) {
            return "Upgrade to 6-Months"
        }

        if (activeType == SubscriptionType.SIX_MONTHS && selectedType == SubscriptionType.MONTHLY) {
            return "Downgrade to Monthly"
        }

        if (selectedType == SubscriptionType.LIFETIME) {
            return "Get Lifetime Access"
        }

        return "Subscribe"
    }

    fun purchase(activity: Activity, onRequireLogin: () -> Unit) {
        val selectedOption = _uiState.value.options.find { it.id == _uiState.value.selectedOptionId }
            ?: return

        viewModelScope.launch {
            val session = getUserSessionUseCase().firstOrNull()
            if (session == null || session.email.isBlank()) {
                onRequireLogin()
                return@launch
            }

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

    fun openManageSubscriptions(context: android.content.Context) {
        val activeSku = when (_uiState.value.activePlanType) {
            SubscriptionType.MONTHLY, SubscriptionType.SIX_MONTHS -> "urdu_fonts_pro"
            SubscriptionType.LIFETIME -> "urdu_fonts_lifetime"
            null -> null
        }
        val url = if (activeSku != null) {
            "https://play.google.com/store/account/subscriptions?sku=$activeSku&package=${context.packageName}"
        } else {
            "https://play.google.com/store/account/subscriptions?package=${context.packageName}"
        }
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(url)
        )
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Unable to open Google Play Store", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

package com.urdufonts.app.ui.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.repo.EntitlementRepository
import com.urdufonts.app.domain.usecases.CheckSubscriptionStatusUseCase
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

import com.urdufonts.app.domain.usecases.GetProductsUseCase

class SubscriptionViewModel(
    private val getSubscriptionOptionsUseCase: GetSubscriptionOptionsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restoreSubscriptionUseCase: RestoreSubscriptionUseCase,
    private val checkSubscriptionStatusUseCase: CheckSubscriptionStatusUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val entitlementRepository: EntitlementRepository,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    // The user's explicit plan pick. null = let active plan or default decide.
    // Lives OUTSIDE uiState so no observer can silently overwrite it.
    // Mirrors the Unit Converter pattern: private var userSelectedPlanId.
    private var userSelectedOptionId: String? = null

    init {
        loadData()
        observeSubscriptionStatus()
        observeEntitlements()
    }

    // Call this when the subscription screen opens so the selection always
    // resets to the active plan (or best-value default) for a fresh experience.
    fun onScreenOpened() {
        userSelectedOptionId = null
        _uiState.update { resolveSelection(it) }
    }

    // ─── Single Source of Truth for Selection ────────────────────────────────
    // Priority:  user explicit tap  >  active/subscribed plan  >  most popular  >  first option
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

    // Convenience wrapper — applies the resolver to an existing state snapshot
    private fun resolveSelection(state: SubscriptionUiState): SubscriptionUiState {
        val resolvedId = resolveSelectedOptionId(
            options        = state.options,
            activePlanType = state.activePlanType,
            userSelectedId = userSelectedOptionId
        )
        return state.copy(selectedOptionId = resolvedId)
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val rawOptions = getSubscriptionOptionsUseCase.getOptions()
            val backendProductsResult = getProductsUseCase("urdufonts")
            val backendProducts = backendProductsResult.getOrDefault(emptyList())

            val options = rawOptions.map { option ->
                val matchingProduct = backendProducts.find { product ->
                    product.sku.equals(option.id, ignoreCase = true) ||
                    (option.id == "monthly-plan" && (product.sku.contains("monthly", ignoreCase = true) || product.sku.contains("pro", ignoreCase = true))) ||
                    (option.id == "6-month-plan" && product.sku.contains("6", ignoreCase = true)) ||
                    (option.id == "urdu_fonts_lifetime" && product.sku.contains("lifetime", ignoreCase = true))
                }
                val backendTitle = matchingProduct?.name?.takeIf { it.isNotBlank() }

                if (backendTitle != null) {
                    option.copy(title = backendTitle)
                } else {
                    option
                }
            }

            val currentEntitlements = entitlementRepository.entitlementsFlow.firstOrNull()
            val features = getSubscriptionOptionsUseCase.getFeatures(currentEntitlements)
            _uiState.update { state ->
                resolveSelection(
                    state.copy(options = options, features = features, isLoading = false)
                )
            }
        }
    }

    fun retryLoadData() = loadData()

    // ─── Observers ────────────────────────────────────────────────────────────
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
                val dynamicFeatures = getSubscriptionOptionsUseCase.getFeatures(entitlements)
                _uiState.update { state ->
                    // If the plan genuinely changed (user just purchased / plan expired),
                    // clear the manual pick so the new active plan becomes the default.
                    if (state.activePlanType != activeType && activeType != null) {
                        userSelectedOptionId = null
                    }
                    resolveSelection(
                        state.copy(
                            activePlanType = activeType,
                            pendingChange  = entitlements.pendingChange,
                            features       = dynamicFeatures
                        )
                    )
                }
            }
        }
    }

    // ─── User Actions ─────────────────────────────────────────────────────────
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

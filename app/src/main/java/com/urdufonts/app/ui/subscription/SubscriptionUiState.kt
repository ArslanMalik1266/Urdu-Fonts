package com.urdufonts.app.ui.subscription

import com.urdufonts.app.domain.models.PendingChangeDetails
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.SubscriptionType

enum class SubscriptionActionType {
    SUBSCRIBE,
    UPGRADE,
    DOWNGRADE,
    MANAGE_EXTERNAL,
    LIFETIME_ACTIVE,
    PROCESSING
}

data class SubscriptionUiState(
    val options: List<SubscriptionOption> = emptyList(),
    val selectedOptionId: String = "",
    val activePlanType: SubscriptionType? = null,
    val pendingChange: PendingChangeDetails? = null,
    val features: List<PremiumFeature> = emptyList(),
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val isProUser: Boolean = false,
    val userMessage: String? = null
) {
    private val selectedOption: SubscriptionOption?
        get() = options.firstOrNull { it.id == selectedOptionId }

    val selectedSubscriptionType: SubscriptionType?
        get() = SubscriptionType.fromSku(selectedOptionId)

    /** True when the selected plan is the one a scheduled change moves to. */
    fun isPendingTarget(optionId: String): Boolean =
        pendingChange?.productSku?.equals(optionId, ignoreCase = true) == true

    val actionType: SubscriptionActionType
        get() {
            if (activePlanType == SubscriptionType.LIFETIME) return SubscriptionActionType.LIFETIME_ACTIVE
            val selected = selectedSubscriptionType
            if (activePlanType != null && activePlanType == selected) return SubscriptionActionType.MANAGE_EXTERNAL
            if (isPendingTarget(selectedOptionId)) return SubscriptionActionType.MANAGE_EXTERNAL
            val active = activePlanType ?: return SubscriptionActionType.SUBSCRIBE
            val targetRank = selected?.rank ?: selectedOption?.tierRank ?: 0
            return when {
                active == SubscriptionType.LEGACY -> SubscriptionActionType.UPGRADE
                targetRank > active.rank -> SubscriptionActionType.UPGRADE
                else -> SubscriptionActionType.DOWNGRADE
            }
        }

    val actionButtonText: String
        get() {
            val name = selectedOption?.title ?: selectedSubscriptionType?.displayName ?: ""
            return when (actionType) {
                SubscriptionActionType.MANAGE_EXTERNAL -> "Manage Subscription"
                SubscriptionActionType.LIFETIME_ACTIVE -> "Lifetime Access Active"
                SubscriptionActionType.UPGRADE -> "Upgrade to $name"
                SubscriptionActionType.DOWNGRADE -> "Switch to $name"
                else -> "Get $name"
            }
        }

    val isActionEnabled: Boolean
        get() = actionType != SubscriptionActionType.LIFETIME_ACTIVE && !isPurchasing && selectedOption != null
}

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
    val selectedSubscriptionType: SubscriptionType?
        get() = when (selectedOptionId) {
            "monthly-plan" -> SubscriptionType.MONTHLY
            "6-month-plan" -> SubscriptionType.SIX_MONTHS
            "urdu_fonts_lifetime", "lifetime-purchase" -> SubscriptionType.LIFETIME
            else -> null
        }

    val actionType: SubscriptionActionType
        get() {
            val selectedType = selectedSubscriptionType

            if (activePlanType != null && activePlanType == selectedType) {
                return SubscriptionActionType.MANAGE_EXTERNAL
            }

            if (activePlanType == SubscriptionType.LIFETIME) {
                return SubscriptionActionType.LIFETIME_ACTIVE
            }

            if (activePlanType == SubscriptionType.MONTHLY && selectedType == SubscriptionType.SIX_MONTHS) {
                return SubscriptionActionType.UPGRADE
            }

            if (activePlanType == SubscriptionType.SIX_MONTHS && selectedType == SubscriptionType.MONTHLY) {
                return SubscriptionActionType.DOWNGRADE
            }

            return SubscriptionActionType.SUBSCRIBE
        }

    val actionButtonText: String
        get() = when (actionType) {
            SubscriptionActionType.MANAGE_EXTERNAL -> "Manage Subscription"
            SubscriptionActionType.LIFETIME_ACTIVE -> "Lifetime Access Active"
            SubscriptionActionType.UPGRADE -> "Upgrade to 6-Months"
            SubscriptionActionType.DOWNGRADE -> "Downgrade to Monthly"
            SubscriptionActionType.SUBSCRIBE -> {
                if (selectedSubscriptionType == SubscriptionType.LIFETIME) {
                    "Get Lifetime Access"
                } else {
                    "Subscribe"
                }
            }
            else -> "Subscribe"
        }
}

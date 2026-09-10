package com.urdufonts.app.ui.subscription

import com.urdufonts.app.domain.models.PendingChangeDetails
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.SubscriptionType

data class SubscriptionUiState(
    val options: List<SubscriptionOption> = emptyList(),
    val selectedOptionId: String = "6-month-plan",
    val activePlanType: SubscriptionType? = null,
    val pendingChange: PendingChangeDetails? = null,
    val features: List<PremiumFeature> = emptyList(),
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val isProUser: Boolean = false,
    val userMessage: String? = null
)

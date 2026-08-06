package com.urdufonts.app.ui.subscription

import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption

data class SubscriptionUiState(
    val options: List<SubscriptionOption> = emptyList(),
    val selectedOptionId: String = "six_months",
    val features: List<PremiumFeature> = emptyList(),
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val isProUser: Boolean = false,
    val userMessage: String? = null
)

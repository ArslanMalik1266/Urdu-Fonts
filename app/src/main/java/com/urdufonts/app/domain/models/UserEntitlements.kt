package com.urdufonts.app.domain.models

data class UserEntitlements(
    val unlocked: Boolean = false,
    val externalUserId: String? = null,
    val entitlements: List<EntitlementItem> = emptyList(),
    val subscription: SubscriptionDetails? = null,
    val pendingChange: PendingChangeDetails? = null,
    val lastUpdated: String? = null
) {
    val hasNoAds: Boolean
        get() = unlocked || entitlements.any { it.key == "no_ads" && it.granted }

    val hasUnlimitedDownloads: Boolean
        get() = unlocked || entitlements.any { it.key == "unlimited_downloads" && it.granted }

    val activeSubscriptionType: SubscriptionType?
        get() {
            if (!unlocked) return null
            return when {
                subscription?.productId == "urdu_fonts_lifetime" || subscription?.sku?.contains("lifetime", ignoreCase = true) == true -> SubscriptionType.LIFETIME
                subscription?.basePlanId == "6-month-plan" || subscription?.sku?.contains("6_months", ignoreCase = true) == true || subscription?.sku?.contains("6-month", ignoreCase = true) == true -> SubscriptionType.SIX_MONTHS
                subscription?.basePlanId == "monthly-plan" || subscription?.sku?.contains("monthly", ignoreCase = true) == true -> SubscriptionType.MONTHLY
                else -> SubscriptionType.LIFETIME
            }
        }
}

data class EntitlementItem(
    val key: String,
    val granted: Boolean = false,
    val expiresAt: String? = null,
    val source: String? = null
)

data class SubscriptionDetails(
    val sku: String? = null,
    val productId: String? = null,
    val basePlanId: String? = null,
    val status: String? = null,
    val expiry: String? = null,
    val autoRenewing: Boolean = false
)

data class PendingChangeDetails(
    val productSku: String? = null,
    val planName: String? = null,
    val effectiveAt: String? = null,
    val updateType: String? = null
)

enum class SubscriptionType {
    MONTHLY,
    SIX_MONTHS,
    LIFETIME
}

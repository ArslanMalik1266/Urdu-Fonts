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
            if (unlocked && (subscription == null || (subscription.sku == null && subscription.basePlanId == null))) {
                return SubscriptionType.LIFETIME
            }
            val basePlan = subscription?.basePlanId.orEmpty().lowercase()
            val sku = subscription?.sku.orEmpty().lowercase()
            val prodId = subscription?.productId.orEmpty().lowercase()

            return when {
                prodId == "urdu_fonts_lifetime" || sku.contains("lifetime") || basePlan.contains("lifetime") -> SubscriptionType.LIFETIME
                basePlan.contains("6-month") || basePlan.contains("6_month") || basePlan.contains("6_months") ||
                        basePlan.contains("6 months") || sku.contains("6_month") || sku.contains("6_months") ||
                        sku.contains("6-month") || basePlan.contains("annual") || basePlan.contains("yearly") ||
                        sku.contains("annual") || sku.contains("yearly") -> SubscriptionType.SIX_MONTHS
                basePlan.contains("monthly") || basePlan.contains("month") || sku.contains("monthly") -> SubscriptionType.MONTHLY
                unlocked -> SubscriptionType.LIFETIME
                else -> null
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

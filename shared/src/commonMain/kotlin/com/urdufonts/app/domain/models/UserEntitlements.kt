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

    /** Fonts this user may download a day; null means no limit (old paid plans and lifetime). */
    val dailyFontLimit: Int?
        get() = when (activeSubscriptionType) {
            null -> SubscriptionType.FREE_DAILY_FONTS
            SubscriptionType.BASIC -> 50
            SubscriptionType.PRO -> 100
            SubscriptionType.BUSINESS -> 200
            SubscriptionType.LEGACY, SubscriptionType.LIFETIME -> null
        }

    /** The plan the user is on now. Null on the free plan. */
    val activeSubscriptionType: SubscriptionType?
        get() {
            if (!unlocked) return null
            val sku = subscription?.sku.orEmpty().lowercase()
            SubscriptionType.fromSku(sku)?.let { return it }
            fun has(key: String) = entitlements.any { it.key == key && it.granted }
            return when {
                has("font_downloads_200") -> SubscriptionType.BUSINESS
                has("font_downloads_100") -> SubscriptionType.PRO
                has("font_downloads_50") -> SubscriptionType.BASIC
                sku.contains("lifetime") -> SubscriptionType.LIFETIME
                // Bought before the three plans existed (old monthly / 6-month plan)
                else -> SubscriptionType.LEGACY
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
    val planName: String? = null,
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

enum class SubscriptionType(val sku: String?, val rank: Int, val displayName: String) {
    BASIC("monthly", 1, "Basic"),
    PRO("monthly-pro", 2, "Pro"),
    BUSINESS("monthly-business", 3, "Business"),
    LEGACY(null, 0, "Premium"),
    LIFETIME("urdu_fonts_lifetime", 4, "Lifetime");

    companion object {
        const val FREE_DAILY_FONTS = 10

        /** App Store product ids (iOS), which the backend reports instead of the Play skus for iOS purchases. */
        private val APP_STORE_IDS = mapOf(
            "urdu_fonts_basic_monthly" to BASIC,
            "urdu_fonts_pro_monthly" to PRO,
            "urdu_fonts_business_monthly" to BUSINESS,
        )

        fun fromSku(sku: String?): SubscriptionType? =
            entries.firstOrNull { it.sku != null && it.sku.equals(sku, ignoreCase = true) }
                ?: APP_STORE_IDS[sku?.lowercase()]
    }
}

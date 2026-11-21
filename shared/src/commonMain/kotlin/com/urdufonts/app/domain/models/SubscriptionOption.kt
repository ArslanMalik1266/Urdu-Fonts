package com.urdufonts.app.domain.models

data class SubscriptionOption(
    val id: String,
    val title: String,
    val billingPeriodText: String,
    val priceText: String,
    val pricePeriodSubtitle: String? = null,
    val originalPriceText: String? = null,
    val discountTag: String? = null,
    val iconRes: Int,
    val isMostPopular: Boolean = false,
    /** What the plan gives, as the entitlement names set on the IAP dashboard. */
    val features: List<String> = emptyList(),
    /** Higher is a bigger plan (Basic 1, Pro 2, Business 3). */
    val tierRank: Int = 0
)

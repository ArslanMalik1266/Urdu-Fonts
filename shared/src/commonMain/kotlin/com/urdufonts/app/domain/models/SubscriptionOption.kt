package com.urdufonts.app.domain.models

import org.jetbrains.compose.resources.DrawableResource
data class SubscriptionOption(
    val id: String,
    val title: String,
    val billingPeriodText: String,
    val priceText: String,
    val pricePeriodSubtitle: String? = null,
    val originalPriceText: String? = null,
    val discountTag: String? = null,
    val iconRes: DrawableResource,
    val isMostPopular: Boolean = false,
    /** What the plan gives, as the entitlement names set on the IAP dashboard. */
    val features: List<String> = emptyList(),
    /** Higher is a bigger plan (Basic 1, Pro 2, Business 3). */
    val tierRank: Int = 0,
    /** The store product id when it differs from [id] (iOS App Store ids); matched against the dashboard sku. */
    val storeProductId: String? = null
)

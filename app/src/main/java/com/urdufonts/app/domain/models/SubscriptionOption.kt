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
    val isMostPopular: Boolean = false
)

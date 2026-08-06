package com.urdufonts.app.domain.usecases

import com.urdufonts.app.R
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption

class GetSubscriptionOptionsUseCase {

    fun getOptions(): List<SubscriptionOption> {
        return listOf(
            SubscriptionOption(
                id = "monthly",
                title = "Monthly",
                billingPeriodText = "Billed monthly",
                priceText = "$9.99",
                pricePeriodSubtitle = "per month",
                iconRes = R.drawable.ic_calender
            ),
            SubscriptionOption(
                id = "six_months",
                title = "6-Months",
                billingPeriodText = "Billed every 6 months",
                priceText = "$49.99",
                originalPriceText = "$59.99",
                discountTag = "15% OFF",
                iconRes = R.drawable.ic_six_months,
                isMostPopular = true
            ),
            SubscriptionOption(
                id = "lifetime",
                title = "Lifetime",
                billingPeriodText = "One-time payment",
                priceText = "$79.99",
                originalPriceText = "$149.99",
                iconRes = R.drawable.ic_infinity
            )
        )
    }

    fun getFeatures(): List<PremiumFeature> {
        return listOf(
            PremiumFeature(
                id = "unlimited_downloads",
                title = "Unlimited Downloads",
                subtitle = "No Daily Limit",
                iconRes = R.drawable.ic_download
            ),
            PremiumFeature(
                id = "premium_fonts",
                title = "Premium Fonts",
                subtitle = "Access to Premium Fonts",
                iconRes = R.drawable.ic_premium
            ),
            PremiumFeature(
                id = "font_family",
                title = "Urdu Font Family",
                subtitle = "Download Multiple\nFont Weights",
                iconRes = R.drawable.preview_icon_splash
            ),
            PremiumFeature(
                id = "no_ads",
                title = "No Ads",
                subtitle = "Download Seamless",
                iconRes = R.drawable.ic_no_ads
            )
        )
    }
}

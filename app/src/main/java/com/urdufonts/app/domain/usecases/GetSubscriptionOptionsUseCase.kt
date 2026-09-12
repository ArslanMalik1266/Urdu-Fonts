package com.urdufonts.app.domain.usecases

import com.urdufonts.app.R
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.repo.BillingRepository

class GetSubscriptionOptionsUseCase(
    private val billingRepository: BillingRepository
) {

    suspend fun getOptions(): List<SubscriptionOption> {
        return billingRepository.getSubscriptionOptions()
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

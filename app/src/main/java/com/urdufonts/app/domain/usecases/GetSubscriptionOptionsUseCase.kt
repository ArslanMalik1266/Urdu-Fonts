package com.urdufonts.app.domain.usecases

import com.urdufonts.app.R
import com.urdufonts.app.domain.models.EntitlementItem
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.UserEntitlements
import com.urdufonts.app.domain.repo.BillingRepository

class GetSubscriptionOptionsUseCase(
    private val billingRepository: BillingRepository
) {

    suspend fun getOptions(): List<SubscriptionOption> {
        return billingRepository.getSubscriptionOptions()
    }

    fun getFeatures(userEntitlements: UserEntitlements? = null): List<PremiumFeature> {
        val items = userEntitlements?.entitlements.orEmpty()
        if (items.isEmpty()) {
            return getDefaultFeatures()
        }

        return items.map { item ->
            mapEntitlementToFeature(item)
        }
    }

    private fun mapEntitlementToFeature(item: EntitlementItem): PremiumFeature {
        return when (item.key.lowercase()) {
            "unlimited_downloads" -> PremiumFeature(
                id = item.key,
                title = "Unlimited Downloads",
                subtitle = "No Daily Limit",
                iconRes = R.drawable.ic_download
            )
            "no_ads" -> PremiumFeature(
                id = item.key,
                title = "No Ads",
                subtitle = "Download Seamless",
                iconRes = R.drawable.ic_no_ads
            )
            "premium_fonts" -> PremiumFeature(
                id = item.key,
                title = "Premium Fonts",
                subtitle = "Access to Premium Fonts",
                iconRes = R.drawable.ic_premium
            )
            "font_family" -> PremiumFeature(
                id = item.key,
                title = "Urdu Font Family",
                subtitle = "Download Multiple\nFont Weights",
                iconRes = R.drawable.preview_icon_splash
            )
            else -> {
                val formattedTitle = item.key.replace("_", " ").split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                PremiumFeature(
                    id = item.key,
                    title = formattedTitle,
                    subtitle = "Included with Pro",
                    iconRes = R.drawable.ic_pass
                )
            }
        }
    }

    private fun getDefaultFeatures(): List<PremiumFeature> {
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

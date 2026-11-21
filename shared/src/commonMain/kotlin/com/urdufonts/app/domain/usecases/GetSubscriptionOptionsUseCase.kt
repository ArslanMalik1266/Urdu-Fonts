package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.EntitlementItem
import com.urdufonts.app.domain.models.PremiumFeature
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.UserEntitlements
import com.urdufonts.app.domain.repo.BillingRepository

class GetSubscriptionOptionsUseCase(
    private val billingRepository: BillingRepository,
    private val icons: FeatureIcons
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
        val key = item.key.lowercase()
        return when {
            key.contains("unlimited") || key.contains("download") -> PremiumFeature(
                id = item.key,
                title = "Unlimited Downloads",
                subtitle = "No Daily Limit",
                iconRes = icons.download
            )
            key.contains("ad") -> PremiumFeature(
                id = item.key,
                title = "No Ads",
                subtitle = "Download Seamless",
                iconRes = icons.noAds
            )
            key.contains("premium") -> PremiumFeature(
                id = item.key,
                title = "Premium Fonts",
                subtitle = "Access to Premium Fonts",
                iconRes = icons.premium
            )
            key.contains("font") || key.contains("family") || key.contains("weight") -> PremiumFeature(
                id = item.key,
                title = "Urdu Font Family",
                subtitle = "Download Multiple\nFont Weights",
                iconRes = icons.fontFamily
            )
            else -> {
                val formattedTitle = item.key.replace("_", " ").split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                PremiumFeature(
                    id = item.key,
                    title = formattedTitle,
                    subtitle = "Included with Pro",
                    iconRes = icons.included
                )
            }
        }
    }

    private fun getDefaultFeatures(): List<PremiumFeature> {
        return listOf(
            PremiumFeature(
                id = "no_ads",
                title = "No Ads",
                subtitle = "Download Seamless",
                iconRes = icons.noAds
            ),
            PremiumFeature(
                id = "unlimited_downloads",
                title = "Unlimited Downloads",
                subtitle = "No Daily Limit",
                iconRes = icons.download
            ),
            PremiumFeature(
                id = "premium_fonts",
                title = "Premium Fonts",
                subtitle = "Access to Premium Fonts",
                iconRes = icons.premium
            ),
            PremiumFeature(
                id = "font_family",
                title = "Urdu Font Family",
                subtitle = "Download Multiple\nFont Weights",
                iconRes = icons.fontFamily
            )
        )
    }
}

/** Icon ids for the premium feature list; the app supplies its own drawable resources. */
interface FeatureIcons {
    val download: Int
    val noAds: Int
    val premium: Int
    val fontFamily: Int
    val included: Int
}

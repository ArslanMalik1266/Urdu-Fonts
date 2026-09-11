package com.urdufonts.app.data.remote.mapper

import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.PendingChangeDto
import com.urdufonts.app.domain.models.EntitlementItem
import com.urdufonts.app.domain.models.PendingChangeDetails
import com.urdufonts.app.domain.models.SubscriptionDetails
import com.urdufonts.app.domain.models.UserEntitlements

object EntitlementsMapper {

    fun toDomain(dto: EntitlementsResponseDto): UserEntitlements {
        var isUnlocked = dto.unlocked
        var activeSku: String? = null
        var activePlanName: String? = null
        var pendingChangeDetails: PendingChangeDetails? = null
        val entitlementList = mutableListOf<EntitlementItem>()

        dto.entitlements.forEach { (key, item) ->
            if (item.unlocked) {
                isUnlocked = true
            }
            if (item.pendingChange != null) {
                pendingChangeDetails = toDomain(item.pendingChange)
            }
            // Prioritize primary subscription entitlement keys or items with actual subscription/plan metadata
            if (key == "urdu_fonts_pro" || key == "pro" || key == "pro_features" || item.subscriptionId != null || !item.planName.isNullOrBlank()) {
                if (!item.productSku.isNullOrBlank()) {
                    activeSku = item.productSku
                }
                if (!item.planName.isNullOrBlank()) {
                    activePlanName = item.planName
                }
            } else if (activeSku == null && !item.productSku.isNullOrBlank()) {
                activeSku = item.productSku
            }

            entitlementList.add(
                EntitlementItem(
                    key = key,
                    granted = item.unlocked,
                    expiresAt = item.expiresAt,
                    source = item.source
                )
            )
        }

        val subDetails = if (isUnlocked && (activeSku != null || activePlanName != null)) {
            SubscriptionDetails(
                sku = activeSku,
                productId = activeSku,
                basePlanId = activePlanName ?: activeSku
            )
        } else null

        return UserEntitlements(
            unlocked = isUnlocked,
            externalUserId = dto.externalUserId,
            entitlements = entitlementList,
            subscription = subDetails,
            pendingChange = pendingChangeDetails
        )
    }

    private fun toDomain(dto: PendingChangeDto): PendingChangeDetails {
        return PendingChangeDetails(
            productSku = dto.productSku,
            planName = dto.planName,
            effectiveAt = dto.effectiveAt,
            updateType = dto.updateType
        )
    }
}

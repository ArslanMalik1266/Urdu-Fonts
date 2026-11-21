package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EntitlementsResponseDto(
    @SerialName("external_user_id")
    val externalUserId: String? = null,
    @SerialName("unlocked")
    val unlocked: Boolean = false,
    @SerialName("entitlements")
    val entitlements: Map<String, EntitlementDetailDto> = emptyMap()
)

@Serializable
data class EntitlementDetailDto(
    @SerialName("type")
    val type: String? = null,
    @SerialName("unlocked")
    val unlocked: Boolean = false,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("source")
    val source: String? = null,
    @SerialName("subscription_id")
    val subscriptionId: Long? = null,
    @SerialName("transaction_id")
    val transactionId: Long? = null,
    @SerialName("product_sku")
    val productSku: String? = null,
    @SerialName("plan_name")
    val planName: String? = null,
    @SerialName("pending_change")
    val pendingChange: PendingChangeDto? = null
)

@Serializable
data class PendingChangeDto(
    @SerialName("product_sku")
    val productSku: String? = null,
    @SerialName("plan_name")
    val planName: String? = null,
    @SerialName("effective_at")
    val effectiveAt: String? = null,
    @SerialName("update_type")
    val updateType: String? = null
)

@Serializable
data class PurchaseConfirmRequestDto(
    // Left out for a signed-in member: the backend takes the member from the token
    @SerialName("external_user_id")
    val externalUserId: String?,
    @SerialName("sku")
    val sku: String,
    @SerialName("purchase_token")
    val purchaseToken: String,
    @SerialName("product_type")
    val productType: String
)

@Serializable
data class PurchaseConfirmResponseDto(
    @SerialName("status")
    val status: String? = null,
    @SerialName("subscription_status")
    val subscriptionStatus: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("entitlements")
    val entitlements: EntitlementsResponseDto? = null
) {
    val isConfirmed: Boolean
        get() = status == "confirmed" || subscriptionStatus == "active" || entitlements?.unlocked == true || entitlements?.entitlements?.values?.any { it.unlocked } == true
}

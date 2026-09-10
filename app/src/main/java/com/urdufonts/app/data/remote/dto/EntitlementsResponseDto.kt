package com.urdufonts.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EntitlementsResponseDto(
    @SerializedName("external_user_id")
    val externalUserId: String? = null,
    @SerializedName("unlocked")
    val unlocked: Boolean = false,
    @SerializedName("entitlements")
    val entitlements: Map<String, EntitlementDetailDto> = emptyMap()
)

data class EntitlementDetailDto(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("unlocked")
    val unlocked: Boolean = false,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("subscription_id")
    val subscriptionId: Long? = null,
    @SerializedName("transaction_id")
    val transactionId: Long? = null,
    @SerializedName("product_sku")
    val productSku: String? = null,
    @SerializedName("plan_name")
    val planName: String? = null,
    @SerializedName("pending_change")
    val pendingChange: PendingChangeDto? = null
)

data class PendingChangeDto(
    @SerializedName("product_sku")
    val productSku: String? = null,
    @SerializedName("plan_name")
    val planName: String? = null,
    @SerializedName("effective_at")
    val effectiveAt: String? = null,
    @SerializedName("update_type")
    val updateType: String? = null
)

data class PurchaseConfirmRequestDto(
    @SerializedName("external_user_id")
    val externalUserId: String,
    @SerializedName("sku")
    val sku: String,
    @SerializedName("purchase_token")
    val purchaseToken: String,
    @SerializedName("product_type")
    val productType: String
)

data class PurchaseConfirmResponseDto(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("subscription_status")
    val subscriptionStatus: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("entitlements")
    val entitlements: EntitlementsResponseDto? = null
) {
    val isConfirmed: Boolean
        get() = status == "confirmed" || subscriptionStatus == "active" || entitlements?.unlocked == true || entitlements?.entitlements?.values?.any { it.unlocked } == true
}

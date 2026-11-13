package com.urdufonts.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductsResponseDto(
    @SerializedName("app_slug") val appSlug: String? = null,
    @SerializedName("products") val products: List<ProductDto> = emptyList()
)

data class ProductDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("product_sku") val productSku: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("plan_name") val planName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("product_type") val productType: String? = null,
    @SerializedName("tier_rank") val tierRank: Int? = null,
    @SerializedName("entitlements") val entitlements: List<ProductEntitlementDto>? = null
) {
    val effectiveSku: String
        get() = productSku ?: sku ?: ""

    val effectiveName: String
        get() = name ?: productName ?: planName ?: effectiveSku
}

data class ProductEntitlementDto(
    @SerializedName("key") val key: String? = null,
    @SerializedName("name") val name: String? = null
)

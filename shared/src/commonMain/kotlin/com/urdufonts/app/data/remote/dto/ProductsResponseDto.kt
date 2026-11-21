package com.urdufonts.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ProductsResponseDto(
    @SerialName("app_slug") val appSlug: String? = null,
    @SerialName("products") val products: List<ProductDto> = emptyList()
)

@Serializable
data class ProductDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("product_sku") val productSku: String? = null,
    @SerialName("sku") val sku: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("product_type") val productType: String? = null,
    @SerialName("tier_rank") val tierRank: Int? = null,
    @SerialName("entitlements") val entitlements: List<ProductEntitlementDto>? = null
) {
    val effectiveSku: String
        get() = productSku ?: sku ?: ""

    val effectiveName: String
        get() = name ?: productName ?: planName ?: effectiveSku
}

@Serializable
data class ProductEntitlementDto(
    @SerialName("key") val key: String? = null,
    @SerialName("name") val name: String? = null
)

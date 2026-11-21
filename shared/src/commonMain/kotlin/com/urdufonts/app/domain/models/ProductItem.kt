package com.urdufonts.app.domain.models

data class ProductItem(
    val sku: String,
    val name: String,
    val tierRank: Int = 0,
    /** The plan's entitlement names, in the order the dashboard lists them. */
    val features: List<String> = emptyList()
)

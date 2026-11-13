package com.urdufonts.app.data.remote.mapper

import com.urdufonts.app.data.remote.dto.ProductsResponseDto
import com.urdufonts.app.domain.models.ProductItem

object ProductsMapper {
    fun toDomain(dto: ProductsResponseDto): List<ProductItem> {
        return dto.products.map { productDto ->
            ProductItem(
                sku = productDto.effectiveSku,
                name = productDto.effectiveName,
                tierRank = productDto.tierRank ?: 0,
                features = productDto.entitlements.orEmpty()
                    .sortedBy { cardOrder(it.key.orEmpty()) }
                    .mapNotNull { it.name?.trim()?.takeIf(String::isNotEmpty) }
            )
        }
    }

    // The order the website's pricing cards list them in; anything new goes last
    private val ORDER = listOf(
        "font_downloads", "calligraphy_downloads", "unlimited_tools", "commercial_calligraphy", "commercial_fonts",
        "png_", "ticket_support", "whatsapp_support", "request_font", "request_calligraphy", "priority_support", "personal_use"
    )

    private fun cardOrder(key: String): Int = ORDER.indexOfFirst { key.startsWith(it) }.let { if (it < 0) ORDER.size else it }
}

package com.urdufonts.app.data.remote.mapper

import com.urdufonts.app.data.remote.dto.ProductsResponseDto
import com.urdufonts.app.domain.models.ProductItem

object ProductsMapper {
    fun toDomain(dto: ProductsResponseDto): List<ProductItem> {
        return dto.products.map { productDto ->
            ProductItem(
                sku = productDto.effectiveSku,
                name = productDto.effectiveName
            )
        }
    }
}

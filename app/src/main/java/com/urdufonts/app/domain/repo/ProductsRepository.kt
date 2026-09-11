package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.ProductItem

interface ProductsRepository {
    suspend fun getProducts(appSlug: String = "urdufonts"): Result<List<ProductItem>>
}

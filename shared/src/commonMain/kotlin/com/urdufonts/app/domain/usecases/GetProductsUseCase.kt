package com.urdufonts.app.domain.usecases

import com.urdufonts.app.domain.models.ProductItem
import com.urdufonts.app.domain.repo.ProductsRepository

class GetProductsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(appSlug: String = "urdufonts"): Result<List<ProductItem>> {
        return repository.getProducts(appSlug)
    }
}

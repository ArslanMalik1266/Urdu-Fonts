package com.urdufonts.app.data.repository

import android.util.Log
import com.urdufonts.app.data.remote.api.EntitlementApiService
import com.urdufonts.app.data.remote.mapper.ProductsMapper
import com.urdufonts.app.domain.models.ProductItem
import com.urdufonts.app.domain.repo.ProductsRepository

class ProductsRepositoryImpl(
    private val apiService: EntitlementApiService
) : ProductsRepository {

    companion object {
        private const val TAG = "ProductsRepo"
        private const val BASE_URL = "https://iap.webscare.app"
        @Volatile private var cachedProducts: List<ProductItem>? = null
    }

    override suspend fun getProducts(appSlug: String): Result<List<ProductItem>> {
        val memoryCache = cachedProducts
        if (!memoryCache.isNullOrEmpty()) {
            Log.d(TAG, "Returning ${memoryCache.size} products from memory cache")
            return Result.success(memoryCache)
        }

        val url = "$BASE_URL/api/v1/apps/$appSlug/products?platform=android"
        Log.d(TAG, "Fetching products from GET $url")
        return try {
            val response = apiService.getProducts(
                url = url,
                authHeader = com.urdufonts.app.data.iap.IapConfig.appBearer
            )
            val domainProducts = ProductsMapper.toDomain(response)
            if (domainProducts.isNotEmpty()) {
                cachedProducts = domainProducts
            }
            Log.d(TAG, "Fetched ${domainProducts.size} products for appSlug=$appSlug -> ${domainProducts.map { "${it.sku}:${it.name}" }}")
            Result.success(domainProducts)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products for appSlug=$appSlug: ${e.message}", e)
            Result.failure(e)
        }
    }
}

package com.urdufonts.app.data.remote.api

import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmRequestDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface EntitlementApiService {

    @GET
    suspend fun getEntitlements(
        @Url url: String,
        @Query("external_user_id") externalUserId: String,
        @Header("Authorization") authHeader: String
    ): EntitlementsResponseDto

    @POST
    suspend fun confirmPurchase(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: PurchaseConfirmRequestDto
    ): PurchaseConfirmResponseDto

    @GET
    suspend fun getProducts(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): com.urdufonts.app.data.remote.dto.ProductsResponseDto
}

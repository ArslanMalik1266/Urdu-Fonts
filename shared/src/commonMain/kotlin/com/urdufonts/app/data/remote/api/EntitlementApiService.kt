package com.urdufonts.app.data.remote.api

import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.ProductsResponseDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmRequestDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Entitlement and product calls; every call takes a full URL. Null headers and parameters are left out. */
class EntitlementApiService(private val client: HttpClient) {

    suspend fun getEntitlements(
        url: String,
        externalUserId: String?,
        authHeader: String,
        deviceSecret: String?
    ): EntitlementsResponseDto = client.get(url) {
        parameter("external_user_id", externalUserId)
        header("Authorization", authHeader)
        header("X-Device-Secret", deviceSecret)
    }.body()

    suspend fun confirmPurchase(
        url: String,
        authHeader: String,
        deviceSecret: String?,
        request: PurchaseConfirmRequestDto
    ): PurchaseConfirmResponseDto = client.post(url) {
        header("Authorization", authHeader)
        header("X-Device-Secret", deviceSecret)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun getProducts(url: String, authHeader: String): ProductsResponseDto =
        client.get(url) { header("Authorization", authHeader) }.body()
}

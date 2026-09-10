package com.urdufonts.app.data.remote.datasource

import android.util.Log
import com.urdufonts.app.BuildConfig
import com.urdufonts.app.data.remote.api.EntitlementApiService
import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmRequestDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmResponseDto

class EntitlementRemoteDataSource(
    private val apiService: EntitlementApiService
) {
    companion object {
        private const val TAG = "UrduFontsEntitlements"
        private const val BASE_URL = "https://iap.webscare.app"
    }

    private val prettyGson = com.google.gson.GsonBuilder().setPrettyPrinting().create()

    suspend fun getEntitlements(externalUserId: String): EntitlementsResponseDto {
        val url = "$BASE_URL/api/v1/me/entitlements"
        Log.d(TAG, "Fetching Entitlements GET $url?external_user_id=$externalUserId")
        val response = apiService.getEntitlements(
            url = url,
            externalUserId = externalUserId,
            authHeader = "Bearer ${BuildConfig.BEARER_TOKEN}"
        )

        val rawPrettyJson = try { prettyGson.toJson(response) } catch (_: Exception) { "" }
        Log.d(TAG, "GET /api/v1/me/entitlements?external_user_id=$externalUserId\n\n$rawPrettyJson")
        Log.d(TAG, "GET Entitlements Response -> ExternalUser: ${response.externalUserId}, EntitlementsCount: ${response.entitlements.size}")
        response.entitlements.forEach { (key, item) ->
            val pendingLog = item.pendingChange?.let { pc ->
                "{productSku: ${pc.productSku}, planName: ${pc.planName}, effectiveAt: ${pc.effectiveAt}}"
            } ?: "null"
            Log.d(TAG, "  -> Entitlement [$key]: unlocked=${item.unlocked}, sku=${item.productSku}, planName=${item.planName}, pendingChange=$pendingLog")
        }
        return response
    }

    suspend fun confirmPurchase(request: PurchaseConfirmRequestDto): PurchaseConfirmResponseDto {
        val url = "$BASE_URL/api/v1/purchases/android/confirm"
        Log.d(TAG, "Confirming Purchase POST $url Payload -> userId: ${request.externalUserId}, sku: ${request.sku}, productType: ${request.productType}, tokenSnippet: ${request.purchaseToken.take(15)}...")
        val response = apiService.confirmPurchase(
            url = url,
            authHeader = "Bearer ${BuildConfig.BEARER_TOKEN}",
            request = request
        )

        val rawConfirmPrettyJson = try { prettyGson.toJson(response) } catch (_: Exception) { "" }
        Log.d(TAG, "POST /api/v1/purchases/android/confirm\n\n$rawConfirmPrettyJson")
        Log.d(TAG, "POST Confirm Purchase Response -> Status: ${response.status}, SubStatus: ${response.subscriptionStatus}, Confirmed: ${response.isConfirmed}")
        response.entitlements?.entitlements?.forEach { (key, item) ->
            val pendingLog = item.pendingChange?.let { pc ->
                "{productSku: ${pc.productSku}, planName: ${pc.planName}, effectiveAt: ${pc.effectiveAt}}"
            } ?: "null"
            Log.d(TAG, "  -> Confirm Entitlement [$key]: unlocked=${item.unlocked}, sku=${item.productSku}, planName=${item.planName}, pendingChange=$pendingLog")
        }
        return response
    }
}

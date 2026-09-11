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
        Log.d(TAG, "GET /api/v1/me/entitlements?external_user_id=$externalUserId\n\n$response")
        Log.d(TAG, "GET Entitlements Response -> ExternalUser: ${response.externalUserId}, Unlocked: ${response.unlocked}, EntitlementsCount: ${response.entitlements.size}")
        logEntitlementsDetails(response.entitlements)
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
        response.entitlements?.entitlements?.let { details ->
            logEntitlementsDetails(details)
        }
        return response
    }

    private fun logEntitlementsDetails(entitlements: Map<String, com.urdufonts.app.data.remote.dto.EntitlementDetailDto>) {
        entitlements.forEach { (key, item) ->
            Log.d(
                TAG,
                "  [EntitlementDetailDto] key='$key' | type=${item.type} | unlocked=${item.unlocked} | " +
                        "expiresAt=${item.expiresAt} | source=${item.source} | subscriptionId=${item.subscriptionId} | " +
                        "transactionId=${item.transactionId} | productSku=${item.productSku} | planName=${item.planName}"
            )
            if (item.pendingChange != null) {
                val pc = item.pendingChange
                Log.d(
                    TAG,
                    "    └─ [PendingChangeDto] productSku=${pc.productSku} | planName=${pc.planName} | " +
                            "effectiveAt=${pc.effectiveAt} | updateType=${pc.updateType}"
                )
            } else {
                Log.d(TAG, "    └─ [PendingChangeDto] null")
            }
        }
    }
}

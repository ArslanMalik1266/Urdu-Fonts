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
        val response = apiService.getEntitlements(
            url = url,
            externalUserId = externalUserId,
            authHeader = "Bearer ${BuildConfig.BEARER_TOKEN}"
        )

        val rawPrettyJson = try { prettyGson.toJson(response) } catch (_: Exception) { "" }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "GET $url?external_user_id=$externalUserId\n\n$rawPrettyJson")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        response.entitlements.forEach { (key, item) ->
            if (item.pendingChange != null) {
                val pc = item.pendingChange
                Log.i(TAG, "🔔 [PENDING CHANGE DETECTED in '$key'] -> productSku='${pc.productSku}', planName='${pc.planName}', effectiveAt='${pc.effectiveAt}'")
            }
        }
        return response
    }

    suspend fun confirmPurchase(request: PurchaseConfirmRequestDto): PurchaseConfirmResponseDto {
        val url = "$BASE_URL/api/v1/purchases/android/confirm"
        val response = apiService.confirmPurchase(
            url = url,
            authHeader = "Bearer ${BuildConfig.BEARER_TOKEN}",
            request = request
        )

        val rawConfirmPrettyJson = try { prettyGson.toJson(response) } catch (_: Exception) { "" }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "POST $url\n\n$rawConfirmPrettyJson")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        response.entitlements?.entitlements?.forEach { (key, item) ->
            if (item.pendingChange != null) {
                val pc = item.pendingChange
                Log.i(TAG, "🔔 [PENDING CHANGE DETECTED in '$key'] -> productSku='${pc.productSku}', planName='${pc.planName}', effectiveAt='${pc.effectiveAt}'")
            }
        }
        return response
    }
}

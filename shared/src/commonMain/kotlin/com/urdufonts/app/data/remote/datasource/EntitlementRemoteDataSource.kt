package com.urdufonts.app.data.remote.datasource

import com.urdufonts.app.platform.AppLog as Log
import com.urdufonts.app.data.remote.api.EntitlementApiService
import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmRequestDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmResponseDto
import com.urdufonts.app.data.remote.PrettyJson
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.encodeToString

class EntitlementRemoteDataSource(
    private val apiService: EntitlementApiService,
    private val identity: com.urdufonts.app.data.iap.IapIdentityProvider
) {
    companion object {
        private const val TAG = "UrduFontsEntitlements"
        private const val BASE_URL = "https://iap.webscare.app"
    }



    suspend fun getEntitlements(externalUserId: String): EntitlementsResponseDto {
        val url = "$BASE_URL/api/v1/me/entitlements"
        val who = identity.current()
        val response = apiService.getEntitlements(
            url = url,
            externalUserId = who.externalUserId,
            authHeader = who.bearer,
            deviceSecret = who.deviceSecret
        )

        val rawPrettyJson = try { PrettyJson.encodeToString(response) } catch (_: Exception) { "" }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "GET $url?external_user_id=$externalUserId\n\n$rawPrettyJson")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        response.entitlements.forEach { (key, item) ->
            item.pendingChange?.let { pc ->
                Log.i(TAG, "🔔 [PENDING CHANGE DETECTED in '$key'] -> productSku='${pc.productSku}', planName='${pc.planName}', effectiveAt='${pc.effectiveAt}'")
            }
        }
        return response
    }

    suspend fun confirmPurchase(request: PurchaseConfirmRequestDto): PurchaseConfirmResponseDto {
        val url = "$BASE_URL/api/v1/purchases/${com.urdufonts.app.data.iap.IapConfig.PLATFORM}/confirm"
        val who = identity.current()
        val sent = request.copy(externalUserId = who.externalUserId)
        Log.d(TAG, "POST $url as ${if (who.externalUserId == null) "member" else "guest ${who.externalUserId}"}: sku=${sent.sku}, type=${sent.productType}, token=${sent.purchaseToken.take(16)}…")
        val response = try {
            apiService.confirmPurchase(
                url = url,
                authHeader = who.bearer,
                deviceSecret = who.deviceSecret,
                request = sent
            )
        } catch (e: io.ktor.client.plugins.ResponseException) {
            Log.e(TAG, "CONFIRM FAILED: HTTP ${e.response.status.value} ${runCatching { e.response.bodyAsText() }.getOrNull()}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "CONFIRM FAILED: ${e::class.simpleName}: ${e.message}")
            throw e
        }

        val rawConfirmPrettyJson = try { PrettyJson.encodeToString(response) } catch (_: Exception) { "" }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "POST $url\n\n$rawConfirmPrettyJson")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        response.entitlements?.entitlements?.forEach { (key, item) ->
            item.pendingChange?.let { pc ->
                Log.i(TAG, "🔔 [PENDING CHANGE DETECTED in '$key'] -> productSku='${pc.productSku}', planName='${pc.planName}', effectiveAt='${pc.effectiveAt}'")
            }
        }
        return response
    }
}

package com.urdufonts.app.data.repository

import com.urdufonts.app.platform.currentTimeMillis

import com.urdufonts.app.platform.AppLog as Log
import com.urdufonts.app.data.remote.AppJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import com.urdufonts.app.data.local.EntitlementLocalDataSource
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.data.remote.datasource.EntitlementRemoteDataSource
import com.urdufonts.app.data.remote.dto.EntitlementsResponseDto
import com.urdufonts.app.data.remote.dto.PurchaseConfirmRequestDto
import com.urdufonts.app.data.remote.mapper.EntitlementsMapper
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.models.UserEntitlements
import com.urdufonts.app.domain.repo.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class EntitlementRepositoryImpl(
    private val remoteDataSource: EntitlementRemoteDataSource,
    private val localDataSource: EntitlementLocalDataSource,
    private val userPreferences: UserPreferences
) : EntitlementRepository {

    companion object {
        private const val TAG = "EntitlementRepoImpl"
        private const val COOLDOWN_MS = 60_000L
    }

    override val entitlementsFlow: Flow<UserEntitlements> = localDataSource.entitlementsJsonFlow
        .map { rawJson ->
            if (rawJson.isNullOrBlank()) {
                UserEntitlements()
            } else {
                try {
                    val dto = AppJson.decodeFromString<EntitlementsResponseDto>(rawJson)
                    EntitlementsMapper.toDomain(dto)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode cached entitlements JSON", e)
                    UserEntitlements()
                }
            }
        }

    override val isProUserFlow: Flow<Boolean> = entitlementsFlow.map { it.unlocked || it.hasNoAds || it.hasUnlimitedDownloads }

    override val activePlanTypeFlow: Flow<SubscriptionType?> = entitlementsFlow.map { it.activeSubscriptionType }

    override suspend fun refreshEntitlements(externalUserId: String, force: Boolean): Result<UserEntitlements> {
        if (externalUserId.isBlank()) {
            return Result.failure(Exception("externalUserId cannot be blank"))
        }

        val lastRefresh = localDataSource.lastRefreshTsFlow.firstOrNull() ?: 0L
        val now = currentTimeMillis()
        if (!force && now - lastRefresh < COOLDOWN_MS) {
            val cached = entitlementsFlow.firstOrNull() ?: UserEntitlements()
            return Result.success(cached)
        }

        return try {
            val dto = remoteDataSource.getEntitlements(externalUserId)
            val jsonString = AppJson.encodeToString(dto)
            val domain = EntitlementsMapper.toDomain(dto)

            localDataSource.saveExternalUserId(externalUserId)
            localDataSource.saveEntitlements(jsonString, domain.unlocked)
            localDataSource.savePlanType(domain.activeSubscriptionType)

            userPreferences.saveSubscriptionStatus(
                isPro = domain.unlocked || domain.hasUnlimitedDownloads,
                planId = domain.subscription?.basePlanId ?: domain.subscription?.sku
            )

            Result.success(domain)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch entitlements for $externalUserId", e)
            val cached = entitlementsFlow.firstOrNull() ?: UserEntitlements()
            Result.failure(e)
        }
    }

    override suspend fun confirmPurchase(
        externalUserId: String,
        sku: String,
        purchaseToken: String,
        productType: String
    ): Result<Boolean> {
        val request = PurchaseConfirmRequestDto(
            externalUserId = externalUserId,
            sku = sku,
            purchaseToken = purchaseToken,
            productType = productType
        )

        return applyConfirmation(externalUserId) { remoteDataSource.confirmPurchase(request) }
    }

    override suspend fun confirmIosPurchase(
        externalUserId: String,
        sku: String,
        transactionId: String,
        signedTransactionInfo: String,
        signedRenewalInfo: String?,
        productType: String
    ): Result<Boolean> {
        val request = com.urdufonts.app.data.remote.dto.IosPurchaseConfirmRequestDto(
            externalUserId = externalUserId,
            sku = sku,
            productType = productType,
            transactionId = transactionId,
            signedTransactionInfo = signedTransactionInfo,
            signedRenewalInfo = signedRenewalInfo
        )
        return applyConfirmation(externalUserId) { remoteDataSource.confirmIosPurchase(request) }
    }

    /** Sends a store purchase to the backend and keeps the entitlements it answers with (both stores). */
    private suspend fun applyConfirmation(
        externalUserId: String,
        send: suspend () -> com.urdufonts.app.data.remote.dto.PurchaseConfirmResponseDto
    ): Result<Boolean> {
        return try {
            val response = send()
            if (response.isConfirmed) {
                val confirmedEntitlements = response.entitlements
                if (confirmedEntitlements != null) {
                    val jsonString = AppJson.encodeToString(confirmedEntitlements)
                    val domain = EntitlementsMapper.toDomain(confirmedEntitlements)
                    localDataSource.saveExternalUserId(externalUserId)
                    localDataSource.saveEntitlements(jsonString, domain.unlocked)
                    localDataSource.savePlanType(domain.activeSubscriptionType)
                    userPreferences.saveSubscriptionStatus(
                        isPro = domain.unlocked || domain.hasUnlimitedDownloads,
                        planId = domain.subscription?.sku
                    )
                }
                // Always force refresh GET /api/v1/me/entitlements to obtain complete state including pending_change
                refreshEntitlements(externalUserId, force = true)
                Result.success(true)
            } else {
                Log.e(TAG, "Backend returned purchase confirm failure: ${response.message}")
                Result.failure(Exception(response.message ?: "Purchase confirmation failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to confirm purchase on backend", e)
            Result.failure(e)
        }
    }
}

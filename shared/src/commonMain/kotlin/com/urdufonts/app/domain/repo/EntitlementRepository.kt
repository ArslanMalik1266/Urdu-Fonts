package com.urdufonts.app.domain.repo

import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.models.UserEntitlements
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    val entitlementsFlow: Flow<UserEntitlements>
    val isProUserFlow: Flow<Boolean>
    val activePlanTypeFlow: Flow<SubscriptionType?>

    suspend fun refreshEntitlements(externalUserId: String, force: Boolean = false): Result<UserEntitlements>
    suspend fun confirmPurchase(
        externalUserId: String,
        sku: String,
        purchaseToken: String,
        productType: String = "subscription"
    ): Result<Boolean>

    /** An App Store purchase (StoreKit 2). Premium only from the backend's answer, as on Android. */
    suspend fun confirmIosPurchase(
        externalUserId: String,
        sku: String,
        transactionId: String,
        signedTransactionInfo: String,
        signedRenewalInfo: String?,
        productType: String
    ): Result<Boolean>
}

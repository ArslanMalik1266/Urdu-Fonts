package com.urdufonts.app.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.urdufonts.app.R
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.data.iap.httpStatus
import com.urdufonts.app.resources.*
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.repo.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

import com.urdufonts.app.domain.repo.EntitlementRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BillingRepositoryImpl(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val entitlementRepository: EntitlementRepository? = null,
    private val iapApi: com.urdufonts.app.data.iap.IapApi? = null,
    private val identity: com.urdufonts.app.data.iap.IapIdentityProvider? = null
) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        private const val TAG = "UrduFontsBilling"
        private const val PRODUCT_ID_SUBS = "urdu_fonts_pro"
        // The three plans on sale: base plans of urdu_fonts_pro (also their IAP skus)
        private val PLANS = listOf(
            com.urdufonts.app.domain.models.SubscriptionType.BASIC,
            com.urdufonts.app.domain.models.SubscriptionType.PRO,
            com.urdufonts.app.domain.models.SubscriptionType.BUSINESS
        )
        private const val PRODUCT_ID_LIFETIME = "urdu_fonts_lifetime"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cachedSubProductDetails: ProductDetails? = null
    private var cachedLifetimeProductDetails: ProductDetails? = null
    private val inFlightMutex = Mutex()
    private val inFlightPurchaseTokens = mutableSetOf<String>()

    override val isProUser: Flow<Boolean> = userPreferences.isProUser

    private val _purchaseProblems = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 4)
    override val purchaseProblems: Flow<String> = _purchaseProblems

    // The builder of the purchase being started, so the member's account id can be added to it
    private var lastFlowBuilder: BillingFlowParams.Builder? = null

    /** The member's account id from IAP, so Google Play ties the purchase to this account. */
    private suspend fun obfuscatedAccountId(): String? = try {
        val token = identity?.memberToken()
        if (token == null) null
        else iapApi?.purchaseIdentity("Bearer $token")?.obfuscatedAccountId?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        Log.w(TAG, "purchase-identity failed: ${e.message}")
        null
    }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }

    init {
        startConnection()
    }

    override fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client Setup Finished Successfully.")
                    scope.launch {
                        querySubProductDetails()
                        queryLifetimeProductDetails()
                        restorePurchases()
                    }
                } else {
                    Log.e(TAG, "Billing Setup Failed: responseCode=${billingResult.responseCode}, message=${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Service Disconnected. Reconnecting...")
            }
        })
    }

    override suspend fun getSubscriptionOptions(): List<SubscriptionOption> {
        val offers = querySubProductDetails()?.subscriptionOfferDetails.orEmpty()
        return PLANS.map { plan ->
            // Play's localized price; the base plan's own offer (no trial or intro offers)
            val price = offers.firstOrNull { it.basePlanId == plan.sku && it.offerId == null }
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
            SubscriptionOption(
                id = plan.sku!!,
                title = plan.displayName,
                billingPeriodText = "Billed monthly",
                priceText = price ?: "",
                pricePeriodSubtitle = "/ month",
                iconRes = when (plan) {
                    com.urdufonts.app.domain.models.SubscriptionType.BASIC -> com.urdufonts.app.resources.Res.drawable.ic_calender
                    com.urdufonts.app.domain.models.SubscriptionType.PRO -> com.urdufonts.app.resources.Res.drawable.ic_premium
                    else -> com.urdufonts.app.resources.Res.drawable.ic_infinity
                },
                isMostPopular = plan == com.urdufonts.app.domain.models.SubscriptionType.PRO,
                tierRank = plan.rank
            )
        }.filter { it.priceText.isNotBlank() }
    }

    private suspend fun querySubProductDetails(): ProductDetails? {
        if (cachedSubProductDetails != null) return cachedSubProductDetails
        if (!billingClient.isReady) {
            startConnection()
        }

        return suspendCancellableCoroutine { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_SUBS)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            billingClient.queryProductDetailsAsync(queryParams) { billingResult, queryProductDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && queryProductDetailsResult.productDetailsList.orEmpty().isNotEmpty()) {
                    cachedSubProductDetails = queryProductDetailsResult.productDetailsList.first()
                    Log.d(TAG, "Queried SUBS Product Details Success: ${cachedSubProductDetails?.title}")
                    continuation.resume(cachedSubProductDetails)
                } else {
                    Log.e(TAG, "Query Sub Product Details Failed: ${billingResult.debugMessage}")
                    continuation.resume(null)
                }
            }
        }
    }

    private suspend fun queryLifetimeProductDetails(): ProductDetails? {
        if (cachedLifetimeProductDetails != null) return cachedLifetimeProductDetails
        if (!billingClient.isReady) {
            startConnection()
        }

        return suspendCancellableCoroutine { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            billingClient.queryProductDetailsAsync(queryParams) { billingResult, queryProductDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && queryProductDetailsResult.productDetailsList.orEmpty().isNotEmpty()) {
                    cachedLifetimeProductDetails = queryProductDetailsResult.productDetailsList.first()
                    Log.d(TAG, "Queried INAPP Lifetime Details Success: ${cachedLifetimeProductDetails?.title}")
                    continuation.resume(cachedLifetimeProductDetails)
                } else {
                    Log.e(TAG, "Query Lifetime Product Details Failed: ${billingResult.debugMessage}")
                    continuation.resume(null)
                }
            }
        }
    }

    private var lastSelectedOptionId: String? = null
    private var lastSentOldPurchaseToken: String? = null

    override suspend fun launchPurchase(activity: Activity, option: SubscriptionOption): Result<Unit> {
        lastSelectedOptionId = option.id

        val billingFlowParams = if (option.id == PRODUCT_ID_LIFETIME || option.id == "lifetime-purchase") {
            lastSentOldPurchaseToken = null
            val lifetimeDetails = queryLifetimeProductDetails()
                ?: return Result.failure(Exception("Lifetime product details unavailable from Google Play"))

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(lifetimeDetails)
                .build()

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🛒 INITIATING ONE-TIME PURCHASE:")
            Log.d(TAG, "  Product ID            : $PRODUCT_ID_LIFETIME")
            Log.d(TAG, "  Title                 : ${option.title}")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .also { lastFlowBuilder = it }
                .build()
        } else {
            val subDetails = querySubProductDetails()
                ?: return Result.failure(Exception("Subscription product details unavailable from Google Play"))

            val selectedOffer = subDetails.subscriptionOfferDetails.orEmpty()
                .find { it.basePlanId == option.id }
                ?: subDetails.subscriptionOfferDetails.orEmpty().firstOrNull()

            if (selectedOffer == null) {
                return Result.failure(Exception("Subscription plan offer not found"))
            }

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(subDetails)
                .setOfferToken(selectedOffer.offerToken)
                .build()

            val flowBuilder = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))

            val existingPurchases = queryPurchasesForType(BillingClient.ProductType.SUBS).orEmpty()
            val activeSubPurchaseToken = existingPurchases.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }?.purchaseToken
                ?: userPreferences.subscriptionPurchaseToken.firstOrNull()

            if (!activeSubPurchaseToken.isNullOrBlank()) {
                lastSentOldPurchaseToken = activeSubPurchaseToken
                val currentPlanType = entitlementRepository?.entitlementsFlow?.firstOrNull()?.activeSubscriptionType
                val targetRank = com.urdufonts.app.domain.models.SubscriptionType.fromSku(option.id)?.rank ?: option.tierRank
                val isDowngrade = currentPlanType != null && targetRank < currentPlanType.rank
                // Same parent product: bigger plan charges the full new price now, smaller one from next renewal
                val replacementMode = if (isDowngrade) {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION
                } else {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
                }

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🛒 INITIATING SUBSCRIPTION UPDATE / CHANGE FLOW:")
                Log.d(TAG, "  Target Plan           : ${option.id} (${option.title})")
                Log.d(TAG, "  Current Active Plan   : ${currentPlanType ?: "UNKNOWN"}")
                Log.d(TAG, "  Is Downgrade          : $isDowngrade")
                Log.d(TAG, "  Replacement Mode      : $replacementMode")
                Log.d(TAG, "  OLD TOKEN SENT        : $activeSubPurchaseToken")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(activeSubPurchaseToken)
                    .setSubscriptionReplacementMode(replacementMode)
                    .build()
                flowBuilder.setSubscriptionUpdateParams(updateParams)
            } else {
                lastSentOldPurchaseToken = null
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🛒 INITIATING NEW PURCHASE:")
                Log.d(TAG, "  Target Plan           : ${option.id} (${option.title})")
                Log.d(TAG, "  OLD TOKEN SENT        : NONE (Fresh Purchase)")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            }

            lastFlowBuilder = flowBuilder
            flowBuilder.build()
        }

        val accountId = obfuscatedAccountId()
        Log.d(TAG, "Obfuscated account id for this purchase: ${accountId ?: "NONE"}")
        val finalParams = if (accountId != null) lastFlowBuilder?.setObfuscatedAccountId(accountId)?.build() ?: billingFlowParams else billingFlowParams

        com.webscare.ads.WebsCareAds.suppressNextReturnAppOpen()
        val result = billingClient.launchBillingFlow(activity, finalParams)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
            Log.e(TAG, "launchBillingFlow failed: code=${result.responseCode}, message=${result.debugMessage}")
            Result.failure(Exception(result.debugMessage))
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        val session = userPreferences.userSession.firstOrNull()
        val userEmail = session?.email
        if (userEmail.isNullOrBlank()) {
            Log.d(TAG, "restorePurchases skipped: user is not logged in.")
            return Result.success(false)
        }

        // Without a connection Play can't tell us what is owned; never treat that as "owns nothing"
        if (!awaitBillingReady()) {
            return Result.failure(Exception("Google Play is not available right now. Please try again."))
        }

        val subsPurchases = queryPurchasesForType(BillingClient.ProductType.SUBS)
            ?: return Result.failure(Exception("Could not reach Google Play. Please try again."))
        val inAppPurchases = queryPurchasesForType(BillingClient.ProductType.INAPP)
            ?: return Result.failure(Exception("Could not reach Google Play. Please try again."))

        val activePurchases = (subsPurchases + inAppPurchases).filter { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    (purchase.products.contains(PRODUCT_ID_SUBS) || purchase.products.contains(PRODUCT_ID_LIFETIME))
        }

        if (activePurchases.isEmpty()) {
            // Play answered and nothing is owned: clear the local flag, let the server have the final word
            userPreferences.saveSubscriptionStatus(isPro = false)
            entitlementRepository?.refreshEntitlements(userEmail, force = true)
            return Result.success(false)
        }

        // Report success only once the backend has accepted at least one purchase
        var anyConfirmed = false
        for (purchase in activePurchases) {
            if (handlePurchase(purchase)) anyConfirmed = true
        }
        return if (anyConfirmed) {
            Result.success(true)
        } else {
            Result.failure(Exception("We found your purchase but could not confirm it yet. Please try again."))
        }
    }

    /** Starts the connection if needed and waits for it, up to [timeoutMs]. */
    private suspend fun awaitBillingReady(timeoutMs: Long = 10_000): Boolean {
        if (billingClient.isReady) return true
        startConnection()
        return kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            while (!billingClient.isReady) kotlinx.coroutines.delay(100)
            true
        } ?: false
    }

    /** The purchases of [productType], or null when Google Play did not answer OK. */
    private suspend fun queryPurchasesForType(productType: String): List<Purchase>? {
        return suspendCancellableCoroutine { continuation ->
            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    Log.e(TAG, "queryPurchases($productType) failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
                    continuation.resume(null)
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    scope.launch {
                        handlePurchase(purchase)
                    }
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled billing flow.")
        } else {
            Log.e(TAG, "Error in onPurchasesUpdated: ${billingResult.debugMessage}")
        }
    }

    /** Sends the purchase to the backend; true when the backend accepted it (or it is already being sent). */
    private suspend fun handlePurchase(purchase: Purchase): Boolean {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return false
        }

        val session = userPreferences.userSession.firstOrNull()
        val externalUserId = session?.email
        if (externalUserId.isNullOrBlank()) {
            Log.w(TAG, "handlePurchase skipped: User is not logged in. Purchases are not linked to anonymous sessions.")
            return false
        }

        val token = purchase.purchaseToken
        val alreadyInFlight = inFlightMutex.withLock {
            if (inFlightPurchaseTokens.contains(token)) {
                true
            } else {
                inFlightPurchaseTokens.add(token)
                false
            }
        }

        if (alreadyInFlight) {
            return true
        }

        return try {
            val productId = purchase.products.firstOrNull() ?: PRODUCT_ID_SUBS

            val productType = if (productId == PRODUCT_ID_LIFETIME) "non_consumable" else "subscription"
            val backendSku = when {
                productId == PRODUCT_ID_LIFETIME -> PRODUCT_ID_LIFETIME
                // IAP knows each plan by its base plan id
                PLANS.any { it.sku == lastSelectedOptionId } -> lastSelectedOptionId!!
                else -> userPreferences.subscriptionPlan.firstOrNull()?.takeIf { it.isNotBlank() } ?: "monthly"
            }

            val oldToken = lastSentOldPurchaseToken ?: userPreferences.subscriptionPurchaseToken.firstOrNull()
            val isSameToken = !oldToken.isNullOrBlank() && oldToken == token
            val tokenComparison = when {
                oldToken.isNullOrBlank() -> "FRESH PURCHASE TOKEN (No previous token)"
                isSameToken -> "SAME TOKEN REUSED BY GOOGLE PLAY (Old Token == New Token)"
                else -> "NEW TOKEN GENERATED BY GOOGLE PLAY (Different from Old Token)"
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📦 GOOGLE PLAY PURCHASE RETURNED:")
            Log.d(TAG, "  Product ID            : $productId")
            Log.d(TAG, "  Order ID              : ${purchase.orderId ?: "N/A"}")
            Log.d(TAG, "  Token Comparison      : $tokenComparison")
            Log.d(TAG, "  Old Token (Sent)      : ${oldToken ?: "NONE"}")
            Log.d(TAG, "  New Token (Received)  : $token")
            Log.d(TAG, "  Sending to Backend    : sku=$backendSku, type=$productType, user=$externalUserId")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            lastSelectedOptionId = null
            val confirmed = entitlementRepository?.confirmPurchase(
                externalUserId = externalUserId,
                sku = backendSku,
                purchaseToken = token,
                productType = productType
            )

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(token)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Acknowledge purchase failed: ${billingResult.debugMessage}")
                    }
                }
            }

            // Premium only when the backend accepted the purchase (confirmPurchase saved its entitlements)
            if (confirmed?.isSuccess == true) {
                Log.d(TAG, "BACKEND ACCEPTED purchase: sku=$backendSku")
                userPreferences.saveSubscriptionStatus(isPro = true, planId = backendSku, purchaseToken = token)
            } else {
                val error = confirmed?.exceptionOrNull()
                Log.e(TAG, "BACKEND DID NOT ACCEPT purchase: sku=$backendSku, ${error?.message}")
                entitlementRepository?.refreshEntitlements(externalUserId, force = true)
                val ownedElsewhere = error?.httpStatus == 409
                _purchaseProblems.tryEmit(
                    if (ownedElsewhere) "This Google Play purchase is linked to a different account. Please contact support."
                    else "We could not confirm your purchase yet. It will be retried when you reopen the app."
                )
            }
            confirmed?.isSuccess == true
        } finally {
            inFlightMutex.withLock {
                inFlightPurchaseTokens.remove(token)
            }
        }
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

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
    private val entitlementRepository: EntitlementRepository? = null
) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        private const val TAG = "UrduFontsBilling"
        private const val PRODUCT_ID_SUBS = "urdu_fonts_pro"
        private const val BASE_PLAN_MONTHLY = "monthly-plan"
        private const val BASE_PLAN_SIX_MONTHS = "6-month-plan"
        private const val PRODUCT_ID_LIFETIME = "urdu_fonts_lifetime"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cachedSubProductDetails: ProductDetails? = null
    private var cachedLifetimeProductDetails: ProductDetails? = null
    private val inFlightMutex = Mutex()
    private val inFlightPurchaseTokens = mutableSetOf<String>()

    override val isProUser: Flow<Boolean> = userPreferences.isProUser

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
        val baseTemplates = listOf(
            SubscriptionOption(
                id = BASE_PLAN_MONTHLY,
                title = "Monthly",
                billingPeriodText = "Billed monthly",
                priceText = "$4.99",
                pricePeriodSubtitle = "per month",
                iconRes = R.drawable.ic_calender
            ),
            SubscriptionOption(
                id = BASE_PLAN_SIX_MONTHS,
                title = "6-Months",
                billingPeriodText = "Billed every 6 months",
                priceText = "$19.99",
                discountTag = "15% OFF",
                iconRes = R.drawable.ic_six_months,
                isMostPopular = true
            ),
            SubscriptionOption(
                id = PRODUCT_ID_LIFETIME,
                title = "Lifetime Access",
                billingPeriodText = "One-time purchase",
                priceText = "$49.99",
                iconRes = R.drawable.ic_infinity
            )
        )

        val subProductDetails = querySubProductDetails()
        val lifetimeProductDetails = queryLifetimeProductDetails()

        val offerDetailsList = subProductDetails?.subscriptionOfferDetails.orEmpty()
        val lifetimePrice = lifetimeProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice

        return baseTemplates.map { template ->
            when (template.id) {
                PRODUCT_ID_LIFETIME -> {
                    if (lifetimePrice != null) {
                        template.copy(priceText = lifetimePrice)
                    } else {
                        template
                    }
                }
                else -> {
                    val matchedOffer = offerDetailsList.find { it.basePlanId == template.id }
                    val formattedPrice = matchedOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    if (formattedPrice != null) {
                        template.copy(priceText = formattedPrice)
                    } else {
                        template
                    }
                }
            }
        }
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

            val existingPurchases = queryPurchasesForType(BillingClient.ProductType.SUBS)
            val activeSubPurchaseToken = existingPurchases.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }?.purchaseToken
                ?: userPreferences.subscriptionPurchaseToken.firstOrNull()

            if (!activeSubPurchaseToken.isNullOrBlank()) {
                lastSentOldPurchaseToken = activeSubPurchaseToken
                val currentEntitlements = entitlementRepository?.entitlementsFlow?.firstOrNull()
                val currentPlanType = currentEntitlements?.activeSubscriptionType
                val tierRank = mapOf(
                    com.urdufonts.app.domain.models.SubscriptionType.MONTHLY to 1,
                    com.urdufonts.app.domain.models.SubscriptionType.SIX_MONTHS to 2,
                    com.urdufonts.app.domain.models.SubscriptionType.LIFETIME to 3
                )
                val targetType = if (option.id == BASE_PLAN_MONTHLY) com.urdufonts.app.domain.models.SubscriptionType.MONTHLY else com.urdufonts.app.domain.models.SubscriptionType.SIX_MONTHS
                val currentRank = currentPlanType?.let { tierRank[it] }
                val targetRank = tierRank[targetType]
                val isDowngrade = currentRank != null && targetRank != null && targetRank < currentRank
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

            flowBuilder.build()
        }

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
            Log.e(TAG, "launchBillingFlow failed: code=${result.responseCode}, message=${result.debugMessage}")
            Result.failure(Exception(result.debugMessage))
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        if (!billingClient.isReady) {
            startConnection()
        }

        val subsPurchases = queryPurchasesForType(BillingClient.ProductType.SUBS)
        val inAppPurchases = queryPurchasesForType(BillingClient.ProductType.INAPP)

        val allPurchases = subsPurchases + inAppPurchases
        val activePurchase = allPurchases.find { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    (purchase.products.contains(PRODUCT_ID_SUBS) || purchase.products.contains(PRODUCT_ID_LIFETIME))
        }

        return if (activePurchase != null) {
            scope.launch {
                handlePurchase(activePurchase)
            }
            Result.success(true)
        } else {
            scope.launch {
                userPreferences.saveSubscriptionStatus(isPro = false)
                if (entitlementRepository != null) {
                    val session = userPreferences.userSession.firstOrNull()
                    val externalUserId = if (!session?.email.isNullOrBlank()) {
                        session.email
                    } else {
                        UserPreferences.getAnonymousDeviceId(context)
                    }
                    entitlementRepository.refreshEntitlements(externalUserId, force = true)
                }
            }
            Result.success(false)
        }
    }

    private suspend fun queryPurchasesForType(productType: String): List<Purchase> {
        return suspendCancellableCoroutine { continuation ->
            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    continuation.resume(emptyList())
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

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
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
            return
        }

        try {
            val productId = purchase.products.firstOrNull() ?: PRODUCT_ID_SUBS
            val session = userPreferences.userSession.firstOrNull()
            val externalUserId = if (!session?.email.isNullOrBlank()) {
                session!!.email
            } else {
                UserPreferences.getAnonymousDeviceId(context)
            }

            val productType = if (productId == PRODUCT_ID_LIFETIME) "non_consumable" else "subscription"
            val backendSku = if (productId == PRODUCT_ID_LIFETIME) "lifetime" else PRODUCT_ID_SUBS

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

            if (entitlementRepository != null) {
                entitlementRepository.confirmPurchase(
                    externalUserId = externalUserId,
                    sku = backendSku,
                    purchaseToken = token,
                    productType = productType
                )
            }

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

            userPreferences.saveSubscriptionStatus(
                isPro = true,
                planId = productId,
                purchaseToken = token
            )
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

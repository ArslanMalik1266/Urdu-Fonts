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

    override suspend fun launchPurchase(activity: Activity, option: SubscriptionOption): Result<Unit> {
        Log.d(TAG, "launchPurchase called for option: ${option.id} (${option.title})")
        lastSelectedOptionId = option.id

        val billingFlowParams = if (option.id == PRODUCT_ID_LIFETIME || option.id == "lifetime-purchase") {
            val lifetimeDetails = queryLifetimeProductDetails()
                ?: return Result.failure(Exception("Lifetime product details unavailable from Google Play"))

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(lifetimeDetails)
                .build()

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
            val activeSubPurchase = existingPurchases.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            if (activeSubPurchase != null) {
                val isUpgrade = option.id == BASE_PLAN_SIX_MONTHS
                val replacementMode = if (isUpgrade) {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
                } else {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION
                }
                Log.d(TAG, "Active subscription found. Applying replacement mode: $replacementMode (isUpgrade: $isUpgrade)")
                val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(activeSubPurchase.purchaseToken)
                    .setSubscriptionReplacementMode(replacementMode)
                    .build()
                flowBuilder.setSubscriptionUpdateParams(updateParams)
            }

            flowBuilder.build()
        }

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        Log.d(TAG, "launchBillingFlow response -> code: ${result.responseCode}, message: ${result.debugMessage}")
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
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
            Log.d(TAG, "Restored active purchase: ${activePurchase.products}")
            scope.launch {
                handlePurchase(activePurchase)
            }
            Result.success(true)
        } else {
            Log.d(TAG, "No active purchase restored.")
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
                    Log.e(TAG, "Query purchases failed for type $productType: ${billingResult.debugMessage}")
                    continuation.resume(emptyList())
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated -> responseCode: ${billingResult.responseCode}, debugMessage: ${billingResult.debugMessage}, purchasesCount: ${purchases?.size ?: 0}")
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
            Log.e(TAG, "Error updating purchases: ${billingResult.debugMessage}")
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "handlePurchase -> products: ${purchase.products}, tokenSnippet: ${purchase.purchaseToken.take(15)}...")
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase Acknowledged Successfully.")
                }
            }
        }

        val productId = purchase.products.firstOrNull() ?: PRODUCT_ID_SUBS
        val session = userPreferences.userSession.firstOrNull()
        val externalUserId = if (!session?.email.isNullOrBlank()) {
            session!!.email
        } else {
            UserPreferences.getAnonymousDeviceId(context)
        }

        if (entitlementRepository != null) {
            val (backendSku, productType) = when {
                productId == PRODUCT_ID_LIFETIME -> "lifetime" to "non_consumable"
                else -> {
                    val extractedBasePlan = try {
                        val jsonObject = org.json.JSONObject(purchase.originalJson)
                        if (jsonObject.has("basePlanId")) jsonObject.getString("basePlanId") else null
                    } catch (e: Exception) {
                        null
                    }
                    val planId = extractedBasePlan ?: lastSelectedOptionId ?: BASE_PLAN_SIX_MONTHS
                    val sku = if (planId == BASE_PLAN_MONTHLY) "monthly" else "6_months"
                    sku to "subscription"
                }
            }

            Log.d(TAG, "Sending confirmPurchase to backend for externalUserId $externalUserId, sku: $backendSku, productType: $productType...")
            entitlementRepository.confirmPurchase(
                externalUserId = externalUserId,
                sku = backendSku,
                purchaseToken = purchase.purchaseToken,
                productType = productType
            )
        }

        userPreferences.saveSubscriptionStatus(
            isPro = true,
            planId = productId,
            purchaseToken = purchase.purchaseToken
        )
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

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

class BillingRepositoryImpl(
    private val context: Context,
    private val userPreferences: UserPreferences
) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingRepositoryImpl"
        private const val PRODUCT_ID_PRO = "urdufonts_pro"
        private const val BASE_PLAN_MONTHLY = "monthly"
        private const val BASE_PLAN_SIX_MONTHS = "6_months"
        private const val BASE_PLAN_YEARLY = "yearly"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cachedProductDetails: ProductDetails? = null

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
                        queryProductDetails()
                        restorePurchases()
                    }
                } else {
                    Log.e(TAG, "Billing Setup Failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Service Disconnected. Reconnecting...")
            }
        })
    }

    override suspend fun getSubscriptionOptions(): List<SubscriptionOption> {
        val productDetails = queryProductDetails() ?: return emptyList()

        val offerDetailsList = productDetails.subscriptionOfferDetails.orEmpty()
        if (offerDetailsList.isEmpty()) return emptyList()

        val baseTemplates = listOf(
            SubscriptionOption(
                id = BASE_PLAN_MONTHLY,
                title = "Monthly",
                billingPeriodText = "Billed monthly",
                priceText = "",
                pricePeriodSubtitle = "per month",
                iconRes = R.drawable.ic_calender
            ),
            SubscriptionOption(
                id = BASE_PLAN_SIX_MONTHS,
                title = "6-Months",
                billingPeriodText = "Billed every 6 months",
                priceText = "",
                originalPriceText = "$59.99",
                discountTag = "15% OFF",
                iconRes = R.drawable.ic_six_months,
                isMostPopular = true
            ),
            SubscriptionOption(
                id = BASE_PLAN_YEARLY,
                title = "Yearly",
                billingPeriodText = "Billed annually",
                priceText = "",
                originalPriceText = "$149.99",
                iconRes = R.drawable.ic_infinity
            )
        )

        return baseTemplates.mapNotNull { template ->
            val matchedOffer = offerDetailsList.find { it.basePlanId == template.id }
            val formattedPrice = matchedOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            if (matchedOffer != null && formattedPrice != null) {
                template.copy(priceText = formattedPrice)
            } else {
                null
            }
        }
    }

    private suspend fun queryProductDetails(): ProductDetails? {
        if (cachedProductDetails != null) return cachedProductDetails
        if (!billingClient.isReady) {
            startConnection()
        }

        return suspendCancellableCoroutine { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            billingClient.queryProductDetailsAsync(queryParams) { billingResult, queryProductDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && queryProductDetailsResult.productDetailsList.orEmpty().isNotEmpty()) {
                    cachedProductDetails = queryProductDetailsResult.productDetailsList.first()
                    continuation.resume(cachedProductDetails)
                } else {
                    Log.e(TAG, "Query Product Details Failed: ${billingResult.debugMessage}")
                    continuation.resume(null)
                }
            }
        }
    }

    override suspend fun launchPurchase(activity: Activity, option: SubscriptionOption): Result<Unit> {
        val productDetails = queryProductDetails()
            ?: return Result.failure(Exception("Unable to fetch product details from Google Play"))

        val selectedOffer = productDetails.subscriptionOfferDetails.orEmpty()
            .find { it.basePlanId == option.id }
            ?: productDetails.subscriptionOfferDetails.orEmpty().firstOrNull()

        if (selectedOffer == null) {
            return Result.failure(Exception("Subscription plan offer not found"))
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(selectedOffer.offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
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

        return suspendCancellableCoroutine { continuation ->
            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val activePurchase = purchasesList.find { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                                purchase.products.contains(PRODUCT_ID_PRO)
                    }

                    if (activePurchase != null) {
                        scope.launch {
                            handlePurchase(activePurchase)
                        }
                        continuation.resume(Result.success(true))
                    } else {
                        scope.launch {
                            userPreferences.saveSubscriptionStatus(isPro = false)
                        }
                        continuation.resume(Result.success(false))
                    }
                } else {
                    continuation.resume(Result.failure(Exception(billingResult.debugMessage)))
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
            Log.e(TAG, "Error updating purchases: ${billingResult.debugMessage}")
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
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

        val planId = purchase.products.firstOrNull() ?: PRODUCT_ID_PRO
        userPreferences.saveSubscriptionStatus(
            isPro = true,
            planId = planId,
            purchaseToken = purchase.purchaseToken
        )
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

package com.urdufonts.app.ios

import com.urdufonts.app.data.iap.IapApi
import com.urdufonts.app.data.iap.IapIdentityProvider
import com.urdufonts.app.data.iap.httpStatus
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.domain.models.SubscriptionType
import com.urdufonts.app.domain.repo.BillingRepository
import com.urdufonts.app.domain.repo.EntitlementRepository
import com.urdufonts.app.platform.AppLog as Log
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.resources.Res
import com.urdufonts.app.resources.ic_calender
import com.urdufonts.app.resources.ic_infinity
import com.urdufonts.app.resources.ic_premium
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * App Store product ids. Create the products in App Store Connect with these ids (or change them here): the
 * three monthly plans in one subscription group, ranked Basic < Pro < Business, and the lifetime unlock as a
 * non-consumable. The backend sku stays the same as on Android.
 */
object IosProductIds {
    var plans: Map<SubscriptionType, String> = mapOf(
        SubscriptionType.BASIC to "urdu_fonts_basic_monthly",
        SubscriptionType.PRO to "Urdu_fonts_pro_monthly",
        SubscriptionType.BUSINESS to "urdu_fonts_business_monthly",
    )
    var lifetime: String = "urdu_fonts_lifetime"

    fun backendSku(productId: String): String =
        plans.entries.firstOrNull { it.value == productId }?.key?.sku
            ?: if (productId == lifetime) SubscriptionType.LIFETIME.sku!! else productId
}

/**
 * StoreKit 2 subscriptions for iOS. Same rules as Google Play billing on Android: the user must be signed in,
 * a purchase is premium only once the IAP backend has accepted it (POST purchases/ios/confirm with the
 * signed transaction and renewal info), and only then is the transaction finished. A failed confirmation leaves
 * it unfinished, so StoreKit delivers it again (Transaction.updates) and it is retried.
 */
class IosBillingRepository(
    private val storeKit: StoreKitBridge,
    private val userPreferences: UserPreferences,
    private val entitlementRepository: EntitlementRepository,
    private val iapApi: IapApi,
    private val identity: IapIdentityProvider,
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val confirming = Mutex()
    private val confirmedIds = mutableSetOf<String>()
    private var products: List<StoreProduct> = emptyList()
    private var observing = false

    override val isProUser: Flow<Boolean> = userPreferences.isProUser

    private val _purchaseProblems = MutableSharedFlow<String>(extraBufferCapacity = 4)
    override val purchaseProblems: Flow<String> = _purchaseProblems

    override fun startConnection() {
        if (observing) return
        observing = true
        // Renewals, Ask-to-Buy approvals and purchases made on another device arrive here
        storeKit.observeTransactionUpdates { transaction -> scope.launch { confirm(transaction) } }
        scope.launch { restorePurchases() }
    }

    override fun endConnection() = Unit

    private suspend fun loadProducts(): List<StoreProduct> {
        if (products.isNotEmpty()) return products
        val ids = IosProductIds.plans.values.toList() + IosProductIds.lifetime
        products = suspendCancellableCoroutine { cont -> storeKit.loadProducts(ids) { cont.resume(it) } }
        if (products.isEmpty()) Log.e(TAG, "App Store returned no products for $ids")
        return products
    }

    override suspend fun getSubscriptionOptions(): List<SubscriptionOption> {
        val byId = loadProducts().associateBy { it.id }
        return PLANS.mapNotNull { plan ->
            val product = byId[IosProductIds.plans[plan]] ?: return@mapNotNull null
            SubscriptionOption(
                id = plan.sku!!,
                title = plan.displayName,
                billingPeriodText = "Billed monthly",
                priceText = product.displayPrice,
                pricePeriodSubtitle = "/ month",
                iconRes = when (plan) {
                    SubscriptionType.BASIC -> Res.drawable.ic_calender
                    SubscriptionType.PRO -> Res.drawable.ic_premium
                    else -> Res.drawable.ic_infinity
                },
                isMostPopular = plan == SubscriptionType.PRO,
                tierRank = plan.rank,
                storeProductId = product.id
            )
        }
    }

    override suspend fun launchPurchase(activity: PlatformActivity, option: SubscriptionOption): Result<Unit> {
        if (userPreferences.userSession.firstOrNull()?.email.isNullOrBlank()) {
            return Result.failure(Exception("Please sign in before buying a plan."))
        }
        val productId = if (option.id == SubscriptionType.LIFETIME.sku || option.id == "lifetime-purchase") IosProductIds.lifetime
        else SubscriptionType.fromSku(option.id)?.let { IosProductIds.plans[it] } ?: option.id
        // The backend requires every App Store purchase to carry the member's app_account_token
        val accountToken = appAccountToken()
            ?: return Result.failure(Exception("We couldn't verify your account. Please sign in again and retry."))

        com.urdufonts.app.ads.suppressNextReturnAppOpen()
        val result = suspendCancellableCoroutine { cont ->
            storeKit.purchase(productId, accountToken) { cont.resume(it) }
        }
        return when (result.status) {
            "success" -> {
                // A downgrade or plan switch comes back as the current transaction with new renewal info:
                // send it again even if this session already confirmed it, so the backend sees the pending change
                result.transaction?.let { confirm(it, force = true) }
                Result.success(Unit)
            }
            // Ask to Buy or extra verification: it arrives later through observeTransactionUpdates
            "pending", "cancelled" -> Result.success(Unit)
            else -> Result.failure(Exception(result.error ?: "The purchase could not be completed."))
        }
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        if (userPreferences.userSession.firstOrNull()?.email.isNullOrBlank()) return Result.success(false)
        val owned = suspendCancellableCoroutine { cont -> storeKit.currentEntitlements { cont.resume(it) } }
        owned.forEach { confirm(it) }
        return Result.success(owned.isNotEmpty())
    }

    /** The member's app_account_token from IAP (a UUID) for StoreKit, so Apple ties the purchase to this account. */
    private suspend fun appAccountToken(): String? = try {
        val token = identity.memberToken()
        val id = if (token == null) null else iapApi.purchaseIdentity("Bearer $token").appAccountToken
        id?.takeIf { UUID_PATTERN.matches(it) }
    } catch (e: Exception) {
        Log.w(TAG, "purchase-identity failed: ${e.message}")
        null
    }

    /** Sends a transaction to the backend once; premium follows only from the backend's answer. */
    private suspend fun confirm(transaction: StoreTransaction, force: Boolean = false) = confirming.withLock {
        if (!force && transaction.transactionId in confirmedIds) return@withLock
        val externalUserId = userPreferences.userSession.firstOrNull()?.email
        if (externalUserId.isNullOrBlank()) return@withLock
        // The iOS catalog's sku is the App Store product id itself (the backend keeps a separate iOS catalog)
        val sku = transaction.productId
        val productType = if (transaction.isSubscription) "subscription" else "non_consumable"
        Log.d(TAG, "Confirming App Store purchase: sku=$sku, type=$productType, transaction=${transaction.transactionId}")

        val confirmed = entitlementRepository.confirmIosPurchase(
            externalUserId = externalUserId,
            sku = sku,
            transactionId = transaction.transactionId,
            signedTransactionInfo = transaction.signedTransaction,
            signedRenewalInfo = transaction.signedRenewalInfo,
            productType = productType
        )
        if (confirmed.isSuccess) {
            confirmedIds += transaction.transactionId
            storeKit.finish(transaction.transactionId)
            userPreferences.saveSubscriptionStatus(isPro = true, planId = IosProductIds.backendSku(sku), purchaseToken = transaction.originalTransactionId)
        } else {
            val error = confirmed.exceptionOrNull()
            Log.e(TAG, "Backend did not accept App Store purchase: ${error?.message}")
            entitlementRepository.refreshEntitlements(externalUserId, force = true)
            _purchaseProblems.tryEmit(
                if (error?.httpStatus == 409) "This App Store purchase is linked to a different account. Please contact support."
                else "We could not confirm your purchase yet. It will be retried when you reopen the app."
            )
        }
    }

    private companion object {
        const val TAG = "UrduFontsBilling"
        val PLANS = listOf(SubscriptionType.BASIC, SubscriptionType.PRO, SubscriptionType.BUSINESS)
        val UUID_PATTERN = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    }
}

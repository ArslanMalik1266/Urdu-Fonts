package com.urdufonts.app.ios

import platform.UIKit.UIView

/*
 * The iOS SDKs this app needs (StoreKit 2, Google Sign-In, Firebase Messaging, the WebsCareAds XCFramework) are Swift APIs that
 * Kotlin cannot call directly. Each is wrapped in one of these small interfaces, implemented in Swift in the
 * iosApp project (iosApp/iosApp/Bridges/), and handed to Kotlin once at start-up in [initUrduFonts].
 * All callbacks may be called on any thread; the Kotlin side moves to the main thread itself.
 */

// ── StoreKit 2 ─────────────────────────────────────────────────────────────

/** A product as the App Store sells it in this country. */
data class StoreProduct(
    val id: String,
    val displayName: String,
    /** Localized price, e.g. "Rs 1,100.00" or "$4.99". */
    val displayPrice: String,
    val isSubscription: Boolean,
)

/** A verified StoreKit 2 transaction. [signedTransaction] is the JWS the backend verifies with Apple. */
data class StoreTransaction(
    val productId: String,
    val transactionId: String,
    val originalTransactionId: String,
    val signedTransaction: String,
    /** The subscription's renewal-info JWS (shows a pending downgrade to the backend); null for one-time products. */
    val signedRenewalInfo: String?,
    val isSubscription: Boolean,
)

/** status: "success", "cancelled", "pending" (Ask to Buy / SCA) or "failed". */
data class StorePurchaseResult(
    val status: String,
    val transaction: StoreTransaction?,
    val error: String?,
)

interface StoreKitBridge {
    fun loadProducts(productIds: List<String>, completion: (List<StoreProduct>) -> Unit)

    /** [appAccountToken] (a UUID string) ties the purchase to the signed-in member, like Play's obfuscated account id. */
    fun purchase(productId: String, appAccountToken: String?, completion: (StorePurchaseResult) -> Unit)

    /** Transaction.currentEntitlements: everything the user owns right now. */
    fun currentEntitlements(completion: (List<StoreTransaction>) -> Unit)

    /** AppStore.sync(): "Restore purchases". */
    fun syncWithAppStore(completion: (Boolean) -> Unit)

    /** Finish a transaction once the backend has it (StoreKit 2's acknowledge). */
    fun finish(transactionId: String)

    /** New transactions arriving outside a purchase (renewals, Ask to Buy approvals, other devices). */
    fun observeTransactionUpdates(onTransaction: (StoreTransaction) -> Unit)
}

// ── Google Sign-In ─────────────────────────────────────────────────────────

data class GoogleAccount(val idToken: String, val email: String, val name: String?, val photoUrl: String?)

interface GoogleSignInBridge {
    /** Shows Google's account picker. Exactly one of the two is non-null. */
    fun signIn(completion: (account: GoogleAccount?, error: String?) -> Unit)
}

// ── Push (Firebase Messaging on top of APNs) ───────────────────────────────

interface PushBridge {
    /** The FCM registration token, or null while APNs/FCM has not given one yet. */
    fun currentToken(completion: (String?) -> Unit)
}

// ── WebsCareAds for iOS (the XCFramework, called from Swift) ───────────────

interface AdsBridge {
    fun initialize(isDebug: Boolean)
    fun setPremium(isPremium: Boolean)
    fun requestConsent(completion: (Boolean) -> Unit)
    fun isPrivacyOptionsRequired(): Boolean
    fun showPrivacyOptions(completion: () -> Unit)

    /** kind: "app_open", "interstitial" or "rewarded". */
    fun preload(kind: String, placement: String)
    fun showSplashAppOpen(placement: String, onContinue: () -> Unit)
    fun showInterstitial(placement: String, onDismissed: () -> Unit)
    fun showRewarded(placement: String, onRewarded: (type: String, amount: Int) -> Unit, onNotReady: () -> Unit)
    fun setAppOpenLoadingScreenVisible(visible: Boolean)

    /** Loads a banner for [placement] into [container]. */
    fun loadBanner(container: UIView, placement: String)

    /** Loads a native ad into [container]; [onFailed] is called if it never fills, so the list can drop the row. */
    fun loadNative(container: UIView, placement: String, onFailed: () -> Unit)
}
/** Everything Swift provides, handed over once in [initUrduFonts]. */
class IosBridges(
    val storeKit: StoreKitBridge,
    val googleSignIn: GoogleSignInBridge,
    val push: PushBridge,
    val ads: AdsBridge,
)

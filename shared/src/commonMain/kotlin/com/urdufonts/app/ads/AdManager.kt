package com.urdufonts.app.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.PlatformContext

/** Every ad slot in the app. Each platform maps these to its own ad SDK placement ids. */
enum class AdPlacement {
    APP_OPEN,
    DOWNLOAD_INTERSTITIAL,
    DOWNLOAD_FONT_REWARDED,
    FONT_DETAILS_BANNER,
    HOME_NATIVE,
    FONT_LIST_NATIVE,
    CATEGORIES_NATIVE,
    STYLES_NATIVE,
}

enum class NativeAdSize { SMALL, MEDIUM }

/**
 * The app's ads, independent of the ad SDK. Android implements it with WebsCareAds (in the app module);
 * iOS with WebsCareAds for iOS. Screens ask for ads by [AdPlacement]; the SDK owns loading, caching,
 * frequency caps, consent and premium suppression.
 * Every show* call continues immediately (calls its callback) when no ad is ready.
 */
interface AdManager {
    fun initSdk(context: PlatformContext)
    fun requestConsent(activity: PlatformActivity, onConsentResult: (Boolean) -> Unit)

    /** True when Google requires a visible "Privacy options" entry (e.g. EEA users). */
    fun isPrivacyOptionsRequired(): Boolean
    fun showPrivacyOptions(activity: PlatformActivity, onComplete: () -> Unit = {})

    fun preloadAppOpen(context: PlatformContext, placement: AdPlacement = AdPlacement.APP_OPEN)

    /** Only call from a real loading screen (the splash); marks it as such while the ad decides. */
    fun showSplashAppOpen(activity: PlatformActivity, placement: AdPlacement = AdPlacement.APP_OPEN, onContinue: () -> Unit)

    fun preloadInterstitial(context: PlatformContext, placement: AdPlacement = AdPlacement.DOWNLOAD_INTERSTITIAL)
    fun showInterstitial(activity: PlatformActivity, placement: AdPlacement = AdPlacement.DOWNLOAD_INTERSTITIAL, onDismissed: () -> Unit)

    fun preloadRewarded(context: PlatformContext, placement: AdPlacement = AdPlacement.DOWNLOAD_FONT_REWARDED)

    /** [onRewarded] runs only from the SDK's earned-reward callback, never on dismiss or no-fill. */
    fun showRewarded(
        activity: PlatformActivity,
        placement: AdPlacement = AdPlacement.DOWNLOAD_FONT_REWARDED,
        onRewarded: (rewardType: String, amount: Int) -> Unit,
        onNotReady: () -> Unit
    )

    fun showDownloadInterstitialWithCooldown(
        activity: PlatformActivity,
        placement: AdPlacement = AdPlacement.DOWNLOAD_INTERSTITIAL,
        onDismissed: () -> Unit
    )

    /** The next return to the app should not show an app-open ad (we are leaving on purpose: store, browser, picker). */
    fun suppressNextReturnAppOpen()

    /** Tells the SDK whether the splash (a real loading screen) is visible. */
    fun setAppOpenLoadingScreenVisible(visible: Boolean)

    // ── Ad views drawn inside Compose screens ──────────────────────────────

    /** A native ad in a list row. [slot] identifies the position so a scrolled-back row keeps its ad. */
    @Composable
    fun NativeAd(placement: AdPlacement, slot: Any, modifier: Modifier, size: NativeAdSize)

    /** True once the native ad for [placement] + [slot] has failed, so lists can leave the row out. */
    fun isNativeAdFailed(placement: AdPlacement, slot: Any): Boolean

    @Composable
    fun BannerAd(placement: AdPlacement, modifier: Modifier)
}

/** Where native ads go in a list: after the first [first] items, then after every [every] items. */
fun isNativeAdSlot(itemCount: Int, first: Int, every: Int): Boolean =
    itemCount == first || (itemCount > first && (itemCount - first) % every == 0)

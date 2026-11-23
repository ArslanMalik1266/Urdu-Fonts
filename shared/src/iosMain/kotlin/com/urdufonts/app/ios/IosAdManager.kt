package com.urdufonts.app.ios

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.urdufonts.app.ads.AdManager
import com.urdufonts.app.ads.AdPlacement
import com.urdufonts.app.ads.NativeAdSize
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.platform.AppBuildConfig
import com.urdufonts.app.platform.AppLog
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

/**
 * The shared [AdManager] on iOS: WebsCareAds 4.2.5 for iOS (XCFramework), reached through [AdsBridge]
 * (AdsBridgeImpl.swift). Same placements and rules as the Android WebsCareAdManagerImpl.
 */
class IosAdManager(
    private val ads: AdsBridge,
    private val userPreferences: UserPreferences,
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isProUser = false

    /** Native containers by placement + slot, so a row scrolled away and back keeps its ad (as on Android). */
    private val nativeViews = mutableMapOf<String, UIView>()
    private val failedNative = mutableStateMapOf<String, Boolean>()
    private val bannerViews = mutableMapOf<String, UIView>()

    override fun initSdk(context: PlatformContext) {
        ads.initialize(AppBuildConfig.isDebug)
        scope.launch {
            userPreferences.isProUser.collectLatest { isPro ->
                isProUser = isPro
                ads.setPremium(isPro)
            }
        }
    }

    override fun requestConsent(activity: PlatformActivity, onConsentResult: (Boolean) -> Unit) =
        ads.requestConsent { gathered ->
            AppLog.d(TAG, "Consent finished, gathered=$gathered")
            scope.launch { onConsentResult(gathered) }
        }

    override fun isPrivacyOptionsRequired(): Boolean = ads.isPrivacyOptionsRequired()

    override fun showPrivacyOptions(activity: PlatformActivity, onComplete: () -> Unit) =
        ads.showPrivacyOptions { scope.launch { onComplete() } }

    override fun preloadAppOpen(context: PlatformContext, placement: AdPlacement) {
        if (!isProUser) ads.preload("app_open", placement.id)
    }

    override fun showSplashAppOpen(activity: PlatformActivity, placement: AdPlacement, onContinue: () -> Unit) =
        ads.showSplashAppOpen(placement.id) { scope.launch { onContinue() } }

    override fun preloadInterstitial(context: PlatformContext, placement: AdPlacement) {
        if (!isProUser) ads.preload("interstitial", placement.id)
    }

    override fun showInterstitial(activity: PlatformActivity, placement: AdPlacement, onDismissed: () -> Unit) =
        ads.showInterstitial(placement.id) { scope.launch { onDismissed() } }

    override fun preloadRewarded(context: PlatformContext, placement: AdPlacement) {
        if (!isProUser) ads.preload("rewarded", placement.id)
    }

    override fun showRewarded(
        activity: PlatformActivity,
        placement: AdPlacement,
        onRewarded: (rewardType: String, amount: Int) -> Unit,
        onNotReady: () -> Unit
    ) = ads.showRewarded(
        placement.id,
        onRewarded = { type, amount -> scope.launch { onRewarded(type, amount) } },
        onNotReady = { scope.launch { onNotReady() } }
    )

    override fun showDownloadInterstitialWithCooldown(activity: PlatformActivity, placement: AdPlacement, onDismissed: () -> Unit) {
        if (isProUser) {
            onDismissed()
            return
        }
        // The library enforces the interstitial cooldown and caps; no fill continues at once
        showInterstitial(activity, placement) {
            ads.preload("interstitial", placement.id)
            onDismissed()
        }
    }

    /** WebsCareAds for iOS has no such call: its return-to-app ad handles app switches itself. */
    override fun suppressNextReturnAppOpen() = Unit

    override fun setAppOpenLoadingScreenVisible(visible: Boolean) = ads.setAppOpenLoadingScreenVisible(visible)

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun NativeAd(placement: AdPlacement, slot: Any, modifier: Modifier, size: NativeAdSize) {
        val isPro by userPreferences.isProUser.collectAsState(initial = false)
        val key = "${placement.id}#$slot"
        if (isPro || failedNative[key] == true) return
        val container = nativeViews.getOrPut(key) {
            UIView(frame = CGRectZero.readValue()).also { view ->
                ads.loadNative(view, placement.id) { scope.launch { failedNative[key] = true } }
            }
        }
        UIKitView(
            factory = { container },
            modifier = modifier.fillMaxWidth().height(if (size == NativeAdSize.SMALL) 132.dp else 320.dp)
        )
    }

    override fun isNativeAdFailed(placement: AdPlacement, slot: Any): Boolean = failedNative["${placement.id}#$slot"] == true

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun BannerAd(placement: AdPlacement, modifier: Modifier) {
        val isPro by userPreferences.isProUser.collectAsState(initial = false)
        if (isPro) return
        val container = bannerViews.getOrPut(placement.id) {
            UIView(frame = CGRectZero.readValue()).also { ads.loadBanner(it, placement.id) }
        }
        UIKitView(factory = { container }, modifier = modifier.fillMaxWidth().height(60.dp))
    }

    private companion object {
        const val TAG = "WebsCareAdsLog"
    }
}

/** The same placement names the Android app uses (from WebsCarePlacements); the iOS policy must define them too. */
val AdPlacement.id: String
    get() = when (this) {
        AdPlacement.APP_OPEN -> "urdufonts_app_open"
        AdPlacement.DOWNLOAD_INTERSTITIAL -> "urdufonts_download_interstitial"
        AdPlacement.DOWNLOAD_FONT_REWARDED -> "urdufonts_download_font_rewarded"
        AdPlacement.FONT_DETAILS_BANNER -> "urdufonts_font_details_banner"
        AdPlacement.HOME_NATIVE -> "urdufonts_home_native"
        AdPlacement.FONT_LIST_NATIVE -> "urdufonts_font_list_native"
        AdPlacement.CATEGORIES_NATIVE -> "urdufonts_categories_native"
        AdPlacement.STYLES_NATIVE -> "urdufonts_styles_native"
    }

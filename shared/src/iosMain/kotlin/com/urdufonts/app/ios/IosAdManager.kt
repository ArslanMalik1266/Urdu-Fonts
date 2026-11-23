package com.urdufonts.app.ios

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.urdufonts.app.ads.AdManager
import com.urdufonts.app.ads.AdPlacement
import com.urdufonts.app.ads.NativeAdSize
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.platform.AppBuildConfig
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import platform.UIKit.UIView

/**
 * The shared [AdManager] on iOS: every call goes to WebsCare Ads for iOS through [AdsBridge] (Swift).
 * Placement ids are the same names as on Android (urdufonts_home_native, ...).
 */
class IosAdManager(
    private val ads: AdsBridge,
    private val userPreferences: UserPreferences,
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isProUser = false
    private val failedNative = mutableStateMapOf<String, Boolean>()

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
        ads.requestConsent { gathered -> scope.launch { onConsentResult(gathered) } }

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
        // The library enforces the cooldown and caps; no fill continues at once
        showInterstitial(activity, placement) {
            ads.preload("interstitial", placement.id)
            onDismissed()
        }
    }

    override fun suppressNextReturnAppOpen() = ads.suppressNextReturnAppOpen()

    override fun setAppOpenLoadingScreenVisible(visible: Boolean) = ads.setAppOpenLoadingScreenVisible(visible)

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun NativeAd(placement: AdPlacement, slot: Any, modifier: Modifier, size: NativeAdSize) {
        val isPro by userPreferences.isProUser.collectAsState(initial = false)
        val key = "${placement.id}#$slot"
        if (isPro || failedNative[key] == true) return
        val view: UIView = remember(key) {
            ads.makeNativeView(placement.id, size == NativeAdSize.SMALL) { scope.launch { failedNative[key] = true } }
        } ?: return
        UIKitView(
            factory = { view },
            modifier = modifier.fillMaxWidth().height(if (size == NativeAdSize.SMALL) 120.dp else 300.dp)
        )
    }

    override fun isNativeAdFailed(placement: AdPlacement, slot: Any): Boolean = failedNative["${placement.id}#$slot"] == true

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun BannerAd(placement: AdPlacement, modifier: Modifier) {
        val isPro by userPreferences.isProUser.collectAsState(initial = false)
        if (isPro) return
        val view: UIView = remember(placement) { ads.makeBannerView(placement.id) } ?: return
        UIKitView(factory = { view }, modifier = modifier.fillMaxWidth().height(60.dp))
    }
}

/** The same placement names the Android app uses with WebsCareAds. */
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

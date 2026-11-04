package com.urdufonts.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context

/**
 * Thin app-side facade over WebsCareAds 4.2. Screens ask for ads by placement name; the library owns
 * loading, caching, network fallback, frequency caps, consent and premium suppression.
 * Every show* call continues immediately (calls its callback) when no ad is ready.
 */
interface AdManager {
    fun initSdk(application: Application)
    fun requestConsent(activity: Activity, onConsentResult: (Boolean) -> Unit)

    /** True when Google requires a visible "Privacy options" entry (e.g. EEA users). */
    fun isPrivacyOptionsRequired(): Boolean
    fun showPrivacyOptions(activity: Activity, onComplete: () -> Unit = {})

    fun preloadAppOpen(context: Context, placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_APP_OPEN)

    /** Only call from a real loading screen (the splash); marks it as such while the ad decides. */
    fun showSplashAppOpen(
        activity: Activity,
        placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_APP_OPEN,
        onContinue: () -> Unit
    )

    fun preloadInterstitial(context: Context, placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_DOWNLOAD_INTERSTITIAL)
    fun showInterstitial(
        activity: Activity,
        placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_DOWNLOAD_INTERSTITIAL,
        onDismissed: () -> Unit
    )

    fun preloadRewarded(context: Context, placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_DOWNLOAD_FONT_REWARDED)

    /** [onRewarded] runs only from the SDK's earned-reward callback, never on dismiss or no-fill. */
    fun showRewarded(
        activity: Activity,
        placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_DOWNLOAD_FONT_REWARDED,
        onRewarded: (rewardType: String, amount: Int) -> Unit,
        onNotReady: () -> Unit
    )

    fun showDownloadInterstitialWithCooldown(
        activity: Activity,
        placement: String = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_DOWNLOAD_INTERSTITIAL,
        onDismissed: () -> Unit
    )
}

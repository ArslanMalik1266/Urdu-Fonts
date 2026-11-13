package com.urdufonts.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.urdufonts.app.BuildConfig
import com.urdufonts.app.data.local.UserPreferences
import com.webscare.ads.WebsCareAds
import com.webscare.ads.admob.AdMobAdapter
import com.webscare.ads.placements.WebsCarePlacements
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * WebsCareAds 4.2 implementation. No ad unit IDs, no direct Google Mobile Ads calls: the library reads
 * this package's Firebase policy, picks test inventory for debug builds and follows remote test_mode
 * for release builds.
 */
class WebsCareAdManagerImpl(
    private val userPreferences: UserPreferences? = null
) : AdManager {

    companion object {
        private const val APP_RESUME_PLACEMENT = "urdufonts_app_resume"
        private const val TAG = "WebsCareAdsLog"
        private const val LIB_TAG = "WebsCareAds"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var isProUser: Boolean = false

    override fun initSdk(application: Application) {
        WebsCareAds.init(application, adapters = listOf(AdMobAdapter()), isDebug = BuildConfig.DEBUG) {
            // Pro users never see ads, including ads that were already loaded
            premiumCheck = { isProUser }
            if (BuildConfig.DEBUG) {
                onAdEvent = { event -> Log.d(LIB_TAG, "onAdEvent: $event") }
                onAdError = { adType, placement, error -> Log.d(LIB_TAG, "onAdError: $adType '$placement' -> $error") }
            }
        }
        userPreferences?.let { prefs ->
            scope.launch {
                prefs.isProUser.collectLatest { isPro ->
                    isProUser = isPro
                    WebsCareAds.setPremium(isPro)
                }
            }
        }
        // Return-to-app (resume) ad uses its own placement, separate from the splash app-open (4.2.1+: any resumed screen unless restricted). Flows that leave
        // the app on purpose (billing, Google sign-in, browser/Play Store/email) call
        // suppressNextReturnAppOpen() first.
        WebsCareAds.enableAutoAppOpen(appResumePlacement())
    }

    override fun requestConsent(activity: Activity, onConsentResult: (Boolean) -> Unit) {
        WebsCareAds.requestConsent(activity) { isConsentGathered ->
            Log.d(TAG, "Consent finished, gathered=$isConsentGathered")
            onConsentResult(isConsentGathered)
        }
    }

    override fun isPrivacyOptionsRequired(): Boolean = WebsCareAds.isPrivacyOptionsRequired()

    override fun showPrivacyOptions(activity: Activity, onComplete: () -> Unit) {
        WebsCareAds.showConsentForm(activity, onComplete)
    }

    override fun preloadAppOpen(context: Context, placement: String) {
        if (isProUser) return
        WebsCareAds.preloadAppOpen(context, placement)
    }

    override fun showSplashAppOpen(activity: Activity, placement: String, onContinue: () -> Unit) {
        // 4.2.5: inside checkConsent, default timeout (counted from the ad request), no own delay.
        // onDismissed runs exactly once on every path (shown+dismissed, no fill, timeout, premium,
        // consent) — navigation happens only there.
        WebsCareAds.checkConsent(activity) {
            WebsCareAds.loadAndShowAppOpen(
                activity,
                placement,
                onFailed = { error -> Log.d(LIB_TAG, "Splash app-open not shown: $error") },
                onDismissed = onContinue
            )
        }
    }

    override fun preloadInterstitial(context: Context, placement: String) {
        if (isProUser) return
        WebsCareAds.preloadInterstitial(context, placement)
    }

    override fun showInterstitial(activity: Activity, placement: String, onDismissed: () -> Unit) {
        WebsCareAds.showInterstitial(
            activity,
            placement,
            onFailed = { error -> Log.d(TAG, "Interstitial $placement not shown: $error") },
            onDismissed = onDismissed
        )
    }

    override fun preloadRewarded(context: Context, placement: String) {
        if (isProUser) return
        WebsCareAds.preloadRewarded(context, placement)
    }

    override fun showRewarded(
        activity: Activity,
        placement: String,
        onRewarded: (rewardType: String, amount: Int) -> Unit,
        onNotReady: () -> Unit
    ) {
        WebsCareAds.showRewarded(
            activity,
            placement,
            onRewarded = onRewarded,
            onNotReady = onNotReady
        )
    }

    override fun showDownloadInterstitialWithCooldown(
        activity: Activity,
        placement: String,
        onDismissed: () -> Unit
    ) {
        if (isProUser) {
            onDismissed()
            return
        }
        // The library enforces the interstitial cooldown and caps; no-fill continues immediately
        showInterstitial(activity, placement) {
            preloadInterstitial(activity.applicationContext, placement)
            onDismissed()
        }
    }

    /**
     * Separate placement for the return-to-app ad. Uses "urdufonts_app_resume" once it exists in
     * Firebase (it then appears in WebsCarePlacements.ALL); until then falls back to app-open.
     */
    private fun appResumePlacement(): String =
        if (APP_RESUME_PLACEMENT in WebsCarePlacements.ALL) APP_RESUME_PLACEMENT
        else WebsCarePlacements.URDUFONTS_APP_OPEN
}

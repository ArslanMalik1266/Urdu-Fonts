package com.urdufonts.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.urdufonts.app.BuildConfig
import com.urdufonts.app.data.local.UserPreferences
import com.webscare.ads.WebsCareAds
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
        private const val TAG = "WebsCareAdsLog"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var isProUser: Boolean = false

    override fun initSdk(application: Application) {
        WebsCareAds.init(application, isDebug = BuildConfig.DEBUG) {
            // Pro users never see ads, including ads that were already loaded
            premiumCheck = { isProUser }
        }
        userPreferences?.let { prefs ->
            scope.launch {
                prefs.isProUser.collectLatest { isPro ->
                    isProUser = isPro
                    WebsCareAds.setPremium(isPro)
                }
            }
        }
        // Return-to-app app-open ads are intentionally NOT enabled: the app has no loading screen on
        // return, and 4.2 forbids app-open ads over already-usable content.
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
        // The splash is a real loading surface; the marker must be cleared on every path
        WebsCareAds.setAppOpenLoadingScreenVisible(true)
        WebsCareAds.showAppOpen(
            activity,
            placement,
            onFailed = { error -> Log.d(TAG, "App open not shown: $error") },
            onDismissed = {
                WebsCareAds.setAppOpenLoadingScreenVisible(false)
                onContinue()
            }
        )
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
}

package com.webscare.urdufonts.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.webscare.ads.WebsCareAds

class WebsCareAdManagerImpl : AdManager {

    companion object {
        private const val TAG = "WebsCareAdsLog"
    }

    override fun initSdk(application: Application) {
        Log.d(TAG, "Initializing Google Mobile Ads & WebsCareAds SDK...")
        try {
            // Print all public methods on WebsCareAds for complete inspection
            val websCareAdsMethods = WebsCareAds::class.java.methods.map { m ->
                "${m.name}(${m.parameterTypes.joinToString { it.simpleName }})"
            }
            Log.d(TAG, "ALL WebsCareAds PUBLIC METHODS: $websCareAdsMethods")

            WebsCareAds.init(application) {
                Log.d(TAG, "WebsCareAds internal engine initialized")
            }

            try {
                val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                com.google.android.gms.ads.MobileAds.setRequestConfiguration(configuration)
                Log.d(TAG, "AdMob RequestConfiguration set for DEVICE_ID_EMULATOR")
            } catch (e: Exception) {
                Log.d(TAG, "setRequestConfiguration note: $e")
            }

            com.google.android.gms.ads.MobileAds.initialize(application) { status ->
                Log.d(TAG, "AdMob MobileAds initialized successfully: ${status.adapterStatusMap}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds SDK", e)
        }
    }

    override fun requestConsent(activity: Activity, onConsentResult: (Boolean) -> Unit) {
        Log.d(TAG, "Requesting UMP consent for activity: ${activity.localClassName}")
        try {
            WebsCareAds.checkConsent(activity) {
                Log.d(TAG, "UMP Consent check callback triggered successfully")
                onConsentResult(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during UMP checkConsent", e)
            onConsentResult(false)
        }
    }

    override fun initAppOpenAd(application: Application, adUnitId: String) {
        Log.d(TAG, "Initializing App Open Ad with Unit ID: $adUnitId")
        try {
            WebsCareAds.enableAutoAppOpen(adUnitId = adUnitId)
            Log.d(TAG, "WebsCareAds.enableAutoAppOpen completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling App Open Ad", e)
        }
    }

    override fun preloadInterstitial(context: Context, adUnitId: String) {
        Log.d(TAG, "Preloading Interstitial Ad with Unit ID: $adUnitId")
        try {
            WebsCareAds.preloadInterstitial(context, adUnitId)
            Log.d(TAG, "WebsCareAds.preloadInterstitial call dispatched")
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading Interstitial Ad", e)
        }
    }

    override fun showInterstitial(
        activity: Activity,
        adUnitId: String,
        onDismissed: () -> Unit
    ) {
        Log.d(TAG, "Attempting to show Interstitial Ad (Unit ID: $adUnitId)...")
        try {
            WebsCareAds.showInterstitial(
                activity = activity,
                adUnitId = adUnitId,
                onDismissed = {
                    Log.d(TAG, "Interstitial Ad dismissed callback triggered")
                    onDismissed()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error showing Interstitial Ad", e)
            onDismissed()
        }
    }

    override fun preloadRewarded(context: Context, adUnitId: String) {
        Log.d(TAG, "Preloading Rewarded Ad with Unit ID: $adUnitId")
        try {
            WebsCareAds.preloadRewarded(context, adUnitId)
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading Rewarded Ad", e)
        }
    }

    override fun showRewarded(
        activity: Activity,
        adUnitId: String,
        onRewarded: (rewardItem: Any?, amount: Int) -> Unit,
        onNotReady: () -> Unit
    ) {
        Log.d(TAG, "Attempting to show Rewarded Ad (Unit ID: $adUnitId)...")
        try {
            WebsCareAds.showRewarded(
                activity = activity,
                adUnitId = adUnitId,
                onRewarded = { reward, amount ->
                    Log.d(TAG, "User rewarded: $amount")
                    onRewarded(reward, amount)
                },
                onNotReady = {
                    Log.w(TAG, "Rewarded Ad not ready")
                    onNotReady()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error showing Rewarded Ad", e)
            onNotReady()
        }
    }

    override fun loadBanner(activity: Activity, container: FrameLayout, adUnitId: String) {
        Log.d(TAG, "loadBanner called for activity: ${activity.localClassName}, Ad Unit: $adUnitId")
        try {
            WebsCareAds.loadBanner(activity, container, adUnitId)
            Log.d(TAG, "WebsCareAds.loadBanner call dispatched to container")
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadBanner", e)
        }
    }

    private var downloadCounter = 0
    private var lastInterstitialTime = 0L
    private val INTERSTITIAL_COOLDOWN_MS = 120_000L

    override fun showDownloadInterstitialWithCooldown(
        activity: Activity,
        adUnitId: String,
        onDismissed: () -> Unit
    ) {
        downloadCounter++
        val currentTime = System.currentTimeMillis()
        val isCooldownPassed = (currentTime - lastInterstitialTime) >= INTERSTITIAL_COOLDOWN_MS
        val isThirdDownload = (downloadCounter % 3 == 0)

        Log.d(TAG, "showDownloadInterstitialWithCooldown -> Download #$downloadCounter. Third Download: $isThirdDownload, Cooldown Passed: $isCooldownPassed")

        if (isThirdDownload && isCooldownPassed) {
            Log.d(TAG, "Conditions met! Showing download interstitial...")
            showInterstitial(activity, adUnitId) {
                lastInterstitialTime = System.currentTimeMillis()
                preloadInterstitial(activity.applicationContext, adUnitId)
                onDismissed()
            }
        } else {
            Log.d(TAG, "Conditions not met (Requires 3rd download & 120s cooldown). Skipping interstitial.")
            onDismissed()
        }
    }
}

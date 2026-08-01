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
                try {
                    WebsCareAds.preloadAppOpen(application, AdConfig.APP_OPEN_AD_UNIT_ID)
                    WebsCareAds.enableAutoAppOpen(adUnitId = AdConfig.APP_OPEN_AD_UNIT_ID)
                    Log.d(TAG, "WebsCareAds.preloadAppOpen & enableAutoAppOpen completed post MobileAds initialization")
                } catch (e: Exception) {
                    Log.e(TAG, "Error enabling App Open Ad post initialization", e)
                }
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

    override fun preloadAppOpen(context: Context, adUnitId: String) {
        Log.d(TAG, "Preloading App Open Ad with Context: ${context.javaClass.simpleName}, Unit ID: $adUnitId")
        try {
            WebsCareAds.preloadAppOpen(context, adUnitId)
            Log.d(TAG, "WebsCareAds.preloadAppOpen call dispatched with context")

            // Direct AdMob Diagnostic check to capture exact AdMob error if library hides it
            try {
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                com.google.android.gms.ads.appopen.AppOpenAd.load(
                    context,
                    adUnitId,
                    adRequest,
                    object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                            Log.d(TAG, "DIRECT ADMOB AppOpenAd LOADED SUCCESSFULLY! $ad")
                        }
                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            Log.e(TAG, "DIRECT ADMOB AppOpenAd FAILED TO LOAD! Code: ${loadAdError.code}, Message: ${loadAdError.message}, Domain: ${loadAdError.domain}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception running direct AdMob diagnostic", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading App Open Ad", e)
        }
    }

    override fun showAppOpenAd(activity: Activity, adUnitId: String, onDismissed: () -> Unit) {
        Log.d(TAG, "Attempting to show App Open Ad (Unit ID: $adUnitId)...")
        try {
            WebsCareAds.showAppOpen(activity, adUnitId) {
                Log.d(TAG, "App Open Ad dismissed callback triggered")
                onDismissed()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing App Open Ad", e)
            onDismissed()
        }
    }

    private var mInterstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null

    override fun preloadInterstitial(context: Context, adUnitId: String) {
        Log.d(TAG, "Preloading Interstitial Ad with Unit ID: $adUnitId")
        try {
            WebsCareAds.preloadInterstitial(context, adUnitId)
            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                        Log.d(TAG, "DIRECT ADMOB InterstitialAd LOADED SUCCESSFULLY! $ad")
                        mInterstitialAd = ad
                    }
                    override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                        Log.e(TAG, "DIRECT ADMOB InterstitialAd FAILED TO LOAD! Code: ${loadAdError.code}, Message: ${loadAdError.message}")
                        mInterstitialAd = null
                    }
                }
            )
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
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Direct AdMob Interstitial Ad dismissed by user")
                    mInterstitialAd = null
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Direct AdMob Interstitial Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Direct AdMob Interstitial Ad is now showing on screen!")
                    mInterstitialAd = null
                }
            }
            ad.show(activity)
        } else {
            Log.w(TAG, "mInterstitialAd is null, invoking WebsCareAds.showInterstitial fallback")
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

    override fun showDownloadInterstitialWithCooldown(
        activity: Activity,
        adUnitId: String,
        onDismissed: () -> Unit
    ) {
        downloadCounter++
        val isThirdDownload = (downloadCounter % 3 == 0)
        val isAdLoaded = WebsCareAds.isAdLoaded(adUnitId)

        Log.d(TAG, "showDownloadInterstitialWithCooldown -> Download #$downloadCounter. Third Download: $isThirdDownload, isAdLoaded: $isAdLoaded")

        if (isThirdDownload) {
            Log.d(TAG, "3rd Download reached! Showing Interstitial Ad for Unit ID: $adUnitId, isAdLoaded: $isAdLoaded...")
            showInterstitial(activity, adUnitId) {
                lastInterstitialTime = System.currentTimeMillis()
                Log.d(TAG, "Interstitial dismissed callback. Re-preloading for next 3rd download cycle...")
                preloadInterstitial(activity.applicationContext, adUnitId)
                onDismissed()
            }
        } else {
            Log.d(TAG, "Download #$downloadCounter (Not 3rd download). Skipping interstitial.")
            onDismissed()
        }
    }
}

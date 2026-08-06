package com.urdufonts.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import com.urdufonts.app.data.local.UserPreferences
import com.webscare.ads.WebsCareAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WebsCareAdManagerImpl(
    private val userPreferences: UserPreferences? = null
) : AdManager {

    companion object {
        private const val TAG = "WebsCareAdsLog"
    }

    private var currentActivityRef: java.lang.ref.WeakReference<Activity>? = null
    private var hasShownLaunchAd = false
    private var mAppOpenAd: com.google.android.gms.ads.appopen.AppOpenAd? = null
    private var isAppOpenLoading = false
    @Volatile
    private var isProUser: Boolean = false

    init {
        userPreferences?.let { prefs ->
            CoroutineScope(Dispatchers.Main.immediate).launch {
                prefs.isProUser.collectLatest { isPro ->
                    Log.d(TAG, "isProUser state updated in WebsCareAdManagerImpl: $isPro")
                    isProUser = isPro
                }
            }
        }
    }

    override fun initSdk(application: Application) {
        Log.d(TAG, "Initializing Google Mobile Ads & WebsCareAds SDK...")
        try {
            application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    currentActivityRef = java.lang.ref.WeakReference(activity)
                }
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    currentActivityRef = java.lang.ref.WeakReference(activity)
                }
                override fun onActivityStarted(activity: Activity) {
                    currentActivityRef = java.lang.ref.WeakReference(activity)
                }
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })

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
                if (!isProUser) {
                    try {
                        WebsCareAds.preloadAppOpen(application, AdConfig.APP_OPEN_AD_UNIT_ID)
                        WebsCareAds.enableAutoAppOpen(adUnitId = AdConfig.APP_OPEN_AD_UNIT_ID)
                        Log.d(TAG, "WebsCareAds.preloadAppOpen & enableAutoAppOpen completed post MobileAds initialization")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error enabling App Open Ad post initialization", e)
                    }
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
        if (isProUser) return
        Log.d(TAG, "Initializing App Open Ad with Unit ID: $adUnitId")
        try {
            WebsCareAds.enableAutoAppOpen(adUnitId = adUnitId)
            Log.d(TAG, "WebsCareAds.enableAutoAppOpen completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling App Open Ad", e)
        }
    }

    override fun preloadAppOpen(context: Context, adUnitId: String) {
        if (isProUser) return
        Log.d(TAG, "[APP_OPEN] preloadAppOpen called with Context: ${context.javaClass.simpleName}, Unit ID: $adUnitId")
        if (mAppOpenAd != null) {
            Log.d(TAG, "🟢 [APP_OPEN] Ad is already preloaded and ready to show!")
            return
        }
        if (isAppOpenLoading) {
            Log.d(TAG, "⏳ [APP_OPEN] Ad is currently loading in background, skipping duplicate request.")
            return
        }

        isAppOpenLoading = true
        Log.d(TAG, "🚀 [APP_OPEN] Dispatching AdMob load request for AppOpenAd...")
        try {
            WebsCareAds.preloadAppOpen(context, adUnitId)
            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
            com.google.android.gms.ads.appopen.AppOpenAd.load(
                context,
                adUnitId,
                adRequest,
                object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                        isAppOpenLoading = false
                        mAppOpenAd = ad
                        Log.d(TAG, "🟢 [APP_OPEN_SUCCESS] AppOpenAd LOADED SUCCESSFULLY! $ad")

                        if (!hasShownLaunchAd && !isProUser) {
                            val act = currentActivityRef?.get()
                            if (act != null && !act.isFinishing && !act.isDestroyed) {
                                Log.d(TAG, "⚡ [APP_OPEN_INSTANT] Ad loaded! Immediately showing on launch for Activity: ${act.localClassName}")
                                hasShownLaunchAd = true
                                showAppOpenAd(act, AdConfig.APP_OPEN_AD_UNIT_ID)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                        isAppOpenLoading = false
                        mAppOpenAd = null
                        Log.e(
                            TAG,
                            "🔴 [APP_OPEN_ERROR] AppOpenAd FAILED TO LOAD! Code: ${loadAdError.code}, Message: '${loadAdError.message}', Domain: '${loadAdError.domain}', ResponseInfo: ${loadAdError.responseInfo}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            isAppOpenLoading = false
            Log.e(TAG, "🔴 [APP_OPEN_ERROR] Exception preloading App Open Ad", e)
        }
    }

    override fun isAppOpenAdLoaded(): Boolean {
        if (isProUser) return false
        val loaded = mAppOpenAd != null || WebsCareAds.isAdLoaded(AdConfig.APP_OPEN_AD_UNIT_ID)
        Log.d(TAG, "[APP_OPEN] isAppOpenAdLoaded check: $loaded (mAppOpenAd != null: ${mAppOpenAd != null})")
        return loaded
    }

    override fun showAppOpenAd(activity: Activity, adUnitId: String, onDismissed: () -> Unit) {
        if (isProUser) {
            Log.d(TAG, "[APP_OPEN] User is PRO. Skipping App Open Ad display.")
            onDismissed()
            return
        }
        Log.d(TAG, "[APP_OPEN] showAppOpenAd called for Activity: ${activity.localClassName}, Unit ID: $adUnitId")
        val ad = mAppOpenAd
        if (ad != null) {
            Log.d(TAG, "🟢 [APP_OPEN] Showing direct mAppOpenAd on screen!")
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "🟢 [APP_OPEN] AppOpenAd full screen content is NOW SHOWING!")
                    mAppOpenAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "🔴 [APP_OPEN_SHOW_ERROR] AppOpenAd failed to show: Code: ${adError.code}, Message: ${adError.message}")
                    mAppOpenAd = null
                    preloadAppOpen(activity.applicationContext, adUnitId)
                    onDismissed()
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "🟢 [APP_OPEN] AppOpenAd dismissed by user. Re-preloading NEXT AppOpenAd for future foreground resume...")
                    mAppOpenAd = null
                    preloadAppOpen(activity.applicationContext, adUnitId)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.w(TAG, "⚠️ [APP_OPEN_NOT_READY] Direct mAppOpenAd is null (not ready yet). Trying WebsCareAds.showAppOpen fallback...")
            try {
                WebsCareAds.showAppOpen(activity, adUnitId) {
                    Log.d(TAG, "WebsCareAds.showAppOpen callback triggered. Re-preloading NEXT AppOpenAd...")
                    preloadAppOpen(activity.applicationContext, adUnitId)
                    onDismissed()
                }
            } catch (e: Exception) {
                Log.e(TAG, "🔴 [APP_OPEN_ERROR] Error in WebsCareAds.showAppOpen fallback", e)
                preloadAppOpen(activity.applicationContext, adUnitId)
                onDismissed()
            }
        }
    }

    private var mInterstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null

    override fun preloadInterstitial(context: Context, adUnitId: String) {
        if (isProUser) return
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
        if (isProUser) {
            Log.d(TAG, "User is PRO. Bypassing Interstitial Ad.")
            onDismissed()
            return
        }
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
        if (isProUser) return
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
        if (isProUser) {
            Log.d(TAG, "User is PRO. Granting reward immediately without ad.")
            onRewarded(null, 1)
            return
        }
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
        if (isProUser) {
            Log.d(TAG, "User is PRO. Skipping Banner Ad load.")
            return
        }
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
        if (isProUser) {
            Log.d(TAG, "User is PRO. Instant download without cooldown ad.")
            onDismissed()
            return
        }
        downloadCounter++
        Log.d(TAG, "showDownloadInterstitialWithCooldown -> Download #$downloadCounter. Showing Interstitial Ad...")

        showInterstitial(activity, adUnitId) {
            lastInterstitialTime = System.currentTimeMillis()
            Log.d(TAG, "Interstitial dismissed callback. Re-preloading for next download...")
            preloadInterstitial(activity.applicationContext, adUnitId)
            onDismissed()
        }
    }
}

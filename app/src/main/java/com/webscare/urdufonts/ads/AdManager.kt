package com.webscare.urdufonts.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.FrameLayout

interface AdManager {
    fun initSdk(application: Application)
    fun requestConsent(activity: Activity, onConsentResult: (Boolean) -> Unit)
    fun initAppOpenAd(application: Application, adUnitId: String = AdConfig.APP_OPEN_AD_UNIT_ID)
    fun preloadInterstitial(context: Context, adUnitId: String = AdConfig.INTERSTITIAL_AD_UNIT_ID)
    fun showInterstitial(
        activity: Activity,
        adUnitId: String = AdConfig.INTERSTITIAL_AD_UNIT_ID,
        onDismissed: () -> Unit
    )
    fun preloadRewarded(context: Context, adUnitId: String = AdConfig.REWARDED_AD_UNIT_ID)
    fun showRewarded(
        activity: Activity,
        adUnitId: String = AdConfig.REWARDED_AD_UNIT_ID,
        onRewarded: (rewardItem: Any?, amount: Int) -> Unit,
        onNotReady: () -> Unit
    )
    fun loadBanner(
        activity: Activity,
        container: FrameLayout,
        adUnitId: String = AdConfig.BANNER_AD_UNIT_ID
    )
    fun showDownloadInterstitialWithCooldown(
        activity: Activity,
        adUnitId: String = AdConfig.INTERSTITIAL_AD_UNIT_ID,
        onDismissed: () -> Unit
    )
}

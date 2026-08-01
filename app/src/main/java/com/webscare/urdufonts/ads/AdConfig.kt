package com.webscare.urdufonts.ads

import android.content.Context
import android.content.pm.ApplicationInfo

object AdConfig {
    // 🟢 Automatic System Detection: True for Debug builds, False for Release builds
    var IS_DEBUG: Boolean = true

    fun init(context: Context) {
        IS_DEBUG = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // 1. Banner Ad (Font Details Banner)
    val BANNER_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/6300978111" // Test ID
        } else {
            "ca-app-pub-4379805490947109/5776477372" // Production ID (Font Details Banner)
        }

    // 2. Native Advanced Ads (Separate IDs per screen for distinct analytics)
    val NATIVE_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/2247696110" // Test ID
        } else {
            "ca-app-pub-4379805490947109/7947345145" // Default Fallback (Home Native)
        }

    val HOME_NATIVE_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/2247696110" // Test ID
        } else {
            "ca-app-pub-4379805490947109/7947345145" // Production ID (Home Native)
        }

    val CATEGORIES_NATIVE_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/2247696110" // Test ID
        } else {
            "ca-app-pub-4379805490947109/3418621355" // Production ID (Categories Native)
        }

    val STYLES_NATIVE_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/2247696110" // Test ID
        } else {
            "ca-app-pub-4379805490947109/2105539687" // Production ID (Styles Native)
        }

    val FONT_LIST_NATIVE_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/2247696110" // Test ID
        } else {
            "ca-app-pub-4379805490947109/9792458015" // Production ID (Font List Native)
        }

    // 3. Interstitial Ad (Download Interstitial)
    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/1033173712" // Test ID
        } else {
            "ca-app-pub-4379805490947109/7205109116" // Production ID (Download Interstitial)
        }

    // 4. Rewarded Video Ad
    val REWARDED_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/5224354917" // Test ID
        } else {
            "ca-app-pub-4379805490947109/5224354917" // Production ID
        }

    // 5. App Open Ad (App Open)
    val APP_OPEN_AD_UNIT_ID: String
        get() = if (IS_DEBUG) {
            "ca-app-pub-3940256099942544/9257395921" // Test ID
        } else {
            "ca-app-pub-4379805490947109/9984029708" // Production ID (App Open)
        }
}

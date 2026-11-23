package com.urdufonts.app.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.PlatformContext

/**
 * The screen the UI is showing in: the Android Activity, or the iOS root view controller. Ads, billing and
 * sign-in need it to present their own UI. Null only while there is no screen (should not happen in practice).
 */
@Composable
expect fun rememberPlatformActivity(): PlatformActivity?

/** A short message at the bottom of the screen (Android Toast; on iOS a brief self-dismissing alert). */
expect fun showToast(message: String)

/** The wallpaper-based colour scheme (Android 12+ "Material You"), or null where there is none. */
@Composable
expect fun dynamicColorScheme(dark: Boolean): ColorScheme?

/**
 * While shown, the screen itself makes room for the keyboard (imePadding) instead of the window panning.
 * Android switches the window's soft-input mode; iOS needs nothing.
 */
@Composable
expect fun ResizeForKeyboardEffect()

/** Whether a real background blur behind dialogs is available (Android 12+, iOS always). */
expect val supportsBackgroundBlur: Boolean

/** Opens things outside the app: web pages, the store, the mail app, other apps. */
expect class ExternalNavigator {
    fun openEmailSupport(email: String = "hello@webscare.com")
    fun openWebPage(url: String)
    fun openPlayStoreForRating()
    fun openOrInstallApp(packageName: String = "com.webscare.urducanvas")
    fun openPlayStoreForPackage(packageName: String)
}

/** The platform context for APIs that need one (Google sign-in). Android: LocalContext; iOS: a stand-in. */
@Composable
expect fun rememberPlatformContext(): PlatformContext

/** Size of the app's cache folders, and clearing them (Settings → Clear cache). */
expect object AppCache {
    fun sizeBytes(): Long
    fun clear(): Boolean
}

/** The store page where the user manages (cancels, changes) their subscription. */
expect fun manageSubscriptionsUrl(activeSku: String?): String

/** A stable anonymous id for this install's device, used as the guest id before sign-in. */
expect fun anonymousDeviceId(): String

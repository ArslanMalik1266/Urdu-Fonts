package com.urdufonts.app.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.AppDirectories
import com.urdufonts.app.platform.IosPlatformContext
import com.urdufonts.app.platform.PlatformContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.UIKit.UIDevice
import platform.Foundation.NSURL
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.darwin.NSEC_PER_SEC

/**
 * The iOS app's App Store ids, for the rating link and for "get the app" links. Fill them in once the apps
 * are on the App Store; until then the links open the web version.
 */
object IosStoreIds {
    var urduFontsAppId: String = ""
    /** Android package name → App Store app id, for the apps the UI links to. */
    val appIdsByAndroidPackage: MutableMap<String, String> = mutableMapOf()
}

@Composable
actual fun rememberPlatformActivity(): PlatformActivity? = PlatformActivity()

actual fun showToast(message: String) {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0), dispatch_get_main_queue()) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@dispatch_after
        val presenter = generateSequence(root) { it.presentedViewController }.last()
        val alert = UIAlertController.alertControllerWithTitle(null, message, UIAlertControllerStyleAlert)
        presenter.presentViewController(alert, animated = true, completion = null)
        // Like a toast: goes away by itself
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (2 * NSEC_PER_SEC).toLong()), dispatch_get_main_queue()) {
            alert.dismissViewControllerAnimated(true, completion = null)
        }
    }
}

@Composable
actual fun dynamicColorScheme(dark: Boolean): ColorScheme? = null

@Composable
actual fun ResizeForKeyboardEffect() = Unit

actual val supportsBackgroundBlur: Boolean = true

actual class ExternalNavigator {

    actual fun openEmailSupport(email: String) {
        val subject = encode("Support - Urdu Fonts App")
        if (!open("mailto:$email?subject=$subject")) showToast("No email app is set up")
    }

    actual fun openWebPage(url: String) {
        if (url.isBlank()) {
            showToast("Link not configured yet")
            return
        }
        if (!open(url)) showToast("Cannot open link")
    }

    actual fun openPlayStoreForRating() {
        val id = IosStoreIds.urduFontsAppId
        if (id.isNotBlank()) open("itms-apps://itunes.apple.com/app/id$id?action=write-review")
        else showToast("Rating will be available once the app is on the App Store")
    }

    /** iOS cannot launch another app by its package name: it opens that app's App Store page instead. */
    actual fun openOrInstallApp(packageName: String) = openPlayStoreForPackage(packageName)

    actual fun openPlayStoreForPackage(packageName: String) {
        val id = IosStoreIds.appIdsByAndroidPackage[packageName]
        if (id != null) open("itms-apps://itunes.apple.com/app/id$id")
        else openWebPage("https://play.google.com/store/apps/details?id=$packageName")
    }

    private fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val app = UIApplication.sharedApplication
        if (!app.canOpenURL(nsUrl)) return false
        app.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
        return true
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun encode(text: String): String =
        (text as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: text
}

@Composable
actual fun rememberPlatformContext(): PlatformContext = IosPlatformContext

actual object AppCache {
    private val root: Path get() = Path(AppDirectories().cacheDir)

    actual fun sizeBytes(): Long = sizeOf(root)

    actual fun clear(): Boolean = try {
        SystemFileSystem.list(root).forEach { deleteTree(it) }
        true
    } catch (e: Exception) {
        false
    }

    private fun sizeOf(path: Path): Long {
        val meta = SystemFileSystem.metadataOrNull(path) ?: return 0L
        return if (meta.isDirectory) SystemFileSystem.list(path).sumOf { sizeOf(it) } else meta.size
    }

    private fun deleteTree(path: Path) {
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) SystemFileSystem.list(path).forEach { deleteTree(it) }
        SystemFileSystem.delete(path, mustExist = false)
    }
}

/** Apple's own page for managing App Store subscriptions. */
actual fun manageSubscriptionsUrl(activeSku: String?): String = "https://apps.apple.com/account/subscriptions"

actual fun anonymousDeviceId(): String {
    val vendorId = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"
    return "anon_ios_" + vendorId.replace("-", "").lowercase()
}

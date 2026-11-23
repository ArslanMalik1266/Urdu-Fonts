package com.urdufonts.app.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.urdufonts.app.ads.suppressNextReturnAppOpen
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.platform.PlatformContext
import com.urdufonts.app.data.local.AndroidDeviceId
import java.io.File
import org.koin.mp.KoinPlatform

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun rememberPlatformActivity(): PlatformActivity? = LocalContext.current.findActivity()

actual fun showToast(message: String) {
    val context = KoinPlatform.getKoinOrNull()?.getOrNull<Context>() ?: return
    // Toasts must be shown from the main thread
    Handler(Looper.getMainLooper()).post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
}

@Composable
actual fun dynamicColorScheme(dark: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

@Composable
actual fun ResizeForKeyboardEffect() {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window
        val before = window?.attributes?.softInputMode
        @Suppress("DEPRECATION")
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose { if (before != null) window.setSoftInputMode(before) }
    }
}

actual val supportsBackgroundBlur: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

actual class ExternalNavigator(private val context: Context) {

    actual fun openEmailSupport(email: String) {
        suppressNextReturnAppOpen()
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Support - Urdu Fonts App")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast("No email client installed")
        }
    }

    actual fun openWebPage(url: String) {
        suppressNextReturnAppOpen()
        if (url.isBlank()) {
            showToast("Link not configured yet")
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            showToast("Cannot open link")
        }
    }

    actual fun openPlayStoreForRating() = openPlayStoreForPackage(context.packageName)

    actual fun openOrInstallApp(packageName: String) {
        suppressNextReturnAppOpen()
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else {
                openPlayStoreForPackage(packageName)
            }
        } catch (e: Exception) {
            openPlayStoreForPackage(packageName)
        }
    }

    actual fun openPlayStoreForPackage(packageName: String) {
        suppressNextReturnAppOpen()
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            openWebPage("https://play.google.com/store/apps/details?id=$packageName")
        }
    }
}

@Composable
actual fun rememberPlatformContext(): PlatformContext = LocalContext.current

private val appContext: Context get() = KoinPlatform.getKoin().get()

actual object AppCache {
    actual fun sizeBytes(): Long =
        listOfNotNull(appContext.cacheDir, appContext.externalCacheDir).sumOf { folderSize(it) }

    actual fun clear(): Boolean = try {
        appContext.cacheDir?.deleteRecursively()
        appContext.externalCacheDir?.deleteRecursively()
        true
    } catch (e: Exception) {
        false
    }

    private fun folderSize(dir: File): Long =
        dir.listFiles()?.sumOf { if (it.isFile) it.length() else folderSize(it) } ?: 0L
}

actual fun manageSubscriptionsUrl(activeSku: String?): String {
    val pkg = appContext.packageName
    return if (activeSku != null) "https://play.google.com/store/account/subscriptions?sku=$activeSku&package=$pkg"
    else "https://play.google.com/store/account/subscriptions?package=$pkg"
}

actual fun anonymousDeviceId(): String = AndroidDeviceId.get(appContext)

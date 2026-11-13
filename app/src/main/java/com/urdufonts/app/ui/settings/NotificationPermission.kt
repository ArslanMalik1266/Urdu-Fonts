package com.urdufonts.app.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Whether the phone lets the app show notifications, and the two ways of asking for it. [ask] shows the
 * system's own question where there is one (Android 13 and later) and does nothing otherwise. [turnOn] is for
 * a switch the customer pressed: it asks, or, when the phone no longer shows the question, opens the app's
 * notification settings. [granted] is read again whenever the app comes back to the front.
 */
class NotificationPermission(
    val granted: Boolean,
    val ask: () -> Unit,
    val turnOn: () -> Unit,
)

@Composable
fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    val activity = context.findActivity()
    var granted by remember { mutableStateOf(context.canNotify()) }
    // The customer may have changed it in the phone's settings while away
    LifecycleResumeEffect(context) {
        granted = context.canNotify()
        onPauseOrDispose { }
    }
    // Set while the question is up because the switch was pressed: a "no" the phone gave by itself (it no
    // longer shows the question) then leads to the app's notification settings instead
    var openSettingsIfRefused by remember { mutableStateOf(false) }
    val question = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = context.canNotify()
        if (!allowed && openSettingsIfRefused && activity != null && !activity.wouldExplain()) context.openNotificationSettings()
        openSettingsIfRefused = false
    }
    val hasQuestion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    return NotificationPermission(
        granted = granted,
        ask = { if (hasQuestion && !granted) question.launch(Manifest.permission.POST_NOTIFICATIONS) },
        turnOn = {
            if (hasQuestion && activity != null) {
                // Before the first question, and after a single "no", the phone still asks
                openSettingsIfRefused = !activity.wouldExplain()
                question.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                context.openNotificationSettings()
            }
        },
    )
}


/** The activity a context belongs to, found through the wrappers Compose puts around it. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.canNotify(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

private fun Activity.wouldExplain(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", packageName, null))
    }
    com.webscare.ads.WebsCareAds.suppressNextReturnAppOpen()
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

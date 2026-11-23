package com.urdufonts.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberNotificationPermission(): NotificationPermission {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    var granted by remember { mutableStateOf(false) }
    var neverAsked by remember { mutableStateOf(true) }

    fun refresh() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            dispatch_async(dispatch_get_main_queue()) {
                granted = status == UNAuthorizationStatusAuthorized ||
                    status == UNAuthorizationStatusProvisional ||
                    status == UNAuthorizationStatusEphemeral
                neverAsked = status == UNAuthorizationStatusNotDetermined
            }
        }
    }

    // The customer may have changed it in Settings while away
    LifecycleResumeEffect(Unit) {
        refresh()
        onPauseOrDispose { }
    }

    fun requestPermission() {
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { allowed, _ ->
            dispatch_async(dispatch_get_main_queue()) {
                if (allowed) UIApplication.sharedApplication.registerForRemoteNotifications()
                refresh()
            }
        }
    }

    return NotificationPermission(
        granted = granted,
        ask = { if (!granted && neverAsked) requestPermission() },
        turnOn = {
            // iOS asks only once; after a "no" the only way back is the app's page in Settings
            if (neverAsked) requestPermission() else openAppSettings()
        },
    )
}

private fun openAppSettings() {
    com.urdufonts.app.ads.suppressNextReturnAppOpen()
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
}

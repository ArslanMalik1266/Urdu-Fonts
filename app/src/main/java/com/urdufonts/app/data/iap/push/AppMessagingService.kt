package com.urdufonts.app.data.iap.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.NotificationTap
import com.urdufonts.app.domain.repo.PushTokenSource
import com.urdufonts.app.domain.usecases.RegisterPushTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume

/**
 * Receives push from the backend. With the app closed or in the background Android shows the notification by
 * itself and this class is not asked; it is only asked while the app is open, and then shows the notification
 * the same way, with the same details for the tap.
 */
class AppMessagingService : FirebaseMessagingService(), KoinComponent {

    private val registerPushToken: RegisterPushTokenUseCase by inject()

    // Outlives the service's own short life, so a registration that has started is finished
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Google gave this install a new token: the backend is told straight away. */
    override fun onNewToken(token: String) {
        scope.launch { registerPushToken() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val data = message.data
        val open = packageManager.getLaunchIntentForPackage(packageName) ?: return
        open.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        // The same extras a notification shown by Android itself carries
        data.forEach { (key, value) -> open.putExtra(key, value) }
        val requestCode = (data[PushKeys.ID] ?: title).hashCode()
        val tap = PendingIntent.getActivity(this, requestCode, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, message.notification?.channelId ?: PushSetup.CHANNEL_TRANSACTIONAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(this, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(message.notification?.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.notification?.body))
            .setAutoCancel(true)
            .setContentIntent(tap)
            .build()
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed) NotificationManagerCompat.from(this).notify(requestCode, notification)
    }
}

/** The two channels the backend names in every push. Created once at app start. */
object PushSetup {
    const val CHANNEL_TRANSACTIONAL = "transactional"
    const val CHANNEL_MARKETING = "marketing"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TRANSACTIONAL, context.getString(R.string.push_channel_transactional), NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_MARKETING, context.getString(R.string.push_channel_marketing), NotificationManager.IMPORTANCE_DEFAULT)
        )
    }
}

/** The phone's Firebase Cloud Messaging token. Null when Google cannot give one now. */
class FirebasePushTokenSource : PushTokenSource {
    override suspend fun token(): String? = try {
        // Google can take long (or never answer) without Play services or network: never hold the caller up
        kotlinx.coroutines.withTimeoutOrNull(15_000) { suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (continuation.isActive) {
                    continuation.resume(if (task.isSuccessful) task.result?.takeIf { it.isNotBlank() } else null)
                }
            }
        } }
    } catch (e: IllegalStateException) {
        android.util.Log.w("IapPush", "Firebase is not started", e)
        // Firebase was not started in this process
        null
    }
}

/** The names of the details the backend puts in every push. All their values are text. */
object PushKeys {
    const val ID = "nid"
    const val TYPE = "type"
    const val CATEGORY = "category"
    const val DEEP_LINK = "deep_link"
    const val TICKET_ID = "ticket_id"
}

/**
 * The notification this intent was opened from, if it was opened from one. Its id is taken off the intent, so
 * the same tap is never acted on twice (the activity can be rebuilt with the same intent).
 */
fun Intent.takeNotificationTap(): NotificationTap? {
    val details = extras ?: return null
    val id = details.getString(PushKeys.ID)?.takeIf { it.isNotBlank() } ?: return null
    removeExtra(PushKeys.ID)
    return NotificationTap(
        id = id,
        type = details.getString(PushKeys.TYPE).orEmpty(),
        category = details.getString(PushKeys.CATEGORY).orEmpty(),
        deepLink = details.getString(PushKeys.DEEP_LINK)?.takeIf { it.isNotBlank() },
        ticketId = details.getString(PushKeys.TICKET_ID)?.toIntOrNull(),
    )
}

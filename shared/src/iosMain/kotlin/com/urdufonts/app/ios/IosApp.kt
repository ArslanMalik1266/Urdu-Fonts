package com.urdufonts.app.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.urdufonts.app.ads.AdManager
import com.urdufonts.app.data.iap.PlanExpiryWatcher
import com.urdufonts.app.di.domainModule
import com.urdufonts.app.di.networkModule
import com.urdufonts.app.di.platformModule
import com.urdufonts.app.di.presentationModule
import com.urdufonts.app.di.sharedDataModule
import com.urdufonts.app.domain.models.NotificationTap
import com.urdufonts.app.domain.models.NotificationTapInbox
import com.urdufonts.app.domain.repo.BillingRepository
import com.urdufonts.app.domain.repo.FontRepository
import com.urdufonts.app.domain.repo.GoogleSignInProvider
import com.urdufonts.app.domain.repo.PushTokenSource
import com.urdufonts.app.domain.usecases.RetryPushRegistrationUseCase
import com.urdufonts.app.platform.AppBuildConfig
import com.urdufonts.app.platform.IosPlatformContext
import com.urdufonts.app.platform.PlatformActivity
import com.urdufonts.app.ui.AppInitViewModel
import com.urdufonts.app.ui.navigation.AppNavigation
import com.urdufonts.app.ui.util.buildAppImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * Called once by the iOS app (iOSApp.swift) before any UI: build-time values, the Swift bridges, Koin with the
 * same shared modules as Android plus the iOS implementations, the image loader, ads and the plan watcher.
 */
fun initUrduFonts(
    bridges: IosBridges,
    apiKey: String,
    iapAppToken: String,
    versionName: String,
    isDebug: Boolean,
) {
    AppBuildConfig.init(apiKey = apiKey, iapAppToken = iapAppToken, versionName = versionName, isDebug = isDebug)

    val iosModule = module {
        single { bridges }
        single<AdManager> { IosAdManager(bridges.ads, get()) }
        single<GoogleSignInProvider> { IosGoogleSignIn(bridges.googleSignIn) }
        single<PushTokenSource> { IosPushTokenSource(bridges.push) }
        single<BillingRepository> { IosBillingRepository(bridges.storeKit, get(), get(), get(), get()) }
        single<FontRepository> { IosFontRepository(apiService = get(), fontDao = get(), directories = get()) }
    }
    startKoin {
        modules(platformModule, networkModule, sharedDataModule, iosModule, domainModule, presentationModule)
    }

    val koin = KoinPlatform.getKoin()
    // One image loader for the whole app (SVG thumbnails, disk cache), as MyApplication sets it on Android
    SingletonImageLoader.setSafe { context: PlatformContext -> buildAppImageLoader(context, koin.get<com.urdufonts.app.platform.AppDirectories>().cacheDir) }

    koin.get<AppInitViewModel>()
    koin.get<PlanExpiryWatcher>().start()
    koin.get<AdManager>().apply {
        initSdk(IosPlatformContext)
        requestConsent(PlatformActivity()) { }
    }
    koin.get<BillingRepository>().startConnection()
}

/** The whole app as one UIViewController, shown by ContentView.swift. */
fun MainViewController(): UIViewController = ComposeUIViewController { AppNavigation() }

/** The app came to the front: resend a push registration that failed, re-check an expired plan. */
fun onAppResumed() {
    val koin = KoinPlatform.getKoinOrNull() ?: return
    MainScope().launch { runCatching { koin.get<RetryPushRegistrationUseCase>()() } }
    koin.get<PlanExpiryWatcher>().onAppResumed()
}

/**
 * A notification was tapped. [userInfo] is the push's data (the same keys as on Android: nid, type, category,
 * deep_link, ticket_id). The app opens where it leads once it is ready (see AppNavigation).
 */
fun onNotificationTapped(userInfo: Map<Any?, *>) {
    fun value(key: String) = userInfo[key]?.toString()?.takeIf { it.isNotBlank() }
    val id = value("nid") ?: return
    val tap = NotificationTap(
        id = id,
        type = value("type").orEmpty(),
        category = value("category").orEmpty(),
        deepLink = value("deep_link"),
        ticketId = value("ticket_id")?.toIntOrNull(),
    )
    KoinPlatform.getKoinOrNull()?.get<NotificationTapInbox>()?.post(tap)
}

/** A new FCM token arrived (Firebase's messaging(_:didReceiveRegistrationToken:)): register it with the backend. */
fun onPushTokenRefreshed() {
    val koin = KoinPlatform.getKoinOrNull() ?: return
    MainScope().launch { runCatching { koin.get<com.urdufonts.app.domain.repo.PushRepository>().register() } }
}

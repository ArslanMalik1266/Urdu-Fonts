package com.urdufonts.app

import android.app.Application
import com.urdufonts.app.di.appModule
import com.urdufonts.app.ui.AppInitViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

class MyApplication : Application(), coil3.SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        // Keys and version for the shared (KMP) network code; must run before Koin builds the HTTP clients
        com.urdufonts.app.platform.AppBuildConfig.init(
            apiKey = BuildConfig.API_KEY,
            iapAppToken = BuildConfig.IAP_APP_TOKEN,
            versionName = BuildConfig.VERSION_NAME,
            isDebug = BuildConfig.DEBUG
        )
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(appModule)
        }
        File(cacheDir, "font_previews").deleteRecursively()
        // The two channels the IAP backend sends its pushes to
        com.urdufonts.app.data.iap.push.PushSetup.createChannels(this)
        getKoin().get<AppInitViewModel>()
        // Re-checks the plan with the server when its time from the entitlement API runs out
        getKoin().get<com.urdufonts.app.data.iap.PlanExpiryWatcher>().start()
        val adManager = getKoin().get<com.urdufonts.app.ads.AdManager>()
        adManager.initSdk(this)
    }

    override fun newImageLoader(context: coil3.PlatformContext): coil3.ImageLoader =
        com.urdufonts.app.ui.util.buildAppImageLoader(context, cacheDir.absolutePath)
}
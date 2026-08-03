package com.urdufonts.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.urdufonts.app.di.appModule
import com.urdufonts.app.ui.AppInitViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        com.urdufonts.app.ads.AdConfig.init(this)
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(appModule)
        }
        File(cacheDir, "font_previews").deleteRecursively()
        getKoin().get<AppInitViewModel>()
        val adManager = getKoin().get<com.urdufonts.app.ads.AdManager>()
        adManager.initSdk(this)
        adManager.initAppOpenAd(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
package com.urdufonts.app.ui.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.svg.SvgDecoder
import okio.Path.Companion.toPath

/**
 * The one image loader for the whole app (both platforms): SVG category/style thumbnails, a 15% memory
 * cache and a 50 MB disk cache in the app's cache folder. Images come over the network through Ktor.
 */
fun buildAppImageLoader(context: PlatformContext, cacheDir: String): ImageLoader =
    ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.15)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory("$cacheDir/image_cache".toPath())
                .maxSizeBytes(50L * 1024L * 1024L)
                .build()
        }
        .components { add(SvgDecoder.Factory()) }
        .build()

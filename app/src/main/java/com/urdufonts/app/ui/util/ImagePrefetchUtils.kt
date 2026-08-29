package com.urdufonts.app.ui.util

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

/**
 * Preloads a list of image URLs into Coil's disk & memory cache synchronously for atomic readiness.
 */
suspend fun preloadImageUrls(context: Context, urls: List<String>) = withContext(Dispatchers.IO) {
    val imageLoader = Coil.imageLoader(context)
    val validUrls = urls.filter { it.isNotBlank() }
    if (validUrls.isEmpty()) return@withContext

    validUrls.map { url ->
        async {
            val request = ImageRequest.Builder(context)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            try {
                imageLoader.enqueue(request).job.await()
            } catch (_: Exception) {}
        }
    }.awaitAll()
}

/**
 * Enqueues image requests asynchronously in background for continuous viewport prefetching.
 */
fun prefetchImageUrlsAsync(context: Context, urls: List<String>) {
    val imageLoader = Coil.imageLoader(context)
    urls.filter { it.isNotBlank() }.forEach { url ->
        val request = ImageRequest.Builder(context)
            .data(url)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
        imageLoader.enqueue(request)
    }
}

/**
 * Pre-fetches images for upcoming items in LazyColumn using snapshotFlow to eliminate recomposition overhead.
 */
@Composable
fun LazyColumnImagePrefetcher(
    listState: LazyListState,
    imageUrls: List<String>,
    bufferAheadCount: Int = 4
) {
    val context = LocalContext.current
    val prefetchedSet = remember { mutableSetOf<String>() }

    LaunchedEffect(listState, imageUrls) {
        if (imageUrls.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { firstVisibleIndex ->
                val startIndex = firstVisibleIndex
                val endIndex = minOf(imageUrls.size, startIndex + bufferAheadCount)
                if (startIndex < endIndex) {
                    val newUrlsToFetch = imageUrls.subList(startIndex, endIndex).filter { url ->
                        url.isNotBlank() && prefetchedSet.add(url)
                    }
                    if (newUrlsToFetch.isNotEmpty()) {
                        prefetchImageUrlsAsync(context, newUrlsToFetch)
                    }
                }
            }
    }
}

/**
 * Pre-fetches images for upcoming items in LazyGrid using snapshotFlow to eliminate recomposition overhead.
 */
@Composable
fun LazyGridImagePrefetcher(
    gridState: LazyGridState,
    imageUrls: List<String>,
    bufferAheadCount: Int = 4
) {
    val context = LocalContext.current
    val prefetchedSet = remember { mutableSetOf<String>() }

    LaunchedEffect(gridState, imageUrls) {
        if (imageUrls.isEmpty()) return@LaunchedEffect
        snapshotFlow { gridState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { firstVisibleIndex ->
                val startIndex = firstVisibleIndex
                val endIndex = minOf(imageUrls.size, startIndex + bufferAheadCount)
                if (startIndex < endIndex) {
                    val newUrlsToFetch = imageUrls.subList(startIndex, endIndex).filter { url ->
                        url.isNotBlank() && prefetchedSet.add(url)
                    }
                    if (newUrlsToFetch.isNotEmpty()) {
                        prefetchImageUrlsAsync(context, newUrlsToFetch)
                    }
                }
            }
    }
}

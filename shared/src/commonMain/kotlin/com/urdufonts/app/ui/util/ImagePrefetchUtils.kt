package com.urdufonts.app.ui.util

import coil3.PlatformContext
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import coil3.compose.LocalPlatformContext
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

typealias ImageRequestFactory = (PlatformContext, String?) -> ImageRequest

/**
 * Preloads the first visible images into Coil's memory cache so the list appears with images already drawn.
 * [requestFor] must be the same factory the card uses (see [CardImageRequests]) or the cache key won't match.
 * Waits at most [maxWaitMs]; on a slow network the list shows anyway and the rest load inside the cards.
 */
suspend fun preloadImageUrls(
    context: PlatformContext,
    urls: List<String>,
    requestFor: ImageRequestFactory,
    maxWaitMs: Long = 500L
) {
    val imageLoader = SingletonImageLoader.get(context)
    val validUrls = urls.filter { it.isNotBlank() }
    if (validUrls.isEmpty()) return

    // enqueue (not execute) so the downloads keep going in the background after the timeout
    val jobs = validUrls.map { url -> imageLoader.enqueue(requestFor(context, url)).job }
    withTimeoutOrNull(maxWaitMs) {
        try {
            jobs.awaitAll()
        } catch (_: Exception) {}
    }
}

/**
 * Enqueues image requests asynchronously in background for continuous viewport prefetching.
 */
fun prefetchImageUrlsAsync(context: PlatformContext, urls: List<String>, requestFor: ImageRequestFactory) {
    val imageLoader = SingletonImageLoader.get(context)
    urls.filter { it.isNotBlank() }.forEach { url ->
        imageLoader.enqueue(requestFor(context, url))
    }
}

/**
 * Pre-fetches images for upcoming items in LazyColumn using snapshotFlow to eliminate recomposition overhead.
 */
@Composable
fun LazyColumnImagePrefetcher(
    listState: LazyListState,
    imageUrls: List<String>,
    requestFor: ImageRequestFactory,
    bufferAheadCount: Int = 4
) {
    val context = LocalPlatformContext.current
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
                        prefetchImageUrlsAsync(context, newUrlsToFetch, requestFor)
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
    requestFor: ImageRequestFactory,
    bufferAheadCount: Int = 4
) {
    val context = LocalPlatformContext.current
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
                        prefetchImageUrlsAsync(context, newUrlsToFetch, requestFor)
                    }
                }
            }
    }
}

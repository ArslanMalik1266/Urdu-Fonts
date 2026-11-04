package com.urdufonts.app.ui.util

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Size

/**
 * Single source of truth for card image requests.
 *
 * Coil's memory-cache key is built from the URL + transformations + size. The preloaders and the
 * cards must build byte-identical requests, otherwise a preloaded image is a memory-cache miss and
 * the card decodes it again and crossfades in. A fixed height (instead of the layout size) keeps the
 * key stable no matter where the request is built from.
 */
object CardImageRequests {
    private const val FONT_CARD_HEIGHT_DP = 36
    private const val STYLE_CARD_HEIGHT_DP = 36
    private const val CATEGORY_CARD_HEIGHT_DP = 44

    fun font(context: Context, url: String?): ImageRequest =
        build(context, url, FONT_CARD_HEIGHT_DP, trim = false)

    fun style(context: Context, url: String?): ImageRequest =
        build(context, url, STYLE_CARD_HEIGHT_DP, trim = true)

    fun category(context: Context, url: String?): ImageRequest =
        build(context, url, CATEGORY_CARD_HEIGHT_DP, trim = true)

    private fun build(context: Context, url: String?, heightDp: Int, trim: Boolean): ImageRequest {
        val heightPx = (heightDp * context.resources.displayMetrics.density).toInt()
        return ImageRequest.Builder(context)
            .data(url)
            .size(Size(Dimension.Undefined, Dimension(heightPx)))
            .apply { if (trim) transformations(TrimTransparentPaddingTransformation()) }
            // Coil skips the crossfade for memory-cache hits, so preloaded images appear instantly
            .crossfade(150)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}

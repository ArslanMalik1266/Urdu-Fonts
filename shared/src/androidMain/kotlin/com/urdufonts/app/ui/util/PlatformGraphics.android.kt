package com.urdufonts.app.ui.util

import androidx.compose.ui.graphics.toArgb
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.Paint
import coil3.PlatformContext
import coil3.size.Size
import coil3.transform.Transformation

actual fun Paint.setBlur(radiusPx: Float) {
    asFrameworkPaint().maskFilter = if (radiusPx > 0f) BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL) else null
}

actual fun Paint.setShadow(radiusPx: Float, dx: Float, dy: Float, color: androidx.compose.ui.graphics.Color) {
    asFrameworkPaint().setShadowLayer(radiusPx, dx, dy, color.toArgb())
}

actual fun PlatformContext.displayDensity(): Float = resources.displayMetrics.density

actual fun trimTransparentPaddingTransformation(): Transformation = TrimTransparentPaddingTransformation()

/**
 * Trims empty transparent padding from decoded SVG / PNG preview bitmaps, so all font preview images
 * render at the same height and proportions across cards.
 */
class TrimTransparentPaddingTransformation : Transformation() {
    override val cacheKey: String = TRIM_CACHE_KEY

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height
        if (width <= 0 || height <= 0) return input
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        val box = findTrimBox(width, height) { x, y -> (pixels[y * width + x] ushr 24) and 0xFF } ?: return input
        return try {
            Bitmap.createBitmap(input, box.x, box.y, box.width, box.height)
        } catch (e: Exception) {
            input
        }
    }
}

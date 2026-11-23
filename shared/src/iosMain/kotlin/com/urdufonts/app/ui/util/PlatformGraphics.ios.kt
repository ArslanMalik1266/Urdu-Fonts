package com.urdufonts.app.ui.util

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asSkiaPaint
import coil3.Bitmap
import coil3.PlatformContext
import coil3.size.Size
import coil3.transform.Transformation
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.IRect
import org.jetbrains.skia.MaskFilter
import platform.UIKit.UIScreen

actual fun Paint.setBlur(radiusPx: Float) {
    // Android's BlurMaskFilter takes a radius; Skia takes a sigma (radius ≈ sigma * √3, as Android converts it)
    asSkiaPaint().maskFilter = if (radiusPx > 0f) MaskFilter.makeBlur(FilterBlurMode.NORMAL, radiusPx * 0.57735f + 0.5f) else null
}

actual fun Paint.setShadow(radiusPx: Float, dx: Float, dy: Float, color: androidx.compose.ui.graphics.Color) {
    // Android's shadow radius → Skia sigma, as for setBlur
    val sigma = radiusPx * 0.57735f + 0.5f
    asSkiaPaint().imageFilter = org.jetbrains.skia.ImageFilter.makeDropShadow(dx, dy, sigma, sigma, color.toArgb())
}

actual fun PlatformContext.displayDensity(): Float = UIScreen.mainScreen.scale.toFloat()

actual fun trimTransparentPaddingTransformation(): Transformation = SkiaTrimTransparentPadding()

private class SkiaTrimTransparentPadding : Transformation() {
    override val cacheKey: String = TRIM_CACHE_KEY

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val box = findTrimBox(input.width, input.height) { x, y -> (input.getColor(x, y) ushr 24) and 0xFF } ?: return input
        val cropped = Bitmap()
        return if (input.extractSubset(cropped, IRect.makeXYWH(box.x, box.y, box.width, box.height))) cropped else input
    }
}

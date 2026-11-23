package com.urdufonts.app.ui.util

import androidx.compose.ui.graphics.Paint
import coil3.PlatformContext
import coil3.transform.Transformation

/**
 * Blurs everything drawn with this paint by [radiusPx] (a soft shadow or glow).
 * Compose has no common blur-paint API: Android uses BlurMaskFilter, iOS uses Skia's MaskFilter.
 */
expect fun Paint.setBlur(radiusPx: Float)

/** Draws a soft [color] shadow, blurred by [radiusPx] and offset by ([dx], [dy]), under whatever this paint fills. */
expect fun Paint.setShadow(radiusPx: Float, dx: Float, dy: Float, color: androidx.compose.ui.graphics.Color)

/** Pixels per dp on this screen (Android displayMetrics.density, iOS UIScreen scale). */
expect fun PlatformContext.displayDensity(): Float

/**
 * Coil transformation that crops fully transparent padding off a preview image, so every font/style
 * thumbnail renders at the same visual height. Reads pixels with each platform's own bitmap type.
 */
expect fun trimTransparentPaddingTransformation(): Transformation

/** Shared crop logic: the box around pixels with alpha above the threshold, plus 2 px of padding. */
internal data class TrimBox(val x: Int, val y: Int, val width: Int, val height: Int)

internal inline fun findTrimBox(width: Int, height: Int, alphaAt: (x: Int, y: Int) -> Int): TrimBox? {
    if (width <= 0 || height <= 0) return null
    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (alphaAt(x, y) > 12) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    if (maxX < minX || maxY < minY) return null
    val startX = (minX - 2).coerceAtLeast(0)
    val startY = (minY - 2).coerceAtLeast(0)
    val cropW = (maxX - minX + 1 + 4).coerceAtMost(width - startX)
    val cropH = (maxY - minY + 1 + 4).coerceAtMost(height - startY)
    return if (cropW <= 0 || cropH <= 0) null else TrimBox(startX, startY, cropW, cropH)
}

internal const val TRIM_CACHE_KEY = "TrimTransparentPaddingTransformation_v1"

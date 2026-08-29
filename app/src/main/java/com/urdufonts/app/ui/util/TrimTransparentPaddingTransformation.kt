package com.urdufonts.app.ui.util

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/**
 * Custom Coil Transformation that automatically Trims Empty Transparent Padding
 * from decoded API SVG / PNG preview bitmaps, ensuring all font preview images
 * render at 100% equal height and uniform proportions across cards!
 */
class TrimTransparentPaddingTransformation : Transformation {
    override val cacheKey: String = "TrimTransparentPaddingTransformation_v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height

        if (width <= 0 || height <= 0) return input

        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1

        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val alpha = (pixels[rowOffset + x] ushr 24) and 0xFF
                if (alpha > 12) { // Non-transparent pixel alpha threshold
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // If no non-transparent pixels found or valid bounds, return original input
        if (maxX < minX || maxY < minY) {
            return input
        }

        val trimmedWidth = maxX - minX + 1
        val trimmedHeight = maxY - minY + 1

        // Add 2px safety padding around text boundaries
        val startX = (minX - 2).coerceAtLeast(0)
        val startY = (minY - 2).coerceAtLeast(0)
        val cropW = (trimmedWidth + 4).coerceAtMost(width - startX)
        val cropH = (trimmedHeight + 4).coerceAtMost(height - startY)

        if (cropW <= 0 || cropH <= 0) return input

        return try {
            Bitmap.createBitmap(input, startX, startY, cropW, cropH)
        } catch (e: Exception) {
            input
        }
    }
}

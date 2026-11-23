package com.urdufonts.app.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** A paint that draws [color] blurred by [blurPx] (the platform blur comes from [setBlur]). */
private fun blurredPaint(color: Color, blurPx: Float): Paint = Paint().apply {
    isAntiAlias = true
    this.color = color
    setBlur(blurPx)
}

fun Modifier.softShadow(
    shadowColor: Color,
    borderRadius: Dp,
    blurValue: Dp,
    offsetY: Dp
): Modifier = this.drawWithCache {
    val radiusPx = borderRadius.toPx()
    val dy = offsetY.toPx()
    val blurPx = blurValue.toPx().coerceAtMost(24.dp.toPx())
    val width = size.width
    val height = size.height
    val paint = blurredPaint(shadowColor, blurPx)

    onDrawBehind {
        if (blurPx > 0f) {
            drawIntoCanvas { canvas ->
                canvas.drawRoundRect(0f, dy, width, height + dy, radiusPx, radiusPx, paint)
            }
        }
    }
}

/** A circle of [color] whose edge is blurred by [radius] px. */
fun Modifier.softBlur(color: Color, radius: Float): Modifier = this.drawWithCache {
    val paint = blurredPaint(color, radius)
    val center = Offset(size.width / 2f, size.height / 2f)
    val circleRadius = size.width / 2f
    onDrawBehind {
        drawIntoCanvas { canvas -> canvas.drawCircle(center, circleRadius, paint) }
    }
}

/**
 * Same look as [softBlur] (a circle whose edge is blurred by [radius] px) drawn as a radial gradient.
 * A real blur re-runs every frame behind every screen; a gradient is nearly free.
 */
fun Modifier.softGlow(color: Color, radius: Float): Modifier = this.drawWithCache {
    val circleRadius = size.width / 2f
    val outerRadius = circleRadius + radius
    val solidStop = ((circleRadius - radius) / outerRadius).coerceIn(0f, 0.99f)
    val glowCenter = Offset(size.width / 2f, size.height / 2f)
    val brush = Brush.radialGradient(
        0f to color,
        solidStop to color,
        (1f + solidStop) / 2f to color.copy(alpha = color.alpha * 0.5f),
        1f to Color.Transparent,
        center = glowCenter,
        radius = outerRadius
    )
    onDrawBehind {
        drawCircle(brush = brush, radius = outerRadius, center = glowCenter)
    }
}

// Same as [softShadow], but the blurred shadow is rendered once into a bitmap shared by all
// same-size cards, instead of re-running the blur filter for every card on every frame.
private data class BakedShadowKey(val width: Int, val height: Int, val color: Int, val radiusPx: Float, val blurPx: Float, val dy: Float)

/** The 8 most recently used shadows. Drawing happens on the UI thread, so no locking is needed. */
private val bakedShadowCache = LinkedHashMap<BakedShadowKey, ImageBitmap>()

private fun cachedShadow(key: BakedShadowKey, create: () -> ImageBitmap): ImageBitmap {
    bakedShadowCache.remove(key)?.let { bakedShadowCache[key] = it; return it }
    val created = create()
    bakedShadowCache[key] = created
    while (bakedShadowCache.size > 8) bakedShadowCache.remove(bakedShadowCache.keys.first())
    return created
}

fun Modifier.bakedSoftShadow(
    shadowColor: Color,
    borderRadius: Dp,
    blurValue: Dp,
    offsetY: Dp
): Modifier = this.drawWithCache {
    val width = size.width.toInt()
    val height = size.height.toInt()
    val radiusPx = borderRadius.toPx()
    val dy = offsetY.toPx()
    val blurPx = blurValue.toPx().coerceAtMost(24.dp.toPx())
    val margin = (blurPx * 2 + abs(dy)).toInt() + 2
    val bitmap = if (width <= 0 || height <= 0 || blurPx <= 0f) null else
        cachedShadow(BakedShadowKey(width, height, shadowColor.toArgb(), radiusPx, blurPx, dy)) {
            ImageBitmap(width + margin * 2, height + margin * 2).also { image ->
                Canvas(image).drawRoundRect(
                    margin.toFloat(), margin + dy, margin + width.toFloat(), margin + height + dy,
                    radiusPx, radiusPx, blurredPaint(shadowColor, blurPx)
                )
            }
        }
    onDrawBehind {
        if (bitmap != null) drawImage(bitmap, topLeft = Offset(-margin.toFloat(), -margin.toFloat()))
    }
}

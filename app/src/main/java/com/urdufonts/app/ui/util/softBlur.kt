package com.urdufonts.app.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

    val paint = Paint().apply {
        val fp = asFrameworkPaint()
        fp.isAntiAlias = true
        fp.color = shadowColor.toArgb()
        if (blurPx > 0f) {
            fp.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    onDrawBehind {
        if (blurPx > 0f) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    dy,
                    width,
                    height + dy,
                    radiusPx,
                    radiusPx,
                    paint.asFrameworkPaint()
                )
            }
        }
    }
}

fun Modifier.softBlur(color: Color, radius: Float): Modifier = this.drawWithCache {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.TRANSPARENT
        setShadowLayer(radius, 0f, 0f, color.toArgb())
    }
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val circleRadius = size.width / 2f

    onDrawBehind {
        drawContext.canvas.nativeCanvas.drawCircle(
            centerX,
            centerY,
            circleRadius,
            paint
        )
    }
}

/**
 * Same look as [softBlur] (a circle whose edge is blurred by [radius] px) drawn as a radial gradient.
 * setShadowLayer re-runs a large blur every frame behind every screen; a gradient is nearly free.
 */
fun Modifier.softGlow(color: Color, radius: Float): Modifier = this.drawWithCache {
    val circleRadius = size.width / 2f
    val outerRadius = circleRadius + radius
    val solidStop = ((circleRadius - radius) / outerRadius).coerceIn(0f, 0.99f)
    val glowCenter = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
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

private val bakedShadowCache = object : LinkedHashMap<BakedShadowKey, androidx.compose.ui.graphics.ImageBitmap>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<BakedShadowKey, androidx.compose.ui.graphics.ImageBitmap>?) = size > 8
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
    val margin = (blurPx * 2 + kotlin.math.abs(dy)).toInt() + 2
    val bitmap = if (width <= 0 || height <= 0 || blurPx <= 0f) null else synchronized(bakedShadowCache) {
        bakedShadowCache.getOrPut(BakedShadowKey(width, height, shadowColor.toArgb(), radiusPx, blurPx, dy)) {
            val bmp = android.graphics.Bitmap.createBitmap(width + margin * 2, height + margin * 2, android.graphics.Bitmap.Config.ARGB_8888)
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = shadowColor.toArgb()
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            android.graphics.Canvas(bmp).drawRoundRect(
                margin.toFloat(), margin + dy, margin + width.toFloat(), margin + height + dy, radiusPx, radiusPx, paint
            )
            bmp.asImageBitmap()
        }
    }
    onDrawBehind {
        if (bitmap != null) drawImage(bitmap, topLeft = androidx.compose.ui.geometry.Offset(-margin.toFloat(), -margin.toFloat()))
    }
}

package com.urdufonts.app.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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

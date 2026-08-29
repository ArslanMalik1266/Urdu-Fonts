package com.urdufonts.app.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// 1. Drop Shadow Modifier
fun Modifier.figmaDropShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    shape: RoundedCornerShape
): Modifier = this.drawWithCache {
    val cornerRadius = shape.topStart.toPx(size, this)
    val blurPx = blur.toPx().coerceAtMost(24.dp.toPx())
    val offX = offsetX.toPx()
    val offY = offsetY.toPx()
    val width = size.width
    val height = size.height

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blurPx > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blurPx,
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(offX, offY)
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                width,
                height,
                cornerRadius,
                cornerRadius,
                paint.asFrameworkPaint()
            )
            canvas.restore()
        }
    }
}

// 2. High-Performance Inner Shadow Modifier with Cached Path Masks
fun Modifier.figmaInnerShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    shape: RoundedCornerShape
): Modifier = this.drawWithCache {
    val cornerRadius = shape.topStart.toPx(size, this)
    val blurPx = blur.toPx().coerceAtMost(24.dp.toPx())
    val offX = offsetX.toPx()
    val offY = offsetY.toPx()

    // 1. Create outer boundary rect path (cached in drawWithCache)
    val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
    val innerPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = rect,
                topLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                topRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
        )
    }

    // 2. Create larger outer masking rect path (cached in drawWithCache)
    val strokeWidth = blurPx * 2 + maxOf(abs(offX), abs(offY))
    val outerRect = rect.inflate(strokeWidth)
    val outerPath = Path().apply {
        addRect(outerRect)
    }

    // 3. Subtract inner path from outer path to create the inverse mask (cached in drawWithCache)
    val maskPath = Path.combine(
        PathOperation.Difference,
        outerPath,
        innerPath
    )

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blurPx > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blurPx,
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    onDrawWithContent {
        drawContent() // Draw card content/background first

        // Clip drawing to card container & apply inner shadow offset
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.clipPath(innerPath)
            canvas.translate(offX, offY)
            canvas.drawPath(maskPath, paint)
            canvas.restore()
        }
    }
}

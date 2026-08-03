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
    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blur.toPx() > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blur.toPx(),
                BlurMaskFilter.Blur.NORMAL
            )
        }
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.save()
            // Translate the canvas to apply Figma's X and Y offsets
            canvas.translate(offsetX.toPx(), offsetY.toPx())
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadius,
                cornerRadius,
                paint.asFrameworkPaint()
            )
            canvas.restore()
        }
    }
}

// 2. Inner Shadow Modifier
fun Modifier.figmaInnerShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blur: Dp,
    shape: RoundedCornerShape
): Modifier = this.drawWithCache {
    val cornerRadius = shape.topStart.toPx(size, this)

    // 1. Create outer boundary rect path
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

    // 2. Create larger outer masking rect path
    val strokeWidth = blur.toPx() * 2 + maxOf(abs(offsetX.toPx()), abs(offsetY.toPx()))
    val outerRect = rect.inflate(strokeWidth)
    val outerPath = Path().apply {
        addRect(outerRect)
    }

    // 3. Subtract inner path from outer path to create the inverse mask
    val maskPath = Path.combine(
        PathOperation.Difference,
        outerPath,
        innerPath
    )

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        if (blur.toPx() > 0f) {
            frameworkPaint.maskFilter = BlurMaskFilter(
                blur.toPx(),
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
            canvas.translate(offsetX.toPx(), offsetY.toPx())
            canvas.drawPath(maskPath, paint)
            canvas.restore()
        }
    }
}

package com.webscare.urdufonts.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

fun Modifier.softShadow(
    shadowColor: Color,
    borderRadius: Dp,
    blurValue: Dp,
    offsetY: Dp
): Modifier = this.composed {
    // Cache the Paint object so it isn't recreated 60-120 times/second
    val paint = remember(shadowColor) {
        Paint().apply {
            val fp = asFrameworkPaint()
            fp.isAntiAlias = true
            fp.color = shadowColor.toArgb()
        }
    }

    drawBehind {
        drawIntoCanvas { canvas ->
            val blurPx = blurValue.toPx()
            if (blurPx > 0f) {
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.maskFilter = BlurMaskFilter(
                    blurPx,
                    BlurMaskFilter.Blur.NORMAL
                )

                val radiusPx = borderRadius.toPx()
                val dy = offsetY.toPx()

                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    0f + dy,
                    size.width,
                    size.height,
                    radiusPx,
                    radiusPx,
                    frameworkPaint
                )
            }
        }
    }
}

fun Modifier.softBlur(color: Color, radius: Float): Modifier = this.composed {
    // Cache the native Paint object
    val paint = remember(color, radius) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(radius, 0f, 0f, color.toArgb())
        }
    }

    drawBehind {
        drawContext.canvas.nativeCanvas.drawCircle(
            size.width / 2,
            size.height / 2,
            size.width / 2,
            paint
        )
    }
}

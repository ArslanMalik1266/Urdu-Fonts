package com.webscare.urdufonts.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
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
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val blurPx = blurValue.toPx()

        if (blurPx > 0f) {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.isAntiAlias = true
            frameworkPaint.color = shadowColor.toArgb()
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
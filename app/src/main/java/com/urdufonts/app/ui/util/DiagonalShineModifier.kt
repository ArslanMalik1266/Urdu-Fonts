package com.urdufonts.app.ui.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Applies an animated diagonal light-sweep shimmer (shine line) across the composable.
 */
@Composable
fun Modifier.diagonalShineEffect(
    durationMillis: Int = 2600,
    shineColor: Color = Color.White.copy(alpha = 0.45f)
): Modifier {
    val transition = rememberInfiniteTransition(label = "diagonal_shine_transition")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diagonal_shine_progress"
    )

    return this.drawWithContent {
        drawContent()

        val width = size.width
        val height = size.height
        val currentX = width * progress
        val bandWidth = width * 0.35f

        val start = Offset(x = currentX - bandWidth, y = 0f)
        val end = Offset(x = currentX + bandWidth, y = height)

        val brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color.Transparent,
                0.40f to Color.Transparent,
                0.47f to shineColor.copy(alpha = shineColor.alpha * 0.25f),
                0.50f to shineColor,
                0.53f to shineColor.copy(alpha = shineColor.alpha * 0.25f),
                0.60f to Color.Transparent,
                1.0f to Color.Transparent
            ),
            start = start,
            end = end
        )

        drawRect(brush = brush)
    }
}

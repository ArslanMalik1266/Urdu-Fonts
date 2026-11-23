package com.urdufonts.app.ui.support.kit

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import com.urdufonts.app.ui.support.kit.Dimens
import kotlin.math.floor

/**
 * The classic spinner: eight rounded bars round a circle that stay still while a bright spot
 * travels round them, each bar one step behind the next. Same shape and timing as the
 * designer's Lottie file (79 frames at 60 fps), drawn in code in our [color].
 */
@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = Dimens.SpinnerSize,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val spin = rememberInfiniteTransition(label = "spinner")
    val step by spin.animateFloat(
        initialValue = 0f,
        targetValue = BARS.toFloat(),
        animationSpec = infiniteRepeatable(tween(LOOP_MS, easing = LinearEasing)),
        label = "spinnerStep",
    )
    Canvas(modifier.size(size)) {
        val width = this.size.width * BAR_WIDTH
        val length = this.size.width * BAR_LENGTH
        val reach = this.size.width * BAR_REACH
        val key = floor(step).toInt()
        val within = step - key
        repeat(BARS) { i ->
            // The bar that was brightest fades a step each beat; the next one lights up fully.
            val from = brightness(i, key)
            val to = brightness(i, key + 1)
            val alpha = from + (to - from) * within
            rotate(degrees = i * (360f / BARS)) {
                drawRoundRect(
                    color = color.copy(alpha = color.alpha * alpha),
                    topLeft = Offset(center.x - width / 2, center.y - reach - length / 2),
                    size = Size(width, length),
                    cornerRadius = CornerRadius(width / 2),
                )
            }
        }
    }
}

/** Opacity of bar [bar] at beat [beat]: 70% for the newest, 10% less for each beat since, 0 last. */
private fun brightness(bar: Int, beat: Int): Float = ((bar - beat) % BARS + BARS) % BARS * STEP_ALPHA

private const val BARS = 8
private const val LOOP_MS = 1_320
private const val STEP_ALPHA = 0.1f
private const val BAR_WIDTH = 0.08f
private const val BAR_LENGTH = 0.24f
private const val BAR_REACH = 0.333f

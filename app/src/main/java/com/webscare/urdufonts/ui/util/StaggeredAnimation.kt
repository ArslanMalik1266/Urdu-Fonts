package com.webscare.urdufonts.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wraps any composable with a staggered fade-in + slide-up entrance animation.
 *
 * @param index       The item's position in the list (0, 1, 2, ...)
 * @param baseDelay   Delay before the very first item starts (ms)
 * @param staggerMs   Extra delay between each consecutive item (ms)
 * @param durationMs  Duration of each item's animation (ms)
 * @param slideUpDp   How many dp the item slides up from
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelay: Long = 100L,
    staggerMs: Long = 50L,
    durationMs: Int = 400,
    slideUpDp: Float = 40f,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(slideUpDp) }

    LaunchedEffect(Unit) {
        delay(baseDelay + index * staggerMs)
        // Run both in parallel
        launch {
            alpha.animateTo(
                1f,
                tween(durationMs, easing = FastOutSlowInEasing)
            )
        }
        offsetY.animateTo(
            0f,
            tween(durationMs, easing = FastOutSlowInEasing)
        )
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha.value }
            .offset(y = offsetY.value.dp)
    ) {
        content()
    }
}

package com.urdufonts.app.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
 * Entrance animation matching the ScreenEntranceController + RecyclerEntrance pattern.
 *
 * Works for BOTH:
 * - Screen chrome (top bar, search bar, headings) → use small orderIndex (0, 1, 2)
 * - List/grid items → use list index as orderIndex
 *
 * @param orderIndex  Position in the entrance sequence (0 = first to appear)
 * @param baseDelayMs Delay before the very first element starts
 * @param staggerMs   Delay between consecutive elements
 * @param durationMs  Duration of each element's fade+slide
 * @param slideUpDp   How far below the element starts before sliding up
 */
@Composable
fun EntranceAnimation(
    orderIndex: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Long = 60L,
    staggerMs: Long = 60L,
    durationMs: Int = 350,
    slideUpDp: Float = 30f,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(slideUpDp) }

    LaunchedEffect(Unit) {
        delay(baseDelayMs + orderIndex * staggerMs)
        launch {
            alpha.animateTo(1f, tween(durationMs))
        }
        offsetY.animateTo(0f, tween(durationMs))
    }

    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha.value }
            .offset(y = offsetY.value.dp)
    ) {
        content()
    }
}

package com.webscare.urdufonts.ui.util

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StaggeredFadeIn(
    index: Int,
    isSeen: Boolean,
    onSeen: () -> Unit,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay((index * 40L).coerceAtMost(300L).milliseconds)
        onSeen()
    }
    val alpha by animateFloatAsState(
        targetValue = if (isSeen) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOut),
        label = "staggered_fade_in_alpha"
    )

    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
        content()
    }
}
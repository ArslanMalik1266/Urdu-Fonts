package com.webscare.urdufonts.ui.util

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StaggeredFadeIn(
    index: Int,
    seenItems: MutableSet<Int>, // Pass a plain, non-state HashSet
    content: @Composable () -> Unit,
) {
    val alreadySeen = remember(index) { seenItems.contains(index) }
    var visible by remember { mutableStateOf(alreadySeen) }

    LaunchedEffect(index) {
        if (!alreadySeen) {
            delay((index * 40L).coerceAtMost(300L).milliseconds)
            visible = true
            seenItems.add(index) // Plain set update — doesn't trigger parent recompositions!
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOut),
        label = "staggered_fade_in_alpha"
    )

    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
        content()
    }
}

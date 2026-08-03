package com.urdufonts.app.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ek baar animate karta hai — scroll back pe re-animate NAHI karta
 *
 * @param index          → item ka position
 * @param animatedSet    → screen level pe shared set — track karta hai kaun animate ho gaya
 * @param staggerMs      → har item ke beech delay (default 55ms)
 * @param content        → actual item composable
 */
@Composable
fun AnimatedEntranceItem(
    index: Int,
    animatedSet: MutableState<Set<Int>>,
    staggerMs: Long = 55L,
    content: @Composable () -> Unit
) {
    // Pehle check: kya yeh item pehle animate ho chuka hai?
    val alreadyAnimated = index in animatedSet.value

    // Animatable — smooth animation ke liye
    val alpha = remember(index) {
        Animatable(if (alreadyAnimated) 1f else 0f)
    }
    val translationY = remember(index) {
        Animatable(if (alreadyAnimated) 0f else 40f)
    }

    // Sirf tab animate karo jab pehli baar aa raha ho
    if (!alreadyAnimated) {
        LaunchedEffect(index) {
            // Stagger delay — max 280ms tak (baad wale items zyada wait na kare)
            delay(minOf(index * staggerMs, 280L))

            // Alpha aur translationY ek saath animate karo
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 280)
                )
            }
            launch {
                translationY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            // ✅ Mark as done — scroll back pe dobara animate NAHI hoga
            animatedSet.value = animatedSet.value + index
        }
    }

    // graphicsLayer — item apni jagah leta hai (blank space nahi)
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translationY.value
        }
    ) {
        content()
    }
}

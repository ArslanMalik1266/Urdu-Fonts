package com.urdufonts.app.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.addPressEffect(
    enabled: Boolean = true,
    onClick: () -> Unit = {}
): Modifier = composed {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestEnabled by rememberUpdatedState(enabled)
    val scale = remember { Animatable(1f) }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    if (!latestEnabled) return@detectTapGestures
                    coroutineScope {
                        val animJob = launch {
                            scale.animateTo(0.9f, tween(80))
                        }
                        try {
                            val released = tryAwaitRelease()
                            if (released) {
                                animJob.join() // Guarantee full visual press dip
                                scale.animateTo(1f, tween(120)) // Animate back up
                                kotlinx.coroutines.delay(50) // Let layout settle for 1-2 frames
                                latestOnClick() // Fire click after animation finishes
                            } else {
                                animJob.cancel()
                                scale.animateTo(1f, tween(120))
                            }
                        } catch (e: Exception) {
                            animJob.cancel()
                            scale.animateTo(1f, tween(120))
                        }
                    }
                }
            )
        }
}


fun Modifier.addPressEffectWithLongClick(
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(if (isPressed) 80 else 120),
        label = "scale"
    )

    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick() },
                onLongPress = {
                    onLongClick()
                }
            )
        }
}
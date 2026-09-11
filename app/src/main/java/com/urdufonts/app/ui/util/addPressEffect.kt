package com.urdufonts.app.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

fun Modifier.addPressEffect(onClick: () -> Unit = {}): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)

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
                    val released = tryAwaitRelease()
                    isPressed = false
                    if (released) currentOnClick()
                }
            )
        }
}

fun Modifier.addPressEffectWithLongClick(
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

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
                onTap = { currentOnClick() },
                onLongPress = {
                    currentOnLongClick()
                }
            )
        }
}
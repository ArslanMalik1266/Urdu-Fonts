package com.urdufonts.app.ui.support.kit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PRESSED_SCALE = 0.95f
private const val PRESS_DOWN_MS = 80
private const val PRESS_UP_MS = 120

/** A short pause after the spring back, so the user sees the whole press before the click acts. */
private const val SETTLE_MS = 50L

/**
 * Every clickable in the app uses this instead of `clickable`: it shrinks to 95% while pressed, the same
 * effect as our UnitConverter app. Even a quick tap plays the full cycle (down, back up, a short settle)
 * before the click fires, so the press is always seen. Put it BEFORE clip and background so the whole
 * visible shape scales, e.g. `Modifier.addPressEffect(onClick).clip(shape).background(color)`. [role] keeps
 * switches, tabs and radio buttons announced correctly to screen readers.
 */
fun Modifier.addPressEffect(
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    role: Role = Role.Button,
    /**
     * False for controls that must react the instant the finger lifts (the tab bar): the click fires on
     * release and the shrink finishes on its own, instead of the click waiting for the full cycle.
     */
    waitForPress: Boolean = true,
): Modifier = composed {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestEnabled by rememberUpdatedState(enabled)
    val scale = remember { Animatable(1f) }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .semantics {
            this.role = role
            if (enabled) onClick { latestOnClick(); true } else disabled()
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    if (!latestEnabled) return@detectTapGestures
                    coroutineScope {
                        val down = launch { scale.animateTo(PRESSED_SCALE, tween(PRESS_DOWN_MS)) }
                        try {
                            if (tryAwaitRelease()) {
                                if (!waitForPress) latestOnClick()
                                down.join() // the full dip, even on a quick tap
                                scale.animateTo(1f, tween(PRESS_UP_MS))
                                if (waitForPress) {
                                    delay(SETTLE_MS)
                                    latestOnClick()
                                }
                            } else {
                                down.cancel()
                                scale.animateTo(1f, tween(PRESS_UP_MS))
                            }
                        } catch (e: Exception) {
                            down.cancel()
                            scale.animateTo(1f, tween(PRESS_UP_MS))
                            throw e
                        }
                    }
                },
            )
        }
}

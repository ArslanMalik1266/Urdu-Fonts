package com.urdufonts.app.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch

/**
 * iOS-style spring overscroll for LazyColumn / LazyVerticalGrid.
 *
 * When the user drags past the top or bottom edge, the list
 * translates with dampening (rubber-band). On release it
 * springs back. On fling-to-edge it bounces with velocity.
 */
@Composable
fun Modifier.springOverscroll(): Modifier {
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val connection = remember {
        object : NestedScrollConnection {

            // ── Drag back from overscroll → consume scroll to reduce offset ──
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (offset.value == 0f) return Offset.Zero

                // Only consume if scrolling back toward zero
                val goingBack =
                    (offset.value > 0 && delta < 0) || (offset.value < 0 && delta > 0)
                if (!goingBack) return Offset.Zero

                val newValue = offset.value + delta
                val crossesZero =
                    (offset.value > 0 && newValue < 0) || (offset.value < 0 && newValue > 0)

                return if (crossesZero) {
                    val consumed = -offset.value
                    scope.launch { offset.snapTo(0f) }
                    Offset(0f, consumed)
                } else {
                    scope.launch { offset.snapTo(newValue) }
                    Offset(0f, delta)
                }
            }

            // ── Overscroll: list couldn't consume → rubber-band translate ──
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    scope.launch {
                        offset.snapTo(offset.value + available.y * 0.5f)
                    }
                    return available
                }
                return Offset.Zero
            }

            // ── Finger lifted while overscrolled → spring back ──
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offset.value != 0f) {
                    offset.animateTo(
                        0f,
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    return available
                }
                return Velocity.Zero
            }

            // ── Fling hit the edge → bounce with velocity ──
            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                if (available.y != 0f) {
                    offset.animateTo(
                        0f,
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialVelocity = available.y * 0.4f
                    )
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    return this
        .nestedScroll(connection)
        .graphicsLayer { translationY = offset.value }
}

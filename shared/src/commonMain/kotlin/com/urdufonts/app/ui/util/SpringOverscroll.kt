package com.urdufonts.app.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs

/** The furthest the content can be pulled past an edge. */
private const val MAX_OFFSET_DP = 160f

/** Share of a drag past the edge that moves the content (the rest is the rubber band's pull). */
private const val RESISTANCE = 0.5f

/** Share of a fling's leftover speed at the edge that the bounce starts with. */
private const val FLING_TO_SPRING = 0.4f

/** Quick, non-bouncy return of a bounce still playing when a new touch starts. */
private const val SETTLE_MS = 120

/** Bouncy and soft, MEDIUM_BOUNCY + STIFFNESS_LOW, the same spring as the Graphic Equalizer app. */
private val SpringBack = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

/**
 * Spring overscroll for one scroll container. Put it on the container, outside its scroll modifier
 * (`Modifier.springOverscroll().verticalScroll(...)`, or in a lazy list's `modifier`). Use
 * [horizontal] = true for rows. It works through nested scroll, so it also bounces content that fits on
 * the screen, and it never touches a normal fling in the middle of a list:
 *  - A drag the list cannot use (past an edge) pulls the content like a rubber band: half the drag at
 *    first, harder the further out, at most 160 dp.
 *  - A drag back gives a held pull back 1:1 before the list scrolls again.
 *  - Letting go while pulled springs back with the bouncy spring.
 *  - A fling that reaches an edge bounces with 40 % of the speed left over at that edge.
 *  - A new touch never catches or cancels anything because of a bounce still playing: the bounce settles
 *    to 0 quickly and the touch scrolls and flings normally.
 * The platform's own overscroll is switched off in UrduFontsTheme, so this is the only edge effect.
 */
@Composable
fun Modifier.springOverscroll(horizontal: Boolean = false, pull: Animatable<Float, AnimationVector1D>? = null): Modifier {
    val maxOffset = MAX_OFFSET_DP * LocalDensity.current.density
    // [pull] lets a screen read how far its content is pulled
    val own = remember { Animatable(0f) }
    val offset = pull ?: own
    val scope = rememberCoroutineScope()
    val connection = remember(horizontal, maxOffset) {
        object : NestedScrollConnection {
            /** The finger is holding a pull made in this drag (a bounce playing is never "held"). */
            private var held = false

            private fun Offset.along(): Float = if (horizontal) x else y

            private fun of(value: Float): Offset = if (horizontal) Offset(value, 0f) else Offset(0f, value)

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val current = offset.value
                if (!held) {
                    // A new drag while a bounce is still playing: let the bounce settle, scroll normally.
                    if (current != 0f || offset.isRunning) {
                        scope.launch { offset.animateTo(0f, tween(SETTLE_MS, easing = FastOutSlowInEasing)) }
                    }
                    return Offset.Zero
                }
                // A held pull is given back 1:1 by a drag the other way, stopping exactly at 0.
                val delta = available.along()
                val goingBack = (current > 0f && delta < 0f) || (current < 0f && delta > 0f)
                if (!goingBack) return Offset.Zero
                val next = current + delta
                val crossesZero = (current > 0f && next < 0f) || (current < 0f && next > 0f)
                return if (crossesZero) {
                    held = false
                    scope.launch { offset.snapTo(0f) }
                    of(-current)
                } else {
                    scope.launch { offset.snapTo(next) }
                    of(delta)
                }
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = available.along()
                if (source != NestedScrollSource.UserInput || delta == 0f) return Offset.Zero
                // Rubber band: the further out it already is, the harder it pulls.
                val current = offset.value
                val damping = (1f - abs(current) / (maxOffset * 1.5f)).coerceIn(0.2f, 1f)
                val next = (current + delta * RESISTANCE * damping).coerceIn(-maxOffset, maxOffset)
                held = true
                scope.launch { offset.snapTo(next) }
                return available
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val wasHeld = held
                held = false
                // Only a pull still held stops the fling; a bounce playing out never does.
                if (!wasHeld || offset.value == 0f) return Velocity.Zero
                offset.animateTo(0f, SpringBack)
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // [available] is the speed left after the list hit its start or end.
                val left = if (horizontal) available.x else available.y
                if (left == 0f) return Velocity.Zero
                offset.animateTo(0f, SpringBack, initialVelocity = left * FLING_TO_SPRING)
                return available
            }
        }
    }
    return this
        .nestedScroll(connection)
        .graphicsLayer { if (horizontal) translationX = offset.value else translationY = offset.value }
}

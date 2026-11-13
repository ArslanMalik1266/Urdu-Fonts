package com.urdufonts.app.ui.support.kit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.urdufonts.app.ui.support.kit.rememberLoopClock
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.res.stringResource

/** The little scene an [EmptyState] shows above its text, each with its own slow motion. */
enum class EmptyArt {
    /** The icon floats on a breathing halo: the default. */
    Float,
    /** A speaker dozing, with z's drifting up: no music yet. */
    Sleeping,
    /** A heart that beats and lets small hearts rise: no favorites. */
    Heart,
    /** Two cards fanning softly behind the icon: no playlists. */
    Stack,
    /** The magnifier circles slowly, a sparkle blinking: nothing found. */
    Search,
    /** Equalizer bars resting low and breathing: an empty queue. */
    Bars,
    /** The icon gives a small shake now and then: something went wrong. */
    Alert,
}

/**
 * The animated picture of an empty or error screen: a Hugeicons [icon] in [tint] on a soft halo,
 * moved by [art]. Everything moves slowly and quietly; nothing loops faster than once a second.
 */
@Composable
fun EmptyIllustration(icon: Int, art: EmptyArt, tint: Color, modifier: Modifier = Modifier, scale: Float = 1f) {
    val clock = rememberLoopClock(running = true)
    val size = Dimens.EmptyIllustration * scale
    Box(
        modifier = modifier
            .size(size * SCENE_SCALE)
            .drawBehind { halo(tint, clock) },
        contentAlignment = Alignment.Center,
    ) {
        when (art) {
            EmptyArt.Stack -> Box(Modifier.size(size).drawBehind { fanningCards(tint, clock) })
            EmptyArt.Bars -> Box(Modifier.size(size).drawBehind { restingBars(tint, clock) })
            EmptyArt.Sleeping -> SleepLetters(clock, tint)
            EmptyArt.Heart -> RisingHearts(icon, clock, tint)
            else -> Unit
        }
        if (art != EmptyArt.Bars) {
            AppIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { iconMotion(art, clock.value) },
                tint = tint,
                size = size * ICON_PART,
            )
        }
        if (art == EmptyArt.Search) Box(Modifier.size(size).drawBehind { sparkle(tint, clock) })
    }
}

/** How the icon itself moves for each scene. */
private fun androidx.compose.ui.graphics.GraphicsLayerScope.iconMotion(art: EmptyArt, t: Float) {
    when (art) {
        EmptyArt.Float, EmptyArt.Stack -> translationY = sin(t * FLOAT_SPEED) * Dimens.Space4.toPx()
        EmptyArt.Sleeping -> {
            // Slow breathing, leaning a little to one side as if asleep.
            val breath = 1f + 0.03f * sin(t * 1.4f)
            scaleX = breath
            scaleY = breath
            rotationZ = -8f
        }
        EmptyArt.Heart -> {
            // Two quick beats, then a rest, like a real heartbeat.
            val phase = (t % HEART_PERIOD) / HEART_PERIOD
            val beat = heartbeat(phase)
            scaleX = 1f + 0.1f * beat
            scaleY = 1f + 0.1f * beat
            translationY = sin(t * FLOAT_SPEED) * Dimens.Space4.toPx()
        }
        EmptyArt.Search -> {
            val a = t * SEARCH_SPEED
            translationX = cos(a) * Dimens.Space8.toPx()
            translationY = sin(a) * Dimens.Space4.toPx()
            rotationZ = sin(a) * 6f
        }
        EmptyArt.Alert -> {
            // A short wobble every few seconds, still the rest of the time.
            val phase = t % ALERT_PERIOD
            rotationZ = if (phase < ALERT_SHAKE) sin(phase / ALERT_SHAKE * PI.toFloat() * 4f) * 9f * (1f - phase / ALERT_SHAKE) else 0f
        }
        EmptyArt.Bars -> Unit
    }
}

private fun heartbeat(phase: Float): Float {
    fun bump(centre: Float, width: Float) = (1f - kotlin.math.abs(phase - centre) / width).coerceAtLeast(0f)
    return maxOf(bump(0.1f, 0.08f), bump(0.3f, 0.08f) * 0.7f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.halo(tint: Color, clock: State<Float>) {
    val breath = 1f + 0.06f * sin(clock.value * 1.1f)
    val radius = size.minDimension / 2 * breath
    drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = HALO_ALPHA), Color.Transparent), center, radius), radius, center)
    drawCircle(tint.copy(alpha = DISC_ALPHA), size.minDimension * 0.3f * breath, center)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.fanningCards(tint: Color, clock: State<Float>) {
    val swing = sin(clock.value * 1.2f)
    val w = size.width * 0.62f
    val h = size.height * 0.7f
    listOf(-1f, 1f).forEach { side ->
        rotate(side * (10f + 4f * swing), pivot = Offset(center.x, size.height * 0.9f)) {
            drawRoundRect(
                tint.copy(alpha = CARD_ALPHA),
                topLeft = Offset(center.x - w / 2, center.y - h / 2),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.16f),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.restingBars(tint: Color, clock: State<Float>) {
    val bars = 5
    val bar = size.width * 0.09f
    val gap = size.width * 0.07f
    val total = bars * bar + (bars - 1) * gap
    val bottom = size.height * 0.72f
    repeat(bars) { i ->
        val level = 0.18f + 0.1f * (0.5f + 0.5f * sin(clock.value * 1.3f + i * 0.9f)) + REST_SHAPE[i]
        val x = center.x - total / 2 + i * (bar + gap) + bar / 2
        drawLine(tint, Offset(x, bottom), Offset(x, bottom - size.height * level), bar, StrokeCap.Round)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.sparkle(tint: Color, clock: State<Float>) {
    val blink = (0.5f + 0.5f * sin(clock.value * 2.2f)).let { it * it }
    val c = Offset(size.width * 0.82f, size.height * 0.2f)
    val r = size.width * 0.07f * (0.6f + 0.4f * blink)
    drawLine(tint.copy(alpha = blink), Offset(c.x - r, c.y), Offset(c.x + r, c.y), size.width * 0.025f, StrokeCap.Round)
    drawLine(tint.copy(alpha = blink), Offset(c.x, c.y - r), Offset(c.x, c.y + r), size.width * 0.025f, StrokeCap.Round)
}

/** Three z's rising from the speaker, each growing and fading as it goes. */
@Composable
private fun SleepLetters(clock: State<Float>, tint: Color) {
    val letter = stringResource(R.string.empty_sleep_letter)
    repeat(3) { i ->
        Text(
            text = letter,
            modifier = Modifier.graphicsLayer {
                val p = ((clock.value / SLEEP_PERIOD) + i / 3f) % 1f
                translationX = (Dimens.Space24 + Dimens.Space8 * p).toPx()
                translationY = -(Dimens.Space16 + Dimens.Space32 * p).toPx()
                val s = 0.7f + 0.6f * p
                scaleX = s
                scaleY = s
                alpha = sin(p * PI.toFloat())
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = tint,
        )
    }
}

/** Small hearts that float up from the big one and fade. */
@Composable
private fun RisingHearts(icon: Int, clock: State<Float>, tint: Color) {
    repeat(3) { i ->
        AppIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                val p = ((clock.value / HEARTS_PERIOD) + i / 3f) % 1f
                translationX = (SIDE[i] * Dimens.Space32.toPx()) + sin(p * 6f + i) * Dimens.Space4.toPx()
                translationY = -(Dimens.Space8 + Dimens.Space32 * 1.4f * p).toPx()
                alpha = sin(p * PI.toFloat()) * 0.8f
                val s = 0.5f + 0.3f * p
                scaleX = s
                scaleY = s
            },
            tint = tint,
            size = Dimens.IconSmall + Dimens.Space4,
        )
    }
}

private val SIDE = floatArrayOf(-0.9f, 0.2f, 1f)
private val REST_SHAPE = floatArrayOf(0.02f, 0.12f, 0.06f, 0.16f, 0.04f)
private const val SCENE_SCALE = 1.5f
private const val ICON_PART = 0.55f
private const val HALO_ALPHA = 0.16f
private const val DISC_ALPHA = 0.1f
private const val CARD_ALPHA = 0.14f
private const val FLOAT_SPEED = 1.6f
private const val HEART_PERIOD = 1.6f
private const val SEARCH_SPEED = 1.1f
private const val ALERT_PERIOD = 3.5f
private const val ALERT_SHAKE = 0.6f
private const val SLEEP_PERIOD = 3.2f
private const val HEARTS_PERIOD = 2.8f

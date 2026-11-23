package com.urdufonts.app.ui.support.kit

import org.jetbrains.compose.resources.DrawableResource
import com.urdufonts.app.resources.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.ui.support.kit.PillShape

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: DrawableResource? = null,
    /** Tighter sides and smaller text, for a button in a narrow column. */
    compact: Boolean = false,
    /** An action under way (sending, saving): a small spinner in place of the text, and taps are ignored. */
    loading: Boolean = false,
    leadingIcon: DrawableResource? = null,
    /** The fill; red ([ColorScheme.error]) for an action that removes something, like Log Out. */
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val shape = PillShape
    Row(
        modifier = modifier
            .addPressEffect(onClick = onClick, enabled = enabled && !loading, role = Role.Button)
            .height(if (compact) Dimens.ButtonHeightCompact else Dimens.ButtonHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = if (compact) Dimens.Space12 else Dimens.Space24),
        horizontalArrangement = Arrangement.spacedBy(if (compact) Dimens.Space4 else Dimens.Space8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            LoadingSpinner(size = Dimens.SupportLoader, color = contentColor)
            return@Row
        }
        if (leadingIcon != null) {
            AppIcon(icon = leadingIcon, contentDescription = null, tint = contentColor, size = Dimens.ButtonChevron)
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f, fill = false),
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingIcon != null) {
            AppIcon(icon = trailingIcon, contentDescription = null, tint = contentColor, size = Dimens.ButtonChevron)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .addPressEffect(onClick = onClick, enabled = enabled, role = Role.Button)
            .height(Dimens.ButtonHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(PillShape)
            .border(BorderStroke(Dimens.Hairline, MaterialTheme.colorScheme.outlineVariant), PillShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Dimens.Space24),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun AppIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .addPressEffect(onClick = onClick, role = Role.Button)
            .size(Dimens.IconButtonSize)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon = icon, contentDescription = contentDescription, tint = tint)
    }
}

/** A white rounded-square button with a soft shadow, for the top bar of Home (search, premium). */
@Composable
fun SquareIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .addPressEffect(onClick = onClick, role = Role.Button)
            .size(Dimens.SquareButton)
            .shadow(Dimens.CardElevation, MaterialTheme.shapes.medium, ambientColor = MaterialTheme.colorScheme.outline, spotColor = MaterialTheme.colorScheme.outline)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSurface, size = Dimens.IconSmall + Dimens.Space4)
    }
}

/**
 * A round white button that beats like a heart: two accent rings ripple out one after the other
 * ("lub-dub"), the button gives a small push on each beat, then a calm pause before the next. Used by
 * the Premium crown on Home, with the 3D [CrownArt] inside.
 */
@Composable
fun PulseIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val beat = rememberInfiniteTransition(label = "heartbeat")
    val clock by beat.animateFloat(
        initialValue = 0f,
        targetValue = HEARTBEAT_CYCLE_MS.toFloat(),
        animationSpec = infiniteRepeatable(tween(HEARTBEAT_CYCLE_MS, easing = LinearEasing)),
        label = "heartbeatClock",
    )
    val glow = MaterialTheme.colorScheme.primary
    // Each beat swells the button a touch and lets it settle back.
    val push = BEAT_STARTS_MS.maxOf { start -> beatPush((clock - start) / BEAT_PUSH_MS) }
    Box(
        modifier = modifier
            .size(Dimens.SquareButton)
            .drawBehind {
                val base = size.minDimension / 2
                BEAT_STARTS_MS.forEachIndexed { index, start ->
                    val t = (clock - start) / RING_MS
                    if (t !in 0f..1f) return@forEachIndexed
                    val spread = 1f - (1f - t) * (1f - t) * (1f - t)
                    val fade = (1f - t) * (1f - t)
                    val strength = if (index == 0) 1f else SECOND_BEAT_STRENGTH
                    val radius = base * (1f + RING_GROWTH * spread)
                    drawCircle(glow.copy(alpha = RING_FILL_ALPHA * fade * strength), radius)
                    drawCircle(glow.copy(alpha = RING_EDGE_ALPHA * fade * strength), radius, style = Stroke(width = base * RING_EDGE_WIDTH))
                }
            }
            .addPressEffect(onClick = onClick, role = Role.Button)
            .graphicsLayer {
                scaleX = 1f + BEAT_SCALE * push
                scaleY = 1f + BEAT_SCALE * push
            }
            .shadow(Dimens.Space4, CircleShape, ambientColor = glow, spotColor = glow)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** A quick rise and a softer fall, 0 outside the push. */
private fun beatPush(t: Float): Float = when {
    t < 0f || t > 1f -> 0f
    t < BEAT_RISE -> t / BEAT_RISE
    else -> 1f - (t - BEAT_RISE) / (1f - BEAT_RISE)
}

/** The heartbeat: two beats [BEAT_STARTS_MS] into each cycle, each ring spreading for [RING_MS]. */
private const val HEARTBEAT_CYCLE_MS = 3800
private val BEAT_STARTS_MS = listOf(0f, 320f)
private const val RING_MS = 1900f
private const val BEAT_PUSH_MS = 280f
private const val BEAT_RISE = 0.35f
private const val BEAT_SCALE = 0.06f
/** How far a ring spreads (part of the radius), how strong its fill and edge start, and the edge width. */
private const val RING_GROWTH = 0.7f
private const val RING_FILL_ALPHA = 0.14f
private const val RING_EDGE_ALPHA = 0.28f
private const val RING_EDGE_WIDTH = 0.05f
private const val SECOND_BEAT_STRENGTH = 0.75f

@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** A small chevron after the text, for links that open another screen ("See All ›"). */
    chevron: Boolean = false,
    /** The accent by default; a quieter colour for a secondary link. */
    color: Color = MaterialTheme.colorScheme.primary,
    /** Off: shown dimmed and ignores taps (nothing to do yet). */
    enabled: Boolean = true,
) {
    val alpha by animateFloatAsState(if (enabled) 1f else DISABLED_ALPHA, tween(DISABLED_FADE_MS))
    Box(
        modifier = modifier
            .addPressEffect(onClick = onClick, enabled = enabled, role = Role.Button)
            .graphicsLayer { this.alpha = alpha }
            .clip(PillShape)
            .padding(horizontal = Dimens.Space12, vertical = Dimens.Space8),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
            if (chevron) AppIcon(Res.drawable.ic_sp_chevron_right, contentDescription = null, tint = color, size = Dimens.IconSmall)
        }
    }
}

/** A disabled text link: dimmed like a disabled button, fading in 200 ms. */
private const val DISABLED_ALPHA = 0.4f
private const val DISABLED_FADE_MS = 200

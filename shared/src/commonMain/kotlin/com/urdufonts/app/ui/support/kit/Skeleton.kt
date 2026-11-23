package com.urdufonts.app.ui.support.kit

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalWindowInfo
import com.urdufonts.app.ui.support.kit.Dimens

/**
 * Where the shimmer's light band is now, 0..1 across the screen. One clock per loading screen
 * ([ShimmerHost]), so every placeholder on it is swept by the same band, left to right, in step.
 */
private val LocalShimmer = compositionLocalOf<State<Float>?> { null }

/** Runs one shimmer for everything inside it. Each loading layout wraps its placeholders in one. */
@Composable
fun ShimmerHost(content: @Composable () -> Unit) {
    val sweep = rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_MS, delayMillis = SHIMMER_PAUSE_MS, easing = LinearEasing)),
        label = "shimmerSweep",
    )
    CompositionLocalProvider(LocalShimmer provides sweep, content = content)
}

/**
 * A placeholder block shown while data loads, with a soft light band sweeping across it. Loads
 * show skeletons, never a bare spinner. Inside a [ShimmerHost] it shares that sweep; on its own it
 * runs its own.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    val shared = LocalShimmer.current
    if (shared == null) {
        ShimmerHost { SkeletonBox(modifier, shape) }
        return
    }
    val base = MaterialTheme.colorScheme.surfaceVariant
    val light = MaterialTheme.colorScheme.surface
    val screenWidth = LocalWindowInfo.current.containerSize.width.toFloat()
    val left = remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .onGloballyPositioned { left.floatValue = it.positionInRoot().x }
            .clip(shape)
            .drawBehind {
                drawRect(base)
                // The band travels from off the left edge of the screen to off the right one, measured
                // in screen space so neighbouring blocks light up as one sweep.
                val band = screenWidth * BAND_WIDTH
                val centre = -band + (screenWidth + band * 2) * shared.value - left.floatValue
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to light.copy(alpha = BAND_ALPHA),
                        1f to Color.Transparent,
                        startX = centre - band,
                        endX = centre + band,
                    ),
                )
            },
    )
}

/** A song row while loading: artwork tile, a title and an artist line, and a small round button. */
@Composable
fun SkeletonSongRow(modifier: Modifier = Modifier, card: Boolean = false, titleWidth: Float = 0.62f) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (card) {
                    Modifier.padding(horizontal = Dimens.ScreenMargin).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface)
                } else {
                    Modifier
                },
            )
            .padding(start = if (card) Dimens.Space12 else Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space8, bottom = Dimens.Space8),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(Dimens.ListArtwork), CircleShape)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            SkeletonBox(Modifier.fillMaxWidth(titleWidth).height(Dimens.SkeletonTitle), CircleShape)
            SkeletonBox(Modifier.fillMaxWidth(titleWidth * ARTIST_PART).height(Dimens.SkeletonLine), CircleShape)
        }
        SkeletonBox(Modifier.size(Dimens.SkeletonButton), CircleShape)
    }
}

/**
 * A list of [rows] loading song rows under one shimmer. Title lengths vary a little from row to row,
 * as real titles do, so it reads as a list and not a grid.
 */
@Composable
fun SkeletonSongList(rows: Int, modifier: Modifier = Modifier, card: Boolean = false) {
    ShimmerHost {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(if (card) Dimens.Space8 else Dimens.Space4)) {
            repeat(rows) { SkeletonSongRow(card = card, titleWidth = TITLE_WIDTHS[it % TITLE_WIDTHS.size]) }
        }
    }
}

private val TITLE_WIDTHS = floatArrayOf(0.66f, 0.5f, 0.74f, 0.58f, 0.7f, 0.46f)
private const val ARTIST_PART = 0.6f
private const val SHIMMER_MS = 1_100
private const val SHIMMER_PAUSE_MS = 250
private const val BAND_WIDTH = 0.35f
private const val BAND_ALPHA = 0.85f

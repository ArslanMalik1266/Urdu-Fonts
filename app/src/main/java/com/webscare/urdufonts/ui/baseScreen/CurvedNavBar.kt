package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.webscare.urdufonts.ui.theme.AppColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class NavBarItem(
    val icon: Int,
    val label: String,
)

/**
 * Why this file differs structurally from a typical "measure-after-the-fact" bottom nav:
 *
 * Item centers used to be discovered via onGloballyPositioned() — compose, let layout
 * happen, read back pixel coordinates into mutable state, then gate the indicator and
 * the animation LaunchedEffect behind a "have all positions arrived yet" flag
 * (positionsReady / indicatorReady / barJustReappeared). That's a feedback loop: layout
 * → state write → recomposition → layout again, and every consumer of "where is item X"
 * has to branch on "not known yet."
 *
 * Since every item occupies an equal slot (Arrangement.SpaceAround over fillMaxWidth,
 * same as before), slot centers are a pure function of the row's width and item count.
 * A single Modifier.layout {} on the outer Box captures that width synchronously during
 * the normal measure pass — no extra recomposition, no readiness flag, because the value
 * isn't "discovered" after the fact, it's computed from a constraint we already have.
 *
 * Everything that actually needs per-frame updates (the indicator's screen position,
 * the bar's Canvas redraw, icon dip/fade) stays exactly as before: plain
 * Modifier.offset { } / Canvas draw lambdas reading cradleCenterX.value, which Compose
 * already skips straight to re-layout/re-draw for, without going through subcomposition.
 */
@Composable
fun CurvedNavBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFFFFFFF),
    indicatorColor: Color = AppColor,
    iconTint: Color = Color(0xFF888888),
    selectedIconTint: Color = Color.White,
    barHeight: Dp = 72.dp,
    indicatorSize: Dp = 60.dp,
) {
    val density = LocalDensity.current
    val itemCount = items.size
    val scope = rememberCoroutineScope()

    val indicatorSizePx = with(density) { indicatorSize.toPx() }
    val r1 = with(density) { 38.dp.toPx() }
    val r2 = with(density) { 18.dp.toPx() }
    val c1Y = with(density) { 8.dp.toPx() }
    val barCornerRadius = with(density) { 24.dp.toPx() }
    val horizontalPaddingPx = with(density) { 8.dp.toPx() }

    val cradleCenterX = remember { Animatable(0f) }

    // ── Item centers: computed, not discovered ──
    // Replaces itemCentersX (mutableStateListOf<Float?>) + barLeftXInRoot +
    // positionsReady derivedStateOf. One list, never null, recomputed only when
    // the row's measured width actually changes (set from Modifier.layout below).
    var rowWidthPx by remember { mutableStateOf(0f) }
    val itemCentersX by remember {
        derivedStateOf {
            if (itemCount == 0) emptyList()
            else {
                val contentWidthPx = (rowWidthPx - 2 * horizontalPaddingPx).coerceAtLeast(0f)
                val slotWidthPx = contentWidthPx / itemCount
                List(itemCount) { i -> horizontalPaddingPx + slotWidthPx * (i + 0.5f) }
            }
        }
    }
    val positionsReady by remember { derivedStateOf { rowWidthPx > 0f } }

    // ── displayedIndex: which item the indicator is physically closest to ──
    // Same "nearest slot" logic as before; just reads the computed list instead
    // of the discovered one, and the readiness check is positionsReady (one flag,
    // not three).
    val displayedIndex by remember {
        derivedStateOf {
            if (!positionsReady) selectedIndex
            else {
                val cx = cradleCenterX.value
                var closestIdx = 0
                var minDist = Float.MAX_VALUE
                itemCentersX.forEachIndexed { idx, x ->
                    val d = abs(cx - x)
                    if (d < minDist) { minDist = d; closestIdx = idx }
                }
                closestIdx
            }
        }
    }

    // ── Crossfade icon inside indicator ── (unchanged)
    val iconAlpha = remember { Animatable(1f) }
    var renderedIconIndex by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(displayedIndex) {
        if (renderedIconIndex != displayedIndex) {
            iconAlpha.animateTo(0f, tween(80))
            renderedIconIndex = displayedIndex
            iconAlpha.animateTo(1f, tween(80))
        }
    }

    // ── Animate cradle to selected item ──
    //
    // THE BUG THIS REPLACES:
    // lastAnimatedIndex was rememberSaveable, but cradleCenterX (the Animatable
    // holding the actual pixel position) was plain remember. Animatable cannot be
    // rememberSaveable without a custom Saver, so it has no way to survive this
    // composable being disposed and recreated (which happens whenever you navigate
    // to a detail screen that isn't part of the same Scaffold/NavHost subtree and
    // back). The result: on return, lastAnimatedIndex correctly restores to e.g. 1
    // (Styles), but cradleCenterX restarts at its initial value, 0f — screen-left.
    // Since lastAnimatedIndex (1) already equals selectedIndex (1), the old code
    // fell into the "else" branch, which only snaps if !isRunning — but by then a
    // composable had already rendered one frame with the indicator at 0f, and the
    // *next* recomposition pass (driven by currentBackStackEntryAsState settling)
    // could re-trigger the animate branch, producing exactly the "slides in from
    // the left" symptom.
    //
    // THE FIX: stop trying to keep two independently-surviving pieces of state
    // (a saved index, and an unsaved pixel position) in sync. Instead, track
    // whether THIS pixel position has ever been placed at all, using a boolean
    // that is allowed to reset with cradleCenterX (both plain remember, so they
    // rise and fall together — single source of truth for "are we initialized").
    // lastAnimatedIndex still exists, but only to decide animate-vs-no-op for
    // genuine in-place tab switches; it is no longer load-bearing for the
    // fresh-mount snap decision.
    var lastAnimatedIndex by remember { mutableIntStateOf(selectedIndex) }
    var hasPlacedCradle by remember { mutableStateOf(false) }
    var indicatorReady by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, itemCentersX, positionsReady) {
        if (!positionsReady) return@LaunchedEffect
        val targetX = itemCentersX.getOrNull(selectedIndex) ?: return@LaunchedEffect

        when {
            !hasPlacedCradle -> {
                // First time this instance has ever known a real position —
                // whether that's a true first launch, or a recomposition after
                // this composable was disposed (detail screen, process restore,
                // config change). Always snap. Never animate from an unplaced
                // position, because there is no "previous tab" to animate from —
                // cradleCenterX's current value (0f) is not a real prior state,
                // it's just the Animatable's construction default.
                cradleCenterX.snapTo(targetX)
                lastAnimatedIndex = selectedIndex
                hasPlacedCradle = true
                indicatorReady = true
            }
            lastAnimatedIndex != selectedIndex -> {
                // Genuine tab switch while this instance has been alive the whole
                // time — spring animate (identical spec to the original).
                lastAnimatedIndex = selectedIndex
                cradleCenterX.animateTo(
                    targetX,
                    spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = 100f
                    )
                )
            }
            else -> {
                // Same tab, e.g. a recomposition triggered by something unrelated
                // (drag gesture finished elsewhere, etc). Only correct drift if
                // nothing is actively animating/dragging.
                if (!cradleCenterX.isRunning) {
                    cradleCenterX.snapTo(targetX)
                }
            }
        }
    }

    var isDragging by remember { mutableStateOf(false) }

    // ── Lift on press — scale + shadow animate ── (unchanged)
    val indicatorScale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "indicator_scale"
    )
    val indicatorShadow by animateFloatAsState(
        targetValue = if (isDragging) 14f else 6f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "indicator_shadow"
    )

    val indicatorAbove = with(density) {
        ((indicatorSizePx / 2f) - c1Y).coerceAtLeast(0f).toDp()
    }
    val totalHeight = barHeight + indicatorAbove

    // ── Pre-compute gradient colors for indicator ── (unchanged)
    val gradientLight = remember(indicatorColor) { lerp(indicatorColor, Color.White, 0.28f) }
    val gradientDark = remember(indicatorColor) { lerp(indicatorColor, Color.Black, 0.12f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            // Learns the bar's own width synchronously during measurement — replaces
            // onGloballyPositioned's "tell me after the fact" round trip. No state
            // write happens unless the width actually changed, same guard as before.
            .layout { measurable, constraints ->
                val width = constraints.maxWidth.toFloat()
                if (rowWidthPx != width) rowWidthPx = width
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .pointerInput(itemCount) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (positionsReady) {
                            isDragging = true
                            scope.launch { cradleCenterX.stop() }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val firstX = itemCentersX.firstOrNull()
                        val lastX = itemCentersX.lastOrNull()
                        if (positionsReady && firstX != null && lastX != null) {
                            val newX = (cradleCenterX.value + dragAmount)
                                .coerceIn(minOf(firstX, lastX), maxOf(firstX, lastX))
                            scope.launch { cradleCenterX.snapTo(newX) }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        if (positionsReady) {
                            val current = cradleCenterX.value
                            var closestIdx = 0
                            var minDist = Float.MAX_VALUE
                            itemCentersX.forEachIndexed { idx, x ->
                                val d = abs(current - x)
                                if (d < minDist) { minDist = d; closestIdx = idx }
                            }
                            lastAnimatedIndex = closestIdx
                            val target = itemCentersX.getOrNull(closestIdx) ?: current
                            scope.launch {
                                cradleCenterX.animateTo(
                                    target,
                                    spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)
                                )
                            }
                            onItemSelected(closestIdx)
                        }
                    },
                    onDragCancel = { isDragging = false }
                )
            }
    ) {
        // ── GLASS BAR canvas ── (unchanged — still a plain Canvas reading cradleCenterX
        // every draw frame, exactly as before; only positionsReady's source changed)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
        ) {
            if (positionsReady) {
                drawGlassBar(
                    cx = cradleCenterX.value,
                    barColor = barColor,
                    r1 = r1, r2 = r2,
                    c1Y = c1Y,
                    barCornerRadius = barCornerRadius,
                )
            } else {
                drawRect(barColor)
            }
        }

        // ── Icons row ── (unchanged layout/animation; reads computed itemCentersX)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isInIndicator = index == displayedIndex
                val maxEffectDistancePx = with(density) { 60.dp.toPx() * 1.5f }
                val dipMaxPx = with(density) { 24.dp.toPx() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset {
                            val itemX = itemCentersX.getOrNull(index)
                            val dist =
                                if (itemX != null) abs(cradleCenterX.value - itemX)
                                else Float.MAX_VALUE
                            val fraction = (dist / maxEffectDistancePx).coerceIn(0f, 1f)
                            val dipPx = dipMaxPx * (1f - fraction)
                            IntOffset(0, dipPx.toInt())
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val targetX = itemCentersX.getOrNull(index)
                            lastAnimatedIndex = index
                            if (targetX != null) {
                                scope.launch {
                                    cradleCenterX.animateTo(
                                        targetX,
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = 50f
                                        )
                                    )
                                }
                            }
                            onItemSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val itemX = itemCentersX.getOrNull(index)
                    val dist =
                        if (itemX != null) abs(cradleCenterX.value - itemX)
                        else Float.MAX_VALUE
                    val fraction = (dist / maxEffectDistancePx).coerceIn(0f, 1f)
                    Icon(
                        imageVector = ImageVector.vectorResource(item.icon),
                        contentDescription = item.label,
                        tint = if (isInIndicator) Color.Transparent
                        else iconTint.copy(alpha = fraction),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ── Floating indicator — GRADIENT + LIFT + GLOSSY ── (unchanged)
        if (positionsReady && indicatorReady) {
            val indicatorTopPx = with(density) {
                (totalHeight - barHeight + c1Y.toDp() - indicatorSize / 2).toPx()
            }
            val indicatorHalfPx = indicatorSizePx / 2f

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (cradleCenterX.value - indicatorHalfPx).toInt(),
                            y = indicatorTopPx.toInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = indicatorScale
                        scaleY = indicatorScale
                    }
                    .size(indicatorSize)
                    .shadow(indicatorShadow.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(gradientLight, indicatorColor, gradientDark),
                            center = Offset(
                                indicatorSizePx * 0.38f,
                                indicatorSizePx * 0.32f
                            ),
                            radius = indicatorSizePx * 0.78f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glossy light reflection spot
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0f)
                            ),
                            center = Offset(size.width * 0.34f, size.height * 0.26f),
                            radius = size.minDimension * 0.28f
                        )
                    )
                }

                // Icon with crossfade
                items.getOrNull(renderedIconIndex)?.let { item ->
                    Icon(
                        imageVector = ImageVector.vectorResource(item.icon),
                        contentDescription = item.label,
                        tint = selectedIconTint,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer { alpha = iconAlpha.value }
                    )
                }
            }
        }
    }
}

// ── Glassmorphism bar drawing ── (UNCHANGED — identical to original)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlassBar(
    cx: Float,
    barColor: Color,
    r1: Float,
    r2: Float,
    c1Y: Float,
    barCornerRadius: Float,
) {
    val w = size.width
    val h = size.height

    val c2Y = r2
    val discriminant = (r1 + r2) * (r1 + r2) - (c2Y - c1Y) * (c2Y - c1Y)
    if (discriminant < 0f) { drawRect(barColor); return }

    val dx = sqrt(discriminant.toDouble()).toFloat()
    val theta = Math.toDegrees(atan2((c1Y - c2Y).toDouble(), dx.toDouble())).toFloat()

    val path = Path().apply {
        moveTo(0f, barCornerRadius)
        quadraticTo(0f, 0f, barCornerRadius, 0f)
        lineTo(cx - dx, 0f)
        arcTo(
            rect = Rect(cx - dx - r2, 0f, cx - dx + r2, r2 * 2f),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f + theta,
            forceMoveTo = false
        )
        arcTo(
            rect = Rect(cx - r1, c1Y - r1, cx + r1, c1Y + r1),
            startAngleDegrees = 180f + theta,
            sweepAngleDegrees = -180f - 2f * theta,
            forceMoveTo = false
        )
        arcTo(
            rect = Rect(cx + dx - r2, 0f, cx + dx + r2, r2 * 2f),
            startAngleDegrees = 180f - theta,
            sweepAngleDegrees = 90f + theta,
            forceMoveTo = false
        )
        lineTo(w - barCornerRadius, 0f)
        quadraticTo(w, 0f, w, barCornerRadius)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    // Layer 1: Semi-transparent glass fill
    drawPath(path = path, color = barColor)

    // Layer 2: Frosted gradient overlay (top = bright, bottom = transparent)
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            startY = 0f,
            endY = h * 0.85f
        )
    )

    // Layer 3: Subtle inner horizontal sheen (iOS-like light band)
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            ),
            startX = w * 0.15f,
            endX = w * 0.85f
        )
    )

    // Strokes: glass edge
    val strokePath = Path().apply {
        moveTo(0f, barCornerRadius)
        quadraticTo(0f, 0f, barCornerRadius, 0f)
        lineTo(cx - dx, 0f)
        arcTo(
            rect = Rect(cx - dx - r2, 0f, cx - dx + r2, r2 * 2f),
            startAngleDegrees = 270f, sweepAngleDegrees = 90f + theta, forceMoveTo = false
        )
        arcTo(
            rect = Rect(cx - r1, c1Y - r1, cx + r1, c1Y + r1),
            startAngleDegrees = 180f + theta,
            sweepAngleDegrees = -180f - 2f * theta, forceMoveTo = false
        )
        arcTo(
            rect = Rect(cx + dx - r2, 0f, cx + dx + r2, r2 * 2f),
            startAngleDegrees = 180f - theta, sweepAngleDegrees = 90f + theta,
            forceMoveTo = false
        )
        lineTo(w - barCornerRadius, 0f)
        quadraticTo(w, 0f, w, barCornerRadius)
    }

    // Outer subtle definition stroke
    drawPath(strokePath, color = Color(0x18000000), style = Stroke(width = 1.5f))
    // Inner white glass-edge highlight
    drawPath(strokePath, color = Color.White.copy(alpha = 0.55f), style = Stroke(width = 1f))
}
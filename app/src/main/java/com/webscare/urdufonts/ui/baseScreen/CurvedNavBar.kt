package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
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
 * Curved bottom nav bar with a floating "cradle" indicator.
 *
 * Architecture mirrors the original View-based CurvedBottomNavigationView:
 * there is a SINGLE source of truth for the cutout/indicator horizontal
 * position — `cradleCenterX`, a real pixel value. Both the canvas cutout
 * and the floating circle read from this same value, so they can never
 * drift apart (which was the cause of the "rough"/double-notch look when
 * the cutout used `fraction * width` while the circle used a different,
 * slot-based formula).
 *
 * Item centers are measured from actual layout via onGloballyPositioned,
 * just like the original's getItemCenterX() walked the real child views —
 * so this works correctly even if items aren't perfectly evenly spaced.
 */
@Composable
fun CurvedNavBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    indicatorColor: Color = AppColor,
    iconTint: Color = Color(0xFF888888),
    selectedIconTint: Color = Color.White,
    barHeight: Dp = 72.dp,
    indicatorSize: Dp = 60.dp,
) {
    val density = LocalDensity.current
    val itemCount = items.size
    val scope = rememberCoroutineScope()

    // ── px conversions (match original) ──────────────────────────────
    val indicatorSizePx = with(density) { indicatorSize.toPx() }
    val r1 = with(density) { 38.dp.toPx() }   // cutout radius
    val r2 = with(density) { 18.dp.toPx() }   // shoulder roundness
    val c1Y = with(density) { 8.dp.toPx() }   // cutout circle center, below bar top
    val barCornerRadius = with(density) { 24.dp.toPx() }

    // ── Real measured per-item center X (mirrors getItemCenterX) ──────
    // Filled in by onGloballyPositioned as each item lays out.
    val itemCentersX = remember(itemCount) { mutableStateListOf<Float?>().apply {
        repeat(itemCount) { add(null) }
    } }

    // Left edge of the outer Box in root/window coordinates. Both the Canvas
    // and the per-item boxes get converted into THIS frame (rather than
    // relying on Compose's "nearest parent" via positionInParent, which is
    // unreliable once padding/Row insert intermediate layout nodes). This is
    // the Compose equivalent of the original's `menuView.x + itemView.x`.
    var barLeftXInRoot by remember { mutableStateOf<Float?>(null) }

    // ── Single source of truth for cutout + indicator position (px) ───
    val cradleCenterX = remember { Animatable(0f) }

    // Whether we currently know real positions for every item yet.
    val positionsReady by remember(itemCount) {
        derivedStateOf { barLeftXInRoot != null && itemCentersX.size == itemCount && itemCentersX.all { it != null } }
    }

    // When measured positions become available, or selectedIndex changes
    // externally (e.g. programmatic nav), animate the cradle to that item —
    // exactly like animateCutoutToItem().
    var lastAnimatedIndex by remember { mutableStateOf(-1) }
    LaunchedEffect(positionsReady, selectedIndex, itemCentersX.toList()) {
        if (!positionsReady) return@LaunchedEffect
        val targetX = itemCentersX[selectedIndex] ?: return@LaunchedEffect
        if (lastAnimatedIndex == -1) {
            // First layout: snap instantly, no animation (matches onSizeChanged
            // setting cradleCenterX directly the first time).
            cradleCenterX.snapTo(targetX)
            lastAnimatedIndex = selectedIndex
        } else if (lastAnimatedIndex != selectedIndex) {
            lastAnimatedIndex = selectedIndex
            cradleCenterX.animateTo(
                targetValue = targetX,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
    }

    var isDragging by remember { mutableStateOf(false) }

    // Indicator sticks above the bar by (indicatorSize/2 - c1Y)
    val indicatorAbove = with(density) {
        ((indicatorSizePx / 2f) - c1Y).coerceAtLeast(0f).toDp()
    }
    val totalHeight = barHeight + indicatorAbove

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .onGloballyPositioned { coords ->
                val left = coords.boundsInRoot().left
                if (barLeftXInRoot != left) barLeftXInRoot = left
            }
            .pointerInput(itemCount) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (positionsReady) {
                            isDragging = true
                            scope.launch { cradleCenterX.stop() } // cancel any running animateTo
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
                            // Snap to closest real item center, like endDrag()/findClosestItem.
                            val current = cradleCenterX.value
                            var closestIdx = 0
                            var minDist = Float.MAX_VALUE
                            itemCentersX.forEachIndexed { idx, x ->
                                if (x != null) {
                                    val d = abs(current - x)
                                    if (d < minDist) {
                                        minDist = d
                                        closestIdx = idx
                                    }
                                }
                            }
                            lastAnimatedIndex = closestIdx
                            val target = itemCentersX[closestIdx] ?: current
                            scope.launch {
                                cradleCenterX.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                )
                            }
                            onItemSelected(closestIdx)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
    ) {
        // ── Curved bar canvas — reads the SAME cradleCenterX as the circle ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
        ) {
            if (positionsReady) {
                drawCurvedBarOriginal(
                    cx = cradleCenterX.value,
                    barColor = barColor,
                    r1 = r1, r2 = r2,
                    c1Y = c1Y,
                    barCornerRadius = barCornerRadius,
                )
            } else {
                // Items not measured yet — draw a plain rounded bar, no cutout,
                // to avoid a cutout flashing in the wrong place before layout.
                drawRect(barColor)
            }
        }

        // ── Icons row — also reports each item's real center X ─────────
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
                val isSelected = index == selectedIndex
                val maxEffectDistancePx = with(density) { 60.dp.toPx() * 1.5f }
                val dipMaxPx = with(density) { 24.dp.toPx() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coords ->
                            val barLeft = barLeftXInRoot ?: return@onGloballyPositioned
                            // Real measured center X relative to the bar's own
                            // left edge — same coordinate frame the Canvas
                            // draws in, regardless of any Row padding/insets.
                            val centerXInRoot = coords.boundsInRoot().left + coords.size.width / 2f
                            val centerX = centerXInRoot - barLeft
                            if (itemCentersX.getOrNull(index) != centerX) {
                                itemCentersX[index] = centerX
                            }
                        }
                        .offset {
                            // Distance-driven dip, linear like the original's
                            // updateIcons() — no per-icon eased animation, so it
                            // can never lag behind the (also un-eased) cradle X
                            // during drag. Deferred to layout phase (not read in
                            // composition) so dragging never recomposes the Row.
                            val itemX = itemCentersX.getOrNull(index)
                            val dist = if (itemX != null) abs(cradleCenterX.value - itemX) else Float.MAX_VALUE
                            val fraction = (dist / maxEffectDistancePx).coerceIn(0f, 1f)
                            val dipPx = dipMaxPx * (1f - fraction)
                            androidx.compose.ui.unit.IntOffset(0, dipPx.toInt())
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
                                        targetValue = targetX,
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    )
                                }
                            }
                            onItemSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Alpha still needs a composition-time value (graphicsLayer
                    // alpha could be deferred too, but Icon's tint alpha must be
                    // set at composition; this is acceptable since color/tint
                    // changes are far less perf-sensitive than position).
                    val itemX = itemCentersX.getOrNull(index)
                    val dist = if (itemX != null) abs(cradleCenterX.value - itemX) else Float.MAX_VALUE
                    val fraction = (dist / maxEffectDistancePx).coerceIn(0f, 1f)
                    Icon(
                        imageVector = ImageVector.vectorResource(item.icon),
                        contentDescription = item.label,
                        tint = if (isSelected) Color.Transparent else iconTint.copy(alpha = fraction),
                        modifier = androidx.compose.ui.Modifier.size(24.dp)
                    )
                }
            }
        }

        // ── Floating indicator circle — same cradleCenterX, no drift ────
        if (positionsReady) {
            val indicatorTopPx = with(density) {
                (totalHeight - barHeight + c1Y.toDp() - indicatorSize / 2).toPx()
            }
            val indicatorHalfPx = indicatorSizePx / 2f
            Box(
                modifier = Modifier
                    .offset {
                        androidx.compose.ui.unit.IntOffset(
                            x = (cradleCenterX.value - indicatorHalfPx).toInt(),
                            y = indicatorTopPx.toInt()
                        )
                    }
                    .size(indicatorSize)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(indicatorColor),
                contentAlignment = Alignment.Center
            ) {
                items.getOrNull(selectedIndex)?.let { item ->
                    Icon(
                        imageVector = ImageVector.vectorResource(item.icon),
                        contentDescription = item.label,
                        tint = selectedIconTint,
                        modifier = androidx.compose.ui.Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// ── Exact port of original onDraw geometry — now takes cx directly ──────
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurvedBarOriginal(
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

        // Left shoulder
        arcTo(
            rect = Rect(cx - dx - r2, 0f, cx - dx + r2, r2 * 2f),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f + theta,
            forceMoveTo = false
        )

        // Main cutout arc
        arcTo(
            rect = Rect(cx - r1, c1Y - r1, cx + r1, c1Y + r1),
            startAngleDegrees = 180f + theta,
            sweepAngleDegrees = -180f - 2f * theta,
            forceMoveTo = false
        )

        // Right shoulder
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

    drawPath(path = path, color = barColor)

    // Stroke — top edge only
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
            startAngleDegrees = 180f - theta, sweepAngleDegrees = 90f + theta, forceMoveTo = false
        )
        lineTo(w - barCornerRadius, 0f)
        quadraticTo(w, 0f, w, barCornerRadius)
    }
    drawPath(strokePath, color = Color(0x22000000), style = Stroke(width = 1.5f))
}
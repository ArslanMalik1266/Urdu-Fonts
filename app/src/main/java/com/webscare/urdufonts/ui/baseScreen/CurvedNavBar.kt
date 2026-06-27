package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.saveable.rememberSaveable
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

    val itemCentersX = remember(itemCount) {
        mutableStateListOf<Float?>().apply { repeat(itemCount) { add(null) } }
    }
    var barLeftXInRoot by remember { mutableStateOf<Float?>(null) }
    val cradleCenterX = remember { Animatable(0f) }
    var indicatorReady by remember { mutableStateOf(false) }

    val positionsReady by remember(itemCount) {
        derivedStateOf {
            barLeftXInRoot != null &&
                    itemCentersX.size == itemCount &&
                    itemCentersX.all { it != null }
        }
    }

    // ── displayedIndex: which item the indicator is physically closest to ──
    val displayedIndex by remember(itemCount) {
        derivedStateOf {
            if (!positionsReady || !indicatorReady) selectedIndex // ✅ !indicatorReady add
            else {
                val cx = cradleCenterX.value
                var closestIdx = 0
                var minDist = Float.MAX_VALUE
                itemCentersX.forEachIndexed { idx, x ->
                    if (x != null) {
                        val d = abs(cx - x)
                        if (d < minDist) { minDist = d; closestIdx = idx }
                    }
                }
                closestIdx
            }
        }
    }


    // ── Crossfade icon inside indicator ──
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
    // lastBarLeft tracks the bar's root position so we detect hide/show re-layouts
    var lastBarLeft by remember { mutableStateOf<Float?>(null) }
    var lastAnimatedIndex by rememberSaveable { mutableIntStateOf(-1) }

        LaunchedEffect(positionsReady, selectedIndex, itemCentersX.toList(), barLeftXInRoot) {
        if (!positionsReady) return@LaunchedEffect
        val targetX = itemCentersX[selectedIndex] ?: return@LaunchedEffect
        val currentBarLeft = barLeftXInRoot ?: return@LaunchedEffect

        // If barLeftXInRoot changed it means the bar was hidden and re-appeared
        // (layout remeasured with new position). Snap immediately — never animate.
        val barJustReappeared = lastBarLeft != currentBarLeft
        lastBarLeft = currentBarLeft

            when {
                lastAnimatedIndex == -1 || barJustReappeared -> {
                    // Fresh start ya bar reappear — silently snap
                    cradleCenterX.snapTo(targetX)
                    lastAnimatedIndex = selectedIndex
                    indicatorReady = true  // ✅ snap ke baad dikhao
                }
                lastAnimatedIndex != selectedIndex -> {
                    lastAnimatedIndex = selectedIndex
                    if (!indicatorReady) {
                        // ✅ indicator abhi first time show ho raha hai — animate mat karo
                        cradleCenterX.snapTo(targetX)
                        indicatorReady = true
                    } else {
                        // ✅ Normal tab switch — spring animate karo
                        cradleCenterX.animateTo(
                            targetX,
                            spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = 100f
                            )
                        )
                    }
                }
                else -> {
                    if (!cradleCenterX.isRunning) {
                        cradleCenterX.snapTo(targetX)
                    }
                    if (!indicatorReady) indicatorReady = true  // ✅ ADD
                }
            }


        }

    var isDragging by remember { mutableStateOf(false) }

    // ── Lift on press — scale + shadow animate ──
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

    // ── Pre-compute gradient colors for indicator ──
    val gradientLight = remember(indicatorColor) { lerp(indicatorColor, Color.White, 0.28f) }
    val gradientDark = remember(indicatorColor) { lerp(indicatorColor, Color.Black, 0.12f) }

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
                                if (x != null) {
                                    val d = abs(current - x)
                                    if (d < minDist) { minDist = d; closestIdx = idx }
                                }
                            }
                            lastAnimatedIndex = closestIdx
                            val target = itemCentersX[closestIdx] ?: current
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
        // ── GLASS BAR canvas ──
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

        // ── Icons row ──
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
                        .onGloballyPositioned { coords ->
                            val barLeft = barLeftXInRoot ?: return@onGloballyPositioned
                            val centerXInRoot =
                                coords.boundsInRoot().left + coords.size.width / 2f
                            val centerX = centerXInRoot - barLeft
                            if (itemCentersX.getOrNull(index) != centerX) {
                                itemCentersX[index] = centerX
                            }
                        }
                        .offset {
                            val itemX = itemCentersX.getOrNull(index)
                            val dist =
                                if (itemX != null) abs(cradleCenterX.value - itemX)
                                else Float.MAX_VALUE
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

        // ── Floating indicator — GRADIENT + LIFT + GLOSSY ──
        if (positionsReady && indicatorReady) {
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

// ── Glassmorphism bar drawing ──
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

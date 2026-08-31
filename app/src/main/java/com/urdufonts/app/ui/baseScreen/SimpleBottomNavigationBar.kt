package com.urdufonts.app.ui.baseScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.theme.AppColor
import kotlin.math.abs

@Composable
fun SimpleBottomNavigationBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFFFFFFF),
    indicatorColor: Color = AppColor,
    iconTint: Color = Color(0xFF888888),
    barHeight: Dp = 72.dp,
) {
    val density = LocalDensity.current
    val itemCount = items.size

    val horizontalPadding = 8.dp
    val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }

    // ── Item centers calculation (Subtracting padding to center items perfectly) ──
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

    // ── Indicator position & last index state ──
    val indicatorCenterX = remember { Animatable(0f) }
    var hasPlacedIndicator by remember { mutableStateOf(false) }
    var lastSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex, itemCentersX, positionsReady) {
        if (!positionsReady) return@LaunchedEffect
        val targetX = itemCentersX.getOrNull(selectedIndex) ?: return@LaunchedEffect

        if (!hasPlacedIndicator) {
            indicatorCenterX.snapTo(targetX)
            hasPlacedIndicator = true
            lastSelectedIndex = selectedIndex
        } else {
            val indexDiff = abs(selectedIndex - lastSelectedIndex)
            lastSelectedIndex = selectedIndex

            // Travelling speed (stiffness) is constant.
            val stiffness = 150f

            // Damping (bounciness) changes dynamically based on distance.
            val damping = if (indexDiff <= 1) {
                0.65f // Short move: snug settle
            } else {
                0.55f // Long move: elastic bounce
            }

            indicatorCenterX.animateTo(
                targetX,
                spring(
                    dampingRatio = damping,
                    stiffness = stiffness
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .background(
                color = barColor,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .layout { measurable, constraints ->
                val width = constraints.maxWidth.toFloat()
                if (rowWidthPx != width) rowWidthPx = width
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
    ) {
        // ── 1. Top Border Rail ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(Color(0x0F000000))
        )

        // ── 2. Top-Aligned Indicator Pill with Physics Stretch ──
        if (positionsReady) {
            val indicatorWidth = 36.dp
            val indicatorWidthPx = with(density) { indicatorWidth.toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (indicatorCenterX.value - indicatorWidthPx / 2).toInt(),
                            y = 0
                        )
                    }
                    .graphicsLayer {
                        val velocity = indicatorCenterX.velocity
                        val stretchFactor = 0.00022f
                        val maxStretch = 1.8f
                        this.scaleX = (1f + abs(velocity) * stretchFactor).coerceAtMost(maxStretch)

                        val activeScaleX = this.scaleX
                        this.scaleY = (1f - (activeScaleX - 1f) * 0.3f).coerceAtLeast(0.7f)
                    }
                    .width(indicatorWidth)
                    .height(4.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
                    )
            )
        }

        // ── 3. Icons Row ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onItemSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val currentIconRes = if (isSelected) item.selectedIcon else item.icon
                    Icon(
                        imageVector = ImageVector.vectorResource(currentIconRes),
                        contentDescription = item.label,
                        tint = if (isSelected) indicatorColor else iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
            }
        }
    }
}

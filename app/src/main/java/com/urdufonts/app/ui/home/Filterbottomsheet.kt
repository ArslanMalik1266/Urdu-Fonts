package com.urdufonts.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.FontClassifier
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

// ─── Enum: which accordion section is open ────────────────────────────────────

enum class FilterSection { NONE, CATEGORIES, STYLES }

// ─── Bottom Sheet ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onToggleCategory: (String) -> Unit,
    onToggleStyle: (String) -> Unit,
    onToggleSection: (FilterSection) -> Unit,
    onClearAll: () -> Unit,
    onApplyFilters: () -> Unit,
    onToggleCategoriesGrid: () -> Unit, // Add state handler callback
    onToggleStylesGrid: () -> Unit,     // Add state handler callback
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val configuration = LocalConfiguration.current
    val targetSheetHeight = (configuration.screenHeightDp * 0.75).dp
    val scrollState = rememberScrollState()

    // Independent expand states so Categories and Styles can both be open at the same time
    var isCategoriesOpen by remember { mutableStateOf(true) }
    var isStylesOpen by remember { mutableStateOf(true) }

    // Option 2 (Sequential): When Styles grid expands, wait for expansion animation to finish, then smoothly auto-scroll down
    LaunchedEffect(uiState.isStylesGridExpanded) {
        if (uiState.isStylesGridExpanded) {
            delay(280)
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(targetSheetHeight)
        ) {
            // Header (Fixed at the top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingBlackColor,
                    fontFamily = NunitoFontFamily
                )
                if (uiState.hasActiveFilters) {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Clear All",
                            color = AppColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }
            }

            // Scrollable Content area with smooth content size animation to prevent jhatka/stutter
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))
                    .verticalScroll(scrollState)
            ) {
                // ── Categories — driven by independent state ──────
                FilterSectionHeader(
                    title = "Categories",
                    isExpanded = isCategoriesOpen,
                    selectedCount = uiState.selectedCategories.size,
                    onClick = { isCategoriesOpen = !isCategoriesOpen }
                )
                AnimatedVisibility(
                    visible = isCategoriesOpen,
                    enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)) + fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)),
                    exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)) + fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)),
                ) {
                    FilterOptionGrid(
                        classifiers = uiState.availableCategories,
                        selectedSlugs = uiState.selectedCategories,
                        onToggle = onToggleCategory,
                        isExpanded = uiState.isCategoriesGridExpanded,
                        onExpandToggle = onToggleCategoriesGrid,
                        initialFullRows = 3
                    )
                }

                // ── Styles — driven by independent state ──────────────
                FilterSectionHeader(
                    title = "Styles",
                    isExpanded = isStylesOpen,
                    selectedCount = uiState.selectedStyles.size,
                    onClick = { isStylesOpen = !isStylesOpen }
                )
                AnimatedVisibility(
                    visible = isStylesOpen,
                    enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)) + fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)),
                    exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)) + fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)),
                ) {
                    FilterOptionGrid(
                        classifiers = uiState.availableStyles,
                        selectedSlugs = uiState.selectedStyles,
                        onToggle = onToggleStyle,
                        isExpanded = uiState.isStylesGridExpanded,
                        onExpandToggle = onToggleStylesGrid,
                        initialFullRows = 1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Apply button (Fixed at the bottom with thin visual divider separator)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = GreyColor.copy(alpha = 0.12f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .addPressEffect { onApplyFilters() }
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColor),
                        contentAlignment = Alignment.Center
                    ) {
                        val total = uiState.totalSelectedFilters
                        Text(
                            text = if (total > 0) "Apply Filters ($total)" else "Apply Filters",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }
            }
        }
    }
}


// ─── Section header with ic_arrow_up / ic_arrow_down drawable ─────────────────

@Composable
private fun FilterSectionHeader(
    title: String,
    isExpanded: Boolean,
    selectedCount: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily
            )
            if (selectedCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$selectedCount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }

        Image(
            painter = painterResource(
                id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            ),
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier.size(22.dp),
            contentScale = ContentScale.Fit
        )
    }
}

// ─── 3-column grid — receives real FontClassifier list from API ───────────────

private sealed class GridItem {
    data class Classifier(val data: FontClassifier) : GridItem()
    data class MoreButton(val count: Int) : GridItem()
    object LessButton : GridItem()
}

@Composable
private fun FilterOptionGrid(
    classifiers: List<FontClassifier>,
    selectedSlugs: Set<String>,
    onToggle: (String) -> Unit,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    initialFullRows: Int = 3
) {
    if (classifiers.isEmpty()) return

    val initialItemsCount = (initialFullRows * 3) + 1

    if (classifiers.size <= initialItemsCount) {
        val rows = classifiers.map { GridItem.Classifier(it) }.chunked(3)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            RenderGridItem(item, selectedSlugs, onToggle, onExpandToggle)
                        }
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
        return
    }

    val allExpandedItems = remember(classifiers) {
        classifiers.map { GridItem.Classifier(it) } + GridItem.LessButton
    }
    val allExpandedRows = remember(allExpandedItems) { allExpandedItems.chunked(3) }

    val staticRows = remember(allExpandedRows, initialFullRows) { allExpandedRows.take(initialFullRows) }

    val partialRowFirstItemIndex = initialFullRows * 3
    val partialRowFirstItem = classifiers.getOrNull(partialRowFirstItemIndex)
    val partialRowSecondItemIndex = partialRowFirstItemIndex + 1
    val partialRowThirdItemIndex = partialRowFirstItemIndex + 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // --- STATIC FULL ROWS ---
        staticRows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        RenderGridItem(item, selectedSlugs, onToggle, onExpandToggle)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- PARTIAL ROW (Slot 1 = Item, Slot 2 = +More/Item, Slot 3 = Blank/Item) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Slot 1
            Box(modifier = Modifier.weight(1f)) {
                partialRowFirstItem?.let {
                    RenderGridItem(GridItem.Classifier(it), selectedSlugs, onToggle, onExpandToggle)
                }
            }

            // Slot 2
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = isExpanded, label = "slot2_crossfade") { expanded ->
                    if (expanded) {
                        classifiers.getOrNull(partialRowSecondItemIndex)?.let {
                            RenderGridItem(GridItem.Classifier(it), selectedSlugs, onToggle, onExpandToggle)
                        } ?: Spacer(modifier = Modifier.fillMaxWidth())
                    } else {
                        RenderGridItem(
                            GridItem.MoreButton(classifiers.size - initialItemsCount),
                            selectedSlugs,
                            onToggle,
                            onExpandToggle
                        )
                    }
                }
            }

            // Slot 3
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = isExpanded, label = "slot3_crossfade") { expanded ->
                    if (expanded) {
                        classifiers.getOrNull(partialRowThirdItemIndex)?.let {
                            RenderGridItem(GridItem.Classifier(it), selectedSlugs, onToggle, onExpandToggle)
                        } ?: Spacer(modifier = Modifier.fillMaxWidth())
                    } else {
                        Spacer(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // --- COLLAPSIBLE EXTRA ROWS ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f)) +
                    fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f)),
            exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f)) +
                    fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(8.dp))

                val collapsibleRows = remember(allExpandedRows, initialFullRows) {
                    allExpandedRows.drop(initialFullRows + 1)
                }

                collapsibleRows.forEachIndexed { index, rowItems ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                RenderGridItem(item, selectedSlugs, onToggle, onExpandToggle)
                            }
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

// --- REUSABLE COMPOSABLE FOR INDIVIDUAL GRID CHIPS ---
@Composable
private fun RenderGridItem(
    item: GridItem,
    selectedSlugs: Set<String>,
    onToggle: (String) -> Unit,
    onExpandToggle: () -> Unit
) {
    when (item) {
        is GridItem.Classifier -> {
            FilterChipItem(
                label = item.data.title,
                slug = item.data.slug,
                isSelected = item.data.slug in selectedSlugs,
                onToggle = { onToggle(item.data.slug) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        is GridItem.MoreButton -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .addPressEffect { onExpandToggle() }
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColor.copy(alpha = 0.08f))
                    .border(1.dp, AppColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "+${item.count} More",
                    color = AppColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily
                )
            }
        }
        is GridItem.LessButton -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .addPressEffect { onExpandToggle() }
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColor.copy(alpha = 0.08f))
                    .border(1.dp, AppColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "Show Less",
                    color = AppColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily
                )
            }
        }
    }
}




// ─── Single chip — solid AppColor circle, no icon ─────────────────────────────

@Composable
private fun FilterChipItem(
    label: String,
    slug: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AppColor else GreyColor.copy(0.15f)
    val textColor = HeadingBlackColor

    Row(
        modifier = modifier
            .addPressEffect {
                onToggle()
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AppColor.copy(alpha = 0.08f) else Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
            fontFamily = NunitoFontFamily
        )
    }
}

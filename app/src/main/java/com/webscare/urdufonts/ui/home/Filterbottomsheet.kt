package com.webscare.urdufonts.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.util.addPressEffect

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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
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
                    color = HeadingBlackColor
                )
                if (uiState.hasActiveFilters) {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Clear All",
                            color = AppColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Categories — driven by categories state ──────
            FilterSectionHeader(
                title = "Categories",
                isExpanded = uiState.expandedFilterSection == FilterSection.CATEGORIES,
                selectedCount = uiState.selectedCategories.size,
                onClick = { onToggleSection(FilterSection.CATEGORIES) }
            )
            AnimatedVisibility(
                visible = uiState.expandedFilterSection == FilterSection.CATEGORIES,
                enter = expandVertically(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(220)),
            ) {
                FilterOptionGrid(
                    classifiers = uiState.availableCategories,
                    selectedSlugs = uiState.selectedCategories,
                    onToggle = onToggleCategory,
                    isExpanded = uiState.isCategoriesGridExpanded, // Consume state
                    onExpandToggle = onToggleCategoriesGrid        // Dispatch event
                )
            }

            // ── Styles — driven by styles state ──────────────
            FilterSectionHeader(
                title = "Styles",
                isExpanded = uiState.expandedFilterSection == FilterSection.STYLES,
                selectedCount = uiState.selectedStyles.size,
                onClick = { onToggleSection(FilterSection.STYLES) }
            )
            AnimatedVisibility(
                visible = uiState.expandedFilterSection == FilterSection.STYLES,
                enter = expandVertically(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(220)),
            ) {
                FilterOptionGrid(
                    classifiers = uiState.availableStyles,
                    selectedSlugs = uiState.selectedStyles,
                    onToggle = onToggleStyle,
                    isExpanded = uiState.isStylesGridExpanded, // Consume state
                    onExpandToggle = onToggleStylesGrid        // Dispatch event
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
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
                        fontWeight = FontWeight.SemiBold
                    )
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
                color = HeadingBlackColor
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
                        fontWeight = FontWeight.Bold
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

// ─── 2-column grid — receives real FontClassifier list from API ───────────────

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
    isExpanded: Boolean,             // Receives state from parent
    onExpandToggle: () -> Unit       // Delegates event click to ViewModel
)
{
    if (classifiers.isEmpty()) return

    val displayItems = remember(classifiers, isExpanded) {
        if (classifiers.size > 9) {
            if (isExpanded) {
                classifiers.map { GridItem.Classifier(it) } + GridItem.LessButton
            } else {
                classifiers.take(8).map { GridItem.Classifier(it) } + GridItem.MoreButton(classifiers.size - 8)
            }
        } else {
            classifiers.map { GridItem.Classifier(it) }
        }
    }

    val rows = displayItems.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
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
                                        .addPressEffect { onExpandToggle() } // Mutates state via viewmodel
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AppColor.copy(alpha = 0.08f))
                                        .border(1.dp, AppColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                ) {
                                    Text(
                                        text = "+${item.count} More",
                                        color = AppColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            is GridItem.LessButton -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .addPressEffect { onExpandToggle() } // Mutates state via viewmodel
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AppColor.copy(alpha = 0.08f))
                                        .border(1.dp, AppColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                ) {
                                    Text(
                                        text = "Show Less",
                                        color = AppColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
    val textColor = if (isSelected) AppColor else GreyColor

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
            textAlign = TextAlign.Center
        )
    }
}

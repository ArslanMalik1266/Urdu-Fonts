package com.webscare.urdufonts.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.domain.models.FontClassifier
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor

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
        // ── Scrollable content area ────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Header ────────────────────────────────────────────────────────
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

            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // ── Scrollable accordion box (Categories + Styles) ───────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()               // height = content ka size
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // ── Categories ────────────────────────────────────────────
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
                        FilterOptionNonLazyGrid(
                            classifiers = uiState.availableCategories,
                            selectedSlugs = uiState.selectedCategories,
                            onToggle = onToggleCategory,
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )

                    // ── Styles ────────────────────────────────────────────────
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
                        FilterOptionNonLazyGrid(
                            classifiers = uiState.availableStyles,
                            selectedSlugs = uiState.selectedStyles,
                            onToggle = onToggleStyle,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // ── Apply button ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColor)
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

// ─── Section header ───────────────────────────────────────────────────────────

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

// ─── Non-lazy 2-column grid (works inside verticalScroll) ─────────────────────
// LazyVerticalGrid nested inside verticalScroll crashes — this replaces it.

@Composable
private fun FilterOptionNonLazyGrid(
    classifiers: List<FontClassifier>,
    selectedSlugs: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (classifiers.isEmpty()) return

    // Split list into rows of 2
    val rows = classifiers.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { classifier ->
                    FilterChipItem(
                        modifier = Modifier.weight(1f),
                        label = classifier.title,
                        slug = classifier.slug,
                        isSelected = classifier.slug in selectedSlugs,
                        onToggle = { onToggle(classifier.slug) }
                    )
                }
                // If odd number of items, fill remaining space
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Single chip ──────────────────────────────────────────────────────────────

@Composable
private fun FilterChipItem(
    modifier: Modifier = Modifier,
    label: String,
    slug: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val borderColor = if (isSelected) AppColor else Color(0xFFE0E0E0)
    val textColor = if (isSelected) AppColor else Color(0xFF555555)

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)) // 👈 border shows selection
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF4CAF50) else Color.White)
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFFCCCCCC),
                    shape = CircleShape
                )
        ) {
            if (isSelected) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tick),
                    contentDescription = "Selected",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
        )
    }
}
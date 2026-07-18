package com.webscare.urdufonts.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.ui.components.AppTopBar
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.FilterButton
import com.webscare.urdufonts.ui.components.FontItemCard
import com.webscare.urdufonts.ui.components.FontListSkeleton
import com.webscare.urdufonts.ui.components.OfflineErrorState
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.StaggeredFadeIn
import com.webscare.urdufonts.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onFontClick: (fontId: String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val seenItems = remember { mutableStateSetOf<Int>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    onMenuClick = onMenuClick,
                    onCartClick = { }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CustomSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.padding(horizontal = 0.dp),
                        onDone = { focusManager.clearFocus() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore Fonts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            fontFamily = NunitoFontFamily,
                            color = HeadingBlackColor.copy(alpha = 0.8f)
                        )
                        FilterButton(
                            onClick = { viewModel.showFilterSheet() },
                            isFilterActive = uiState.hasActiveFilters
                        )
                    }
                }

                Crossfade(
                    targetState = when {
                        uiState.errorMessage != null -> "error"
                        uiState.isLoading -> "loading"
                        else -> "content"
                    },
                    animationSpec = tween(durationMillis = 350),
                    label = "home_screen_transition"
                ) { state ->
                    when (state) {
                        "loading" -> {
                            FontListSkeleton()
                        }
                        "error" -> {
                            OfflineErrorState(
                                message = uiState.errorMessage!!,
                                onRetry = viewModel::retry
                            )
                        }
                        "content" -> {
                            val listState = rememberLazyListState()
                            LaunchedEffect(uiState.fonts) {
                                listState.animateScrollToItem(0)
                            }
                            Box(modifier = Modifier.clipToBounds()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.springOverscroll(),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(
                                        items = uiState.fonts,
                                        key = { _, fontItem -> fontItem.id }
                                    ) { index, fontItem ->
                                        val showDivider = index < uiState.fonts.lastIndex
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        ) {
                                            StaggeredFadeIn(
                                                index = index,
                                                seenItems = seenItems
                                            ) {
                                                FontItemCard(
                                                    fontItem = fontItem,
                                                    onDownloadClick = {},
                                                    onFontClick = { onFontClick(fontItem.id.toString()) }
                                                )
                                            }
                                            if (showDivider) {
                                                HorizontalDivider(
                                                    thickness = 0.5.dp,
                                                    color = GreyColor.copy(alpha = 0.2f)
                                                )
                                            }
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(60.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                uiState = uiState,
                sheetState = filterSheetState,
                onDismiss = viewModel::hideFilterSheet,
                onToggleCategory = viewModel::toggleCategory,
                onToggleStyle = viewModel::toggleStyle,
                onToggleSection = viewModel::toggleFilterSection,
                onClearAll = viewModel::clearAllFilters,
                onApplyFilters = viewModel::applyFiltersAndClose,
                onToggleCategoriesGrid = viewModel::toggleCategoriesGrid,
                onToggleStylesGrid = viewModel::toggleStylesGrid
            )
        }
    }
}

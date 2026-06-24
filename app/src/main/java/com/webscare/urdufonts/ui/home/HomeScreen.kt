package com.webscare.urdufonts.ui.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.domain.models.FontListItem
import com.webscare.urdufonts.ui.components.AppTopBar
import com.webscare.urdufonts.ui.components.BannerCard
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.FilterButton
import com.webscare.urdufonts.ui.components.FontItemCard
import com.webscare.urdufonts.ui.components.OfflineErrorState
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
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
                        // FilterButton now opens the bottom sheet
                        FilterButton(onClick = { viewModel.showFilterSheet() })
                    }
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColor)
                        }
                    }

                    uiState.errorMessage != null -> {
                        OfflineErrorState(
                            message = uiState.errorMessage!!,
                            onRetry = viewModel::retry
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.springOverscroll(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.fonts,
                                key = { item ->
                                    when (item) {
                                        is FontListItem.Font -> item.fontItem.id
                                        is FontListItem.Banner -> {
                                            "banner_${uiState.fonts.indexOf(item)}"
                                        }
                                    }
                                }
                            ) { item ->
                                val index = uiState.fonts.indexOf(item)
                                val nextItem = uiState.fonts.getOrNull(index + 1)
                                val prevItem = uiState.fonts.getOrNull(index - 1)
                                when (item) {

                                    is FontListItem.Font -> {
                                        val showDivider = nextItem !is FontListItem.Banner
                                        FontItemCard(
                                            fontItem = item.fontItem,
                                            onDownloadClick = {},
                                            onFontClick = { onFontClick(item.fontItem.id.toString()) },
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            showDivider = showDivider
                                        )
                                    }
                                        is FontListItem.Banner -> BannerCard(
                                        bannerItem = item.bannerItem,
                                        modifier = Modifier.fillMaxWidth()
                                        )

                                }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }

        // ── Filter bottom sheet ────────────────────────────────────────────────
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
            )
        }
    }
}
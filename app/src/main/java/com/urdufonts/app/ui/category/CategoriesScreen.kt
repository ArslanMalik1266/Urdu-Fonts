package com.urdufonts.app.ui.category

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.ui.components.CustomSearchBar
import com.urdufonts.app.ui.components.OfflineErrorState
import com.urdufonts.app.ui.components.SimpleTopAppBar
import com.urdufonts.app.ui.util.springOverscroll
import com.urdufonts.app.ui.util.BlurOverlay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCartClick: () -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {},
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    com.urdufonts.app.ui.util.LogScreenEntry("CategoriesScreen")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SimpleTopAppBar(
                    title = "Urdu Font Categories",
                    onCartClick = onCartClick,
                    onSubscriptionClick = onSubscriptionClick,
                    showSubscriptionIcon = true
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)) {
                    CustomSearchBar(
                        readOnly = true,
                        placeholderText = "Search fonts, categories, styles...",
                        onClick = onSearchClick,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }

                when {
                    uiState.isLoading -> {
                        CategoriesSkeleton()
                    }
                    uiState.errorMessage != null -> {
                        OfflineErrorState(
                            message = uiState.errorMessage!!,
                            onRetry = viewModel::retry
                        )
                    }
                    else -> {
                        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                        val visibleCategories = uiState.categories

                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            com.urdufonts.app.ui.util.PerfDiagnostics.logScreenTransition("CategoriesScreen", "Content Rendered (${uiState.categories.size} items)")
                        }
                        androidx.compose.runtime.LaunchedEffect(gridState) {
                            androidx.compose.runtime.snapshotFlow { gridState.firstVisibleItemIndex }
                                .collect { idx ->
                                    com.urdufonts.app.ui.util.PerfDiagnostics.logScrollEvent("CategoriesScreen", idx, uiState.categories.size)
                                }
                        }
                        com.urdufonts.app.ui.util.LazyGridImagePrefetcher(
                            gridState = gridState,
                            imageUrls = remember(uiState.categories) { uiState.categories.mapNotNull { it.thumbnailUrl } },
                            requestFor = com.urdufonts.app.ui.util.CardImageRequests::category,
                            bufferAheadCount = 4
                        )
                        Box(modifier = Modifier.clipToBounds()) {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.springOverscroll(),
                                contentPadding = PaddingValues(
                                    start = 18.dp,
                                    end = 18.dp,
                                    top = 8.dp,
                                    bottom = 80.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                 visibleCategories.forEachIndexed { index, category ->
                                     item(
                                         key = category.id,
                                         contentType = "category_card"
                                     ) {
                                         // No per-card fade: images are preloaded, so the first screen appears at once.
                                         // A staggered alpha fade made the green previews look grey for ~200ms.
                                         CategoryItemCard(
                                             category = category,
                                             onClick = { onCategoryClick(category) }
                                         )
                                     }
                                     // Medium native after 2 rows (4 tiles, 2 per row), then after every 7 rows (14 tiles)
                                     if (com.urdufonts.app.ui.components.ads.isNativeAdSlot(index + 1, first = 4, every = 14)) {
                                         item(
                                             span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) },
                                             contentType = "native_ad"
                                         ) {
                                             com.urdufonts.app.ui.components.ads.ComposableWebsCareNative(
                                                 placement = com.webscare.ads.placements.WebsCarePlacements.URDUFONTS_CATEGORIES_NATIVE,
                                                 slot = index,
                                                 modifier = Modifier.padding(vertical = 0.dp)
                                             )
                                         }
                                     }
                                 }
                            }
                        }
                    }
                }
            }
        }
    }
}

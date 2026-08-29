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
import com.urdufonts.app.ui.util.StaggeredFadeIn
import com.urdufonts.app.ui.util.springOverscroll
import com.urdufonts.app.ui.util.BlurOverlay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCartClick: () -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onSubscriptionClick: () -> Unit = {},
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val seenItems = remember { mutableStateSetOf<Int>() }
    com.urdufonts.app.ui.util.LogScreenEntry("CategoriesScreen")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SimpleTopAppBar(title = "Urdu Font Categories", onCartClick = onCartClick, onSubscriptionClick = onSubscriptionClick)
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
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)) {
                    CustomSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.padding(horizontal = 0.dp),
                        onDone = {
                            focusManager.clearFocus()
                        }
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
                        var renderLimit by remember { mutableStateOf(4) }
                        androidx.compose.runtime.LaunchedEffect(uiState.categories) {
                            if (uiState.categories.isNotEmpty()) {
                                renderLimit = 4
                                kotlinx.coroutines.delay(30)
                                renderLimit = uiState.categories.size
                            }
                        }
                        val visibleCategories = remember(uiState.categories, renderLimit) {
                            uiState.categories.take(renderLimit)
                        }

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
                                    top = 16.dp,
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
                                         StaggeredFadeIn(
                                             index = index,
                                             seenItems = seenItems
                                         ) {
                                             CategoryItemCard(
                                                 category = category,
                                                 onClick = { onCategoryClick(category) }
                                             )
                                         }
                                     }
                                     if (index > 0 && (index + 1) % 8 == 0) {
                                         item(
                                             span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) },
                                             contentType = "native_ad"
                                         ) {
                                             com.urdufonts.app.ui.components.ads.ComposableWebsCareNative(
                                                 adUnitId = com.urdufonts.app.ads.AdConfig.CATEGORIES_NATIVE_AD_UNIT_ID,
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

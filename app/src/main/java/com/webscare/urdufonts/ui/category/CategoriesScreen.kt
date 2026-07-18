package com.webscare.urdufonts.ui.category

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
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.OfflineErrorState
import com.webscare.urdufonts.ui.components.SimpleTopAppBar
import com.webscare.urdufonts.ui.util.StaggeredFadeIn
import com.webscare.urdufonts.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCartClick: () -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val seenItems = remember { mutableStateSetOf<Int>() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SimpleTopAppBar(title = "Categories", onCartClick = onCartClick)
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
                Column(modifier = Modifier.padding(16.dp)) {
                    CustomSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.padding(horizontal = 0.dp),
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                }

                Crossfade(
                    targetState = when {
                        uiState.errorMessage != null -> "error"
                        uiState.isLoading -> "loading"
                        else -> "content"
                    },
                    animationSpec = tween(durationMillis = 350),
                    label = "categories_screen_transition"
                ) { state ->
                    when (state) {
                        "loading" -> {
                            CategoriesSkeleton()
                        }
                        "error" -> {
                            OfflineErrorState(
                                message = uiState.errorMessage!!,
                                onRetry = viewModel::retry
                            )
                        }
                        "content" -> {
                            Box(modifier = Modifier.clipToBounds()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.springOverscroll(),
                                    contentPadding = PaddingValues(
                                        start = 18.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 18.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    itemsIndexed(
                                        items = uiState.categories,
                                        key = { _, category -> category.id }
                                    ) { index, category ->
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
                                    item {
                                        Spacer(modifier = Modifier.height(60.dp))
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

package com.urdufonts.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.domain.models.StyleItem
import com.urdufonts.app.domain.repo.EntitlementRepository
import com.urdufonts.app.ui.components.CustomSearchBar
import com.urdufonts.app.ui.components.FontListSkeleton
import com.urdufonts.app.ui.components.SimpleTopAppBar
import com.urdufonts.app.ui.search.components.SearchCategoriesResults
import com.urdufonts.app.ui.search.components.SearchEmptyView
import com.urdufonts.app.ui.search.components.SearchFontsResults
import com.urdufonts.app.ui.search.components.SearchStylesResults
import com.urdufonts.app.ui.search.components.SearchTabRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onFontClick: (String) -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
    onStyleClick: (StyleItem) -> Unit,
    initialTab: String? = null,
    onSubscriptionClick: () -> Unit = {},
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val initialIndex = remember(initialTab) {
        when (SearchTab.fromString(initialTab)) {
            SearchTab.FONTS -> 0
            SearchTab.STYLES -> 1
            SearchTab.CATEGORIES -> 2
        }
    }
    val pagerState = rememberPagerState(initialPage = initialIndex) { 3 }

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val tab = when (pagerState.currentPage) {
            0 -> SearchTab.FONTS
            1 -> SearchTab.STYLES
            else -> SearchTab.CATEGORIES
        }
        viewModel.onTabSelected(tab)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SimpleTopAppBar(
                title = "Search Result",
                onBackClick = onBackClick,
                onSubscriptionClick = onSubscriptionClick,
                showSubscriptionIcon = true,
                containerColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            CustomSearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                placeholderText = "Search fonts, categories, styles...",
                focusRequester = focusRequester,
                onDone = { focusManager.clearFocus() },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            )

            SearchTabRow(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                fontCount = uiState.fonts.size,
                styleCount = uiState.styles.size,
                categoryCount = uiState.categories.size
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            when {
                                uiState.isLoading -> FontListSkeleton()
                                uiState.fonts.isEmpty() -> SearchEmptyView(query = uiState.query)
                                else -> SearchFontsResults(
                                    fonts = uiState.fonts,
                                    onFontClick = { onFontClick(it.id.toString()) }
                                )
                            }
                        }
                        1 -> {
                            when {
                                uiState.isLoading -> FontListSkeleton()
                                uiState.styles.isEmpty() -> SearchEmptyView(query = uiState.query)
                                else -> SearchStylesResults(
                                    styles = uiState.styles,
                                    onStyleClick = onStyleClick
                                )
                            }
                        }
                        2 -> {
                            when {
                                uiState.isLoading -> FontListSkeleton()
                                uiState.categories.isEmpty() -> SearchEmptyView(query = uiState.query)
                                else -> SearchCategoriesResults(
                                    categories = uiState.categories,
                                    onCategoryClick = onCategoryClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

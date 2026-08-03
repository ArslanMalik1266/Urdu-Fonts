package com.urdufonts.app.ui.fontList

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.ui.components.CustomSearchBar
import com.urdufonts.app.ui.components.FontItemCard
import com.urdufonts.app.ui.components.FontListSkeleton
import com.urdufonts.app.ui.components.SimpleTopAppBar
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.StaggeredFadeIn
import com.urdufonts.app.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontListScreen(
    title: String,
    onBackClick: () -> Unit,
    onFontClick: (fontId: String) -> Unit,
    viewModel: FontListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val seenItems = remember { mutableStateSetOf<Int>() }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = title,
                onBackClick = onBackClick,
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
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onDone = {
                    focusManager.clearFocus()
                }
            )

            Crossfade(
                targetState = when {
                    uiState.isLoading -> "loading"
                    uiState.fonts.isEmpty() -> "empty"
                    else -> "content"
                },
                animationSpec = tween(durationMillis = 350),
                label = "font_list_screen_transition"
            ) { state ->
                when (state) {
                    "loading" -> {
                        FontListSkeleton()
                    }
                    "empty" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No fonts found", fontFamily = NunitoFontFamily)
                        }
                    }
                    "content" -> {
                        Box(modifier = Modifier.clipToBounds()) {
                            LazyColumn(
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
                                                onFontClick = { onFontClick(fontItem.id.toString()) },
                                            )
                                        }
                                        if (showDivider) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = GreyColor.copy(alpha = 0.2f)
                                            )
                                        }
                                    }

                                    if (index > 0 && (index + 1) % 8 == 0) {
                                        com.urdufonts.app.ui.components.ads.ComposableWebsCareNative(
                                            adUnitId = com.urdufonts.app.ads.AdConfig.FONT_LIST_NATIVE_AD_UNIT_ID,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
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

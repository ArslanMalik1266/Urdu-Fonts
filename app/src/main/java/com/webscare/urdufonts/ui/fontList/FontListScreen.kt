package com.webscare.urdufonts.ui.fontList

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
import androidx.compose.material3.CircularProgressIndicator
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
import com.webscare.urdufonts.ui.components.BannerCard
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.FontItemCard
import com.webscare.urdufonts.ui.components.SimpleTopAppBar
import com.webscare.urdufonts.ui.home.HomeViewModel
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.util.StaggeredFadeIn
import com.webscare.urdufonts.ui.util.springOverscroll
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
                    focusManager.clearFocus() // This is the command that clears the focus
                }
            )

            // ADD THIS: Handle loading and empty states like in HomeScreen
            when {
                uiState.isLoading -> {
                    // Show a loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColor)
                    }
                }

                uiState.fonts.isEmpty() -> {
                    // Show empty state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No fonts found")
                    }
                }

                else -> {
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
                                val showDivider =
                                    index < uiState.fonts.lastIndex // Divider shown for all items except the last
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    StaggeredFadeIn(
                                        index = index,
                                        isSeen = index in seenItems,
                                        onSeen = { seenItems.add(index) }
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

                            }
                            item {
                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }

                }
            }
        }
    }
}
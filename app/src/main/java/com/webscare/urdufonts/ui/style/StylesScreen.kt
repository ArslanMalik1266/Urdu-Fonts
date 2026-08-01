package com.webscare.urdufonts.ui.style

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.OfflineErrorState
import com.webscare.urdufonts.ui.components.SimpleTopAppBar
import com.webscare.urdufonts.ui.util.StaggeredFadeIn
import com.webscare.urdufonts.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylesScreen(
    onCartClick: () -> Unit = {},
    onStyleClick: (StyleItem) -> Unit = {},
    viewModel: StylesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val seenItems = remember { mutableStateSetOf<Int>() }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SimpleTopAppBar(title = "Styles", onCartClick = onCartClick)
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
                    label = "styles_screen_transition"
                ) { state ->
                    when (state) {
                        "loading" -> {
                            StylesSkeleton()
                        }
                        "error" -> {
                            OfflineErrorState(
                                message = uiState.errorMessage!!,
                                onRetry = viewModel::retry
                            )
                        }
                        "content" -> {
                            Box(modifier = Modifier.clipToBounds()) {
                                LazyColumn(
                                    modifier = Modifier.springOverscroll(),
                                    contentPadding = PaddingValues(
                                        start = 18.dp,
                                        end = 18.dp,
                                        top = 16.dp,
                                        bottom = 80.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    itemsIndexed(
                                        items = uiState.styles,
                                        key = { _, style -> style.id }
                                    ) { index, style ->
                                        StaggeredFadeIn(
                                            index = index,
                                            seenItems = seenItems
                                        ) {
                                            StyleItemCard(
                                                style = style,
                                                onClick = { onStyleClick(style) }
                                            )
                                        }

                                        if (index > 0 && (index + 1) % 6 == 0) {
                                            com.webscare.urdufonts.ui.components.ads.ComposableWebsCareNative(
                                                modifier = Modifier.padding(vertical = 8.dp)
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

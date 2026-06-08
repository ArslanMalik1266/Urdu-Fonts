package com.webscare.urdufonts.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webscare.urdufonts.domain.models.FontListItem
import com.webscare.urdufonts.ui.MainViewModel
import com.webscare.urdufonts.ui.components.AppTopBar
import com.webscare.urdufonts.ui.components.BackgroundBlobEffect
import com.webscare.urdufonts.ui.components.BannerCard
import com.webscare.urdufonts.ui.components.CustomSearchBar
import com.webscare.urdufonts.ui.components.FilterButton
import com.webscare.urdufonts.ui.components.FontItemCard
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer Item 1", modifier = Modifier.padding(16.dp))
                Text("Drawer Item 2", modifier = Modifier.padding(16.dp))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Scaffold(
                topBar = {
                    AppTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onCartClick = { }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CustomSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = {},
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explore Fonts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                color = HeadingBlackColor
                            )
                            FilterButton(onClick = { })
                        }
                    }

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.errorMessage!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = viewModel::retry) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.fonts,
                                    key = { item ->
                                        when (item) {
                                            is FontListItem.Font -> item.fontItem.id
                                            is FontListItem.Banner -> item.bannerItem.id
                                        }
                                    }
                                ) { item ->
                                    when (item) {
                                        is FontListItem.Font -> FontItemCard(
                                            fontItem = item.fontItem,
                                            onDownloadClick = {},
                                            modifier = Modifier.padding(horizontal = 16.dp)

                                        )

                                        is FontListItem.Banner -> BannerCard(
                                            bannerItem = item.bannerItem,
                                            modifier = Modifier.fillMaxWidth()
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
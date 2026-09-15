package com.urdufonts.app.ui.subscription

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: SubscriptionViewModel = koinViewModel()
) {
    SubscriptionBottomSheet(
        onDismiss = onBackClick,
        onNavigateToLogin = onNavigateToLogin,
        viewModel = viewModel
    )
}

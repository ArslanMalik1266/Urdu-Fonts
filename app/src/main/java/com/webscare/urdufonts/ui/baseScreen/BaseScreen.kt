package com.webscare.urdufonts.ui.baseScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BaseScreen(
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            bottomBar()
        }
    }
}
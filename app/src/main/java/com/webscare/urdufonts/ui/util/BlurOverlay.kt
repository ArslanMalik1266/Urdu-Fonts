package com.webscare.urdufonts.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.webscare.urdufonts.ui.theme.AppColor

@Composable
fun BlurOverlay(modifier: Modifier) {
    Box(modifier = modifier) {

        // Left circle — green, half off screen top-left
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-120).dp, y = (-200).dp)
                .softBlur(color = Color(0xFFDEF7E9).copy(0.5f), radius = 120f)
        )

        // Right circle — yellow, half off screen top-right
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (170).dp, y = (-200).dp)
                .softBlur(color = Color(0xFFFFF6DE).copy(0.5f), radius = 120f)
        )
    }
}
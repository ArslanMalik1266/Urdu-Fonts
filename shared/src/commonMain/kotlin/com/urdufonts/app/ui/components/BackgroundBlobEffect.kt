package com.urdufonts.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.theme.BackgroundCream
import com.urdufonts.app.ui.theme.BackgroundLight

@Composable
fun BackgroundBlobEffect(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawCircle(
            color = Color.Red.copy(alpha = 0.8f),
            radius = canvasWidth * 0.4f,
            center = Offset(canvasWidth * 0.3f, canvasHeight * -0.05f)
        )

        drawCircle(
            color = Color.Blue.copy(alpha = 0.8f),
            radius = canvasWidth * 0.4f,
            center = Offset(canvasWidth * 0.7f, canvasHeight * -0.05f)
        )
    }
}
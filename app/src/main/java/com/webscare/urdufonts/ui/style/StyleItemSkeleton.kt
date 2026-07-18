package com.webscare.urdufonts.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.webscare.urdufonts.ui.util.ShimmerBox

@Composable
fun StyleItemSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .width(48.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}

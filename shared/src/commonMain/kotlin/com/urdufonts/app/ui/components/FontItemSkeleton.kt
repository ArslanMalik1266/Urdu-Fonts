package com.urdufonts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.util.ShimmerBox

@Composable
fun FontItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = 12.dp,
                    bottom = 24.dp,
                    start = 8.dp,
                    end = 8.dp
                )
        ) {
            // Header Placeholder
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShimmerBox(modifier = Modifier.width(40.dp).height(12.dp).clip(RoundedCornerShape(4.dp)))
                    ShimmerBox(modifier = Modifier.width(40.dp).height(12.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Preview Text Placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(24.dp).clip(RoundedCornerShape(4.dp)))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(4.dp)))
                }
                Spacer(modifier = Modifier.width(16.dp))
                ShimmerBox(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)))
            }
        }
    }
}

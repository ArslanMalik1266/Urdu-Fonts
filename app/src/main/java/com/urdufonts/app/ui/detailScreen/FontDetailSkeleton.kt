package com.urdufonts.app.ui.detailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.util.ShimmerBox
import com.urdufonts.app.ui.util.springOverscroll

@Composable
fun FontDetailSkeleton(
    innerPadding: PaddingValues,
    selectedTab: DetailTab,
    onTabSelected: (DetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color.White)
    ) {
        // Tab Row acts as a solid visual anchor (navigation outline remains visible)
        DetailTabRow(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        Box(modifier = Modifier.clipToBounds()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .springOverscroll()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Preview Card Placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 2. Metadata Chips Placeholder
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(4.dp)))
                ShimmerBox(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)))
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Section Header Placeholder (Preview)
            ShimmerBox(modifier = Modifier.width(70.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Preview Text Field Placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 5. Size Slider & Style Toggles Placeholder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ShimmerBox(modifier = Modifier.width(36.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
                ShimmerBox(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)))
                ShimmerBox(modifier = Modifier.size(32.dp).clip(CircleShape))
                ShimmerBox(modifier = Modifier.size(32.dp).clip(CircleShape))
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 6. Section Header Placeholder (Weights)
            ShimmerBox(modifier = Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(12.dp))

            // 7. Dynamic Weights List Placeholder Rows
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 8. About Section Placeholder
            ShimmerBox(modifier = Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 9. Info Section Placeholder (Table)
            ShimmerBox(modifier = Modifier.width(50.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(120.dp)) // Bottom padding for FAB
        }
    }
}
}

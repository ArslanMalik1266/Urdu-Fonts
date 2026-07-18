package com.webscare.urdufonts.ui.category

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.ColorFilter
import coil.compose.AsyncImage
import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.addPressEffect
import com.webscare.urdufonts.ui.util.softShadow

@Composable
fun CategoryItemCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .addPressEffect {
                onClick()
            }
            .softShadow(
                shadowColor = HeadingBlackColor.copy(0.03f),
                offsetY = (0).dp,
                blurValue = 8.dp,
                borderRadius = 16.dp

            )
            .clip(RoundedCornerShape(16.dp))
            .drawWithCache {
                val glowCenter = Offset(size.width / 2f, size.height / 2f)
                val glowRadius = size.maxDimension * 0.62f
                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color.White,
                        Color.White,
                        Color.White,
                        Color(0xFFEFF9F1),
                        Color(0xFFEFF9F1)
                    ),
                    center = glowCenter,
                    radius = glowRadius
                )
                onDrawBehind {
                    drawRect(brush = brush)
                }
            }
            .border(
                width = 1.dp,
                color = GreyColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = category.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreyColor,
            textAlign = TextAlign.Center,
            fontFamily = NunitoFontFamily,
            lineHeight = 18.sp
        )

        AsyncImage(
            model = category.thumbnailUrl,
            contentDescription = category.title,
            modifier = Modifier.height(32.dp),
            colorFilter = ColorFilter.tint(AppColor)
        )
    }
}
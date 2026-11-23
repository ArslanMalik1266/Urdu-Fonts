package com.urdufonts.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import coil3.compose.LocalPlatformContext
import androidx.compose.runtime.remember
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.compose.AsyncImage
import com.urdufonts.app.domain.models.CategoryItem
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow
import androidx.compose.ui.layout.ContentScale
import com.urdufonts.app.ui.util.figmaDropShadow
import com.urdufonts.app.ui.util.figmaInnerShadow
import com.urdufonts.app.ui.util.bakedFigmaCardBackground

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow

// Define reusable style tokens at the top of your file to avoid magic values
private val CardCornerShape = RoundedCornerShape(16.dp)
private val AmbientGlowColor = Color(0xFFEFF9F1) // Subtle green opacity
private val TranslucentTransitionColor = Color(0xFFF9FDFB)          // Smooth transition step

@Composable
fun CategoryItemCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.urdufonts.app.ui.util.LogItemRender("CategoriesScreen", category.id, category.title)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .addPressEffect {
                onClick()
            }
            // Figma drop shadow + 2 inner shadows + white border, pre-rendered once and shared by all cards
            .bakedFigmaCardBackground(CardCornerShape)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = category.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,                  // Slightly thicker to match design
            color = HeadingBlackColor.copy(alpha = 0.8f), // High contrast charcoal text
            textAlign = TextAlign.Center,
            fontFamily = NunitoFontFamily,
            lineHeight = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val context = LocalPlatformContext.current
        val imageRequest = remember(category.thumbnailUrl) {
            com.urdufonts.app.ui.util.CardImageRequests.category(context, category.thumbnailUrl)
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Main Layer (Green tinted)
            AsyncImage(
                model = imageRequest,
                contentDescription = category.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                colorFilter = ColorFilter.tint(AppColor)
            )
        }
    }
}

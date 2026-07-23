package com.webscare.urdufonts.ui.category

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
import coil.compose.AsyncImage
import com.webscare.urdufonts.domain.models.CategoryItem
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.addPressEffect
import com.webscare.urdufonts.ui.util.softShadow
import androidx.compose.ui.layout.ContentScale
import com.webscare.urdufonts.ui.util.figmaDropShadow
import com.webscare.urdufonts.ui.util.figmaInnerShadow

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .addPressEffect {
                onClick()
            }
            // A. Figma Drop shadow: X=4, Y=4, Blur=14.27, Color=Black (4% Opacity)
            .figmaDropShadow(
                color = Color.Black.copy(alpha = 0.04f),
                offsetX = 4.dp,
                offsetY = 4.dp,
                blur = 14.dp,
                shape = CardCornerShape
            )
            .clip(CardCornerShape)
            // B. Card background base
            .background(Color.White)
            // C. Figma Inner Shadow 1: X=6, Y=6, Blur=19, Color=Black (4% Opacity)
            .figmaInnerShadow(
                color = Color.Black.copy(alpha = 0.04f),
                offsetX = 6.dp,
                offsetY = 6.dp,
                blur = 19.dp,
                shape = CardCornerShape
            )
            // D. Figma Inner Shadow 2: X=6, Y=6, Blur=46, Color=#D5FFCE (32% Opacity)
            .figmaInnerShadow(
                color = Color(0xFFD5FFCE).copy(alpha = 0.32f),
                offsetX = 6.dp,
                offsetY = 6.dp,
                blur = 46.dp,
                shape = CardCornerShape
            )
            // E. Target Border Color
            .border(
                width = 1.dp,
                color = Color.White,
                shape = CardCornerShape
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = category.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,                  // Slightly thicker to match design
            color = HeadingBlackColor.copy(alpha = 0.8f), // High contrast charcoal text
            textAlign = TextAlign.Center,
            fontFamily = NunitoFontFamily,
            lineHeight = 18.sp
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Shadow Layer (Offset, Blurred, Soft dark-tint)
            AsyncImage(
                model = category.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 4.dp)
                    .offset(x = 1.dp, y = 1.5.dp)
                    .blur(2.dp),
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.15f))
            )

            // Main Layer (Green tinted)
            AsyncImage(
                model = category.thumbnailUrl,
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

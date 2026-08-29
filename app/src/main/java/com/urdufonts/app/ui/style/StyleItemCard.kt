package com.urdufonts.app.ui.style

import android.R.attr.fontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.ColorFilter
import coil.compose.AsyncImage
import com.urdufonts.app.domain.models.StyleItem
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer

private val StyleCardShape = RoundedCornerShape(12.dp)
private val StyleCardBackgroundBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFFF9FEF8),
        Color(0xFFEAFBE6)
    )
)

@Composable
fun StyleItemCard(
    style: StyleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.urdufonts.app.ui.util.LogItemRender("StylesScreen", style.id, style.title)

    Box(
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
                borderRadius = 12.dp

            )
            .clip(StyleCardShape)
            .background(brush = StyleCardBackgroundBrush)
            .border(
                width = 1.dp,
                color = GreyColor.copy(alpha = 0.05f),
                shape = StyleCardShape
            )
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = style.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreyColor,
                lineHeight = 20.sp,
                fontFamily = NunitoFontFamily

            )

            val context = androidx.compose.ui.platform.LocalContext.current
            coil.compose.AsyncImage(
                model = androidx.compose.runtime.remember(style.thumbnailUrl) {
                    coil.request.ImageRequest.Builder(context)
                        .data(style.thumbnailUrl)
                        .transformations(com.urdufonts.app.ui.util.TrimTransparentPaddingTransformation())
                        .crossfade(true)
                        .crossfade(300)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build()
                },
                contentDescription = style.title,
                modifier = Modifier.height(36.dp),
                colorFilter = ColorFilter.tint(AppColor)
            )
        }
    }
}
package com.webscare.urdufonts.ui.style

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
import com.webscare.urdufonts.domain.models.StyleItem
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.addPressEffect
import com.webscare.urdufonts.ui.util.softShadow

@Composable
fun StyleItemCard(
    style: StyleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFFF9FEF8),
                        Color(0xFFEAFBE6)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GreyColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
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
                fontWeight = FontWeight.Bold,
                color = GreyColor,
                lineHeight = 18.sp,
                fontFamily = NunitoFontFamily

            )

            AsyncImage(
                model = style.thumbnailUrl,
                contentDescription = "",
                modifier = Modifier.height(40.dp),
                colorFilter = ColorFilter.tint(AppColor)
            )
        }
    }
}
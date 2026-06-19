package com.webscare.urdufonts.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.domain.models.StyleItem
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.util.addPressEffect
import com.webscare.urdufonts.ui.util.softShadow

@Composable
fun StyleItemCard(
    style: StyleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .addPressEffect{
                onClick()
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                        Color(0xFF57C073).copy(alpha = 0.1f),
                        Color(0xFF57C073).copy(alpha = 0.2f)

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
                fontWeight = FontWeight.Medium,
                color = GreyColor,
                lineHeight = 18.sp
            )

            Text(
                text = style.urduText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor
            )
        }
    }
}
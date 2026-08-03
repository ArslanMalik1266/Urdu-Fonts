package com.urdufonts.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.theme.YellowColor
import com.urdufonts.app.ui.util.addPressEffect

@Composable
fun FilterButton(
    onClick: () -> Unit,
    isFilterActive: Boolean = false,   // ✅ ADD
    modifier: Modifier = Modifier
) {
    // ✅ Active hone pe yellow, warna white
    val bgColor = if (isFilterActive) YellowColor.copy(alpha = 0.8f) else Color.White
    val iconTint = if (isFilterActive) HeadingBlackColor else GreyColor.copy(alpha = 0.4f)
    val textColor = if (isFilterActive) HeadingBlackColor else GreyColor
    val borderColor = if (isFilterActive) YellowColor.copy(alpha = 0.5f) else GreyColor.copy(alpha = 0.2f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .addPressEffect(onClick = onClick)
            .clip(CircleShape)
            .border(BorderStroke(0.5.dp, borderColor), CircleShape)  // ✅ border bhi change
            .background(bgColor)                                       // ✅ bg change
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = "Filter",
                tint = iconTint,   // ✅ icon tint change
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFilterActive) "Filtered" else "Filters",  // ✅ text bhi update (optional)
                fontSize = 12.sp,
                fontWeight = if (isFilterActive) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 16.sp,
                color = textColor,   // ✅ text color change
                fontFamily = NunitoFontFamily
            )
        }
    }
}

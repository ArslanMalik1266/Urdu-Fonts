package com.webscare.urdufonts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily

@Composable
fun AnimatedDownloadButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 56.dp,
        animationSpec = tween(durationMillis = 400),
        label = "fab_width"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 28.dp,
        animationSpec = tween(durationMillis = 400),
        label = "fab_corner"
    )

    // When collapsed, offset right by half of (280-56)/2 = 112dp to appear at end
    val offsetX by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 112.dp,
        animationSpec = tween(durationMillis = 400),
        label = "fab_offset"
    )
    val showText = width > 220.dp
    Box(
        modifier = modifier
            .offset(x = offsetX)           // ✅ slides from end to center
            .width(width)
            .height(56.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
            if (showText) {
                Text(
                    text = "Download",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    fontFamily = NunitoFontFamily
                )
            }

        }
    }
}
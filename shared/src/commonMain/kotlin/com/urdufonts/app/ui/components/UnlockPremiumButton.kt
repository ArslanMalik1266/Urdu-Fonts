package com.urdufonts.app.ui.components

import com.urdufonts.app.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect

/**
 * Replaces the Download button on premium fonts the user hasn't unlocked. Same size, colour and
 * shrink-on-scroll behaviour as [AnimatedDownloadButton], so the page layout doesn't change.
 */
@Composable
fun UnlockPremiumButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = com.urdufonts.app.ui.util.screenWidthDp()
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 56.dp,
        animationSpec = tween(durationMillis = 400),
        label = "unlock_width"
    )
    val offsetX by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else (screenWidth / 2) - 44.dp,
        animationSpec = tween(durationMillis = 400),
        label = "unlock_offset"
    )

    Row(
        modifier = modifier
            .offset(x = offsetX)
            .width(width)
            .height(56.dp)
            .addPressEffect { onClick() }
            .clip(RoundedCornerShape(28.dp))
            .background(AppColor),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_diamond),
            contentDescription = "Unlock Premium",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = "Unlock Premium",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = NunitoFontFamily,
                maxLines = 1
            )
        }
    }
}

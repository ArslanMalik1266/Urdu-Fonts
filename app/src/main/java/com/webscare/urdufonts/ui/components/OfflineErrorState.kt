package com.webscare.urdufonts.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily

@Composable
fun OfflineErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNoInternet = message.contains("internet", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true) ||
            message.contains("hostname", ignoreCase = true) ||
            message.contains("resolve", ignoreCase = true)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Outer ring
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                // Inner ring with pulse
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AppColor.copy(alpha = 0.12f))
                        .alpha(if (isNoInternet) pulseAlpha else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isNoInternet) R.drawable.ic_no_wifi
                            else R.drawable.ic_error
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(AppColor),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isNoInternet) "You're Offline" else "Something Went Wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isNoInternet)
                    "Check your connection and tap\nretry to continue."
                else message,
                fontSize = 14.sp,
                color = GreyColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 40.dp),
                fontFamily = NunitoFontFamily
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRetry
                    )
                    .padding(horizontal = 36.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontFamily = NunitoFontFamily
                )
            }
        }
    }
}
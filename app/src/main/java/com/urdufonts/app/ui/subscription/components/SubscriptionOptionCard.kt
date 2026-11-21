package com.urdufonts.app.ui.subscription.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.SubscriptionOption
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.theme.YellowColor
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow

@Composable
fun SubscriptionOptionCard(
    option: SubscriptionOption,
    isSelected: Boolean,
    isCurrentPlan: Boolean,
    isPendingTargetPlan: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showTopBadge = isCurrentPlan || option.isMostPopular || isPendingTargetPlan
    val displayPrice = remember(option.priceText) { option.priceText.removeSuffix(".00") }
    val cardShape = RoundedCornerShape(Dimens.cornerLarge)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                shadowColor = HeadingBlackColor.copy(alpha = 0.04f),
                borderRadius = Dimens.cornerLarge,
                blurValue = 12.dp,
                offsetY = 2.dp
            )
            .addPressEffect(onClick = onClick)
            .clip(cardShape)
            .background(Color.White)
            .then(
                if (isSelected) Modifier.border(1.5.dp, DarkGreen, cardShape) else Modifier
            )
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.space12, vertical = Dimens.space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) DarkGreen else Color(0xFFD1D5DB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DarkGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.space12))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Dimens.cornerMedium))
                    .background(Color(0xFFEFF7F2)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = option.iconRes),
                    contentDescription = option.title,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(DarkGreen)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.space12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor
                )
                Text(
                    text = option.billingPeriodText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = GreyColor
                )
            }

            Spacer(modifier = Modifier.width(Dimens.space8))

            Column(horizontalAlignment = Alignment.End) {
                if (showTopBadge) {
                    Spacer(modifier = Modifier.height(Dimens.space8))
                }
                Text(
                    text = displayPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor,
                    lineHeight = 16.sp
                )
                option.pricePeriodSubtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoFontFamily,
                        color = GreyColor,
                        lineHeight = 12.sp
                    )
                }
                option.discountTag?.let { discountTag ->

                    Spacer(modifier = Modifier.height(Dimens.space4))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.space4))
                            .background(Color(0xFFE2F3E7))
                            .padding(horizontal = Dimens.space8, vertical = 2.dp)
                    ) {
                        Text(
                            text = discountTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = DarkGreen
                        )
                    }
                }
            }
        }

      }

        if (showTopBadge) {
            OptionTopBadge(
                isPendingTargetPlan = isPendingTargetPlan,
                isCurrentPlan = isCurrentPlan
            )
        }
    }
}

@Composable
private fun BoxScope.OptionTopBadge(
    isPendingTargetPlan: Boolean,
    isCurrentPlan: Boolean
) {
    val badgeText = when {
        isPendingTargetPlan -> stringResource(id = R.string.subscription_downgrade_scheduled)
        isCurrentPlan -> stringResource(id = R.string.subscription_current_plan)
        else -> stringResource(id = R.string.subscription_most_popular)
    }
    val badgeBgColor = when {
        isPendingTargetPlan -> Color(0xFFD97706)
        isCurrentPlan -> YellowColor
        else -> DarkGreen
    }
    val badgeTextColor = when {
        isPendingTargetPlan -> Color.White
        isCurrentPlan -> HeadingBlackColor
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .clip(RoundedCornerShape(topEnd = Dimens.cornerLarge, bottomStart = Dimens.cornerMedium))
            .background(badgeBgColor)
            .padding(horizontal = Dimens.space12, vertical = Dimens.space4)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isCurrentPlan && !isPendingTargetPlan) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_premium),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.space4))
            }
            Text(
                text = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = badgeTextColor
            )
        }
    }
}

@Composable
fun PlanFeatureRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "✓",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = NunitoFontFamily,
            color = HeadingBlackColor.copy(alpha = 0.8f)
        )
    }
}

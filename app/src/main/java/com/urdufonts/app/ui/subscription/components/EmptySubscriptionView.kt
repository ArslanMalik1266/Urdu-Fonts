package com.urdufonts.app.ui.subscription.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow

@Composable
fun EmptySubscriptionView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.space24, vertical = Dimens.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.subscription_no_plans_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = HeadingBlackColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.space8))
        Text(
            text = stringResource(id = R.string.subscription_no_plans_desc),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = NunitoFontFamily,
            color = GreyColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.space24))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(44.dp)
                .addPressEffect(onClick = onRetry)
                .softShadow(
                    shadowColor = DarkGreen.copy(alpha = 0.2f),
                    borderRadius = Dimens.cornerLarge,
                    blurValue = 10.dp,
                    offsetY = 0.dp
                )
                .clip(RoundedCornerShape(Dimens.cornerPill))
                .background(DarkGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.action_retry),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = Color.White
            )
        }
    }
}

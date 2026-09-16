package com.urdufonts.app.ui.subscription.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow

@Composable
fun SubscriptionFooter(
    buttonText: String,
    onActionClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonBgColor = if (enabled) DarkGreen else DarkGreen.copy(alpha = 0.5f)
    val buttonContentAlpha = if (enabled) 1f else 0.85f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main CTA Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .then(
                    if (enabled) {
                        Modifier
                            .addPressEffect(onClick = onActionClick)
                            .softShadow(
                                shadowColor = DarkGreen.copy(alpha = 0.25f),
                                borderRadius = Dimens.cornerLarge,
                                blurValue = 14.dp,
                                offsetY = 4.dp
                            )
                    } else Modifier
                )
                .clip(RoundedCornerShape(Dimens.cornerPill))
                .background(buttonBgColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_premium),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = buttonContentAlpha),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.space8))
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White.copy(alpha = buttonContentAlpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.space12))

        // Secure Payment & Restore Purchases Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pass),
                    contentDescription = stringResource(id = R.string.subscription_secure_payment),
                    tint = GreyColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.space4))
                Text(
                    text = stringResource(id = R.string.subscription_secure_payment),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = NunitoFontFamily,
                    color = GreyColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.space16))
            Text(
                text = "•",
                fontSize = 12.sp,
                color = GreyColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(Dimens.space16))

            Text(
                text = stringResource(id = R.string.subscription_restore_purchases),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = DarkGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.space4))
                    .clickable { onRestoreClick() }
                    .padding(Dimens.space4)
            )
        }
    }
}

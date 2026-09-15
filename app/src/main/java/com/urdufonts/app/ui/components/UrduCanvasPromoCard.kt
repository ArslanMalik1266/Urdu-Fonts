package com.urdufonts.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.theme.PromoCardBgDark
import com.urdufonts.app.ui.theme.PromoCardBgLight
import com.urdufonts.app.ui.theme.PromoCardBorderDark
import com.urdufonts.app.ui.theme.PromoCardBorderLight
import com.urdufonts.app.ui.theme.PromoCardButtonBg
import com.urdufonts.app.ui.theme.PromoCardButtonText
import com.urdufonts.app.ui.theme.PromoCardSubtitleDark
import com.urdufonts.app.ui.theme.PromoCardSubtitleLight
import com.urdufonts.app.ui.theme.PromoCardTitleDark
import com.urdufonts.app.ui.theme.PromoCardTitleLight
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.diagonalShineEffect

@Composable
fun UrduCanvasPromoCard(
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val cardBg = if (isDarkTheme) PromoCardBgDark else PromoCardBgLight
    val cardBorder = if (isDarkTheme) PromoCardBorderDark else PromoCardBorderLight
    val titleColor = if (isDarkTheme) PromoCardTitleDark else PromoCardTitleLight
    val subtitleColor = if (isDarkTheme) PromoCardSubtitleDark else PromoCardSubtitleLight

    val cardShape = RoundedCornerShape(Dimens.cornerLarge)
    val iconShape = RoundedCornerShape(Dimens.cornerMedium)
    val buttonShape = RoundedCornerShape(Dimens.cornerPill)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardBg)
            .border(BorderStroke(1.dp, cardBorder), cardShape)
            .diagonalShineEffect()
            .padding(
                start = Dimens.space12,
                top = Dimens.space12,
                end = Dimens.space12,
                bottom = Dimens.space12
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.space12)
        ) {
            // Urdu Canvas App Icon
            Image(
                painter = painterResource(id = R.drawable.ic_urdu_canvas),
                contentDescription = stringResource(id = R.string.urdu_canvas_app_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(iconShape)
            )

            // Text Titles
            Column(
                modifier = Modifier.weight(1f),

            ) {
                Text(
                    text = stringResource(id = R.string.promo_urdu_canvas_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    fontFamily = NunitoFontFamily,
                    lineHeight = 14.sp
                )
                Text(
                    text = stringResource(id = R.string.promo_urdu_canvas_subtitle),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = subtitleColor,
                    fontFamily = NunitoFontFamily,
                    lineHeight = 12.sp
                )
            }

            // Open App Button Pill
            Box(
                modifier = Modifier
                    .addPressEffect { onOpenApp() }
                    .clip(buttonShape)
                    .background(PromoCardButtonBg)
                    .padding(
                        start = Dimens.space12,
                        top = Dimens.space8,
                        end = Dimens.space8,
                        bottom = Dimens.space8
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.space4)
                ) {
                    Text(
                        text = stringResource(id = R.string.promo_open_app),
                        color = PromoCardButtonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = PromoCardButtonText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


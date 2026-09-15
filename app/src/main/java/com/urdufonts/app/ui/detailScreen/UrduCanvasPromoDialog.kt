package com.urdufonts.app.ui.detailScreen

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.Dimens
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.theme.PromoCardBorderLight
import com.urdufonts.app.ui.util.addPressEffect

@Composable
fun UrduCanvasPromoDialog(
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { /* Dismiss disabled on outside click / back press */ },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val isAndroid12OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        // Full-screen translucent white scrim (underlying screen handles background blur on API 31+)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isAndroid12OrAbove) Color.White.copy(alpha = 0.40f)
                    else Color.White.copy(alpha = 0.85f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Flat Dialog Card (no shadow / elevation)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.space24)
                    .clip(RoundedCornerShape(Dimens.cornerLarge))
                    .background(Color.White)
                    .border(
                        BorderStroke(1.dp, PromoCardBorderLight),
                        RoundedCornerShape(Dimens.cornerLarge)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks inside dialog card */ }
                    .padding(Dimens.space16)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row: Title & Close Cross Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.promo_dialog_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingBlackColor,
                            fontFamily = NunitoFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .addPressEffect { onDismiss() }
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = stringResource(id = R.string.cd_close_dialog),
                                tint = GreyColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.space16))

                    // Urdu Canvas Promo Image (Clickable with press effect)
                    Image(
                        painter = painterResource(id = R.drawable.ic_urdu_canvas_promo),
                        contentDescription = stringResource(id = R.string.cd_urdu_canvas_promo_image),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .addPressEffect { onOpenApp() }
                            .clip(RoundedCornerShape(Dimens.cornerMedium))
                    )

                    Spacer(modifier = Modifier.height(Dimens.space16))

                    // Action Buttons: Outlined "Open App" (weight 1f) and Compact Filled "Skip"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.space12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open App Button (Outlined)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .addPressEffect { onOpenApp() }
                                .clip(RoundedCornerShape(Dimens.cornerPill))
                                .border(
                                    BorderStroke(1.dp, AppColor),
                                    RoundedCornerShape(Dimens.cornerPill)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.promo_open_app),
                                color = AppColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily
                            )
                        }

                        // Skip Button (Compact Filled)
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .addPressEffect { onDismiss() }
                                .clip(RoundedCornerShape(Dimens.cornerPill))
                                .background(AppColor)
                                .padding(horizontal = Dimens.space24),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.action_skip),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}


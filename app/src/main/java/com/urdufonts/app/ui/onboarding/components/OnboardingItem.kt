package com.urdufonts.app.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.onboarding.model.OnboardingPage
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

@Composable
fun OnboardingItem(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            modifier = Modifier.weight(0.2f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = page.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = HeadingBlackColor,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center,
                fontFamily = NunitoFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.subtitle,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = GreyColor,
                lineHeight = 24.sp,
                fontFamily = NunitoFontFamily
            )
        }
    }
}
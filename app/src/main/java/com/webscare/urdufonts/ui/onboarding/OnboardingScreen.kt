package com.webscare.urdufonts.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.ui.onboarding.components.CustomPageIndicator
import com.webscare.urdufonts.ui.onboarding.components.OnboardingItem
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.util.addPressEffect
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit
) {
    val pages = viewModel.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(0.75f)
        ) { position ->
            OnboardingItem(page = pages[position])
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f),
            verticalArrangement = Arrangement.Top
        ) {
            CustomPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.15f)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .addPressEffect { viewModel.onSkipClicked(onNavigateToHome) }
                    .clip(RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Skip",
                    color = GreyColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .addPressEffect {
                        scope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                viewModel.onContinueClicked(
                                    pagerState.currentPage,
                                    onNavigateToHome
                                )
                            }
                        }
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Continue",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
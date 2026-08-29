package com.urdufonts.app.ui.onboarding

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
import com.urdufonts.app.ui.onboarding.components.CustomPageIndicator
import com.urdufonts.app.ui.onboarding.components.OnboardingItem
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.GreyColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import kotlinx.coroutines.launch

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit
) {
    val pages = viewModel.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { position ->
            val pageOffset = (pagerState.currentPage - position) + pagerState.currentPageOffsetFraction
            OnboardingItem(
                page = pages[position],
                pageOffset = pageOffset
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .addPressEffect { viewModel.onSkipClicked(onNavigateToHome) }
                        .clip(RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Skip",
                        color = GreyColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = NunitoFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .addPressEffect {
                            scope.launch {
                                if (pagerState.currentPage < pages.size - 1) {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage + 1,
                                        animationSpec = tween(
                                            durationMillis = 650,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
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
                        fontSize = 16.sp,
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }
    }
}
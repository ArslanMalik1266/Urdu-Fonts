package com.urdufonts.app.ui.detailScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.components.TopBarButton
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                TopBarButton(
                    iconRes = R.drawable.ic_back,
                    onClick = { onBackClick() },
                    contentDescription = "ic_back"
                )
            }
        },
        actions = {
//            Box(modifier = Modifier.padding(end = 8.dp)) {
//                TopBarButton(
//                    iconRes = R.drawable.ic_share,
//                    onClick = { onShareClick() },
//                    contentDescription = "ic_share"
//                )
//            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        modifier = modifier
    )
}
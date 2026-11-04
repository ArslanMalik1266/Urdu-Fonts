package com.urdufonts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.R
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.NunitoFontFamily

/** Small "Premium" pill shown next to a premium font's name, using the same diamond as the top bar. */
@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(AppColor.copy(alpha = 0.1f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_diamond),
            contentDescription = null,
            tint = AppColor,
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = "Premium",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor,
            fontFamily = NunitoFontFamily
        )
    }
}

package com.urdufonts.app.ui.subscription.components

import com.urdufonts.app.resources.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urdufonts.app.ui.theme.NunitoFontFamily

@Composable
fun PendingChangeBanner(
    pendingPlanName: String,
    effectiveAt: String? = null,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(effectiveAt) {
        if (!effectiveAt.isNullOrBlank()) {
            com.urdufonts.app.ui.util.formatIsoDay(effectiveAt) ?: effectiveAt
        } else null
    }

    val dateSuffix = if (!formattedDate.isNullOrBlank()) "on $formattedDate" else "on next billing date"

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFFFBEB),
        border = BorderStroke(0.8.dp, Color(0xFFF59E0B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_pass),
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your plan changes to $pendingPlanName $dateSuffix",
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = NunitoFontFamily,
                color = Color(0xFF92400E)
            )
        }
    }
}

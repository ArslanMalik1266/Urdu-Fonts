package com.urdufonts.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.softShadow

@Composable
fun TopBarButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 16
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .addPressEffect(onClick = onClick)
            .softShadow(
                shadowColor = HeadingBlackColor.copy(0.05f),
                offsetY = (0).dp,
                blurValue = 8.dp,
                borderRadius = 999.dp

            )
            .clip(CircleShape)
            .background(Color.White)
            .semantics { role = Role.Button }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}
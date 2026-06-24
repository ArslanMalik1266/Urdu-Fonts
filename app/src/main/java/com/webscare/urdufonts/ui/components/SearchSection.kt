package com.webscare.urdufonts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.softShadow

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .softShadow(
                shadowColor = HeadingBlackColor.copy(0.02f),
                offsetY = (0).dp,
                blurValue = 8.dp,
                borderRadius = 999.dp

            )
            .clip(CircleShape)
            .background(Color.White)
            .border(0.5.dp, GreyColor.copy(alpha = 0.2f), CircleShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        ) { innerTextField ->
            if (query.isEmpty()) {
                Text(
                    "Search",
                    color = GreyColor.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = NunitoFontFamily
                )
            }
            innerTextField()
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search Icon",
            tint = GreyColor.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}
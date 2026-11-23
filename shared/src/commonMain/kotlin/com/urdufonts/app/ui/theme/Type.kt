package com.urdufonts.app.ui.theme

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import com.urdufonts.app.resources.*
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with

/** Nunito from the shared resources. Compose Multiplatform loads fonts inside composition. */
val NunitoFontFamily: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_semi_bold, FontWeight.SemiBold),
    Font(Res.font.nunito_medium, FontWeight.Medium),
)

val Typography: Typography
    @Composable get() = Typography(
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
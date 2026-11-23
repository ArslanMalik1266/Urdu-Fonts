package com.urdufonts.app.ui.theme

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun UrduFontsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = (if (dynamicColor) com.urdufonts.app.ui.platform.dynamicColorScheme(darkTheme) else null)
        ?: if (darkTheme) DarkColorScheme else LightColorScheme

    // The phone's font-size setting must not change the app's text (layouts are designed at 1x)
    val density = LocalDensity.current
    val fixedDensity = Density(density = density.density, fontScale = 1f)

    // No platform stretch / glow: scroll containers carry Modifier.springOverscroll() instead
    CompositionLocalProvider(
        LocalDensity provides fixedDensity,
        LocalOverscrollFactory provides null
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
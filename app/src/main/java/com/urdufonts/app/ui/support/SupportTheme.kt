package com.urdufonts.app.ui.support

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.urdufonts.app.ui.support.kit.LocalAppSnackbar

/** UrduFonts' green look for the Help & Support screens (the rest of the app styles its screens by hand). */
private val SupportColors = lightColorScheme(
    primary = Color(0xFF18A34B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9F6EE),
    onPrimaryContainer = Color(0xFF0F7A36),
    secondaryContainer = Color(0xFFEEF1EE),
    onSecondaryContainer = Color(0xFF4A524C),
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF1E1E1E),
    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFEEF1EE),
    onSurfaceVariant = Color(0xFF6B736D),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    outline = Color(0xFFB4BAB5),
    outlineVariant = Color(0xFFE7EAE7),
    error = Color(0xFFE53935),
    onError = Color.White,
)

private val SupportShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

/** Wraps a support screen in its colours and gives it the snackbar the screens show their messages in. */
@Composable
fun SupportTheme(content: @Composable () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    // The support screens lift themselves above the keyboard (imePadding). The rest of the app lets the window
    // pan instead, which would lift them a second time: while a support screen shows, the window only resizes
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    androidx.compose.runtime.DisposableEffect(activity) {
        val window = activity?.window
        val before = window?.attributes?.softInputMode
        @Suppress("DEPRECATION")
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose { if (before != null) window.setSoftInputMode(before) }
    }
    MaterialTheme(
        colorScheme = SupportColors,
        typography = MaterialTheme.typography,
        shapes = SupportShapes,
    ) {
        CompositionLocalProvider(LocalAppSnackbar provides snackbar) {
            Box(Modifier.fillMaxSize()) {
                content()
                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 80.dp),
                )
            }
        }
    }
}

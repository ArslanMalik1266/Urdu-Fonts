package com.urdufonts.app.ui.support.kit

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

/** The snackbar the support screens show their short messages with. SupportScreens provides it. */
val LocalAppSnackbar = compositionLocalOf<SnackbarHostState> { error("SupportTheme provides the snackbar") }

package com.urdufonts.app.ui.support.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import com.urdufonts.app.ui.support.kit.Dimens

/**
 * The one bottom sheet of the app: song menus, create playlist, pickers, tool sheets. Every sheet has
 * the same plain surface, no washes or gradients. Only the Premium sheet passes a [header]: drawn edge to
 * edge from the very top (it draws its own [SheetHandle]), above the padded content.
 * A close button inside a sheet calls [LocalSheetClose], so the sheet slides down before it is removed.
 * An action that changes what is behind the sheet uses [LocalSheetCloseThen]: changing it during the slide
 * re-measures the sheet, which then snaps away instead of sliding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss by rememberUpdatedState(onDismiss)
    val closeThen: (() -> Unit) -> Unit = { after ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                dismiss()
                after()
            }
        }
    }
    val close: () -> Unit = { closeThen {} }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = if (header == null) ({ BottomSheetDefaults.DragHandle() }) else null,
    ) {
        CompositionLocalProvider(LocalSheetClose provides close, LocalSheetCloseThen provides closeThen) {
            header?.invoke()
            Column(
                modifier = modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space8),
                content = content,
            )
        }
    }
}

/** Slides the enclosing [AppBottomSheet] down, then calls its onDismiss. Outside a sheet it does nothing. */
val LocalSheetClose = staticCompositionLocalOf<() -> Unit> { {} }

/** Slides the enclosing [AppBottomSheet] down, calls its onDismiss, then runs the action. Outside a sheet it runs the action at once. */
val LocalSheetCloseThen = staticCompositionLocalOf<(() -> Unit) -> Unit> { { it() } }

/** The grab handle, for a sheet header that draws its own top. */
@Composable
fun SheetHandle(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = Dimens.Space12), contentAlignment = Alignment.Center) {
        Box(Modifier.size(Dimens.SheetHandleWidth, Dimens.SheetHandleHeight).clip(CircleShape).background(color))
    }
}

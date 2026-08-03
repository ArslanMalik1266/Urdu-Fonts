package com.urdufonts.app.ui.baseScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics

/**
 * bottomBarVisible controls whether the bar is shown — WITHOUT ever removing it
 * from composition. MyBottomNavigationBar/CurvedNavBar hold animation state
 * (cradle position, last-selected tab) in `remember {}`. If this composable's
 * `bottomBar` slot were ever wrapped in `if (visible) { bottomBar() }` at the
 * call site, Compose would dispose that subtree when visible flips to false and
 * create a brand-new instance — with fresh, reset `remember` state — when it
 * flips back to true. That was the original cause of the indicator visibly
 * "sliding in from the left" on returning from a detail screen.
 *
 * HOW HIDING WORKS — two mistakes from earlier drafts, corrected here:
 *
 * Mistake 1: hiding with graphicsLayer{alpha=0f} alone, then trying to block
 * touches with `Modifier.pointerInput(Unit) { awaitCancellation() }`. That
 * doesn't make the area non-interactive — it makes it an active touch SINK.
 * pointerInput's gesture detector still occupies the full layout bounds and
 * intercepts every touch that lands there; it just never resolves a gesture
 * from them. Events get swallowed, not passed through, so the real screen
 * underneath (whatever UI a detail screen has in that same bottom strip)
 * silently stops receiving taps. That produced exactly the "dead clickable
 * area" bug.
 *
 * Mistake 2 (caught before shipping): the right way to make an always-composed
 * element untouchable is to give it zero size — a zero-size element can't be
 * hit-tested, so touches fall through naturally with no special-case swallowing
 * logic. But collapsing the size based on the INSTANT `bottomBarVisible`
 * boolean — the same instant signal driving the animated alpha/translation —
 * means the hit-test area (and a same-layer clip) would vanish at t=0, before
 * the 180ms fade/slide animation is visually finished. The fade would never
 * actually be seen; the bar would just disappear immediately either way.
 *
 * Fix: collapse the reported size only once the fade-out has actually
 * completed (`hasFullyFadedOut`, set from the alpha animation's finishedListener),
 * not from the raw boolean. While the fade is still playing, the bar keeps its
 * real size — visible and (briefly) still technically hit-testable, but by the
 * time a user could process "the bar is fading" and try to tap through it,
 * the 180ms animation has already finished and the area has collapsed.
 */
@Composable
fun BaseScreen(
    bottomBarVisible: Boolean = true,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var hasFullyFadedOut by remember { mutableStateOf(!bottomBarVisible) }

    val barAlpha by animateFloatAsState(
        targetValue = if (bottomBarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "bottom_bar_alpha",
        finishedListener = { endValue ->
            // Only collapse hit-testing once we've actually finished animating
            // TO hidden. If endValue is 1f (animating back to visible), make
            // sure we're not still marked collapsed.
            hasFullyFadedOut = endValue == 0f
        }
    )
    // Small slide so the bar tucks away rather than just fading in place —
    // purely cosmetic, doesn't touch any of CurvedNavBar's own animation state.
    val barTranslationYPx by animateFloatAsState(
        targetValue = if (bottomBarVisible) 0f else 24f,
        animationSpec = tween(durationMillis = 180),
        label = "bottom_bar_translation"
    )

    // The moment bottomBarVisible flips back to true, immediately un-collapse —
    // don't wait for the enter animation to finish. We want the bar tappable
    // again as soon as it starts becoming visible, not 180ms later.
    if (bottomBarVisible && hasFullyFadedOut) {
        hasFullyFadedOut = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        content()

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    alpha = barAlpha
                    translationY = barTranslationYPx
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val reportedWidth = if (hasFullyFadedOut) 0 else placeable.width
                    val reportedHeight = if (hasFullyFadedOut) 0 else placeable.height
                    layout(reportedWidth, reportedHeight) {
                        placeable.place(0, 0)
                    }
                }
                .then(
                    if (!hasFullyFadedOut) Modifier
                    else Modifier.semantics { invisibleToUser() }
                )
        ) {
            bottomBar()
        }
    }
}
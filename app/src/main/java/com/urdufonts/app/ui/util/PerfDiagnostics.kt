package com.urdufonts.app.ui.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

object PerfDiagnostics {
    private const val TAG = "PerfDiagnostics"
    private var tabTapTime = 0L

    fun logTabTap(targetRoute: String) {
        tabTapTime = System.currentTimeMillis()
        Log.d(TAG, "==================================================")
        Log.d(TAG, "👆 [1. TAB TAP] User clicked tab -> '$targetRoute'")
    }

    fun logNavStart(targetRoute: String) {
        val delta = if (tabTapTime > 0) "${System.currentTimeMillis() - tabTapTime}ms after tap" else "direct"
        Log.d(TAG, "🔀 [2. NAV START] Navigating to '$targetRoute' | ($delta)")
    }

    fun logScreenStart(screenName: String) {
        val delta = if (tabTapTime > 0) "${System.currentTimeMillis() - tabTapTime}ms after tap" else "direct"
        Log.d(TAG, "🎬 [3. SCREEN ENTER] $screenName composable executing | ($delta)")
    }

    fun logScreenTransition(screenName: String, event: String) {
        val now = System.currentTimeMillis()
        val delta = if (tabTapTime > 0) "${now - tabTapTime}ms TOTAL since TAB TAP" else "initial"
        Log.d(TAG, "🚀 [4. CONTENT RENDERED] Screen: $screenName | Event: $event | ($delta)")
    }

    fun logScrollEvent(screenName: String, firstVisibleIndex: Int, totalItems: Int) {
        Log.d(TAG, "📜 [Scroll Event] $screenName | FirstVisibleIndex: $firstVisibleIndex / $totalItems")
    }

    fun logImageLoad(url: String, event: String, durationMs: Long = 0) {
        val urlSnippet = url.takeLast(35)
        if (durationMs > 0) {
            Log.d(TAG, "🖼️ [Image Load] URL: ...$urlSnippet | Event: $event | Took: ${durationMs}ms")
        } else {
            Log.d(TAG, "🖼️ [Image Load] URL: ...$urlSnippet | Event: $event")
        }
    }
}

@Composable
fun LogScreenEntry(screenName: String) {
    SideEffect {
        PerfDiagnostics.logScreenStart(screenName)
    }
}

@Composable
fun LogItemRender(screenName: String, itemIndex: Int, itemLabel: String) {
    SideEffect {
        Log.d("PerfDiagnostics", "🎨 [Item Render] Screen: $screenName | Item #$itemIndex: '$itemLabel' composed")
    }
}

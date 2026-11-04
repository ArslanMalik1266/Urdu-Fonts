package com.urdufonts.app.ui.components.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.data.local.UserPreferences
import com.webscare.ads.NativeSize
import com.webscare.ads.WebsCareAds
import org.koin.compose.koinInject

private const val TAG = "WebsCareAdsLog"

/** Space held for a native ad that is still loading, until one of that placement has loaded and been measured. */
private fun defaultReservedHeight(size: NativeSize) = when (size) {
    NativeSize.SMALL -> 120.dp
    else -> 250.dp
}

/**
 * Where native ads go in a list: after the first [first] items, then after every [every] items.
 * [itemCount] is how many items come before the candidate slot (row index + 1).
 */
fun isNativeAdSlot(itemCount: Int, first: Int, every: Int): Boolean =
    itemCount == first || (itemCount > first && (itemCount - first) % every == 0)

/**
 * Native ad slot inside a lazy list.
 *
 * - [slot] identifies the position (e.g. the row index). The loaded ad view is kept per placement + slot
 *   for as long as the activity lives, so scrolling a row away and back re-attaches the same ad instead
 *   of requesting a new one (WebsCareAds: don't recreate ad views to manufacture refreshes).
 * - While the ad loads, its height is reserved so the list doesn't jump when it arrives; the slot only
 *   collapses if the ad really fails (no fill / offline).
 */
@Composable
fun ComposableWebsCareNative(
    placement: String,
    slot: Any,
    modifier: Modifier = Modifier,
    nativeSize: NativeSize = NativeSize.MEDIUM,
    userPreferences: UserPreferences = koinInject()
) {
    val isProUser by userPreferences.isProUser.collectAsStateWithLifecycle(initialValue = false)
    if (isProUser) return

    val activity = LocalContext.current as? Activity ?: return
    val density = LocalDensity.current
    val key = "$placement#$slot"
    val entry = remember(key) { NativeAdViewCache.getOrCreate(activity, key, placement, nativeSize) }
    var state by remember(key) { mutableStateOf(entry.state) }
    entry.onStateChanged = { state = it }

    if (state == NativeSlotState.FAILED) return

    val reserved = NativeAdViewCache.measuredHeightPx[placement]
        ?.let { with(density) { it.toDp() } } ?: defaultReservedHeight(nativeSize)

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .then(if (state == NativeSlotState.LOADING) Modifier.heightIn(min = reserved) else Modifier)
            .wrapContentHeight(),
        factory = {
            // A cached view may still be attached to the previous (disposed) row's container
            (entry.container.parent as? ViewGroup)?.removeView(entry.container)
            entry.container
        }
    )
}

internal enum class NativeSlotState { LOADING, LOADED, FAILED }

internal class NativeSlot(val container: FrameLayout) {
    var state: NativeSlotState = NativeSlotState.LOADING
        set(value) {
            field = value
            onStateChanged?.invoke(value)
        }
    var onStateChanged: ((NativeSlotState) -> Unit)? = null
}

/** Loaded native ad views per activity, keyed by placement + slot; dropped when the activity is destroyed. */
internal object NativeAdViewCache {
    private val slots = HashMap<String, NativeSlot>()
    private var owner: Activity? = null
    private var callbacksRegistered = false

    /** Last measured height per placement, used to reserve space for the next slot of that placement. */
    val measuredHeightPx = HashMap<String, Int>()

    fun getOrCreate(activity: Activity, key: String, placement: String, size: NativeSize): NativeSlot {
        if (owner !== activity) {
            clear()
            owner = activity
            registerCleanup(activity.application)
        }
        return slots.getOrPut(key) { createSlot(activity, placement, size) }
    }

    private fun createSlot(activity: Activity, placement: String, size: NativeSize): NativeSlot {
        val container = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val slot = NativeSlot(container)
        container.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                slot.state = NativeSlotState.LOADED
                container.post {
                    if (container.height > 0) measuredHeightPx[placement] = container.height
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
                if (container.childCount == 0) slot.state = NativeSlotState.FAILED
            }
        })
        try {
            WebsCareAds.loadNative(activity, container, placement, size)
        } catch (e: Exception) {
            Log.e(TAG, "loadNative failed for $placement", e)
            slot.state = NativeSlotState.FAILED
        }
        return slot
    }

    private fun registerCleanup(application: Application) {
        if (callbacksRegistered) return
        callbacksRegistered = true
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityDestroyed(activity: Activity) {
                if (activity === owner) clear()
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    private fun clear() {
        slots.values.forEach { slot ->
            (slot.container.parent as? ViewGroup)?.removeView(slot.container)
            slot.container.removeAllViews()
        }
        slots.clear()
        owner = null
    }
}

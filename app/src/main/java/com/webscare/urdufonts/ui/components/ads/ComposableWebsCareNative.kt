package com.webscare.urdufonts.ui.components.ads

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.webscare.ads.NativeSize
import com.webscare.ads.WebsCareAds
import com.webscare.urdufonts.ads.AdConfig

private const val TAG = "WebsCareAdsLog"

@Composable
fun ComposableWebsCareNative(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.NATIVE_AD_UNIT_ID,
    nativeSize: NativeSize = NativeSize.MEDIUM
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (activity != null) {
        Log.d(TAG, "ComposableWebsCareNative composed for Ad Unit: $adUnitId, size: $nativeSize")
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                Log.d(TAG, "Creating FrameLayout container for WebsCareAds.loadNative...")
                FrameLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    post {
                        try {
                            Log.d(TAG, "Calling WebsCareAds.loadNative with activity: ${activity.localClassName}")
                            WebsCareAds.loadNative(activity, this, adUnitId, nativeSize)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in WebsCareAds.loadNative", e)
                        }
                    }
                }
            }
        )
    } else {
        Log.w(TAG, "ComposableWebsCareNative: Activity is null, cannot load native ad")
    }
}

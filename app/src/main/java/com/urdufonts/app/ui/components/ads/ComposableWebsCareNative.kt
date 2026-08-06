package com.urdufonts.app.ui.components.ads

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.ads.AdConfig
import com.urdufonts.app.data.local.UserPreferences
import com.webscare.ads.NativeSize
import com.webscare.ads.WebsCareAds
import org.koin.compose.koinInject

private const val TAG = "WebsCareAdsLog"

@Composable
fun ComposableWebsCareNative(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.NATIVE_AD_UNIT_ID,
    nativeSize: NativeSize = NativeSize.MEDIUM,
    userPreferences: UserPreferences = koinInject()
) {
    val isProUser by userPreferences.isProUser.collectAsStateWithLifecycle(initialValue = false)
    if (isProUser) return

    val context = LocalContext.current
    val activity = context as? Activity
    var isAdVisible by remember { mutableStateOf(false) }

    if (activity != null) {
        Log.d(TAG, "ComposableWebsCareNative composed for Ad Unit: $adUnitId, size: $nativeSize")
        AndroidView(
            modifier = if (isAdVisible) modifier.fillMaxWidth().wrapContentHeight() else Modifier.size(0.dp),
            factory = { ctx ->
                Log.d(TAG, "Creating FrameLayout container for WebsCareAds.loadNative...")
                FrameLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )

                    setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                        override fun onChildViewAdded(parent: View?, child: View?) {
                            if (parent is ViewGroup && parent.childCount > 0) {
                                Log.d(TAG, "Native Ad Container child added, setting visibility VISIBLE")
                                isAdVisible = true
                                parent.visibility = View.VISIBLE
                            }
                        }

                        override fun onChildViewRemoved(parent: View?, child: View?) {
                            if (parent is ViewGroup && parent.childCount == 0) {
                                Log.w(TAG, "Native Ad Container child removed (Load Failed), collapsing to GONE")
                                isAdVisible = false
                                parent.visibility = View.GONE
                            }
                        }
                    })

                    post {
                        try {
                            Log.d(TAG, "Calling WebsCareAds.loadNative with activity: ${activity.localClassName}, Unit ID: $adUnitId")
                            WebsCareAds.loadNative(activity, this, adUnitId, nativeSize)

                            try {
                                val adLoader = com.google.android.gms.ads.AdLoader.Builder(ctx, adUnitId)
                                    .forNativeAd { nativeAd ->
                                        Log.d(TAG, "🟢 [DIRECT_NATIVE_SUCCESS] Direct AdMob NativeAd LOADED SUCCESSFULLY! Headline: '${nativeAd.headline}'")
                                    }
                                    .withAdListener(object : com.google.android.gms.ads.AdListener() {
                                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                                            Log.e(
                                                TAG,
                                                "🔴 [DIRECT_NATIVE_ERROR] Direct AdMob NativeAd FAILED TO LOAD! Code: ${loadAdError.code}, Message: '${loadAdError.message}', Domain: '${loadAdError.domain}', ResponseInfo: ${loadAdError.responseInfo}"
                                            )
                                        }
                                    })
                                    .build()
                                adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                            } catch (e: Exception) {
                                Log.e(TAG, "Exception running direct Native AdMob diagnostic", e)
                            }

                            if (childCount == 0) {
                                isAdVisible = false
                                visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in WebsCareAds.loadNative", e)
                            isAdVisible = false
                            visibility = View.GONE
                        }
                    }
                }
            }
        )
    } else {
        Log.w(TAG, "ComposableWebsCareNative: Activity is null, cannot load native ad")
    }
}

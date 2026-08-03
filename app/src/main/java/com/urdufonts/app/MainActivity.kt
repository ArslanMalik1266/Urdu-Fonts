package com.urdufonts.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.urdufonts.app.ui.home.HomeScreen
import com.urdufonts.app.ui.navigation.AppNavigation
import com.urdufonts.app.ui.theme.UrduFontsTheme

class MainActivity : ComponentActivity() {
    private val adManager: com.urdufonts.app.ads.AdManager by lazy {
        org.koin.java.KoinJavaComponent.getKoin().get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideBottomBar()

        android.util.Log.d("WebsCareAdsLog", "MainActivity onCreate -> Preloading App Open Ad & requesting consent")
        adManager.preloadAppOpen(this)
        adManager.requestConsent(this) { isGathered ->
            android.util.Log.d("WebsCareAdsLog", "MainActivity requestConsent finished: isGathered = $isGathered")
        }

        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)

        setContent {
            AppNavigation()
        }
    }

    private var isAppInBackground = false
    private var isShowingAppOpenAd = false

    override fun onStart() {
        super.onStart()
        android.util.Log.d("WebsCareAdsLog", "MainActivity onStart -> App in foreground")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("WebsCareAdsLog", "MainActivity onResume -> Activity resumed (isAppInBackground: $isAppInBackground)")
        if (isAppInBackground && !isShowingAppOpenAd) {
            isAppInBackground = false
            isShowingAppOpenAd = true
            android.util.Log.d("WebsCareAdsLog", "App resumed from background -> Triggering App Open Ad")
            adManager.showAppOpenAd(this) {
                isShowingAppOpenAd = false
                android.util.Log.d("WebsCareAdsLog", "App Open Ad shown & dismissed on Resume")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("WebsCareAdsLog", "MainActivity onPause -> Activity paused")
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d("WebsCareAdsLog", "MainActivity onStop -> App moving to background")
        if (!isShowingAppOpenAd) {
            isAppInBackground = true
        }
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun hideBottomBar() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.statusBars())      // top bar dikhao
        controller.hide(WindowInsetsCompat.Type.navigationBars())  // bottom bar hide karo
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}


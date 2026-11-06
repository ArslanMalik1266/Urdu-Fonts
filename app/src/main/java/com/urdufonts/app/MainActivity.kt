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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val adManager: com.urdufonts.app.ads.AdManager by lazy {
        org.koin.java.KoinJavaComponent.getKoin().get()
    }

    private val userPreferences: com.urdufonts.app.data.local.UserPreferences by lazy {
        org.koin.java.KoinJavaComponent.getKoin().get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideBottomBar()

        android.util.Log.d("WebsCareAdsLog", "MainActivity onCreate -> Requesting consent")
        adManager.requestConsent(this) { isGathered ->
            android.util.Log.d("WebsCareAdsLog", "MainActivity requestConsent finished: isGathered = $isGathered")
        }

        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)

        setContent {
            AppNavigation()
        }
    }

    // Return-to-app app-open ads are handled by WebsCareAds.enableAutoAppOpen (see WebsCareAdManagerImpl).


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


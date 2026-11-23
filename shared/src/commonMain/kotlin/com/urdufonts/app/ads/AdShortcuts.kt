package com.urdufonts.app.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

/** The app is about to leave on purpose (store, browser, picker): no app-open ad on the way back. */
fun suppressNextReturnAppOpen() {
    KoinPlatform.getKoinOrNull()?.getOrNull<AdManager>()?.suppressNextReturnAppOpen()
}

/** True once the native ad for [placement] + [slot] has failed, so a list can leave that row out. */
fun isNativeAdFailed(placement: AdPlacement, slot: Any): Boolean =
    KoinPlatform.getKoinOrNull()?.getOrNull<AdManager>()?.isNativeAdFailed(placement, slot) ?: true

/** A native ad row, drawn by the platform's ad SDK. */
@Composable
fun NativeAdSlot(
    placement: AdPlacement,
    slot: Any,
    modifier: Modifier = Modifier,
    nativeSize: NativeAdSize = NativeAdSize.MEDIUM,
    adManager: AdManager = koinInject()
) = adManager.NativeAd(placement, slot, modifier, nativeSize)

/** A banner ad, drawn by the platform's ad SDK. */
@Composable
fun BannerAdSlot(
    modifier: Modifier = Modifier,
    placement: AdPlacement = AdPlacement.FONT_DETAILS_BANNER,
    adManager: AdManager = koinInject()
) = adManager.BannerAd(placement, modifier)

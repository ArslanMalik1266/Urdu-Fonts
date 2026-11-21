package com.urdufonts.app.di

import com.urdufonts.app.R
import com.urdufonts.app.domain.usecases.FeatureIcons

/** Android drawables for the shared premium feature list. */
object AndroidFeatureIcons : FeatureIcons {
    override val download = R.drawable.ic_download
    override val noAds = R.drawable.ic_no_ads
    override val premium = R.drawable.ic_premium
    override val fontFamily = R.drawable.preview_icon_splash
    override val included = R.drawable.ic_pass
}

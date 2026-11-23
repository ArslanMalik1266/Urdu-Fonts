package com.urdufonts.app.ui.support.kit

import androidx.compose.ui.unit.dp

/** The spacing scale (4 / 8 / 12 / 16 / 24 / 32) and fixed component sizes. */
object Dimens {
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space24 = 24.dp
    val Space32 = 32.dp

    /** Side margin of every screen. */
    val ScreenMargin = Space16

    /** Gap between the top app bar and the first thing on every screen. */
    val ContentTop = Space8

    val IconSize = 22.dp
    val IconSmall = 16.dp
    val IconTiny = 12.dp
    val LogoSize = 112.dp
    val SpinnerSize = 44.dp
    val SplashSpinnerBottom = 56.dp
    val IconButtonSize = 40.dp
    val ButtonHeight = 44.dp
    val ButtonHeightCompact = 40.dp
    val ButtonChevron = 18.dp
    val ChipHeight = 30.dp
    val ChipIcon = 14.dp
    val TabRowHeight = 40.dp
    val CrownButtonArt = 28.dp
    val AutoPlaylistIconBox = 40.dp
    val SearchFieldHeight = 44.dp
    val VolumeFaderWidth = 84.dp
    val VolumeFaderHeight = 220.dp
    val BassKnob = 150.dp
    val SmallKnob = 84.dp
    val StageHeight = 96.dp
    val ChoicePicture = 18.dp
    val StripArtwork = 36.dp
    val Hairline = 1.dp

    val BottomBarHeight = 60.dp
    val ActiveDotSize = 4.dp

    val EmptyIllustration = 96.dp
    val SheetHandleWidth = 36.dp
    val SheetHandleHeight = 4.dp

    val PageDot = 8.dp
    val PageDotActiveWidth = 20.dp

    // Onboarding illustrations
    val IllustrationCardHeight = 260.dp
    val MiniSliderHeight = 150.dp
    val MiniSliderTrack = 6.dp
    val MiniSliderKnob = 22.dp
    val SpectrumBarWidth = 8.dp
    val SpectrumHeight = 120.dp
    val LiveDot = 8.dp
    val IllustrationTile = 92.dp
    val IllustrationTileCenter = 112.dp
    val IllustrationTileIcon = 28.dp
    val ElevationSoft = 8.dp
    val OnboardingButtonHeight = 50.dp
    val OnboardingPhoneWidth = 216.dp
    val OnboardingPhoneHeight = 400.dp
    val OnboardingPhoneBezel = 6.dp
    val OnboardingPhoneCorner = 34.dp
    val OnboardingMockChipHeight = 22.dp
    val OnboardingMockThumb = 15.dp
    val OnboardingMockTrack = 4.dp
    val OnboardingMockSliders = 150.dp
    val OnboardingMockSpectrum = 110.dp
    val OnboardingMockIcon = 12.dp
    val OnboardingWaveBar = 7.dp
    val OnboardingTile = 92.dp
    val OnboardingTileCenter = 120.dp
    val OnboardingTileIcon = 32.dp
    val OnboardingTileGap = 12.dp

    // Lists and player
    val ListArtwork = 44.dp
    val SkeletonTitle = 12.dp
    val SkeletonLine = 9.dp
    val SkeletonButton = 28.dp
    val GridArtworkDecode = 160.dp
    val MiniPlayerHeight = 60.dp
    val MiniPlayerArtwork = 44.dp
    val MiniPlayerPlay = 40.dp
    val MiniProgress = 3.dp
    val MiniPlayingBars = 12.dp
    val MiniStylePreviewWidth = 64.dp
    val MiniStylePreviewHeight = 36.dp
    val MiniStyleBadge = 14.dp
    val NowPlayingArtworkDecode = 360.dp
    val PlayButton = 68.dp
    val ControlDisc = 52.dp
    val ArtworkGlow = 24.dp
    val PlayerCardCorner = 24.dp
    val MiniArtworkCorner = 12.dp
    val PlayerArtworkCorner = 24.dp
    val SeekTrack = 4.dp
    val SeekThumb = 16.dp
    val SheetArtwork = 48.dp
    val ActionRowHeight = 42.dp
    val SectionHeaderHeight = 36.dp
    val RowHeight = 56.dp
    val DetailsArtwork = 72.dp
    val PickerMaxHeight = 360.dp
    val CoverChip = 56.dp
    val PlaylistHeaderCover = 112.dp
    val PickerCheck = 24.dp
    val CoverBorder = 2.dp
    val PlaylistCardCover = 48.dp
    val DragElevation = 8.dp
    val NoElevation = 0.dp
    val QueueIndexWidth = IconSize

    // Equalizer
    val EqSliderWidth = 22.dp
    val EqSliderHeight = 200.dp
    val EqThumb = 22.dp
    val EqTrack = 6.dp
    val EqAxisWidth = 28.dp
    val KnobSize = 76.dp
    val PresetChipWidth = 72.dp
    val PresetChipHeight = 72.dp
    val ParametricHeight = 240.dp
    val ParametricNode = 28.dp
    val SpectrumChartHeight = 260.dp
    val SpectrumAxisWidth = 36.dp

    val SettingsIconBox = 34.dp
    /** How close to the queue sheet's edge a dragged row starts the list scrolling. */
    val QueueAutoScrollEdge = 64.dp
    /** The border of a chosen card or row. */
    val SelectionBorder = 1.dp
    /** The fixed space under the parametric band list (Width slider or a hint). */
    val ParametricControls = 76.dp
    val ParametricBandHeader = 30.dp
    val AccentDot = 14.dp
    val AccentSwatch = 28.dp
    val AccentRing = 1.5.dp
    val AccentCheck = 14.dp
    /** The crown badge on a locked accent colour or mini player look. */
    val LockBadge = 18.dp
    val LockBadgeIcon = 13.dp
    /** Free height under the Now Playing cover needed for its banner: a 50 dp banner with 16 dp above and below. */
    val NowPlayingBannerSpace = 82.dp

    // Home
    val IconLarge = 28.dp
    val HeroPicture = 160.dp
    /** The hero picture never gets narrower than this, however little room the button leaves. */
    val HeroPictureMin = 96.dp
    val HeroPictureHeight = 178.dp
    val HeroKnob = 16.dp
    val HeroScrew = 5.dp
    val QuickToolArt = 40.dp
    val ChromeThumb = 28.dp
    val SwitchScale = 0.75f
    val RowPlayButton = 36.dp
    val SquareButton = 38.dp
    val RowMenuWidth = 40.dp
    val PlayingBars = 18.dp
    val HeroElevation = 10.dp
    val CardElevation = 6.dp
    val PremiumFeatureIcon = 40.dp
    val PlanRadio = 22.dp

    // Help & Support
    val SupportBorder = 1.dp
    val SupportLoader = 20.dp
    val SupportIcon = 20.dp
    val SupportFab = 56.dp
    val SupportFabClearance = 96.dp
    val UnreadDot = 8.dp
    val SkeletonLineSmall = 10.dp
    val SkeletonChipWidth = 80.dp
    val TicketDescriptionMinHeight = 112.dp
    val SheetNoteMinHeight = 96.dp
    val ChatSkeletonBubble = 44.dp
    val BubbleRadius = 20.dp
    val BubbleTailRadius = 6.dp
    val BubblePaddingH = 14.dp
    val ChatClock = 12.dp
    val ChatPictureMaxWidth = 220.dp
    val ChatRefreshPull = 84.dp
    val ReplyFieldRadius = 22.dp
    val StarLarge = 36.dp
    val StarSmall = 16.dp
    val PlanPriceSkeletonWidth = 56.dp
    val PlanPriceSkeletonHeight = 16.dp
    /** Subscription sheets: the success burst, its disc, the app icon tile and the status badge icon. */
    val SubBurst = 120.dp
    val SubCheckDisc = 64.dp
    val SubAppIcon = 64.dp
    val PremiumHeaderIcon = 56.dp
    val PlanRowPadding = 16.dp
    /** Room above each plan card for its pill (half the pill rises above the border). */
    val PlanPillRoom = 10.dp
    val SubBadgeIcon = 16.dp
    val SubFeatureIcon = 30.dp

    // Music access
    /** The Music Access screen's hero card with the animated headphones. */
    val AccessHero = 220.dp
}

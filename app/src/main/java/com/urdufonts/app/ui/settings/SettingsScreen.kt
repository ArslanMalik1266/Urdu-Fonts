package com.urdufonts.app.ui.settings

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.MoreAppItem
import com.urdufonts.app.ui.components.SimpleTopAppBar
import com.urdufonts.app.ui.home.HomeViewModel
import com.urdufonts.app.ui.theme.AppColor
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.figmaDropShadow
import com.urdufonts.app.ui.util.figmaInnerShadow
import com.urdufonts.app.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import java.io.File

private val BackgroundColor = Color.White
private val SettingsCardShape = RoundedCornerShape(14.dp)

private fun Modifier.settingsCardStyle(): Modifier = this
    .figmaDropShadow(
        color = Color.Black.copy(alpha = 0.05f),
        offsetX = 0.dp,
        offsetY = 4.dp,
        blur = 16.dp,
        shape = SettingsCardShape
    )
    .clip(SettingsCardShape)
    .background(Color.White)
    .border(BorderStroke(0.5.dp, CardBorderColor), SettingsCardShape)
private val GreenAccent = Color(0xFF18A34B)
private val DarkGreenAccent = Color(0xFF0F7A36)
private val SoftGreenIconBg = Color(0xFFE9F6EE)
private val IconTintGreen = Color(0xFF158A45)
private val CardBorderColor = Color(0xFFE7EAE7)
private val SectionTitleColor = Color(0xFF9AA19B)
private val DividerColor = Color(0xFFEEF1EE)
private val ChevronColor = Color(0xFFB4BAB5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onOpenWebPage: (String) -> Unit,
    onOpenEmailSupport: () -> Unit,
    onRateUsClick: () -> Unit,
    onOpenHelpSupport: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    notificationsViewModel: NotificationsViewModel = koinViewModel()
) {
    val drawerUiState by viewModel.drawerUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = remember(context) { getAppVersionName(context) }
    // Cache size walks the whole cache folder, so compute it off the main thread to avoid ANRs
    var cacheSizeText by remember { mutableStateOf("…") }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    val cacheScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        cacheSizeText = withContext(Dispatchers.IO) { calculateCacheSize(context) }
    }

    if (showClearCacheDialog) {
        ClearCacheConfirmationDialog(
            onDismiss = { showClearCacheDialog = false },
            onConfirm = {
                showClearCacheDialog = false
                cacheScope.launch {
                    val cleared = withContext(Dispatchers.IO) { clearAppCache(context) }
                    Toast.makeText(
                        context,
                        if (cleared) "Cache cleared successfully" else "Failed to clear cache",
                        Toast.LENGTH_SHORT
                    ).show()
                    cacheSizeText = withContext(Dispatchers.IO) { calculateCacheSize(context) }
                }
            }
        )
    }

    // Notifications: the app's own choice, shown on only while the phone allows them too
    val notificationsChoice by notificationsViewModel.enabled.collectAsStateWithLifecycle()
    val notificationPermission = rememberNotificationPermission()
    val notificationsOn = notificationsChoice && notificationPermission.granted
    LaunchedEffect(notificationsViewModel) {
        notificationsViewModel.offFailed.collect {
            Toast.makeText(context, context.getString(R.string.settings_notifications_failed), Toast.LENGTH_SHORT).show()
        }
    }

    val entitlementRepository: com.urdufonts.app.domain.repo.EntitlementRepository = org.koin.compose.koinInject()
    val adManager: com.urdufonts.app.ads.AdManager = org.koin.compose.koinInject()
    val userEntitlements by entitlementRepository.entitlementsFlow.collectAsStateWithLifecycle(initialValue = com.urdufonts.app.domain.models.UserEntitlements())
    val showSubIcon = userEntitlements.activeSubscriptionType != com.urdufonts.app.domain.models.SubscriptionType.LIFETIME

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            SimpleTopAppBar(
                title = "Settings",
                onBackClick = onBackClick,
                onSubscriptionClick = onNavigateToSubscription,
                showSubscriptionIcon = true,
                containerColor = BackgroundColor
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .springOverscroll()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ── 1. Login / User Header Card ──────────────────────────────
                SettingsUserCard(
                    isLoggedIn = drawerUiState.isLoggedIn,
                    userName = drawerUiState.userName,
                    userSubtitle = drawerUiState.userSubtitle,
                    profileImageUrl = drawerUiState.profileImageUrl,
                    activePlanType = userEntitlements.activeSubscriptionType,
                    pendingChange = userEntitlements.pendingChange,
                    onLoginClick = onNavigateToProfile,
                    onManageSubscription = onNavigateToSubscription
                )

                // ── 2. FONTS Section ─────────────────────────────────────────
                SettingsSection(title = "FONTS") {
                    SettingsRow(
                        iconRes = R.drawable.ic_font_family,
                        label = "Fonts",
                        showDivider = true,
                        onClick = onNavigateToHome
                    )
                    SettingsRow(
                        iconRes = R.drawable.ic_downloaded,
                        label = "My Premium Fonts",
                        showDivider = false,
                        onClick = onNavigateToSubscription
                    )
                }

                // ── 3. ACCOUNT Section ───────────────────────────────────────
                SettingsSection(title = "ACCOUNT") {
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_profile,
                        label = "Profile",
                        showDivider = true,
                        onClick = onNavigateToProfile
                    )
                    SettingsSwitchRow(
                        iconRes = R.drawable.ic_sp_bell,
                        label = stringResource(R.string.settings_notifications),
                        checked = notificationsOn,
                        showDivider = true,
                        onCheckedChange = {
                            if (notificationsOn) {
                                notificationsViewModel.onNotifications(false)
                            } else {
                                // The phone must allow them too: it asks, or opens the app's notification settings
                                if (!notificationPermission.granted) notificationPermission.turnOn()
                                notificationsViewModel.onNotifications(true)
                            }
                        }
                    )
                    SettingsRow(
                        iconRes = R.drawable.ic_cloud_download,
                        label = "Clear Cache",
                        trailingText = cacheSizeText,
                        showDivider = false,
                        onClick = {
                            showClearCacheDialog = true
                        }
                    )
                }

                // ── 4. SUPPORT & INFO Section ────────────────────────────────
                SettingsSection(title = "SUPPORT & INFO") {
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_support,
                        label = stringResource(R.string.support_title),
                        showDivider = true,
                        onClick = {
                            // Asked here so a support reply can reach the phone
                            if (!notificationPermission.granted) notificationPermission.ask()
                            onOpenHelpSupport()
                        }
                    )
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_privacy,
                        label = "Privacy Policy",
                        showDivider = true,
                        onClick = { onOpenWebPage("https://urdufonts.com/privacy-policy") }
                    )
                    // Google (UMP) requires a way to change ad consent for some users, e.g. in the EEA
                    if (adManager.isPrivacyOptionsRequired()) {
                        SettingsRow(
                            iconRes = R.drawable.ic_drawer_privacy,
                            label = "Ad privacy options",
                            showDivider = true,
                            onClick = { (context as? android.app.Activity)?.let { adManager.showPrivacyOptions(it) } }
                        )
                    }
                    SettingsRow(
                        iconRes = R.drawable.ic_email,
                        label = "Contact Us",
                        showDivider = true,
                        onClick = onOpenEmailSupport
                    )
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_rate,
                        label = "Rate Us",
                        showDivider = false,
                        onClick = onRateUsClick
                    )
                }

                // ── 5. MORE APPS Section (Dynamic from API) ──────────────────
                // Defaults only when the API gave nothing; before it answers, show no rows (no 5 -> 4 jump)
                val appsList = when {
                    drawerUiState.moreApps.isNotEmpty() -> drawerUiState.moreApps
                    drawerUiState.moreAppsLoaded -> defaultAppsList
                    else -> emptyList()
                }

                if (appsList.isNotEmpty()) SettingsSection(title = "MORE APPS") {
                    appsList.forEachIndexed { index, app ->
                        MoreAppApiRow(
                            app = app,
                            showDivider = true,
                            onClick = { app.playstoreUrl?.let { onOpenWebPage(it) } }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .addPressEffect{ onOpenWebPage("https://play.google.com/store/apps/developer?id=WebsCare") }
                            .background(Color(0xFFFAFBFA))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View all apps",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreenAccent,
                            fontFamily = NunitoFontFamily
                        )
                    }
                }

                // ── 6. Footer ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "UrduFonts.com",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreenAccent,
                        fontFamily = NunitoFontFamily,
                        modifier = Modifier
                            .addPressEffect { onOpenWebPage("https://urdufonts.com") }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "© 2026 WebsCare Pvt. Ltd. All rights reserved.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFA8AEA9),
                        fontFamily = NunitoFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── Component Helpers ───────────────────────────────────────────────────────

@Composable
private fun SettingsUserCard(
    isLoggedIn: Boolean,
    userName: String?,
    userSubtitle: String,
    profileImageUrl: String?,
    activePlanType: com.urdufonts.app.domain.models.SubscriptionType?,
    pendingChange: com.urdufonts.app.domain.models.PendingChangeDetails? = null,
    onLoginClick: () -> Unit,
    onManageSubscription: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .settingsCardStyle()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isLoggedIn || activePlanType != null) Color(0xFFE7F5EC) else Color(0xFFF0F2EF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoggedIn && !profileImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isLoggedIn) {
                        Text(
                            text = (userName?.take(1) ?: "U").uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreenAccent
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_drawer_profile),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color(0xFF9AA19B)),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                val planSubtitleText = when (activePlanType) {
                    com.urdufonts.app.domain.models.SubscriptionType.LIFETIME -> "Lifetime Access Active"
                    com.urdufonts.app.domain.models.SubscriptionType.LEGACY -> "Premium Plan Active"
                    com.urdufonts.app.domain.models.SubscriptionType.BASIC,
                    com.urdufonts.app.domain.models.SubscriptionType.PRO,
                    com.urdufonts.app.domain.models.SubscriptionType.BUSINESS -> "${activePlanType.displayName} Plan Active"
                    null -> if (isLoggedIn) {
                        if (userSubtitle.isNotBlank() && userSubtitle != "Access premium Urdu fonts") userSubtitle else "Free Plan"
                    } else {
                        userSubtitle.ifBlank { "Access premium Urdu fonts" }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = if (isLoggedIn && !userName.isNullOrBlank()) userName else "Join Urdu Fonts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HeadingBlackColor,
                        fontFamily = NunitoFontFamily
                    )
                    Text(
                        text = planSubtitleText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8B928D),
                        fontFamily = NunitoFontFamily
                    )

                    pendingChange?.let { pending ->
                        val targetName = pending.planName ?: pending.productSku ?: "Monthly"
                        val formattedDate = if (!pending.effectiveAt.isNullOrBlank()) {
                            try {
                                val instant = java.time.Instant.parse(pending.effectiveAt)
                                java.time.format.DateTimeFormatter
                                    .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
                                    .withZone(java.time.ZoneId.systemDefault())
                                    .format(instant)
                            } catch (e: Exception) {
                                pending.effectiveAt
                            }
                        } else ""
                        val effectiveText = if (formattedDate.isNotBlank()) " on $formattedDate" else ""
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFFBEB))
                                .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Downgrading to $targetName$effectiveText",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                fontFamily = NunitoFontFamily
                            )
                        }
                    }
                }
            }

            if (!isLoggedIn) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .addPressEffect { onLoginClick() }
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "Login / Signup",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .addPressEffect { onManageSubscription() }
                        .clip(CircleShape)
                        .border(BorderStroke(1.2.dp, GreenAccent), CircleShape)
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "Manage subscription",
                        color = DarkGreenAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.sp,
            color = SectionTitleColor,
            fontFamily = NunitoFontFamily,
            modifier = Modifier.padding(start = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .settingsCardStyle()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    iconRes: Int,
    label: String,
    trailingText: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .addPressEffect { onClick() }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SoftGreenIconBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(IconTintGreen),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.weight(1f)
            )

            if (trailingText != null) {
                Text(
                    text = trailingText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SectionTitleColor,
                    fontFamily = NunitoFontFamily
                )
            }

            Image(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ChevronColor),
                modifier = Modifier
                    .size(13.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 58.dp)
            )
        }
    }
}

/** A settings row ending in a switch. The row itself does not press: only its switch is tapped. */
@Composable
private fun SettingsSwitchRow(
    iconRes: Int,
    label: String,
    checked: Boolean,
    showDivider: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SoftGreenIconBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(IconTintGreen),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GreenAccent,
                    checkedBorderColor = GreenAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = DividerColor,
                    uncheckedBorderColor = CardBorderColor
                )
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 58.dp)
            )
        }
    }
}

@Composable
private fun MoreAppApiRow(
    app: MoreAppItem,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .addPressEffect { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(BorderStroke(1.dp, Color(0xFFDEE3DF)), RoundedCornerShape(9.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (!app.iconUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(app.iconUrl),
                        contentDescription = app.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Text(
                        text = "اردو",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreenAccent
                    )
                }
            }

            Text(
                text = app.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HeadingBlackColor,
                fontFamily = NunitoFontFamily,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ChevronColor),
                modifier = Modifier
                    .size(13.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 58.dp)
            )
        }
    }
}

// ─── Default Fallback List for More Apps ─────────────────────────────────────

private val defaultAppsList = listOf(
    MoreAppItem(1, "UrduCanvas: Urdu Design Editor", null, "https://play.google.com/store/apps/details?id=com.webscare.urducanvas"),
    MoreAppItem(2, "Roman Urdu to Urdu Script", null, "https://play.google.com/store/apps/details?id=com.webscare.romanurdutransliteration"),
    MoreAppItem(3, "Urdu Speech to Text Transcribe", null, "https://play.google.com/store/apps/details?id=com.webscare.urduspeechtotext"),
    MoreAppItem(4, "Urdu Text to Speech and Voice", null, "https://play.google.com/store/apps/details?id=com.webscare.urdutexttospeech"),
    MoreAppItem(5, "Urdu OCR – Scan Image to Text", null, "https://play.google.com/store/apps/details?id=com.urduocr.scanner")
)

// ─── App Version Helper ────────────────────────────────────────────────────────

private fun getAppVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "1.0.0"
    } catch (_: Exception) {
        "1.0.0"
    }
}

// ─── Cache Helpers ────────────────────────────────────────────────────────────

private fun calculateCacheSize(context: Context): String {
    var size: Long = 0
    try {
        context.cacheDir?.let { size += getFolderSize(it) }
        context.externalCacheDir?.let { size += getFolderSize(it) }
    } catch (_: Exception) {}
    val mb = size / (1024f * 1024f)
    return if (mb < 0.1f) "0.1 MB" else String.format("%.1f MB", mb)
}

private fun getFolderSize(folder: File): Long {
    var length: Long = 0
    val files = folder.listFiles() ?: return 0
    for (file in files) {
        length += if (file.isDirectory) getFolderSize(file) else file.length()
    }
    return length
}

private fun clearAppCache(context: Context): Boolean {
    return try {
        context.cacheDir?.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
        true
    } catch (_: Exception) {
        false
    }
}

// ─── Confirmation Dialog ───────────────────────────────────────────────────────

@Composable
private fun ClearCacheConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Header in soft tint circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cloud_download),
                        contentDescription = null,
                        tint = AppColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Clear Cache?",
                    fontSize = 18.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = HeadingBlackColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Are you sure you want to clear temporary app cache files? This will free up storage space.",
                    fontSize = 14.sp,
                    fontFamily = NunitoFontFamily,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .addPressEffect { onDismiss() }
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // Confirm Clear Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .addPressEffect { onConfirm() }
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColor)
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Clear Cache",
                            fontSize = 14.sp,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

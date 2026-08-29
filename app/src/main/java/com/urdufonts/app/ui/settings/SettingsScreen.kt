package com.urdufonts.app.ui.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.urdufonts.app.R
import com.urdufonts.app.domain.models.MoreAppItem
import com.urdufonts.app.ui.components.SimpleTopAppBar
import com.urdufonts.app.ui.home.HomeViewModel
import com.urdufonts.app.ui.theme.DarkGreen
import com.urdufonts.app.ui.theme.HeadingBlackColor
import com.urdufonts.app.ui.theme.NunitoFontFamily
import com.urdufonts.app.ui.util.addPressEffect
import com.urdufonts.app.ui.util.figmaDropShadow
import com.urdufonts.app.ui.util.figmaInnerShadow
import com.urdufonts.app.ui.util.springOverscroll
import org.koin.androidx.compose.koinViewModel
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
    viewModel: HomeViewModel = koinViewModel()
) {
    val drawerUiState by viewModel.drawerUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = remember(context) { getAppVersionName(context) }
    var cacheSizeText by remember { mutableStateOf(calculateCacheSize(context)) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            SimpleTopAppBar(
                title = "Settings",
                onBackClick = onBackClick,
                onSubscriptionClick = onNavigateToSubscription,
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
                    SettingsRow(
                        iconRes = R.drawable.ic_cloud_download,
                        label = "Clear Cache",
                        trailingText = cacheSizeText,
                        showDivider = false,
                        onClick = {
                            clearAppCache(context)
                            cacheSizeText = calculateCacheSize(context)
                        }
                    )
                }

                // ── 4. SUPPORT & INFO Section ────────────────────────────────
                SettingsSection(title = "SUPPORT & INFO") {
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_support,
                        label = "Support",
                        showDivider = true,
                        onClick = onOpenEmailSupport
                    )
                    SettingsRow(
                        iconRes = R.drawable.ic_drawer_privacy,
                        label = "Privacy Policy",
                        showDivider = true,
                        onClick = { onOpenWebPage("https://urdufonts.com/privacy-policy") }
                    )
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
                val appsList = drawerUiState.moreApps.ifEmpty { defaultAppsList }

                SettingsSection(title = "MORE APPS") {
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
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Urdu Fonts App · Version $appVersion",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8B928D),
                        fontFamily = NunitoFontFamily
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
                        .background(if (isLoggedIn) Color(0xFFE7F5EC) else Color(0xFFF0F2EF)),
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

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isLoggedIn && !userName.isNullOrBlank()) userName else "Join Urdu Fonts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HeadingBlackColor,
                        fontFamily = NunitoFontFamily
                    )
                    Text(
                        text = if (isLoggedIn) "Premium active · All fonts unlocked" else userSubtitle.ifBlank { "Access premium Urdu fonts" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8B928D),
                        fontFamily = NunitoFontFamily
                    )
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

private fun clearAppCache(context: Context) {
    try {
        context.cacheDir?.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
        Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to clear cache", Toast.LENGTH_SHORT).show()
    }
}

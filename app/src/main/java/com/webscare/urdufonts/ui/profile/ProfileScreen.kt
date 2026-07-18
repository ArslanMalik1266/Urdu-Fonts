package com.webscare.urdufonts.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.webscare.urdufonts.R
import com.webscare.urdufonts.ui.components.TopBarButton
import com.webscare.urdufonts.ui.theme.AppColor
import com.webscare.urdufonts.ui.theme.GreyColor
import com.webscare.urdufonts.ui.theme.DarkGreen
import com.webscare.urdufonts.ui.theme.HeadingBlackColor
import com.webscare.urdufonts.ui.theme.NunitoFontFamily
import com.webscare.urdufonts.ui.util.addPressEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreenInternal(
        uiState = uiState,
        onBackClick = onBackClick,
        onLogin = { email, password -> viewModel.onLoginClick(email, password) },
        onLogout = { viewModel.onLogoutClick() }
    )
}

@Composable
internal fun ProfileScreenInternal(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.drawar_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
        )

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopBarButton(
                    iconRes            = R.drawable.ic_back,
                    onClick            = onBackClick,
                    contentDescription = "Back"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = "Settings",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily,
                    color      = HeadingBlackColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.isLoggedIn,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "profile_state"
            ) { isLoggedIn ->
                if (isLoggedIn) {
                    LoggedInContent(uiState = uiState, onLogout = onLogout)
                } else {
                    LoggedOutContent(
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage,
                        onLogin = onLogin
                    )
                }
            }
        }

        ProfileFooter(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ─── Logged-in view ───────────────────────────────────────────────────────────

@Composable
private fun LoggedInContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Image(
                painter = painterResource(R.drawable.profile_image), // ← your placeholder image
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = uiState.userName,
            fontSize = 16.sp,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = GreyColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = uiState.email,
            fontSize = 12.sp,
            fontFamily = NunitoFontFamily,
            color = GreyColor.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email field (read-only)
        ProfileReadOnlyField(
            label = "Email",
            value = uiState.email,
            leadingIcon = R.drawable.ic_drawer_profile
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username field (read-only)
        ProfileReadOnlyField(
            label = "Username",
            value = uiState.userName,
            leadingIcon = R.drawable.ic_drawer_profile
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Logout button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .addPressEffect()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColor)
                .padding(vertical = 14.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Logged-out / Login view ──────────────────────────────────────────────────

@Composable
private fun LoggedOutContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_drawer_profile),
                contentDescription = null,
                tint = GreyColor.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Join Urdu Fonts",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = NunitoFontFamily,
            color = HeadingBlackColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Login to access premium Urdu fonts",
            fontSize = 12.sp,
            color = GreyColor,
            fontFamily = NunitoFontFamily,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Email input
        ProfileInputField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            leadingIcon = R.drawable.ic_drawer_profile,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password input
        ProfileInputField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            leadingIcon = R.drawable.ic_drawer_privacy,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        // Error message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Login button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .addPressEffect()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLoading) AppColor.copy(alpha = 0.6f) else AppColor)
                .padding(vertical = 14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login / Signup",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Reusable field components ────────────────────────────────────────────────

@Composable
private fun ProfileReadOnlyField(
    label: String,
    value: String,
    leadingIcon: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                tint = GreyColor.copy(0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 12.sp, fontFamily = NunitoFontFamily, color =GreyColor.copy(0.5f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF7F7F7))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(text = value, fontSize = 14.sp,fontFamily = NunitoFontFamily, color = GreyColor.copy(0.5f))
        }
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColor,
        unfocusedBorderColor = Color(0xFFEEEEEE),
        focusedLabelColor = AppColor,
        unfocusedLabelColor = GreyColor,
        cursorColor = AppColor,
        focusedContainerColor = Color(0xFFF7F7F7),
        unfocusedContainerColor = Color(0xFFF7F7F7)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp ,fontFamily = NunitoFontFamily) },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                tint = GreyColor,
                modifier = Modifier.size(18.dp)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Urdu Fonts App",
                fontSize = 11.sp,
                color = DarkGreen,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium
            )
            Text(text = "  |  ", fontSize = 11.sp, color = GreyColor.copy(alpha = 0.3f), fontFamily = NunitoFontFamily)
            Text(text = "Version 1.0.0",fontFamily = NunitoFontFamily, fontSize = 11.sp, color = GreyColor)
        }
    }
}
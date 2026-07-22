package com.webscare.urdufonts.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
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
import com.webscare.urdufonts.ui.theme.RedColor
import com.webscare.urdufonts.ui.util.addPressEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onBackClick()
            viewModel.clearNavigateToHome()
        }
    }

    ProfileScreenInternal(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onLogin = { email, password -> viewModel.onLoginClick(email, password) },
        onSignUp = { name, email, pass, confirmPass ->
            viewModel.onSignUpClick(
                name,
                email,
                pass,
                confirmPass
            )
        },
        onVerifyOtp = { otp -> viewModel.onVerifyOtpClick(otp) },
        onCancelOtp = { viewModel.cancelOtp() },
        onToggleMode = { viewModel.toggleAuthMode() },
        onLogout = { viewModel.onLogoutClick() },
        onGoogleSignIn = { viewModel.onGoogleSignInClick(context) }
    )
}

@Composable
internal fun ProfileScreenInternal(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, pass: String, confirmPass: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onCancelOtp: () -> Unit,
    onToggleMode: () -> Unit,
    onLogout: () -> Unit,
    onGoogleSignIn: () -> Unit
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopBarButton(
                    iconRes = R.drawable.ic_back,
                    onClick = onBackClick,
                    contentDescription = "Back"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor
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
                        uiState = uiState,
                        onLogin = onLogin,
                        onSignUp = onSignUp,
                        onVerifyOtp = onVerifyOtp,
                        onCancelOtp = onCancelOtp,
                        onToggleMode = onToggleMode,
                        onGoogleSignIn = onGoogleSignIn
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        ProfileFooter(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ─── Logged-in view ───────────────────────────────────────────────────────────

@Composable
private fun LoggedInContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                painter = if (uiState.profileImageUrl != null) {
                    rememberAsyncImagePainter(uiState.profileImageUrl)
                } else {
                    painterResource(R.drawable.profile_image)
                },
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
                .addPressEffect() { showLogoutDialog = true }
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

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                }
            )
        }
    }
}

// ─── Logged-out / Login view ──────────────────────────────────────────────────

@Composable
private fun LoggedOutContent(
    uiState: ProfileUiState,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, pass: String, confirmPass: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onCancelOtp: () -> Unit,
    onToggleMode: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSignUpMode) {
        name = ""
        email = ""
        password = ""
        confirmPassword = ""
    }

    AnimatedContent(
        targetState = uiState.isOtpMode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "otp_mode_transition"
    ) { isOtpMode ->
        if (isOtpMode) {
            OtpVerificationContent(
                uiState = uiState,
                onVerifyOtp = onVerifyOtp,
                onCancelOtp = onCancelOtp
            )
        } else {
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

                // Dynamic Header Title
                Text(
                    text = if (uiState.isSignUpMode) "Create Account" else "Join Urdu Fonts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily,
                    color = HeadingBlackColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic Header Subtext
                Text(
                    text = if (uiState.isSignUpMode) "Sign up to download and manage custom fonts" else "Login to access premium Urdu fonts",
                    fontSize = 12.sp,
                    color = GreyColor,
                    fontFamily = NunitoFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. Name Field (Only in Sign Up Mode)
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.isSignUpMode,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column {
                        ProfileInputField(
                            label = "Full Name",
                            value = name,
                            onValueChange = { name = it },
                            leadingIcon = R.drawable.ic_drawer_profile,
                            keyboardType = KeyboardType.Text
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // 2. Email input (Always shown)
                ProfileInputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = R.drawable.ic_drawer_profile,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Password input (Always shown)
                ProfileInputField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = R.drawable.ic_drawer_privacy,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                // 4. Confirm Password input (Only in Sign Up Mode)
                AnimatedVisibility(
                    visible = uiState.isSignUpMode,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInputField(
                            label = "Confirm Password",
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            leadingIcon = R.drawable.ic_drawer_privacy,
                            keyboardType = KeyboardType.Password,
                            isPassword = true
                        )
                    }
                }

                // Error message display
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 5. Dynamic Auth Action Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .addPressEffect {
                            if (!uiState.isLoading) {
                                if (uiState.isSignUpMode) {
                                    onSignUp(name, email, password, confirmPassword)
                                } else {
                                    onLogin(email, password)
                                }
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (uiState.isLoading) AppColor.copy(alpha = 0.6f) else AppColor)
                        .padding(vertical = 14.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isSignUpMode) "Register" else "Login / Signup",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Google Sign-In OR Divider & Button
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                    Text(
                        text = "OR",
                        fontSize = 11.sp,
                        fontFamily = NunitoFontFamily,
                        color = GreyColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .addPressEffect {
                            if (!uiState.isLoading && !uiState.isGoogleLoading) {
                                onGoogleSignIn()
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isGoogleLoading) {
                        CircularProgressIndicator(
                            color = AppColor,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isGoogleLoading) "Signing in..." else "Continue with Google",
                        fontSize = 15.sp,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = GreyColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Dynamic Toggle Account Mode Text Link
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isSignUpMode) "Already have an account? " else "Don't have an account? ",
                        fontSize = 13.sp,
                        fontFamily = NunitoFontFamily,
                        color = GreyColor
                    )
                    Text(
                        text = if (uiState.isSignUpMode) "Log In" else "Sign Up",
                        fontSize = 13.sp,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AppColor, // 🟢 App Theme Green
                        modifier = Modifier
                            .addPressEffect()
                            .addPressEffect{ onToggleMode() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpVerificationContent(
    uiState: ProfileUiState,
    onVerifyOtp: (String) -> Unit,
    onCancelOtp: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Verification Icon in soft tint circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_drawer_privacy),
                contentDescription = null,
                tint = AppColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Verify Email",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NunitoFontFamily,
            color = HeadingBlackColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "We sent a 6-digit verification code to \n${uiState.registrationEmail}",
            fontSize = 12.sp,
            color = GreyColor,
            fontFamily = NunitoFontFamily,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Custom OTP digit input cells with hidden BasicTextField
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 6) {
                        otpCode = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.01f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    val isFocusedCell = otpCode.length == index
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFocusedCell) AppColor.copy(alpha = 0.05f) else Color(0xFFF7F7F7))
                            .border(
                                width = 1.dp,
                                color = if (isFocusedCell) AppColor else Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = HeadingBlackColor
                        )
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = Color.Red,
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .addPressEffect {
                    if (!uiState.isLoading) {
                        onVerifyOtp(otpCode)
                    }
                }
                .clip(RoundedCornerShape(12.dp))
                .background(if (uiState.isLoading) AppColor.copy(alpha = 0.6f) else AppColor)
                .padding(vertical = 14.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verify & Register",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Back to Sign Up",
            fontSize = 13.sp,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            color = AppColor,
            modifier = Modifier
                .addPressEffect { onCancelOtp() }
                .padding(4.dp)
        )
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
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = NunitoFontFamily,
                color = GreyColor.copy(0.5f)
            )
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
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = NunitoFontFamily,
                color = GreyColor.copy(0.5f)
            )
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
        label = { Text(label, fontSize = 12.sp, fontFamily = NunitoFontFamily) },
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
            Text(
                text = "  |  ",
                fontSize = 11.sp,
                color = GreyColor.copy(alpha = 0.3f),
                fontFamily = NunitoFontFamily
            )
            Text(
                text = "Version 1.0.0",
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                color = GreyColor
            )
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
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
                // Warning Icon in soft tint circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RedColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = null,
                        tint = RedColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Leaving so soon?",
                    fontSize = 18.sp,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = HeadingBlackColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Are you sure you want to log out of your Urdu Fonts account?",
                    fontSize = 14.sp,
                    fontFamily = NunitoFontFamily,
                    color = GreyColor,
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
                            .addPressEffect() { onDismiss() }
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = GreyColor
                        )
                    }

                    // Confirm Log Out Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .addPressEffect() { onConfirm() }
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedColor)
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Yes, Log Out",
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
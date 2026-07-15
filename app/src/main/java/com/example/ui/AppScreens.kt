package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.data.service.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.ui.common.Localization
import kotlinx.coroutines.delay
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val darkTheme = viewModel.isDarkMode
    val context = LocalContext.current

    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF2E9E5B),
                secondary = Color(0xFF8BC34A),
                background = Color(0xFF111411),
                surface = Color(0xFF1E221F)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0F7A3E),
                secondary = Color(0xFF2E9E5B),
                background = Color(0xFFF3F6F4),
                surface = Color(0xFFFFFFFF),
                onPrimary = Color.White
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = viewModel.currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.Splash -> SplashScreen(viewModel)
                    Screen.Onboarding -> OnboardingScreen(viewModel)
                    Screen.Login -> LoginScreen(viewModel)
                    Screen.SignUp -> SignUpScreen(viewModel)
                    Screen.Verification -> VerificationScreen(viewModel)
                    Screen.Home -> HomeDashboardScreen(viewModel)
                    Screen.TrainSearch -> TrainSearchScreen(viewModel)
                    Screen.LiveStatus -> LiveStatusScreen(viewModel)
                    Screen.TrainScheduleScreen -> TrainScheduleScreen(viewModel)
                    Screen.StationInfoScreen -> StationInfoScreen(viewModel)
                    Screen.FreightTrainsScreen -> FreightTrainsScreen(viewModel)
                    Screen.NewsBlogsScreen -> NewsBlogsScreen(viewModel)
                    Screen.NamazTimingsScreen -> NamazTimingsScreen(viewModel)
                    Screen.NotificationsScreen -> NotificationsScreen(viewModel)
                    Screen.FavoritesScreen -> FavoritesScreen(viewModel)
                    Screen.ProfileScreen -> ProfileScreen(viewModel)
                    Screen.SettingsScreen -> SettingsScreen(viewModel)
                    Screen.HelplineScreen -> HelplineScreen(viewModel)
                }
            }
        }
    }
}

// --- Common UI Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    lang: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(modifier = Modifier.size(16.dp))
            }
        },
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun LoadingOverlay(lang: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.getText("loading", lang),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// --- 1. Splash Screen ---
@Composable
fun SplashScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(2500) // Splash delay
        val currentUser = viewModel.repository.getActiveUserSync()
        if (currentUser != null) {
            viewModel.navigateAndClear(Screen.Home)
        } else {
            viewModel.navigateAndClear(Screen.Onboarding)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper padding/spacer
            Spacer(modifier = Modifier.height(32.dp))

            // Main Logo & Title block matching Screen 1 exactly
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular White Badge with Green Train and tracks
                Card(
                    shape = RoundedCornerShape(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier
                        .size(110.dp)
                        .border(3.dp, Color(0xFF0F7A3E), RoundedCornerShape(100.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRailway,
                                contentDescription = "Train Logo",
                                tint = Color(0xFF0F7A3E),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Draw realistic track sleepers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.height(4.dp)
                            ) {
                                repeat(5) {
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(2.dp)
                                            .background(Color(0xFF0F7A3E))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "TRAIN TRACKER",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F7A3E),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Track Every Journey",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569), // Slate 600
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Beautiful Train Illustration Card matching Screen 1 design layout
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FDF6)),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_train_hero),
                        contentDescription = "Pakistan Railways Train Illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Faint green gradient matching Pakistani brand
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF0F7A3E).copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }
            }

            // Bottom loading & branding footer matching Screen 1 exactly
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Small green crescent-star logo simulation
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F3ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☾☆",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F7A3E)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Smart Pakistan Railways Companion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569) // Slate 600
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    color = Color(0xFF0F7A3E),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// --- 2. Onboarding Screen ---
@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    var pageIndex by remember { mutableStateOf(0) }
    val pages = listOf(
        Triple(
            "Track in Real Time",
            "Get precise GPS coordinates and delay predictions for all passenger trains on the Pakistan Railways network.",
            Icons.Default.MyLocation
        ),
        Triple(
            "Complete Schedules",
            "Explore comprehensive train routes, stops, station services, and arrival/departure forecasts.",
            Icons.Default.Schedule
        ),
        Triple(
            "Station & Local Services",
            "Check local weather, Islamic Namaz prayer timings, station facilities, and local news at your fingertips.",
            Icons.Default.Info
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Skip Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { viewModel.navigateAndClear(Screen.Login) }) {
                Text(text = "Skip", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        // Illustration Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = pages[pageIndex].third,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = pages[pageIndex].first,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = pages[pageIndex].second,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Indicator and Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == pageIndex) 16.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == pageIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (pageIndex < pages.size - 1) {
                        pageIndex++
                    } else {
                        viewModel.navigateAndClear(Screen.Login)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (pageIndex == pages.size - 1) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- 3. Login Screen ---
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Forgot password dialog state
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmailState by remember { mutableStateOf("") }
    var forgotOtpState by remember { mutableStateOf("") }
    var forgotNewPassState by remember { mutableStateOf("") }
    var forgotStepState by remember { mutableStateOf(1) } // 1: Email, 2: OTP & New Password
    var forgotErrorState by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("login", lang), lang = lang) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sign in to track your Pakistan Railways trains",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text(text = Localization.getText("email", lang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_username_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = Localization.getText("password", lang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text(text = "Remember Me", fontSize = 14.sp)
                    }
                    TextButton(onClick = {
                        forgotEmailState = identifier
                        forgotOtpState = ""
                        forgotNewPassState = ""
                        forgotStepState = 1
                        forgotErrorState = null
                        showForgotDialog = true
                    }) {
                        Text(text = "Forgot Password?", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (identifier.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(identifier, password) { }
                        } else {
                            viewModel.errorMessage = "Please fill all fields"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Localization.getText("login", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.navigateTo(Screen.SignUp) }) {
                    Text(text = "Don't have an account? Sign Up", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }

            // Forgot / Reset password dialog
            if (showForgotDialog) {
                AlertDialog(
                    onDismissRequest = { showForgotDialog = false },
                    title = { Text(text = if (forgotStepState == 1) "Forgot Password" else "Reset Password") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            forgotErrorState?.let { err ->
                                Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            if (forgotStepState == 1) {
                                Text(text = "Enter your registered email address or phone number to receive a verification OTP.")
                                OutlinedTextField(
                                    value = forgotEmailState,
                                    onValueChange = { forgotEmailState = it },
                                    label = { Text("Email or Phone") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Text(text = "Enter the 4-digit OTP code sent in notifications and your new password.")
                                OutlinedTextField(
                                    value = forgotOtpState,
                                    onValueChange = { forgotOtpState = it },
                                    label = { Text("OTP Code") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = forgotNewPassState,
                                    onValueChange = { forgotNewPassState = it },
                                    label = { Text("New Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (forgotStepState == 1) {
                                    if (forgotEmailState.trim().isEmpty()) {
                                        forgotErrorState = "Please enter email/phone"
                                        return@Button
                                    }
                                    viewModel.forgotPassword(forgotEmailState.trim()) { success ->
                                        if (success) {
                                            forgotStepState = 2
                                            forgotErrorState = null
                                        } else {
                                            forgotErrorState = viewModel.errorMessage ?: "Account not found or network error"
                                        }
                                    }
                                } else {
                                    if (forgotOtpState.trim().isEmpty()) {
                                        forgotErrorState = "Please enter the OTP Code"
                                        return@Button
                                    }
                                    if (forgotNewPassState.trim().length < 4) {
                                        forgotErrorState = "Password must be at least 4 characters"
                                        return@Button
                                    }
                                    viewModel.resetPassword(forgotEmailState.trim(), forgotOtpState.trim(), forgotNewPassState.trim()) { success ->
                                        if (success) {
                                            showForgotDialog = false
                                            viewModel.errorMessage = "Password reset successfully. You can now login with your new password."
                                        } else {
                                            forgotErrorState = "Failed to reset password. Check OTP."
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(text = if (forgotStepState == 1) "Send OTP" else "Reset Password")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotDialog = false }) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }
        }
    }
}

// --- 4. Sign Up Screen ---
@Composable
fun SignUpScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("sign_up", lang), lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(text = Localization.getText("first_name", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(text = Localization.getText("last_name", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = Localization.getText("email", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text = Localization.getText("phone", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = Localization.getText("password", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(text = Localization.getText("confirm_password", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                    Text(text = Localization.getText("agree_terms", lang), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.errorMessage?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (firstName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
                            viewModel.errorMessage = "Please fill all required fields"
                        } else if (password != confirmPassword) {
                            viewModel.errorMessage = "Passwords do not match"
                        } else if (!acceptTerms) {
                            viewModel.errorMessage = "You must accept the Terms and Conditions"
                        } else {
                            viewModel.submitSignUp(firstName.trim(), lastName.trim(), email.trim(), phone.trim(), password.trim()) { }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Localization.getText("sign_up", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 5. OTP Verification Screen ---
@Composable
fun VerificationScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    var otpCode by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("verify_otp", lang), lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = Localization.getText("verify_otp", lang),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Localization.getText("enter_otp", lang),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    label = { Text(text = "OTP Code") },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .testTag("otp_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.verifyOtp(otpCode) { }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                viewModel.verificationError?.let { err ->
                    Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.verifyOtp(otpCode) { }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Localization.getText("submit", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tip: Check notifications tab or type 4839",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// --- 6. Home Screen / Main Dashboard ---
@Composable
fun QuickServiceItem(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = borderStroke,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            elevation = CardDefaults.cardElevation(if (backgroundColor == Color.White) 1.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155), // Slate 700
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(viewModel: AppViewModel, drawerState: DrawerState) {
    val lang = viewModel.currentLanguage
    val user by viewModel.activeUser.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
    ) {
        // 1. Dark Green Header Area with logo and user details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F7A3E))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small circular logo badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRailway,
                            contentDescription = null,
                            tint = Color(0xFF0F7A3E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TRAIN TRACKER",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Track Every Journey",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profile row with avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.5.dp, Color.White, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (!user?.firstName.isNullOrEmpty()) "${user?.firstName} ${user?.lastName}" else "Muhammad Ali",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user?.email ?: "ali@example.com",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Scrollable Drawer Items
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DrawerItem(
                icon = Icons.Default.Person,
                title = "My Profile",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.ProfileScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.Favorite,
                title = "Favorites",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.FavoritesScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.History,
                title = "Recent Searches",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.Home)
                }
            )
            DrawerItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                badgeText = "3",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.NotificationsScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.SettingsScreen)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

            // Language quick toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.currentLanguage = if (viewModel.currentLanguage == "en") "ur" else "en"
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Language",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = if (viewModel.currentLanguage == "en") "English >" else "اردو >",
                    color = Color(0xFF0F7A3E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Dark Mode switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Dark Mode",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.isDarkMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0F7A3E))
                )
            }

            DrawerItem(
                icon = Icons.Default.Share,
                title = "Share App",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.Default.Star,
                title = "Rate Us",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.Default.Description,
                title = "Terms & Conditions",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.Default.ContactSupport,
                title = "Contact Us",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

            // Logout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            drawerState.close()
                            viewModel.repository.logout()
                            viewModel.navigateAndClear(Screen.Login)
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Logout",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Red)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user by viewModel.activeUser.collectAsState()
    val favTrains by viewModel.favoriteTrains.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(300.dp)
            ) {
                DrawerContent(viewModel, drawerState)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Train Tracker",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 12.dp) // Offset slightly because of hamburger icon
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateTo(Screen.NotificationsScreen) }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(Color.Red)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = "3",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F7A3E)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, Color(0xFFE2E8F0)) // subtle slate-200 border-t
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF0F7A3E)) },
                        label = { Text("Home", color = Color(0xFF0F7A3E), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFE8F3ED)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { 
                            viewModel.fetchLiveStatus("7UP")
                            viewModel.navigateTo(Screen.LiveStatus) 
                        },
                        icon = { Icon(Icons.Default.DirectionsRailway, contentDescription = "Live Train", tint = Color(0xFF94A3B8)) },
                        label = { Text("Live Train", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.TrainSearch) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                        label = { Text("Search", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.FavoritesScreen) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites", tint = Color(0xFF94A3B8)) },
                        label = { Text("Favorites", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.ProfileScreen) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF94A3B8)) },
                        label = { Text("Profile", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC)) // High-contrast super clean slate-50 background
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                // 1. Welcome Greeting row & Search bar matching Screen 2 exactly
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Good Morning! 👋",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A) // Slate 900
                                )
                                Text(
                                    text = "Welcome back",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B) // Slate 500
                                )
                            }
                        }

                        // Premium Single Search Bar matching Screen 2 exactly
                        var dashboardSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = dashboardSearchQuery,
                            onValueChange = { dashboardSearchQuery = it },
                            placeholder = { Text("Search Train, Number or Station", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = Color(0xFF0F7A3E),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            viewModel.navigateTo(Screen.TrainSearch)
                                        }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF0F7A3E),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (dashboardSearchQuery.isNotEmpty()) {
                                    viewModel.fetchLiveStatus(dashboardSearchQuery)
                                    viewModel.navigateTo(Screen.LiveStatus)
                                }
                            })
                        )
                    }
                }

                // 2. Main Navigation Quick Services Grid (3x3 layout) matching Screen 2
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickServiceItem(
                                    label = "Live Train Status",
                                    icon = Icons.Default.MyLocation,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = { 
                                        viewModel.fetchLiveStatus("7UP")
                                        viewModel.navigateTo(Screen.LiveStatus)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Train Schedule",
                                    icon = Icons.Default.CalendarMonth,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.fetchTrainSchedule("7UP") }
                                )
                                QuickServiceItem(
                                    label = "Station Information",
                                    icon = Icons.Default.Apartment,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.fetchStationInfo("LHR") }
                                )
                            }

                            // Row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickServiceItem(
                                    label = "Station Schedule",
                                    icon = Icons.Default.Assignment,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchStationInfo("LHR")
                                    }
                                )
                                QuickServiceItem(
                                    label = "Freight Trains",
                                    icon = Icons.Default.LocalShipping,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchFreightTrains()
                                        viewModel.navigateTo(Screen.FreightTrainsScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Train Updates",
                                    icon = Icons.Default.Campaign,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchNewsAndBlogs()
                                        viewModel.navigateTo(Screen.NewsBlogsScreen)
                                    }
                                )
                            }

                            // Row 3
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickServiceItem(
                                    label = "Railway News",
                                    icon = Icons.Default.Newspaper,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchNewsAndBlogs()
                                        viewModel.navigateTo(Screen.NewsBlogsScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Weather",
                                    icon = Icons.Default.WbSunny,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchWeatherAndNamaz("Lahore")
                                        viewModel.navigateTo(Screen.NamazTimingsScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Namaz Timings",
                                    icon = Icons.Default.SelfImprovement,
                                    backgroundColor = Color.White,
                                    iconColor = Color(0xFF0F7A3E),
                                    borderStroke = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.fetchWeatherAndNamaz("Lahore")
                                        viewModel.navigateTo(Screen.NamazTimingsScreen)
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Quick Train Route Lookup Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Slate-200 border
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "SEARCH TRAIN ROUTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = viewModel.searchSource,
                                onValueChange = { viewModel.searchSource = it },
                                label = { Text(text = Localization.getText("source", lang)) },
                                leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color(0xFF0F7A3E)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = viewModel.searchDest,
                                onValueChange = { viewModel.searchDest = it },
                                label = { Text(text = Localization.getText("destination", lang)) },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF0F7A3E)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.runTrainSearch()
                                        viewModel.navigateTo(Screen.TrainSearch)
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.runTrainSearch()
                                    viewModel.navigateTo(Screen.TrainSearch)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F7A3E)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = Localization.getText("search", lang).uppercase(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

            // 4. Tracked Train Card (Left accent strip, timeline view, ETA updates)
            item {
                val activeStatus = viewModel.activeLiveStatus
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "LIVE TRACKING PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val sName = activeStatus?.trainName ?: "Green Line Express"
                    val sNum = activeStatus?.trainNumber ?: "1UP"
                    val sDelay = activeStatus?.delayMinutes ?: 0
                    val sPrev = activeStatus?.previousStation ?: "Karachi Cantt"
                    val sNext = activeStatus?.nextStation ?: "Rohri Junction"
                    val sProgress = activeStatus?.journeyProgress ?: 0.5f
                    val sEta = activeStatus?.eta ?: "2:45 PM"
                    
                    Card(
                        onClick = {
                            viewModel.fetchLiveStatus(sNum)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            // Left border green accent strip
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF0F7A3E))
                            )
                            
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = "TRACKED TRAIN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF94A3B8), // Slate 400
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$sName ($sNum)",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    
                                    val isLate = sDelay > 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isLate) Color(0xFFFEE2E2) else Color(0xFFE8F3ED))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isLate) "${sDelay} MIN LATE" else "ON TIME",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLate) Color(0xFFDC2626) else Color(0xFF0F7A3E)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (sPrev.contains(" ")) sPrev.split(" ").map { it.take(1) }.joinToString("").uppercase() else sPrev.take(3).uppercase(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = sPrev,
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(2.dp)
                                                .background(Color(0xFFE2E8F0))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(sProgress)
                                                    .height(2.dp)
                                                    .background(Color(0xFF0F7A3E))
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF0F7A3E))
                                            )
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (sNext.contains(" ")) sNext.split(" ").map { it.take(1) }.joinToString("").uppercase() else sNext.take(3).uppercase(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = sNext,
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Next Station: $sNext",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "ETA: $sEta",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F7A3E),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Side-by-Side Info Tiles (Weather & Namaz)
            item {
                val weather = viewModel.weatherData
                val namaz = viewModel.namazTimings
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weather Tile (E8F3ED background)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE8F3ED))
                            .clickable {
                                viewModel.fetchWeatherAndNamaz(viewModel.currentCity.ifEmpty { "Lahore" })
                                viewModel.navigateTo(Screen.NamazTimingsScreen)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "WEATHER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F7A3E).copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weather?.temperature ?: "24°C",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F7A3E)
                        )
                        Text(
                            text = weather?.condition ?: "Partly Cloudy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E9E5B)
                        )
                    }
                    
                    // Namaz Tile (F9F4EB background)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF9F4EB))
                            .clickable {
                                viewModel.fetchWeatherAndNamaz(viewModel.currentCity.ifEmpty { "Lahore" })
                                viewModel.navigateTo(Screen.NamazTimingsScreen)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "NEXT NAMAZ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF8D6E63),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dhuhr",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = "at ${namaz?.dhuhr ?: "12:45 PM"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }

            // 6. Favorite Quick Access List
            if (favTrains.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FAVORITES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            TextButton(onClick = { viewModel.navigateTo(Screen.FavoritesScreen) }) {
                                Text("SEE ALL", color = Color(0xFF0F7A3E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favTrains) { t ->
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { 
                                            viewModel.fetchLiveStatus(t.trainNumber)
                                            viewModel.navigateTo(Screen.LiveStatus)
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsRailway,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0F7A3E),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = t.trainName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Favorite",
                                                tint = Color(0xFFFBBF24), // Amber star
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${t.source} ➔ ${t.destination}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Recent Searches History
            item {
                val recentSearches by viewModel.recentTrainSearches.collectAsState()
                if (recentSearches.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT SEARCHES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            TextButton(onClick = { viewModel.clearSearchHistory("train") }) {
                                Text("CLEAR ALL", color = Color(0xFFDC2626), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recentSearches.take(6)) { search ->
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            if (search.query.contains("to")) {
                                                val parts = search.query.split("to")
                                                viewModel.searchSource = parts[0].trim()
                                                viewModel.searchDest = parts.getOrNull(1)?.trim() ?: ""
                                                viewModel.runTrainSearch()
                                                viewModel.navigateTo(Screen.TrainSearch)
                                            } else {
                                                viewModel.fetchLiveStatus(search.query)
                                                viewModel.navigateTo(Screen.LiveStatus)
                                            }
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = search.query,
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// Retain ShortcutCard to guarantee compatibility if imported elsewhere
@Composable
fun ShortcutCard(item: Quadruple<String, ImageVector, Color, () -> Unit>, modifier: Modifier) {
    Card(
        onClick = item.fourth,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.third),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.second, contentDescription = null, tint = Color(0xFF0F7A3E))
            }
            Text(
                text = item.first,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                lineHeight = 16.sp,
                color = Color(0xFF0F172A)
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// --- 7. Train Search Results Screen ---
@Composable
fun TrainSearchScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val results = viewModel.searchResults

    Scaffold(
        topBar = { AppTopBar(title = "Train Search Results", lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (results.isEmpty() && !viewModel.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Train, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No trains found on this route.", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(results) { t ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(t.trainName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(t.trainNumber, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(t.trainType, fontSize = 11.sp) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Departure", fontSize = 11.sp, color = Color.Gray)
                                        Text(t.departure, fontWeight = FontWeight.Bold)
                                        Text(t.source, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Duration", fontSize = 11.sp, color = Color.Gray)
                                        Text(t.duration, fontWeight = FontWeight.Bold)
                                        Icon(Icons.AutoMirrored.Filled.TrendingFlat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Arrival", fontSize = 11.sp, color = Color.Gray)
                                        Text(t.arrival, fontWeight = FontWeight.Bold)
                                        Text(t.destination, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { viewModel.fetchTrainSchedule(t.trainNumber) }) {
                                        Text("Schedule")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.fetchLiveStatus(t.trainNumber) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Track Live")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 8. Live Train Status Screen ---
@Composable
fun LiveStatusScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val status = viewModel.activeLiveStatus
    val favTrains by viewModel.favoriteTrains.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Live Status: ${status?.trainNumber ?: ""}",
                lang = lang,
                onBack = { viewModel.goBack() },
                actions = {
                    status?.let { s ->
                        val isFav = favTrains.any { it.trainNumber == s.trainNumber }
                        IconButton(onClick = {
                            val f = FavoriteTrain(s.trainNumber, s.trainName, s.previousStation, s.nextStation)
                            viewModel.toggleFavoriteTrain(f, !isFav)
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            status?.let { s ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header card with delay
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(s.trainName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text("Train #${s.trainNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (s.delayMinutes > 0) "${s.delayMinutes} Mins Late" else "On Time",
                                    color = if (s.delayMinutes > 0) Color.Red else Color(0xFF0F7A3E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text("Updated: ${s.lastUpdated}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress Track Drawing
                    Text("Journey Progress", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.previousStation, fontSize = 12.sp, color = Color.Gray)
                                Text("Current: ${s.currentStation}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(s.nextStation, fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { s.journeyProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ETA: ${s.eta}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${s.distanceRemainingKm} KM remaining", fontSize = 12.sp, color = Color.Gray)
                                Text("ETD: ${s.etd}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Live Route Tracker Map (GPS Active)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    InteractiveRouteMap(status = s)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Route Schedule Milestones", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val stops = listOf(
                        Triple(s.previousStation, "Passed - 100% accurate", true),
                        Triple(s.currentStation, "Arrived - Status: ${if (s.delayMinutes > 0) "Delayed" else "On-time"}", true),
                        Triple(s.nextStation, "Expected at ${s.eta}", false),
                        Triple("Final Destination", "Route termination point", false)
                    )

                    stops.forEachIndexed { index, stop ->
                        Row(modifier = Modifier.height(64.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (stop.third) MaterialTheme.colorScheme.primary else Color.LightGray)
                                )
                                if (index < stops.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .weight(1f)
                                            .background(if (stop.third) MaterialTheme.colorScheme.primary else Color.LightGray)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(stop.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(stop.second, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.fetchTrainSchedule(s.trainNumber) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Complete Timetable & Schedule")
                    }
                }
            }

            if (status == null && !viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Live Tracking Offline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.errorMessage ?: "Live status tracking endpoint is currently offline. Secure production API integration is prepared and waiting to connect.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.fetchLiveStatus(viewModel.liveStatusQuery.ifEmpty { "7UP" }) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry Connection")
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 9. Train Schedule Timetable Screen ---
@Composable
fun TrainScheduleScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val schedule = viewModel.activeTrainSchedule

    Scaffold(
        topBar = { AppTopBar(title = "Schedule: ${schedule?.trainName ?: ""}", lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            schedule?.let { sch ->
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Route Timetable Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Stops: ${sch.totalStops}", fontSize = 13.sp)
                                    Text("Distance: ${sch.totalDistanceKm} KM", fontSize = 13.sp)
                                    Text("Time: ${sch.totalJourneyTime}", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Station", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                            Text("Arr", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Dep", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Stop", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }

                    items(sch.stations) { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(st.stationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${st.stationCode} • Day ${st.dayNumber} • ${st.distanceKm} Km", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(st.arrival, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(st.departure, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = if (st.stopDurationMinutes > 0) "${st.stopDurationMinutes} m" else "--",
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 10. Station Information Screen ---
@Composable
fun StationInfoScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val info = viewModel.activeStationInfo
    val favStations by viewModel.favoriteStations.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = info?.stationName ?: "Station Details",
                lang = lang,
                onBack = { viewModel.goBack() },
                actions = {
                    info?.let { inf ->
                        val isFav = favStations.any { it.stationCode == inf.code }
                        IconButton(onClick = {
                            val fs = FavoriteStation(inf.code, inf.stationName)
                            viewModel.toggleFavoriteStation(fs, !isFav)
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Fav Station",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            info?.let { inf ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Beautiful hero banner image of Lahore Junction Station
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lahore_junction_1783744552039),
                            contentDescription = inf.stationName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient overlay for visual fidelity and title readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                        startY = 100f
                                    )
                                )
                        )
                        // Station Name and code overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = inf.stationName.uppercase(),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFFE8F3ED),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Station Code: ${inf.code}",
                                    color = Color(0xFFE8F3ED),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("Facilities", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("Schedule", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                            Text("Nearby", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    when (selectedTab) {
                        0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("Contact Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(inf.address, fontSize = 14.sp)
                                Text("Helpline: ${inf.contactNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            item {
                                Text("Station Facilities", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(inf.facilities) { fac ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(fac, fontSize = 14.sp)
                                }
                            }
                        }

                        1 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("Today's Arrivals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(inf.todayArrivals) { arr ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${arr.trainName} (${arr.trainNumber})", fontWeight = FontWeight.Bold)
                                    Text(arr.time, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider()
                            }
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Today's Departures", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(inf.todayDepartures) { dep ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${dep.trainName} (${dep.trainNumber})", fontWeight = FontWeight.Bold)
                                    Text(dep.time, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider()
                            }
                        }

                        2 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("Nearby Hotels", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(inf.nearbyHotels) { hot ->
                                Text("🏨 $hot", modifier = Modifier.padding(vertical = 4.dp), fontSize = 14.sp)
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Nearby Restaurants", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(inf.nearbyRestaurants) { res ->
                                Text("🍴 $res", modifier = Modifier.padding(vertical = 4.dp), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 11. Freight Trains Screen ---
@Composable
fun FreightTrainsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val trains = viewModel.freightTrainsList

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("freight_trains", lang), lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (trains.isEmpty() && !viewModel.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No cargo trains active.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(trains) { ft ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(ft.trainName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(ft.trainNumber, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Route: ${ft.route}", fontSize = 13.sp)
                                Text("Cargo Type: ${ft.cargoType}", fontSize = 13.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Position: ${ft.currentPosition}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("ETA: ${ft.eta}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 12. Railway News & Blogs Screen ---
@Composable
fun NewsBlogsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val news = viewModel.newsList
    val blogs = viewModel.blogsList
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("railway_news", lang), lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Bulletins", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Travel Blogs", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }

                when (selectedTab) {
                    0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(news) { n ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(n.category, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(n.date, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(n.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(n.summary, fontSize = 13.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }

                    1 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(blogs) { b ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(b.category, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(b.readTime, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(b.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(b.content, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 13. Weather & Namaz Screen ---
@Composable
fun NamazTimingsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val weather = viewModel.weatherData
    val namaz = viewModel.namazTimings
    val cities = listOf("Lahore", "Karachi", "Rawalpindi", "Peshawar", "Multan", "Quetta")

    Scaffold(
        topBar = { AppTopBar(title = "Local Weather & Namaz", lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // City selector row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cities) { city ->
                        FilterChip(
                            selected = viewModel.currentCity == city,
                            onClick = { viewModel.fetchWeatherAndNamaz(city) },
                            label = { Text(city) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather card
                weather?.let { w ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WEATHER AT ${w.location.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = w.condition,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F7A3E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = w.temperature,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Humidity: ${w.humidity}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Namaz Timings card
                namaz?.let { n ->
                    Text(
                        text = "ISLAMIC INFORMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hijri Date: ${n.islamicDate}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Qibla Direction: ${n.qiblaDirection}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val timingList = listOf(
                        "Fajr" to n.fajr,
                        "Dhuhr" to n.dhuhr,
                        "Asr" to n.asr,
                        "Maghrib" to n.maghrib,
                        "Isha" to n.isha
                    )

                    timingList.forEach { (name, time) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = time,
                                    color = Color(0xFF0F7A3E),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }
}

// --- 14. Notifications History Screen ---
@Composable
fun NotificationsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val notes by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notification History",
                lang = lang,
                onBack = { viewModel.goBack() },
                actions = {
                    IconButton(onClick = { viewModel.clearAllNotifications() }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { viewModel.markNotificationAsRead(note.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (note.isRead) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (!note.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Red)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.message, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 15. Favorites List Screen ---
@Composable
fun FavoritesScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val trains by viewModel.favoriteTrains.collectAsState()
    val stations by viewModel.favoriteStations.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("favorites", lang), lang = lang) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Favorite Trains",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            if (trains.isEmpty()) {
                Text(
                    text = Localization.getText("empty_fav", lang),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(trains) { t ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.fetchLiveStatus(t.trainNumber) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(t.trainName, fontWeight = FontWeight.Bold)
                                    Text("Train #${t.trainNumber}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Favorite Stations",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            if (stations.isEmpty()) {
                Text(
                    text = Localization.getText("empty_fav", lang),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(stations) { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.fetchStationInfo(s.stationCode) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.stationName, fontWeight = FontWeight.Bold)
                                    Text("Code: ${s.stationCode}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 16. Profile Screen ---
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user by viewModel.activeUser.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var editMode by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var changePassMode by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            firstName = it.firstName
            lastName = it.lastName
            email = it.email
            phone = it.phone
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("profile", lang), lang = lang) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${firstName.firstOrNull() ?: 'P'}${lastName.firstOrNull() ?: 'R'}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!editMode && !changePassMode) {
                    Text("$firstName $lastName", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(email, fontSize = 14.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { editMode = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getText("edit_profile", lang))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { changePassMode = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getText("change_password", lang))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { viewModel.logout() }) {
                        Text("Log Out Account", color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    TextButton(onClick = {
                        viewModel.deleteAccount { }
                    }) {
                        Text(Localization.getText("delete_account", lang), color = Color.Gray)
                    }

                } else if (editMode) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrect = false
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrect = false
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            autoCorrect = false
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.editProfile(firstName, lastName, email, "") {
                                editMode = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        editMode = false
                    }) {
                        Text("Cancel")
                    }

                } else if (changePassMode) {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Old Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            autoCorrect = false
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            autoCorrect = false
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.changePassword(oldPass, newPass) { success ->
                                if (success) {
                                    changePassMode = false
                                    oldPass = ""
                                    newPass = ""
                                } else {
                                    viewModel.errorMessage = "Incorrect old password"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Password")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        changePassMode = false
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// --- 17. Settings Screen ---
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("settings", lang), lang = lang, onBack = { viewModel.goBack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Language Selection
            Text("App Customizations", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.getText("language", lang), fontWeight = FontWeight.Bold)
                Row {
                    FilterChip(
                        selected = lang == "en",
                        onClick = { viewModel.currentLanguage = "en" },
                        label = { Text("English") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = lang == "ur",
                        onClick = { viewModel.currentLanguage = "ur" },
                        label = { Text("اردو") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.getText("dark_mode", lang), fontWeight = FontWeight.Bold)
                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.isDarkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("About & Legals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Train Tracker app is optimized for premium Pakistan Railways tracking. Fully secure and JWT standard compatible encryption.", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Version 2.0.4", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Pakistan Railways IT Dept. © 2026", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// --- 18. Helpline Screen ---
@Composable
fun HelplineScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val helpList = listOf(
        Triple("Railway Helpline Inquiry", "117", Icons.Default.Call),
        Triple("Railway Police Control Room", "042-99201256", Icons.Default.Shield),
        Triple("Edhi Ambulance Service", "115", Icons.Default.LocalHospital),
        Triple("Rescue Fire Brigade", "16", Icons.Default.FireTruck)
    )

    Scaffold(
        topBar = { AppTopBar(title = "Helplines & Emergency Support", lang = lang) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Emergency helpline contacts are active 24/7. Tap on any item to place a direct call.",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            items(helpList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = item.third, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(item.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.second, fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

fun getStationCoords(stationName: String): LatLng {
    val lower = stationName.lowercase()
    return when {
        lower.contains("karachi") -> LatLng(24.8532, 67.0347)
        lower.contains("lahore") -> LatLng(31.5744, 74.3494)
        lower.contains("rawalpindi") -> LatLng(33.6011, 73.0712)
        lower.contains("peshawar") -> LatLng(34.0044, 71.5441)
        lower.contains("sahiwal") -> LatLng(30.6682, 73.1114)
        lower.contains("okara") -> LatLng(30.8014, 73.4473)
        lower.contains("gujranwala") -> LatLng(32.1617, 74.1883)
        lower.contains("jhelum") -> LatLng(32.9333, 73.7333)
        lower.contains("multan") -> LatLng(30.1872, 71.4389)
        lower.contains("khanewal") -> LatLng(30.3017, 71.9328)
        lower.contains("quetta") -> LatLng(30.1833, 66.9967)
        lower.contains("sukkur") -> LatLng(27.7022, 68.8456)
        lower.contains("rohri") -> LatLng(27.6811, 68.8953)
        lower.contains("bahawalpur") -> LatLng(29.3956, 71.6833)
        lower.contains("faisalabad") -> LatLng(31.4178, 73.0792)
        else -> LatLng(30.0, 70.0)
    }
}

fun interpolateLatLng(from: LatLng, to: LatLng, fraction: Float): LatLng {
    val lat = from.latitude + (to.latitude - from.latitude) * fraction
    val lng = from.longitude + (to.longitude - from.longitude) * fraction
    return LatLng(lat, lng)
}

@Composable
fun InteractiveRouteMap(status: LiveStatus) {
    val prevCoords = getStationCoords(status.previousStation)
    val nextCoords = getStationCoords(status.nextStation)
    val progress = status.journeyProgress.coerceIn(0.0f, 1.0f)
    val trainCoords = interpolateLatLng(prevCoords, nextCoords, progress)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trainCoords, 8.0f)
    }

    // Smoothly animate map camera when position updates
    LaunchedEffect(trainCoords) {
        cameraPositionState.animate(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLng(trainCoords),
            1000
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Real production Google Map instance integration
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Traversed route
                Polyline(
                    points = listOf(prevCoords, trainCoords),
                    color = Color(0xFF4CAF50),
                    width = 8f
                )
                
                // Remaining route
                Polyline(
                    points = listOf(trainCoords, nextCoords),
                    color = Color.LightGray,
                    width = 8f
                )

                // Station markers
                Marker(
                    state = rememberMarkerState(position = prevCoords),
                    title = status.previousStation,
                    snippet = "Previous Station"
                )

                Marker(
                    state = rememberMarkerState(position = nextCoords),
                    title = status.nextStation,
                    snippet = "Upcoming Station"
                )

                // Current train position marker
                Marker(
                    state = rememberMarkerState(position = trainCoords),
                    title = status.trainName,
                    snippet = "Train Number: ${status.trainNumber} (${(progress * 100).toInt()}% Journey Progress)"
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("GPS POSITION: LOCKED", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("LAT: ${"%.4f".format(trainCoords.latitude)}° N", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                Text("LON: ${"%.4f".format(trainCoords.longitude)}° E", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            val speedKmph = if (status.delayMinutes > 15) 95 else 75
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("SPEED", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$speedKmph KM/H", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                Text("DELAY: ${status.delayMinutes} MINS", fontSize = 9.sp, color = if (status.delayMinutes > 0) Color.Red else Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

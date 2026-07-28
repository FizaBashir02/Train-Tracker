package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.data.repository.LocalTrainData
import com.example.data.service.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.ui.common.Localization
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.ui.theme.MyApplicationTheme

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val darkTheme = viewModel.isDarkMode
    val context = LocalContext.current
    val layoutDirection = if (viewModel.currentLanguage == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

    BackHandler(enabled = viewModel.canGoBack) {
        viewModel.goBack()
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MyApplicationTheme(darkTheme = darkTheme) {
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
                        Screen.TrainSearch -> TrainScheduleScreen(viewModel)
                        Screen.TrainScheduleScreen -> TrainScheduleScreen(viewModel)
                        Screen.TrainDetailScreen -> TrainDetailScreen(viewModel)
                        Screen.StationInfoScreen -> StationInfoScreen(viewModel)
                        Screen.RouteScreen -> RouteScreen(viewModel)
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
}

// --- Common UI Components ---

@Composable
fun AppBottomNavigation(currentScreen: Screen, viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { viewModel.navigateTo(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(Localization.getText("home", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.TrainScheduleScreen || currentScreen == Screen.TrainSearch,
            onClick = {
                viewModel.loadTrainsSchedule()
                viewModel.navigateTo(Screen.TrainScheduleScreen)
            },
            icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule") },
            label = { Text(Localization.getText("train_schedule", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.StationInfoScreen,
            onClick = {
                viewModel.loadStations()
                viewModel.navigateTo(Screen.StationInfoScreen)
            },
            icon = { Icon(Icons.Default.Place, contentDescription = "Stations") },
            label = { Text(Localization.getText("station_info", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.FavoritesScreen,
            onClick = {
                viewModel.navigateTo(Screen.FavoritesScreen)
            },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text(Localization.getText("favorites", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.ProfileScreen || currentScreen == Screen.SettingsScreen,
            onClick = { viewModel.navigateTo(Screen.ProfileScreen) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text(Localization.getText("profile", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
    }
}

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
        delay(1000) // Splash delay
        viewModel.navigateAndClear(Screen.Home)
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
                            // Draw realistic railway sleepers
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
                    text = "TRAIN SCHEDULE",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F7A3E),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Schedule Every Journey",
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
            "Explore Train Schedules",
            "Browse comprehensive departure and arrival schedules for all passenger trains on the Pakistan Railways network.",
            Icons.Default.Schedule
        ),
        Triple(
            "Complete Schedules",
            "Explore comprehensive train routes, stops, station services, and arrival/departure forecasts.",
            Icons.Default.Schedule
        ),
        Triple(
            "Station & Local Services",
            "Check local weather, Islamic prayer timings, station facilities, and local news at your fingertips.",
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

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                    text = "Sign in to view Pakistan Railways train schedules",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text(text = "Email or Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_username_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
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
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text(text = "Remember Me", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.successMessage?.let { successMsg ->
                    Text(
                        text = successMsg,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

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
                            viewModel.login(identifier.trim(), password) { }
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
                        val cleanEmail = email.trim()
                        val cleanPhone = phone.trim().replace(Regex("[\\s\\-]"), "")
                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
                        val isPhoneValid = cleanPhone.matches(Regex("^((\\+923|923|03)\\d{9})$"))

                        if (firstName.trim().isEmpty() || lastName.trim().isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || password.isEmpty()) {
                            viewModel.errorMessage = "Please fill all required fields"
                        } else if (!isEmailValid) {
                            viewModel.errorMessage = "Please enter a valid email address"
                        } else if (!isPhoneValid) {
                            viewModel.errorMessage = "Please enter a valid Pakistani mobile number (e.g. 03001234567)"
                        } else if (password.length < 8) {
                            viewModel.errorMessage = "Password must be at least 8 characters"
                        } else if (password != confirmPassword) {
                            viewModel.errorMessage = "Passwords do not match"
                        } else if (!acceptTerms) {
                            viewModel.errorMessage = "You must accept the Terms and Conditions"
                        } else {
                            viewModel.submitSignUp(firstName.trim(), lastName.trim(), cleanEmail, cleanPhone, password) { }
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
            colors = CardDefaults.cardColors(containerColor = if (backgroundColor == Color.White) MaterialTheme.colorScheme.surface else backgroundColor),
            border = borderStroke ?: BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            elevation = CardDefaults.cardElevation(1.dp)
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
            color = MaterialTheme.colorScheme.onSurface,
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
            .background(MaterialTheme.colorScheme.surface)
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
                            text = "TRAIN SCHEDULE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Schedule Every Journey",
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
                icon = Icons.Default.Home,
                title = "Home",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.Home)
                }
            )
            DrawerItem(
                icon = Icons.Default.CalendarMonth,
                title = "Schedules",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.loadTrainsSchedule()
                    viewModel.navigateTo(Screen.TrainScheduleScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.Place,
                title = "Stations",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.loadStations()
                    viewModel.navigateTo(Screen.StationInfoScreen)
                }
            )
            DrawerItem(
                icon = Icons.AutoMirrored.Filled.AltRoute,
                title = "Routes",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.loadRoutes()
                    viewModel.navigateTo(Screen.RouteScreen)
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
                icon = Icons.Default.WbSunny,
                title = "Weather & Prayer Timings",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.fetchWeatherAndNamaz("Lahore")
                    viewModel.navigateTo(Screen.NamazTimingsScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.Person,
                title = "My Profile",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.ProfileScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.LocalShipping,
                title = "Freight Trains",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.fetchFreightTrains()
                    viewModel.navigateTo(Screen.FreightTrainsScreen)
                }
            )
            DrawerItem(
                icon = Icons.Default.Campaign,
                title = "News & Updates",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.fetchNewsAndBlogs()
                    viewModel.navigateTo(Screen.NewsBlogsScreen)
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
                icon = Icons.Default.Phone,
                title = "Emergency Helplines",
                onClick = {
                    scope.launch { drawerState.close() }
                    viewModel.navigateTo(Screen.HelplineScreen)
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
                        viewModel.updateLanguage(if (viewModel.currentLanguage == "en") "ur" else "en")
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Localization.getText("language", lang),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = if (viewModel.currentLanguage == "en") "English >" else "اردو >",
                    color = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Localization.getText("dark_mode", lang),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
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
                title = "About Us",
                onClick = {
                    scope.launch { drawerState.close() }
                }
            )
            DrawerItem(
                icon = Icons.AutoMirrored.Filled.ContactSupport,
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
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Log Out",
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
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

    LaunchedEffect(Unit) {
        if (viewModel.trainsList.isEmpty()) {
            viewModel.loadTrainsSchedule()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
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
                                text = Localization.getText("app_name", lang),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateTo(Screen.NotificationsScreen) }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = MaterialTheme.colorScheme.onPrimary
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
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                AppBottomNavigation(currentScreen = Screen.Home, viewModel = viewModel)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                // 1. Welcome Greeting row & Search bar
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
                                    text = if (lang == "ur") "خوش آمدید! 👋" else "Good Morning! 👋",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = Localization.getText("app_name", lang),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Single Search Bar
                        var dashboardSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = dashboardSearchQuery,
                            onValueChange = { dashboardSearchQuery = it },
                            placeholder = { Text(Localization.getText("search_train", lang), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.primary,
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
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (dashboardSearchQuery.isNotEmpty()) {
                                    viewModel.filterOptions = viewModel.filterOptions.copy(query = dashboardSearchQuery)
                                    viewModel.loadTrainsSchedule()
                                    viewModel.navigateTo(Screen.TrainScheduleScreen)
                                }
                            })
                        )
                    }
                }

                // 2. Quick Access Services Grid
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1 - Core Scheduling Features
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickServiceItem(
                                    label = "Train Schedule",
                                    icon = Icons.Default.CalendarMonth,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    onClick = { 
                                        viewModel.loadTrainsSchedule()
                                        viewModel.navigateTo(Screen.TrainScheduleScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Stations",
                                    icon = Icons.Default.Apartment,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.selectedStation = null
                                        viewModel.loadStations()
                                        viewModel.navigateTo(Screen.StationInfoScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Routes",
                                    icon = Icons.AutoMirrored.Filled.AltRoute,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.loadRoutes()
                                        viewModel.navigateTo(Screen.RouteScreen)
                                    }
                                )
                            }

                            // Row 2 - Secondary Options
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickServiceItem(
                                    label = "Favorites",
                                    icon = Icons.Default.Favorite,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.navigateTo(Screen.FavoritesScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Freight",
                                    icon = Icons.Default.LocalShipping,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.filterOptions = viewModel.filterOptions.copy(trainType = "Freight")
                                        viewModel.loadTrainsSchedule()
                                        viewModel.navigateTo(Screen.TrainScheduleScreen)
                                    }
                                )
                                QuickServiceItem(
                                    label = "Weather & Namaz",
                                    icon = Icons.Default.WbSunny,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
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

                // 3. Quick Train Route Search Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "SEARCH TRAIN SCHEDULE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = viewModel.filterOptions.source,
                                onValueChange = { viewModel.filterOptions = viewModel.filterOptions.copy(source = it) },
                                label = { Text(text = Localization.getText("source", lang)) },
                                leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = viewModel.filterOptions.destination,
                                onValueChange = { viewModel.filterOptions = viewModel.filterOptions.copy(destination = it) },
                                label = { Text(text = Localization.getText("destination", lang)) },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.loadTrainsSchedule()
                                        viewModel.navigateTo(Screen.TrainScheduleScreen)
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.loadTrainsSchedule()
                                    viewModel.navigateTo(Screen.TrainScheduleScreen)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "SEARCH SCHEDULES", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. TODAY'S TRAINS SCHEDULE SECTION (Arrivals & Departures)
                item {
                    val todayTrains = viewModel.trainsList.ifEmpty { LocalTrainData.getDummyTrains() }.take(6)
                    TodayTrains(
                        trains = todayTrains,
                        onTrainClick = { trainNum ->
                            viewModel.selectTrainDetails(trainNum)
                        },
                        onViewAllClick = {
                            viewModel.loadTrainsSchedule()
                            viewModel.navigateTo(Screen.TrainScheduleScreen)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
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
                        // Weather Tile
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
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
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = weather?.temperature ?: "24°C",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = weather?.condition ?: "Partly Cloudy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        // Namaz Tile
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .clickable {
                                    viewModel.fetchWeatherAndNamaz(viewModel.currentCity.ifEmpty { "Lahore" })
                                    viewModel.navigateTo(Screen.NamazTimingsScreen)
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "NEXT PRAYER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dhuhr",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "at ${namaz?.dhuhr ?: "12:45 PM"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
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
                                    text = "FAVORITE SCHEDULES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                TextButton(onClick = { viewModel.navigateTo(Screen.FavoritesScreen) }) {
                                    Text("SEE ALL", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(favTrains) { t ->
                                    Card(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clickable { 
                                                viewModel.selectTrainDetails(t.trainNumber)
                                            },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
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
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = t.trainName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Favorite",
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "${t.source} ➔ ${t.destination}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                TextButton(onClick = { viewModel.clearSearchHistory("train") }) {
                                    Text("CLEAR ALL", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(recentSearches.take(6)) { search ->
                                    Card(
                                        modifier = Modifier.clickable {
                                            if (search.query.contains("to")) {
                                                val parts = search.query.split("to")
                                                viewModel.filterOptions = viewModel.filterOptions.copy(
                                                    source = parts[0].trim(),
                                                    destination = parts.getOrNull(1)?.trim() ?: ""
                                                )
                                                viewModel.loadTrainsSchedule()
                                                viewModel.navigateTo(Screen.TrainScheduleScreen)
                                            } else {
                                                viewModel.filterOptions = viewModel.filterOptions.copy(query = search.query)
                                                viewModel.loadTrainsSchedule()
                                                viewModel.navigateTo(Screen.TrainScheduleScreen)
                                            }
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = search.query,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
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
    val results = viewModel.trainsList

    LaunchedEffect(Unit) {
        if (results.isEmpty()) {
            viewModel.loadTrainsSchedule()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Train Search Results", lang = lang, onBack = { viewModel.goBack() }) },
        bottomBar = { AppBottomNavigation(currentScreen = Screen.TrainSearch, viewModel = viewModel) }
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
                            onClick = { viewModel.selectTrainDetails(t.trainNumber) },
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
                                        Text("Train #${t.trainNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                                        Text(t.departureTime, fontWeight = FontWeight.Bold)
                                        Text(t.sourceStation, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Duration", fontSize = 11.sp, color = Color.Gray)
                                        Text(t.duration, fontWeight = FontWeight.Bold)
                                        Icon(Icons.AutoMirrored.Filled.TrendingFlat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Arrival", fontSize = 11.sp, color = Color.Gray)
                                        Text(t.arrivalTime, fontWeight = FontWeight.Bold)
                                        Text(t.destinationStation, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status: ${t.status}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (t.status == "On Time") Color(0xFF0F7A3E) else Color.Red,
                                        fontSize = 12.sp
                                    )
                                    Button(
                                        onClick = { viewModel.selectTrainDetails(t.trainNumber) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("View Schedule")
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

// --- TodayTrains Component ---
@Composable
fun TodayTrains(
    trains: List<TrainScheduleItem>,
    onTrainClick: (String) -> Unit,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TODAY'S TRAIN SCHEDULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )
            }
            TextButton(onClick = onViewAllClick) {
                Text("VIEW ALL (50+)", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            trains.forEach { train ->
                Card(
                    onClick = { onTrainClick(train.trainNumber) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${train.trainName} (${train.trainNumber})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${train.sourceStation} ➔ ${train.destinationStation}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (train.status.uppercase()) {
                                                "ON TIME" -> MaterialTheme.colorScheme.primaryContainer
                                                "BOARDING" -> Color(0xFFFEF3C7)
                                                "DEPARTED" -> Color(0xFFE2E8F0)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = train.status.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (train.status.uppercase()) {
                                            "ON TIME" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "BOARDING" -> Color(0xFF92400E)
                                            "DEPARTED" -> Color(0xFF475569)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DepartureBoard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Dep: ${train.departureTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Arr: ${train.arrivalTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = train.platform,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 9. Train Schedule Timetable Screen ---
@Composable
fun TrainScheduleScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val filter = viewModel.filterOptions
    val trains = viewModel.trainsList

    LaunchedEffect(Unit) {
        if (trains.isEmpty()) {
            viewModel.loadTrainsSchedule()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = Localization.getText("train_schedule", lang),
                lang = lang,
                onBack = { viewModel.goBack() }
            )
        },
        bottomBar = {
            AppBottomNavigation(currentScreen = Screen.TrainScheduleScreen, viewModel = viewModel)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = filter.query,
                onValueChange = { q -> viewModel.loadTrainsSchedule(filter.copy(query = q)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(Localization.getText("search_train", lang), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.loadTrainsSchedule(filter.copy(query = "")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Type Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("All", "Express", "Passenger", "Freight")
                items(types) { type ->
                    FilterChip(
                        selected = filter.trainType == type,
                        onClick = { viewModel.loadTrainsSchedule(filter.copy(trainType = type)) },
                        label = { Text(type, fontSize = 12.sp) }
                    )
                }
            }

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("All", "On Time", "Delayed", "Boarding Soon", "Departed", "Arrived")
                items(statuses) { status ->
                    FilterChip(
                        selected = filter.status == status,
                        onClick = { viewModel.loadTrainsSchedule(filter.copy(status = status)) },
                        label = { Text(status, fontSize = 12.sp) }
                    )
                }
            }

            // Sorting Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${trains.size} Trains Found", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("Departure", "Duration", "Fare").forEach { sort ->
                        Text(
                            text = sort,
                            fontSize = 11.sp,
                            fontWeight = if (filter.sortBy == sort) FontWeight.Bold else FontWeight.Normal,
                            color = if (filter.sortBy == sort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.loadTrainsSchedule(filter.copy(sortBy = sort)) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Trains Schedule List
            if (trains.isEmpty() && !viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsRailway, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No train schedules match your filter.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trains) { train ->
                        TrainScheduleCard(train = train, onClick = { viewModel.selectTrainDetails(train.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TrainScheduleCard(train: TrainScheduleItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Number, Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = train.trainNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = train.trainName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val statusBg = when (train.status) {
                    "On Time" -> Color(0xFFE8F5E9)
                    "Delayed" -> Color(0xFFFFEBEE)
                    "Boarding Soon" -> Color(0xFFE3F2FD)
                    else -> Color(0xFFF5F5F5)
                }
                val statusTxt = when (train.status) {
                    "On Time" -> Color(0xFF2E7D32)
                    "Delayed" -> Color(0xFFC62828)
                    "Boarding Soon" -> Color(0xFF1565C0)
                    else -> Color.DarkGray
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = train.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusTxt,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route & Timings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(train.departureTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(train.sourceStation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(train.duration, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("${train.distance} KM", fontSize = 10.sp, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(train.arrivalTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(train.destinationStation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Badges: Platform, Seats, Fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Pf. ${train.platform}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (train.availableSeats > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0F7A3E))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${train.availableSeats} Seats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F7A3E))
                        }
                    }
                }

                if (train.fareEconomy > 0) {
                    Text(
                        text = "Rs. ${train.fareEconomy}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TrainDetailScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val train = viewModel.selectedTrain
    val favTrains by viewModel.favoriteTrains.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = train?.trainName ?: "Train Details",
                lang = lang,
                onBack = { viewModel.goBack() },
                actions = {
                    train?.let { tr ->
                        val isFav = favTrains.any { it.trainNumber == tr.trainNumber }
                        IconButton(onClick = {
                            val f = FavoriteTrain(tr.trainNumber, tr.trainName, tr.sourceStation, tr.destinationStation)
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
        if (train == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Train information not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(train.trainName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Train #${train.trainNumber} • ${train.trainType}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = train.status,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickInfoCol("Platform", train.platform)
                                QuickInfoCol("Seats", "${train.availableSeats} Available")
                                QuickInfoCol("Distance", "${train.distance} KM")
                                QuickInfoCol("Days", train.daysOfOperation.joinToString(","))
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ticket Fares & Classes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FareItemCard("Economy", "Rs. ${train.fareEconomy}", Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                FareItemCard("Business", "Rs. ${train.fareBusiness}", Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                FareItemCard("AC Standard", "Rs. ${train.fareAC}", Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Text("Intermediate Stations & Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                }

                items(train.intermediateStations) { st ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(st.stationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${st.stationCode} • ${st.distanceKm} KM • Platform ${st.platform}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Arr: ${st.arrival}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Dep: ${st.departure}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (st.stopDurationMinutes > 0) {
                                    Text("Halt: ${st.stopDurationMinutes}m", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickInfoCol(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun FareItemCard(classTitle: String, fareText: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(classTitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(fareText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RouteScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val routes = viewModel.routesList

    LaunchedEffect(Unit) {
        if (routes.isEmpty()) {
            viewModel.loadRoutes()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = Localization.getText("routes", lang), lang = lang, onBack = { viewModel.goBack() }) },
        bottomBar = { AppBottomNavigation(currentScreen = Screen.RouteScreen, viewModel = viewModel) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routes) { route ->
                Card(
                    onClick = { viewModel.selectRouteDetails(route.routeId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(route.routeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${route.origin} ➔ ${route.terminus}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Distance: ${route.totalDistanceKm} KM", fontSize = 12.sp, color = Color.Gray)
                            Text("Stations: ${route.stations.size}", fontSize = 12.sp, color = Color.Gray)
                            Text("Active Trains: ${route.trainsCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- 10. Station Information Screen ---
@Composable
fun StationInfoScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val info = viewModel.selectedStation
    val stations = viewModel.stationsList
    val favStations by viewModel.favoriteStations.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var stationSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (stations.isEmpty()) {
            viewModel.loadStations()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = info?.stationName ?: Localization.getText("station_info", lang),
                lang = lang,
                onBack = {
                    if (info != null) {
                        viewModel.selectedStation = null
                    } else {
                        viewModel.goBack()
                    }
                },
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
        },
        bottomBar = {
            AppBottomNavigation(currentScreen = Screen.StationInfoScreen, viewModel = viewModel)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (info == null) {
                // Stations List View
                Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    OutlinedTextField(
                        value = stationSearchQuery,
                        onValueChange = { stationSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search Station Name or Code", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (stationSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { stationSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    val filteredStations = stations.filter {
                        it.stationName.contains(stationSearchQuery, ignoreCase = true) ||
                        it.stationCode.contains(stationSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredStations) { st ->
                            Card(
                                onClick = { viewModel.selectStationDetails(st.stationCode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = st.stationCode,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(st.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(st.address, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                // Station Detail View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Hero banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lahore_junction_1783744552039),
                            contentDescription = info.stationName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                        startY = 80f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = info.stationName.uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
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
                                    text = "Station Code: ${info.code}",
                                    color = Color(0xFFE8F3ED),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("Facilities", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("Schedule", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                            Text("Nearby", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    when (selectedTab) {
                        0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("Contact Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(info.address, fontSize = 14.sp)
                                Text("Helpline: ${info.contactNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            item {
                                Text("Station Facilities", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(info.facilities) { fac ->
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
                            items(info.todayArrivals) { arr ->
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
                            items(info.todayDepartures) { dep ->
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
                            items(info.nearbyHotels) { hot ->
                                Text("🏨 $hot", modifier = Modifier.padding(vertical = 4.dp), fontSize = 14.sp)
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Nearby Restaurants", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(info.nearbyRestaurants) { res ->
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
                                Text("Type: ${ft.trainType}", fontSize = 13.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Route: ${ft.sourceStation} ➔ ${ft.destinationStation}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Status: ${ft.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

// --- 13. Weather & Prayer Timings Screen ---
@Composable
fun NamazTimingsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val context = androidx.compose.ui.platform.LocalContext.current
    val weather = viewModel.weatherData
    val prayerData = viewModel.prayerTimesData
    val settings = viewModel.prayerSettings
    val countdownText = viewModel.prayerCountdownText
    val cities = com.example.util.PrayerTimeCalculator.defaultCities.map { it.city }

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = com.example.ui.common.Localization.getText("prayer_timings", lang),
                lang = lang,
                onBack = { viewModel.goBack() },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Prayer Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // City selector row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cities) { city ->
                        FilterChip(
                            selected = viewModel.currentCity == city,
                            onClick = { viewModel.fetchWeatherAndNamaz(city) },
                            label = { Text(city, fontSize = 12.sp) },
                            leadingIcon = if (viewModel.currentCity == city) {
                                { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Card: Next Prayer & Countdown Timer
                prayerData?.let { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F7A3E)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFA7F3D0),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = com.example.ui.common.Localization.getText("next_prayer", lang).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0),
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = com.example.ui.common.Localization.getText(p.nextPrayerName.lowercase(), lang),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = "at ${p.nextPrayerFormattedTime}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Countdown Badge Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = com.example.ui.common.Localization.getText("starts_in", lang),
                                        fontSize = 11.sp,
                                        color = Color(0xFFD1FAE5)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = countdownText,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date & Station Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = p.locationName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = p.gregorianDate,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = p.hijriDate,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F7A3E),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Qibla",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Qibla: ${p.qiblaDirection}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                }
                                Text(
                                    text = "${settings.method.name} • ${settings.school.name}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = com.example.ui.common.Localization.getText("prayer_timings", lang).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Prayer Items List
                    p.items.forEach { item ->
                        val translatedName = com.example.ui.common.Localization.getText(item.name.lowercase(), lang)
                        val isCurrent = item.isCurrent
                        val isNext = item.isNext

                        val cardBg = when {
                            isCurrent -> Color(0xFFDCFCE7)
                            isNext -> Color(0xFFFEF3C7)
                            else -> Color.White
                        }
                        val borderColor = when {
                            isCurrent -> Color(0xFF16A34A)
                            isNext -> Color(0xFFD97706)
                            else -> Color(0xFFE2E8F0)
                        }

                        val pIcon = when (item.name.lowercase()) {
                            "fajr" -> Icons.Default.WbTwilight
                            "sunrise" -> Icons.Default.WbSunny
                            "dhuhr" -> Icons.Default.WbSunny
                            "asr" -> Icons.Default.WbSunny
                            "maghrib" -> Icons.Default.NightsStay
                            "isha" -> Icons.Default.NightsStay
                            else -> Icons.Default.Schedule
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(if (isCurrent || isNext) 1.5.dp else 1.dp, borderColor),
                            elevation = CardDefaults.cardElevation(if (isCurrent || isNext) 2.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = pIcon,
                                        contentDescription = item.name,
                                        tint = if (isCurrent) Color(0xFF15803D) else if (isNext) Color(0xFFB45309) else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = translatedName,
                                            fontWeight = if (isCurrent || isNext) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) Color(0xFF14532D) else if (isNext) Color(0xFF78350F) else Color(0xFF334155),
                                            fontSize = 15.sp
                                        )
                                        if (isCurrent) {
                                            Text(
                                                text = "✓ ${com.example.ui.common.Localization.getText("current_prayer", lang)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        } else if (isNext) {
                                            Text(
                                                text = "➜ ${com.example.ui.common.Localization.getText("next_prayer", lang)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.timeFormatted,
                                    color = if (isCurrent) Color(0xFF15803D) else if (isNext) Color(0xFFB45309) else Color(0xFF0F7A3E),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weather card summary if available
                weather?.let { w ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WEATHER IN ${w.location.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = w.condition,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F7A3E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = w.temperature,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay(lang)
            }
        }
    }

    // Prayer Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Prayer Timing Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Calculation Method:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    com.example.util.CalculationMethod.values().forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePrayerSettings(method = method, context = context)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = settings.method == method,
                                onClick = { viewModel.updatePrayerSettings(method = method, context = context) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(method.displayName, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Asr School:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    com.example.util.AsrSchool.values().forEach { school ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePrayerSettings(school = school, context = context)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = settings.school == school,
                                onClick = { viewModel.updatePrayerSettings(school = school, context = context) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(school.displayName, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Time Format:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    com.example.util.TimeFormat.values().forEach { format ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePrayerSettings(timeFormat = format, context = context)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = settings.timeFormat == format,
                                onClick = { viewModel.updatePrayerSettings(timeFormat = format, context = context) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(format.displayName, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prayer Notifications:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updatePrayerSettings(notificationsEnabled = enabled, context = context)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
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
        topBar = { AppTopBar(title = Localization.getText("favorites", lang), lang = lang, onBack = { viewModel.goBack() }) }
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
                                .clickable { viewModel.selectTrainDetails(t.trainNumber) },
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
                                .clickable { viewModel.selectStationDetails(s.stationCode) },
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
        topBar = { AppTopBar(title = Localization.getText("profile", lang), lang = lang, onBack = { viewModel.goBack() }) }
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
                        Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
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
                                    viewModel.errorMessage = "Incorrect old password."
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
            Text(Localization.getText("app_customization", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                        onClick = { viewModel.updateLanguage("en") },
                        label = { Text("English") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = lang == "ur",
                        onClick = { viewModel.updateLanguage("ur") },
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
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Default City (Weather & Namaz)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(com.example.util.PrayerTimeCalculator.defaultCities.map { it.city }) { city ->
                        FilterChip(
                            selected = viewModel.currentCity == city,
                            onClick = { viewModel.fetchWeatherAndNamaz(city) },
                            label = { Text(city, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(Localization.getText("about_legals", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Train Schedule app is optimized for Pakistan Railways train schedules, timings, and routes. Fully secure and JWT standard compatible encryption.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text("${Localization.getText("version", lang)} 2.0.4", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(Localization.getText("copyright", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        topBar = { AppTopBar(title = "Helplines & Emergency Support", lang = lang, onBack = { viewModel.goBack() }) }
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



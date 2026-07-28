package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.service.*
import com.example.util.validatePassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Login : Screen()
    object SignUp : Screen()
    object Verification : Screen()
    object Home : Screen()
    object TrainSearch : Screen()
    object TrainScheduleScreen : Screen()
    object TrainDetailScreen : Screen()
    object StationInfoScreen : Screen()
    object RouteScreen : Screen()
    object FreightTrainsScreen : Screen()
    object NewsBlogsScreen : Screen()
    object NamazTimingsScreen : Screen()
    object NotificationsScreen : Screen()
    object FavoritesScreen : Screen()
    object ProfileScreen : Screen()
    object SettingsScreen : Screen()
    object HelplineScreen : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)
    private val prefs = application.getSharedPreferences("app_settings_prefs", android.content.Context.MODE_PRIVATE)

    // --- Localization ---
    var currentLanguage by mutableStateOf(prefs.getString("language", "en") ?: "en")

    fun updateLanguage(lang: String) {
        currentLanguage = lang
        prefs.edit().putString("language", lang).apply()
    }

    // --- Dark Mode ---
    var isDarkMode by mutableStateOf(prefs.getBoolean("is_dark_mode", false))

    fun updateDarkMode(dark: Boolean) {
        isDarkMode = dark
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    // --- Navigation backstack simulation ---
    private val screenStack = mutableListOf<Screen>(Screen.Splash)
    var currentScreen by mutableStateOf<Screen>(Screen.Splash)
        private set

    val canGoBack: Boolean get() = screenStack.size > 1 && currentScreen != Screen.Home && currentScreen != Screen.Splash && currentScreen != Screen.Login

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Home) {
            screenStack.clear()
            screenStack.add(Screen.Home)
            currentScreen = Screen.Home
            return
        }
        if (currentScreen != screen) {
            screenStack.add(screen)
            currentScreen = screen
        }
    }

    fun goBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            currentScreen = screenStack.last()
        }
    }

    fun navigateAndClear(screen: Screen) {
        screenStack.clear()
        screenStack.add(screen)
        currentScreen = screen
    }

    // --- Database Flows ---
    val activeUser: StateFlow<User?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteTrains: StateFlow<List<FavoriteTrain>> = repository.favoriteTrains
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteStations: StateFlow<List<FavoriteStation>> = repository.favoriteStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTrainSearches: StateFlow<List<RecentSearch>> = repository.getRecentSearches("train")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentStationSearches: StateFlow<List<RecentSearch>> = repository.getRecentSearches("station")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Temporary SignUp Store for Verification ---
    var tempSignUpData: SignUpData? = null
    var verificationOtpInput by mutableStateOf("")
    var verificationError by mutableStateOf<String?>(null)
    var generatedOtp by mutableStateOf("")

    data class SignUpData(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val passwordHash: String
    )

    // --- Dynamic / API States ---
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Train search & Train Schedule Module
    var filterOptions by mutableStateOf(FilterOptions())
    var trainsList by mutableStateOf<List<TrainScheduleItem>>(emptyList())
    var selectedTrain by mutableStateOf<TrainScheduleItem?>(null)

    // Stations
    var stationQuery by mutableStateOf("")
    var stationsList by mutableStateOf<List<StationItem>>(emptyList())
    var selectedStation by mutableStateOf<StationItem?>(null)

    // Routes
    var routesList by mutableStateOf<List<RouteItem>>(emptyList())
    var selectedRoute by mutableStateOf<RouteItem?>(null)

    // Freight Trains
    var freightTrainsList by mutableStateOf<List<TrainScheduleItem>>(emptyList())

    // News and Blogs
    var newsList by mutableStateOf<List<NewsItem>>(emptyList())
    var blogsList by mutableStateOf<List<BlogItem>>(emptyList())

    // Weather and Namaz
    var currentCity by mutableStateOf("Lahore")
    var weatherData by mutableStateOf<WeatherData?>(null)
    var namazTimings by mutableStateOf<NamazTimingsData?>(null)
    var prayerTimesData by mutableStateOf<com.example.util.PrayerTimesData?>(null)
    var prayerSettings by mutableStateOf(com.example.util.PrayerTimingSettings())
    var prayerCountdownText by mutableStateOf("00:00:00")

    init {
        startPrayerTicker()
        // Hydrate default notifications
        viewModelScope.launch {
            repository.notifications.collect { list ->
                if (list.isEmpty()) {
                    repository.addNotification(
                        "Welcome to Train Tracker",
                        "The official Pakistan Railways information assistant is now active.",
                        "announcement"
                    )
                    repository.addNotification(
                        "Schedule Update: Khyber Mail",
                        "Khyber Mail (1UP) is running on time today. Track its live progress on our map.",
                        "update"
                    )
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(emailOrPhone: String, passwordRaw: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.login(emailOrPhone, passwordRaw)
                isLoading = false
                if (success) {
                    successMessage = null
                    navigateAndClear(Screen.Home)
                } else {
                    errorMessage = "Invalid email/phone or password"
                }
                onResult(success)
            } catch (e: Exception) {
                isLoading = false
                errorMessage = com.example.data.service.ApiClient.parseError(e)
                onResult(false)
            }
        }
    }

    fun submitSignUp(firstName: String, lastName: String, email: String, phone: String, passwordRaw: String, onResult: (Boolean) -> Unit) {
        val check = validatePassword(passwordRaw)
        if (!check.first) {
            errorMessage = check.second ?: "Invalid password"
            onResult(false)
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.signUp(firstName, lastName, email, phone, passwordRaw)
                isLoading = false
                if (success) {
                    successMessage = "Account created successfully. Please login."
                    errorMessage = null
                    navigateAndClear(Screen.Login)
                    onResult(true)
                } else {
                    errorMessage = "Account already exists or registration failed. Please try again."
                    onResult(false)
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = com.example.data.service.ApiClient.parseError(e)
                onResult(false)
            }
        }
    }

    fun verifyOtp(otp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val temp = tempSignUpData
            if (temp != null) {
                isLoading = true
                verificationError = null
                try {
                    val success = repository.verifyOtp(temp.email, otp)
                    isLoading = false
                    if (success) {
                        tempSignUpData = null
                        verificationError = null
                        navigateAndClear(Screen.Home)
                        onResult(true)
                    } else {
                        verificationError = "Invalid verification code or server is currently offline."
                        onResult(false)
                    }
                } catch (e: Exception) {
                    isLoading = false
                    verificationError = com.example.data.service.ApiClient.parseError(e)
                    onResult(false)
                }
            } else {
                verificationError = "Session expired. Please sign up again."
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            navigateAndClear(Screen.Login)
        }
    }

    fun editProfile(firstName: String, lastName: String, email: String, phone: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateProfile(firstName, lastName, email, phone)
            onResult(success)
        }
    }

    fun changePassword(oldRaw: String, newRaw: String, onResult: (Boolean) -> Unit) {
        val check = validatePassword(newRaw)
        if (!check.first) {
            errorMessage = check.second ?: "Invalid password"
            onResult(false)
            return
        }
        viewModelScope.launch {
            val success = repository.changePassword(oldRaw, newRaw)
            onResult(success)
        }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteAccount()
            if (success) {
                navigateAndClear(Screen.Login)
            }
            onResult(success)
        }
    }

    fun uploadProfilePicture(url: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.uploadProfilePicture(url)
            onResult(success)
        }
    }

    // --- Train Schedule Module Actions ---
    fun loadTrainsSchedule(options: FilterOptions = filterOptions) {
        filterOptions = options
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                trainsList = repository.getAllTrainsSchedule(options)
            } catch (e: Exception) {
                errorMessage = "Failed to load schedules: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectTrainDetails(trainIdOrNum: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                selectedTrain = repository.getTrainDetails(trainIdOrNum)
                navigateTo(Screen.TrainDetailScreen)
            } catch (e: Exception) {
                errorMessage = "Failed to load train details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadStations(query: String = stationQuery) {
        stationQuery = query
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                stationsList = repository.getStationsList(query)
            } catch (e: Exception) {
                errorMessage = "Failed to load stations: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectStationDetails(code: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                selectedStation = repository.getStationDetails(code)
                navigateTo(Screen.StationInfoScreen)
            } catch (e: Exception) {
                errorMessage = "Failed to load station details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                routesList = repository.getRoutesList()
            } catch (e: Exception) {
                errorMessage = "Failed to load routes: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRouteDetails(routeId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                selectedRoute = repository.getRouteDetails(routeId)
                navigateTo(Screen.RouteScreen)
            } catch (e: Exception) {
                errorMessage = "Failed to load route details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchFreightTrains() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                freightTrainsList = repository.getFreightTrainsApi()
            } catch (e: Exception) {
                errorMessage = "Failed to load freight trains: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchNewsAndBlogs() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                newsList = repository.getNewsApi()
                blogsList = repository.getBlogsApi()
            } catch (e: Exception) {
                errorMessage = "Failed to load news/blogs: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun startPrayerTicker() {
        viewModelScope.launch {
            while (isActive) {
                recalculatePrayerTimes()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun recalculatePrayerTimes() {
        val coords = com.example.util.PrayerTimeCalculator.getCoordinatesForCity(currentCity)
        val calculated = com.example.util.PrayerTimeCalculator.calculatePrayerTimes(
            locationName = currentCity,
            lat = coords.lat,
            lng = coords.lng,
            settings = prayerSettings
        )
        prayerTimesData = calculated

        val totalSecs = calculated.countdownSeconds
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        prayerCountdownText = String.format(java.util.Locale.ENGLISH, "%02d:%02d:%02d", hrs, mins, secs)

        namazTimings = NamazTimingsData(
            islamicDate = calculated.hijriDate,
            fajr = calculated.fajr,
            sunrise = calculated.sunrise,
            dhuhr = calculated.dhuhr,
            asr = calculated.asr,
            maghrib = calculated.maghrib,
            isha = calculated.isha,
            qiblaDirection = calculated.qiblaDirection
        )
    }

    fun updatePrayerSettings(
        method: com.example.util.CalculationMethod = prayerSettings.method,
        school: com.example.util.AsrSchool = prayerSettings.school,
        timeFormat: com.example.util.TimeFormat = prayerSettings.timeFormat,
        notificationsEnabled: Boolean = prayerSettings.notificationsEnabled,
        context: android.content.Context? = null
    ) {
        prayerSettings = com.example.util.PrayerTimingSettings(method, school, timeFormat, notificationsEnabled)
        recalculatePrayerTimes()

        context?.let { ctx ->
            if (notificationsEnabled) {
                prayerTimesData?.let { data ->
                    com.example.data.service.PrayerNotificationManager.schedulePrayerNotifications(ctx, data.items, currentCity)
                }
            } else {
                com.example.data.service.PrayerNotificationManager.cancelAllPrayerNotifications(ctx)
            }
        }
    }

    fun fetchWeatherAndNamaz(city: String = currentCity) {
        viewModelScope.launch {
            isLoading = true
            currentCity = city
            errorMessage = null
            try {
                weatherData = repository.getWeatherApi(city)
                recalculatePrayerTimes()
            } catch (e: Exception) {
                errorMessage = "Failed to load localized info: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- Database Operations ---
    fun toggleFavoriteTrain(train: FavoriteTrain, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteTrain(train, isFav)
        }
    }

    fun toggleFavoriteStation(station: FavoriteStation, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteStation(station, isFav)
        }
    }

    fun clearSearchHistory(type: String) {
        viewModelScope.launch {
            repository.clearRecentSearches(type)
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }
}

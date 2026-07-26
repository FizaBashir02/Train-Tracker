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
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Login : Screen()
    object SignUp : Screen()
    object Verification : Screen()
    object Home : Screen()
    object TrainSearch : Screen()
    object LiveStatus : Screen()
    object TrainScheduleScreen : Screen()
    object StationInfoScreen : Screen()
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

    // --- Localization ---
    var currentLanguage by mutableStateOf("en") // "en" or "ur"

    // --- Dark Mode ---
    var isDarkMode by mutableStateOf(false)

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

    // Train search
    var searchSource by mutableStateOf("")
    var searchDest by mutableStateOf("")
    var searchTypeFilter by mutableStateOf("All") // "All", "Express", "Passenger"
    var searchResults by mutableStateOf<List<TrainSearchItem>>(emptyList())

    // Live status
    var liveStatusQuery by mutableStateOf("")
    var activeLiveStatus by mutableStateOf<LiveStatus?>(null)

    // Train Schedule
    var activeTrainSchedule by mutableStateOf<TrainSchedule?>(null)

    // Station Info
    var stationQuery by mutableStateOf("")
    var activeStationInfo by mutableStateOf<StationInfo?>(null)

    // Freight Trains
    var freightTrainsList by mutableStateOf<List<FreightTrainItem>>(emptyList())

    // News and Blogs
    var newsList by mutableStateOf<List<NewsItem>>(emptyList())
    var blogsList by mutableStateOf<List<BlogItem>>(emptyList())

    // Weather and Namaz
    var currentCity by mutableStateOf("Lahore")
    var weatherData by mutableStateOf<WeatherData?>(null)
    var namazTimings by mutableStateOf<NamazTimingsData?>(null)

    init {
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
                    tempSignUpData = SignUpData(firstName, lastName, email, phone, passwordRaw)
                    navigateTo(Screen.Verification)
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

    fun forgotPassword(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.forgotPassword(email)
                isLoading = false
                if (success) {
                    onResult(true)
                } else {
                    errorMessage = "Forgot password request failed. Account not found or service offline."
                    onResult(false)
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = com.example.data.service.ApiClient.parseError(e)
                onResult(false)
            }
        }
    }

    fun resetPassword(email: String, otpCode: String, newPasswordRaw: String, onResult: (Boolean) -> Unit) {
        val check = validatePassword(newPasswordRaw)
        if (!check.first) {
            errorMessage = check.second ?: "Invalid password"
            onResult(false)
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.resetPassword(email, otpCode, newPasswordRaw)
                isLoading = false
                if (success) {
                    onResult(true)
                } else {
                    errorMessage = "Failed to reset password. Verify OTP code and try again."
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

    // --- Search & Fetch APIs via Gemini/Fallback ---
    fun runTrainSearch() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                searchResults = repository.searchTrainsApi(searchSource, searchDest, searchTypeFilter)
            } catch (e: Exception) {
                errorMessage = "Search failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchLiveStatus(trainNumber: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            liveStatusQuery = trainNumber
            try {
                activeLiveStatus = repository.getLiveStatusApi(trainNumber)
                navigateTo(Screen.LiveStatus)
            } catch (e: Exception) {
                errorMessage = "Failed to load live status: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchTrainSchedule(trainNumber: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                activeTrainSchedule = repository.getTrainScheduleApi(trainNumber)
                navigateTo(Screen.TrainScheduleScreen)
            } catch (e: Exception) {
                errorMessage = "Failed to load schedule: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchStationInfo(stationCode: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            stationQuery = stationCode
            try {
                activeStationInfo = repository.getStationInfoApi(stationCode)
                navigateTo(Screen.StationInfoScreen)
            } catch (e: Exception) {
                errorMessage = "Failed to load station info: ${e.localizedMessage}"
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

    fun fetchWeatherAndNamaz(city: String = currentCity) {
        viewModelScope.launch {
            isLoading = true
            currentCity = city
            errorMessage = null
            try {
                weatherData = repository.getWeatherApi(city)
                namazTimings = repository.getNamazTimingsApi(city)
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

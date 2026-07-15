package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.service.*
import kotlinx.coroutines.flow.Flow

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    internal val userDao = db.userDao()
    private val favoriteDao = db.favoriteDao()
    private val searchDao = db.searchDao()
    private val notificationDao = db.notificationDao()
    private val trainDao = db.trainDao()
    private val stationDao = db.stationDao()
    private val scheduleDao = db.scheduleDao()
    private val newsDao = db.newsDao()
    private val blogDao = db.blogDao()
    private val weatherCacheDao = db.weatherCacheDao()
    private val prayerTimesCacheDao = db.prayerTimesCacheDao()
    private val analyticsDao = db.analyticsDao()

    // --- Authentication Flow ---
    val activeUser: Flow<User?> = userDao.getActiveUserFlow()

    suspend fun getActiveUserSync(): User? {
        return userDao.getActiveUser()
    }

    suspend fun getUserByEmailOrPhone(email: String, phone: String): User? {
        return userDao.getUserByEmailOrPhone(email, phone)
    }

    suspend fun signUp(firstName: String, lastName: String, email: String, phone: String, passwordHash: String): Boolean {
        val response = ApiClient.apiService.signUp(SignUpRequest(firstName, lastName, email, phone, passwordHash))
        if (response.success) {
            logAnalyticsEvent("remote_sign_up_success", "user_email: $email")
            val user = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                passwordHash = passwordHash,
                isLoggedIn = false,
                isOtpVerified = false
            )
            userDao.insertUser(user)
            return true
        }
        return false
    }

    suspend fun verifyOtp(email: String, otpCode: String): Boolean {
        val response = ApiClient.apiService.verifyOtp(OtpRequest(email, otpCode))
        ApiClient.getTokenManager()?.let { tm ->
            tm.saveToken(response.token)
            tm.saveRefreshToken(response.refreshToken)
        }
        val cached = userDao.getUserByEmailOrPhone(email, email)
        val updated = if (cached != null) {
            cached.copy(
                firstName = response.user.firstName,
                lastName = response.user.lastName,
                isLoggedIn = true,
                isOtpVerified = true
            )
        } else {
            User(
                firstName = response.user.firstName,
                lastName = response.user.lastName,
                email = response.user.email,
                phone = response.user.phone,
                passwordHash = "",
                isLoggedIn = true,
                isOtpVerified = true
            )
        }
        userDao.logoutAll()
        userDao.insertUser(updated)
        addNotification(
            "Account Setup Completed",
            "Welcome ${response.user.firstName}! Your profile has been verified and registered securely.",
            "announcement"
        )
        logAnalyticsEvent("otp_verified", "user_email: $email")
        return true
    }

    suspend fun login(identifier: String, passwordHash: String): Boolean {
        try {
            val response = ApiClient.apiService.login(AuthRequest(identifier, "", passwordHash))
            ApiClient.getTokenManager()?.let { tm ->
                tm.saveToken(response.token)
                tm.saveRefreshToken(response.refreshToken)
            }
            userDao.logoutAll()
            val cachedUser = User(
                firstName = response.user.firstName,
                lastName = response.user.lastName,
                email = response.user.email,
                phone = response.user.phone,
                passwordHash = passwordHash,
                isLoggedIn = true,
                isOtpVerified = true
            )
            userDao.insertUser(cachedUser)
            addNotification(
                "Successful Login",
                "You have logged into your Pakistan Railways companion profile.",
                "announcement"
            )
            logAnalyticsEvent("remote_login_success", "user_email: $identifier")
            return true
        } catch (e: Exception) {
            logAnalyticsEvent("remote_login_failed", "error: ${e.message}")
            val user = userDao.getUserByEmailOrPhone(identifier, identifier)
            if (user != null && user.passwordHash == passwordHash && user.isOtpVerified) {
                userDao.logoutAll()
                val loggedInUser = user.copy(isLoggedIn = true)
                userDao.updateUser(loggedInUser)
                logAnalyticsEvent("offline_login_success", "user_email: $identifier")
                return true
            }
            throw e
        }
    }

    suspend fun forgotPassword(email: String): Boolean {
        val response = ApiClient.apiService.forgotPassword(ForgotPasswordRequest(email))
        logAnalyticsEvent("remote_forgot_password_success", "email: $email")
        return response.success
    }

    suspend fun resetPassword(email: String, otpCode: String, newPasswordHash: String): Boolean {
        val response = ApiClient.apiService.resetPassword(ResetPasswordRequest(email, otpCode, newPasswordHash))
        if (response.success) {
            val user = userDao.getUserByEmailOrPhone(email, email)
            if (user != null) {
                val updated = user.copy(passwordHash = newPasswordHash)
                userDao.updateUser(updated)
            }
            addNotification(
                "Password Reset Success",
                "Your account password was changed successfully.",
                "alert"
            )
            logAnalyticsEvent("password_reset", "user_email: $email")
            return true
        }
        return false
    }

    suspend fun updateProfile(firstName: String, lastName: String, email: String, phone: String): Boolean {
        try {
            val response = ApiClient.apiService.updateProfile(UpdateProfileRequest(firstName, lastName, phone))
            if (response.success) {
                val currentUser = userDao.getActiveUser() ?: return false
                val updated = currentUser.copy(firstName = firstName, lastName = lastName, email = email, phone = phone)
                userDao.updateUser(updated)
                logAnalyticsEvent("profile_update_success", "user_email: $email")
                return true
            }
        } catch (e: Exception) {
            logAnalyticsEvent("profile_update_failed", "error: ${e.message}")
        }
        return false
    }

    suspend fun changePassword(oldHash: String, newHash: String): Boolean {
        try {
            val response = ApiClient.apiService.changePassword(ChangePasswordRequest(oldHash, newHash))
            if (response.success) {
                val currentUser = userDao.getActiveUser() ?: return false
                val updated = currentUser.copy(passwordHash = newHash)
                userDao.updateUser(updated)
                logAnalyticsEvent("change_password_success", "user_email: ${currentUser.email}")
                return true
            }
        } catch (e: Exception) {
            logAnalyticsEvent("change_password_failed", "error: ${e.message}")
        }
        return false
    }

    suspend fun deleteAccount(): Boolean {
        try {
            val response = ApiClient.apiService.deleteAccount()
            if (response.success) {
                val currentUser = userDao.getActiveUser() ?: return false
                userDao.deleteUser(currentUser)
                logAnalyticsEvent("delete_account_success", "user_email: ${currentUser.email}")
                return true
            }
        } catch (e: Exception) {
            logAnalyticsEvent("delete_account_failed", "error: ${e.message}")
        }
        return false
    }

    suspend fun uploadProfilePicture(profilePictureUrl: String): Boolean {
        try {
            val response = ApiClient.apiService.uploadProfilePicture(UploadProfilePictureRequest(profilePictureUrl))
            if (response.success) {
                val currentUser = userDao.getActiveUser() ?: return false
                val updated = currentUser.copy(profilePictureUrl = profilePictureUrl)
                userDao.updateUser(updated)
                logAnalyticsEvent("upload_profile_picture_success", "user_email: ${currentUser.email}")
                return true
            }
        } catch (e: Exception) {
            logAnalyticsEvent("upload_profile_picture_failed", "error: ${e.message}")
        }
        return false
    }

    suspend fun logout() {
        val currentUser = userDao.getActiveUser()
        if (currentUser != null) {
            logAnalyticsEvent("logout", "user_email: ${currentUser.email}")
        }
        ApiClient.getTokenManager()?.clear()
        userDao.logoutAll()
    }

    // --- Favorites ---
    val favoriteTrains: Flow<List<FavoriteTrain>> = favoriteDao.getFavoriteTrains()
    val favoriteStations: Flow<List<FavoriteStation>> = favoriteDao.getFavoriteStations()

    suspend fun toggleFavoriteTrain(train: FavoriteTrain, isFav: Boolean) {
        if (isFav) {
            favoriteDao.insertFavoriteTrain(train)
            logAnalyticsEvent("fav_train_add", "train: ${train.trainNumber}")
        } else {
            favoriteDao.deleteFavoriteTrain(train)
            logAnalyticsEvent("fav_train_remove", "train: ${train.trainNumber}")
        }
    }

    suspend fun toggleFavoriteStation(station: FavoriteStation, isFav: Boolean) {
        if (isFav) {
            favoriteDao.insertFavoriteStation(station)
            logAnalyticsEvent("fav_station_add", "station: ${station.stationCode}")
        } else {
            favoriteDao.deleteFavoriteStation(station)
            logAnalyticsEvent("fav_station_remove", "station: ${station.stationCode}")
        }
    }

    fun isFavoriteTrainFlow(trainNumber: String): Flow<Boolean> = favoriteDao.isFavoriteTrain(trainNumber)
    fun isFavoriteStationFlow(stationCode: String): Flow<Boolean> = favoriteDao.isFavoriteStation(stationCode)

    // --- Searches ---
    fun getRecentSearches(type: String): Flow<List<RecentSearch>> = searchDao.getRecentSearches(type)

    suspend fun addRecentSearch(query: String, type: String) {
        searchDao.insertSearch(RecentSearch(query = query, type = type))
    }

    suspend fun clearRecentSearches(type: String) {
        searchDao.clearSearches(type)
    }

    // --- Notifications ---
    val notifications: Flow<List<NotificationItem>> = notificationDao.getNotifications()

    suspend fun addNotification(title: String, message: String, category: String) {
        notificationDao.insertNotification(NotificationItem(title = title, message = message, category = category))
    }

    suspend fun markNotificationAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() {
        notificationDao.clearAllNotifications()
    }

    suspend fun registerFcmToken(email: String, fcmToken: String): Boolean {
        return try {
            val response = ApiClient.apiService.registerFcmToken(FcmTokenRequest(email, fcmToken))
            response.success
        } catch (e: Exception) {
            logAnalyticsEvent("fcm_token_registration_failed", "error: ${e.message}")
            false
        }
    }

    // --- Cache and Fallbacks Integration ---
    suspend fun searchTrainsApi(source: String, dest: String, type: String): List<TrainSearchItem> {
        if (source.isNotEmpty() || dest.isNotEmpty()) {
            val q = if (source.isNotEmpty() && dest.isNotEmpty()) "$source to $dest" else source.ifEmpty { dest }
            addRecentSearch(q, "train")
        }
        logAnalyticsEvent("train_search", "src: $source, dst: $dest, type: $type")
        
        // Caching Policy: Check local cache first and return if within TTL (4 hours)
        val cachedTrains = trainDao.getAllTrainsSync()
        if (cachedTrains.isNotEmpty()) {
            val firstCached = cachedTrains.first()
            if (System.currentTimeMillis() - firstCached.timestamp < 4 * 60 * 60 * 1000) {
                return cachedTrains.map {
                    TrainSearchItem(
                        trainName = it.trainName,
                        trainNumber = it.trainNumber,
                        source = it.source,
                        destination = it.destination,
                        departure = it.departureTime,
                        arrival = it.arrivalTime,
                        duration = it.routeDesc,
                        trainType = it.type
                    )
                }
            }
        }

        return try {
            val remoteResults = ApiClient.apiService.searchTrains(source, dest, type)
            if (remoteResults.isNotEmpty()) {
                val entities = remoteResults.map {
                    TrainEntity(
                        trainNumber = it.trainNumber,
                        trainName = it.trainName,
                        source = it.source,
                        destination = it.destination,
                        type = it.trainType,
                        departureTime = it.departure,
                        arrivalTime = it.arrival,
                        routeDesc = it.duration,
                        timestamp = System.currentTimeMillis()
                    )
                }
                trainDao.insertTrains(entities)
            }
            remoteResults
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_search_failed", "error: ${e.message}. Using cache fallback.")
            if (cachedTrains.isNotEmpty()) {
                cachedTrains.map {
                    TrainSearchItem(
                        trainName = it.trainName,
                        trainNumber = it.trainNumber,
                        source = it.source,
                        destination = it.destination,
                        departure = it.departureTime,
                        arrival = it.arrivalTime,
                        duration = it.routeDesc,
                        trainType = it.type
                    )
                }
            } else {
                throw e
            }
        }
    }

    suspend fun getLiveStatusApi(trainNumber: String): LiveStatus {
        addRecentSearch(trainNumber, "train")
        logAnalyticsEvent("live_status_fetch", "train: $trainNumber")
        
        // Live Status requires direct real-time updates. No Room caching is suitable as primary, always query remote API directly.
        return ApiClient.apiService.getLiveStatus(trainNumber)
    }

    private suspend fun syncSchedule(trainNumber: String, remote: TrainSchedule) {
        val entities = remote.stations.mapIndexed { index, st ->
            ScheduleEntity(
                trainNumber = trainNumber,
                stationCode = st.stationCode,
                arrivalTime = st.arrival,
                departureTime = st.departure,
                stopMinutes = st.stopDurationMinutes,
                distanceKm = st.distanceKm,
                stopNumber = index + 1,
                timestamp = System.currentTimeMillis()
            )
        }
        scheduleDao.deleteSchedulesForTrain(trainNumber)
        scheduleDao.insertSchedules(entities)
    }

    private suspend fun syncStation(stationCode: String, remote: StationInfo) {
        val entity = StationEntity(
            stationCode = stationCode,
            stationName = remote.stationName,
            locationDescription = remote.address,
            facilitiesList = remote.facilities.joinToString(","),
            timestamp = System.currentTimeMillis()
        )
        stationDao.insertStations(listOf(entity))
    }

    private suspend fun syncNews(remote: List<NewsItem>) {
        val entities = remote.map {
            NewsEntity(
                newsId = it.title.hashCode().toString(),
                title = it.title,
                content = it.summary,
                category = it.category,
                timestamp = System.currentTimeMillis()
            )
        }
        newsDao.insertNews(entities)
    }

    private suspend fun syncBlogs(remote: List<BlogItem>) {
        val entities = remote.map {
            BlogEntity(
                blogId = it.title.hashCode().toString(),
                title = it.title,
                content = it.content,
                category = it.category,
                author = "PR Staff",
                timestamp = System.currentTimeMillis()
            )
        }
        blogDao.insertBlogs(entities)
    }

    suspend fun getTrainScheduleApi(trainNumber: String): TrainSchedule {
        logAnalyticsEvent("train_schedule_fetch", "train: $trainNumber")
        
        // Check cache with 4 hour TTL
        val cachedSchedules = scheduleDao.getSchedulesForTrainSync(trainNumber)
        if (cachedSchedules.isNotEmpty()) {
            val firstCached = cachedSchedules.first()
            if (System.currentTimeMillis() - firstCached.timestamp < 4 * 60 * 60 * 1000) {
                val localStations = cachedSchedules.map {
                    ScheduleStation(
                        stationName = it.stationCode,
                        stationCode = it.stationCode,
                        arrival = it.arrivalTime,
                        departure = it.departureTime,
                        distanceKm = it.distanceKm,
                        stopDurationMinutes = it.stopMinutes,
                        dayNumber = 1
                    )
                }
                return TrainSchedule(
                    trainName = "Train $trainNumber",
                    trainNumber = trainNumber,
                    stations = localStations,
                    totalStops = localStations.size,
                    totalDistanceKm = localStations.lastOrNull()?.distanceKm ?: 0,
                    totalJourneyTime = "N/A"
                )
            }
        }

        return try {
            val remote = ApiClient.apiService.getTrainSchedule(trainNumber)
            syncSchedule(trainNumber, remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_schedule_failed", "error: ${e.message}. Using cache fallback.")
            if (cachedSchedules.isNotEmpty()) {
                val localStations = cachedSchedules.map {
                    ScheduleStation(
                        stationName = it.stationCode,
                        stationCode = it.stationCode,
                        arrival = it.arrivalTime,
                        departure = it.departureTime,
                        distanceKm = it.distanceKm,
                        stopDurationMinutes = it.stopMinutes,
                        dayNumber = 1
                    )
                }
                TrainSchedule(
                    trainName = "Train $trainNumber",
                    trainNumber = trainNumber,
                    stations = localStations,
                    totalStops = localStations.size,
                    totalDistanceKm = localStations.lastOrNull()?.distanceKm ?: 0,
                    totalJourneyTime = "N/A"
                )
            } else {
                throw e
            }
        }
    }

    suspend fun getStationInfoApi(stationCode: String): StationInfo {
        addRecentSearch(stationCode, "station")
        logAnalyticsEvent("station_info_fetch", "station: $stationCode")

        val cached = stationDao.getStationByCode(stationCode)
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < 4 * 60 * 60 * 1000)) {
            return StationInfo(
                stationName = cached.stationName,
                code = stationCode,
                address = cached.locationDescription,
                contactNumber = "117",
                facilities = cached.facilitiesList.split(","),
                nearbyHotels = emptyList(),
                nearbyRestaurants = emptyList(),
                nearbyBusStops = emptyList(),
                todayArrivals = emptyList(),
                todayDepartures = emptyList(),
                delayedTrains = emptyList()
            )
        }

        return try {
            val remote = ApiClient.apiService.getStationInfo(stationCode)
            syncStation(stationCode, remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_station_info_failed", "error: ${e.message}. Using cache fallback.")
            if (cached != null) {
                StationInfo(
                    stationName = cached.stationName,
                    code = stationCode,
                    address = cached.locationDescription,
                    contactNumber = "117",
                    facilities = cached.facilitiesList.split(","),
                    nearbyHotels = emptyList(),
                    nearbyRestaurants = emptyList(),
                    nearbyBusStops = emptyList(),
                    todayArrivals = emptyList(),
                    todayDepartures = emptyList(),
                    delayedTrains = emptyList()
                )
            } else {
                throw e
            }
        }
    }

    suspend fun getFreightTrainsApi(): List<FreightTrainItem> {
        logAnalyticsEvent("freight_trains_fetch", "")
        return ApiClient.apiService.getFreightTrains()
    }

    suspend fun getWeatherApi(location: String): WeatherData {
        logAnalyticsEvent("weather_fetch", "location: $location")
        val cached = weatherCacheDao.getWeather(location)
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < 30 * 60 * 1000)) {
            return WeatherData(
                location = cached.city,
                temperature = cached.temperature,
                humidity = cached.humidity,
                condition = cached.condition
            )
        }
        return try {
            val remote = ApiClient.apiService.getWeather(location)
            weatherCacheDao.insertWeather(
                WeatherCacheEntity(
                    city = remote.location,
                    temperature = remote.temperature,
                    condition = remote.condition,
                    humidity = remote.humidity,
                    wind = "12 km/h",
                    timestamp = System.currentTimeMillis()
                )
            )
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_weather_failed", "error: ${e.message}. Using cache fallback.")
            if (cached != null) {
                WeatherData(
                    location = cached.city,
                    temperature = cached.temperature,
                    humidity = cached.humidity,
                    condition = cached.condition
                )
            } else {
                throw e
            }
        }
    }

    suspend fun getNamazTimingsApi(location: String): NamazTimingsData {
        logAnalyticsEvent("namaz_fetch", "location: $location")
        val cached = prayerTimesCacheDao.getPrayerTimes(location)
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < 24 * 60 * 60 * 1000)) {
            return NamazTimingsData(
                islamicDate = cached.date,
                fajr = cached.fajr,
                dhuhr = cached.dhuhr,
                asr = cached.asr,
                maghrib = cached.maghrib,
                isha = cached.isha,
                qiblaDirection = "261° (W)"
            )
        }
        return try {
            val remote = ApiClient.apiService.getNamazTimings(location)
            prayerTimesCacheDao.insertPrayerTimes(
                PrayerTimesCacheEntity(
                    city = location,
                    date = remote.islamicDate,
                    fajr = remote.fajr,
                    dhuhr = remote.dhuhr,
                    asr = remote.asr,
                    maghrib = remote.maghrib,
                    isha = remote.isha,
                    timestamp = System.currentTimeMillis()
                )
            )
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_namaz_failed", "error: ${e.message}. Using cache fallback.")
            if (cached != null) {
                NamazTimingsData(
                    islamicDate = cached.date,
                    fajr = cached.fajr,
                    dhuhr = cached.dhuhr,
                    asr = cached.asr,
                    maghrib = cached.maghrib,
                    isha = cached.isha,
                    qiblaDirection = "261° (W)"
                )
            } else {
                throw e
            }
        }
    }

    suspend fun getNewsApi(): List<NewsItem> {
        val cachedNews = newsDao.getAllNewsSync()
        if (cachedNews.isNotEmpty()) {
            val firstCached = cachedNews.first()
            if (System.currentTimeMillis() - firstCached.timestamp < 4 * 60 * 60 * 1000) {
                return cachedNews.map {
                    NewsItem(
                        title = it.title,
                        category = it.category,
                        date = "Today",
                        summary = it.content
                    )
                }
            }
        }

        return try {
            val remote = ApiClient.apiService.getNews()
            syncNews(remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_news_failed", "error: ${e.message}. Using cache fallback.")
            if (cachedNews.isNotEmpty()) {
                cachedNews.map {
                    NewsItem(
                        title = it.title,
                        category = it.category,
                        date = "Today",
                        summary = it.content
                    )
                }
            } else {
                throw e
            }
        }
    }

    suspend fun getBlogsApi(): List<BlogItem> {
        val cachedBlogs = blogDao.getAllBlogsSync()
        if (cachedBlogs.isNotEmpty()) {
            val firstCached = cachedBlogs.first()
            if (System.currentTimeMillis() - firstCached.timestamp < 4 * 60 * 60 * 1000) {
                return cachedBlogs.map {
                    BlogItem(
                        title = it.title,
                        category = it.category,
                        readTime = "5 mins",
                        content = it.content
                    )
                }
            }
        }

        return try {
            val remote = ApiClient.apiService.getBlogs()
            syncBlogs(remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_blogs_failed", "error: ${e.message}. Using cache fallback.")
            if (cachedBlogs.isNotEmpty()) {
                cachedBlogs.map {
                    BlogItem(
                        title = it.title,
                        category = it.category,
                        readTime = "5 mins",
                        content = it.content
                    )
                }
            } else {
                throw e
            }
        }
    }

    suspend fun logAnalyticsEvent(eventName: String, eventData: String) {
        try {
            analyticsDao.insertEvent(AnalyticsEvent(eventName = eventName, eventData = eventData))
        } catch (e: Exception) {}
    }
}

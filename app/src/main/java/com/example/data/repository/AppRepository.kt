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
            val response = ApiClient.apiService.login(LoginRequest(identifier, passwordHash))
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
    suspend fun searchTrainsApi(source: String, dest: String, type: String): List<TrainScheduleItem> {
        if (source.isNotEmpty() || dest.isNotEmpty()) {
            val q = if (source.isNotEmpty() && dest.isNotEmpty()) "$source to $dest" else source.ifEmpty { dest }
            addRecentSearch(q, "train")
        }
        logAnalyticsEvent("train_search", "src: $source, dst: $dest, type: $type")
        return try {
            ApiClient.apiService.searchTrains(source, dest, type)
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_search_failed", "error: ${e.message}. Using cache fallback.")
            LocalTrainData.getDummyTrains().filter { train ->
                val matchSource = source.isEmpty() || train.sourceStation.contains(source, ignoreCase = true)
                val matchDest = dest.isEmpty() || train.destinationStation.contains(dest, ignoreCase = true)
                val matchType = type == "All" || train.trainType.equals(type, ignoreCase = true)
                matchSource && matchDest && matchType
            }
        }
    }

    // --- Train Schedule Module Repository Methods ---
    suspend fun getAllTrainsSchedule(filter: FilterOptions): List<TrainScheduleItem> {
        val baseList = try {
            val remote = ApiClient.apiService.getAllTrains(
                name = filter.query.takeIf { it.isNotEmpty() },
                source = filter.source.takeIf { it.isNotEmpty() },
                destination = filter.destination.takeIf { it.isNotEmpty() },
                status = filter.status.takeIf { it != "All" },
                type = filter.trainType.takeIf { it != "All" }
            )
            if (remote.isNotEmpty()) remote else LocalTrainData.getDummyTrains()
        } catch (e: Exception) {
            LocalTrainData.getDummyTrains()
        }

        var result = baseList.filter { item ->
            val matchesQuery = filter.query.isBlank() || 
                item.trainName.contains(filter.query, ignoreCase = true) || 
                item.trainNumber.contains(filter.query, ignoreCase = true) ||
                item.sourceStation.contains(filter.query, ignoreCase = true) ||
                item.destinationStation.contains(filter.query, ignoreCase = true)

            val matchesSource = filter.source.isBlank() || 
                item.sourceStation.contains(filter.source, ignoreCase = true)

            val matchesDest = filter.destination.isBlank() || 
                item.destinationStation.contains(filter.destination, ignoreCase = true)

            val matchesStatus = filter.status == "All" || 
                item.status.equals(filter.status, ignoreCase = true)

            val matchesType = filter.trainType == "All" || 
                item.trainType.equals(filter.trainType, ignoreCase = true)

            matchesQuery && matchesSource && matchesDest && matchesStatus && matchesType
        }

        result = when (filter.sortBy) {
            "Duration" -> result.sortedBy { it.duration }
            "Fare" -> result.sortedBy { it.fareEconomy }
            else -> result.sortedBy { it.departureTime }
        }

        return result
    }

    suspend fun getTrainDetails(trainIdOrNum: String): TrainScheduleItem {
        return try {
            ApiClient.apiService.getTrainDetails(trainIdOrNum)
        } catch (e: Exception) {
            LocalTrainData.getDummyTrains().find { 
                it.id.equals(trainIdOrNum, ignoreCase = true) || 
                it.trainNumber.equals(trainIdOrNum, ignoreCase = true) 
            } ?: LocalTrainData.getDummyTrains().first()
        }
    }

    suspend fun getStationsList(query: String = ""): List<StationItem> {
        val stations = try {
            val remote = ApiClient.apiService.getStations(query.takeIf { it.isNotBlank() })
            if (remote.isNotEmpty()) remote else LocalTrainData.defaultStations
        } catch (e: Exception) {
            LocalTrainData.defaultStations
        }

        if (query.isBlank()) return stations
        return stations.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.code.contains(query, ignoreCase = true) 
        }
    }

    suspend fun getStationDetails(code: String): StationItem {
        return try {
            ApiClient.apiService.getStationDetails(code)
        } catch (e: Exception) {
            LocalTrainData.defaultStations.find { it.code.equals(code, ignoreCase = true) }
                ?: LocalTrainData.defaultStations.first()
        }
    }

    suspend fun getRoutesList(): List<RouteItem> {
        return try {
            val remote = ApiClient.apiService.getRoutes()
            if (remote.isNotEmpty()) remote else LocalTrainData.defaultRoutes
        } catch (e: Exception) {
            LocalTrainData.defaultRoutes
        }
    }

    suspend fun getRouteDetails(routeId: String): RouteItem {
        return try {
            ApiClient.apiService.getRouteDetails(routeId)
        } catch (e: Exception) {
            LocalTrainData.defaultRoutes.find { it.routeId.equals(routeId, ignoreCase = true) }
                ?: LocalTrainData.defaultRoutes.first()
        }
    }

    private suspend fun syncSchedule(trainNumber: String, remote: TrainScheduleItem) {
        val entities = remote.intermediateStations.mapIndexed { index, st ->
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

    private suspend fun syncStation(stationCode: String, remote: StationItem) {
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

    suspend fun getTrainScheduleApi(trainNumber: String): TrainScheduleItem {
        logAnalyticsEvent("train_schedule_fetch", "train: $trainNumber")
        return try {
            val remote = ApiClient.apiService.getTrainSchedule(trainNumber)
            syncSchedule(trainNumber, remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_schedule_failed", "error: ${e.message}. Using cache fallback.")
            LocalTrainData.getDummyTrains().find { it.trainNumber.equals(trainNumber, ignoreCase = true) }
                ?: LocalTrainData.getDummyTrains().first()
        }
    }

    suspend fun getStationInfoApi(stationCode: String): StationItem {
        addRecentSearch(stationCode, "station")
        logAnalyticsEvent("station_info_fetch", "station: $stationCode")
        return try {
            val remote = ApiClient.apiService.getStationDetails(stationCode)
            syncStation(stationCode, remote)
            remote
        } catch (e: Exception) {
            logAnalyticsEvent("retrofit_station_info_failed", "error: ${e.message}. Using cache fallback.")
            LocalTrainData.defaultStations.find { it.code.equals(stationCode, ignoreCase = true) }
                ?: LocalTrainData.defaultStations.first()
        }
    }

    suspend fun getFreightTrainsApi(): List<TrainScheduleItem> {
        logAnalyticsEvent("freight_trains_fetch", "")
        return try {
            ApiClient.apiService.getFreightTrains()
        } catch (e: Exception) {
            LocalTrainData.getDummyTrains().filter { it.trainType.equals("Freight", ignoreCase = true) }
        }
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

    suspend fun getNamazTimingsApi(location: String, settings: com.example.util.PrayerTimingSettings = com.example.util.PrayerTimingSettings()): NamazTimingsData {
        logAnalyticsEvent("namaz_fetch", "location: $location")
        val coords = com.example.util.PrayerTimeCalculator.getCoordinatesForCity(location)
        val calculated = com.example.util.PrayerTimeCalculator.calculatePrayerTimes(
            locationName = location,
            lat = coords.lat,
            lng = coords.lng,
            settings = settings
        )

        prayerTimesCacheDao.insertPrayerTimes(
            PrayerTimesCacheEntity(
                city = location,
                date = calculated.hijriDate,
                fajr = calculated.fajr,
                sunrise = calculated.sunrise,
                dhuhr = calculated.dhuhr,
                asr = calculated.asr,
                maghrib = calculated.maghrib,
                isha = calculated.isha,
                timestamp = System.currentTimeMillis()
            )
        )

        return NamazTimingsData(
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

package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email OR phone = :phone LIMIT 1")
    suspend fun getUserByEmailOrPhone(email: String, phone: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): User?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_trains ORDER BY addedAt DESC")
    fun getFavoriteTrains(): Flow<List<FavoriteTrain>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTrain(train: FavoriteTrain)

    @Delete
    suspend fun deleteFavoriteTrain(train: FavoriteTrain)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_trains WHERE trainNumber = :trainNumber)")
    fun isFavoriteTrain(trainNumber: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_stations ORDER BY addedAt DESC")
    fun getFavoriteStations(): Flow<List<FavoriteStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteStation(station: FavoriteStation)

    @Delete
    suspend fun deleteFavoriteStation(station: FavoriteStation)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationCode = :stationCode)")
    fun isFavoriteStation(stationCode: String): Flow<Boolean>
}

@Dao
interface SearchDao {
    @Query("SELECT * FROM recent_searches WHERE type = :type ORDER BY timestamp DESC LIMIT 15")
    fun getRecentSearches(type: String): Flow<List<RecentSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearch)

    @Query("DELETE FROM recent_searches WHERE type = :type")
    suspend fun clearSearches(type: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAllSearches()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

@Dao
interface TrainDao {
    @Query("SELECT * FROM trains_cache")
    fun getAllTrains(): Flow<List<TrainEntity>>

    @Query("SELECT * FROM trains_cache")
    suspend fun getAllTrainsSync(): List<TrainEntity>

    @Query("SELECT * FROM trains_cache WHERE trainNumber = :trainNumber LIMIT 1")
    suspend fun getTrainByNumber(trainNumber: String): TrainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrains(trains: List<TrainEntity>)
}

@Dao
interface StationDao {
    @Query("SELECT * FROM stations_cache")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations_cache WHERE stationCode = :stationCode LIMIT 1")
    suspend fun getStationByCode(stationCode: String): StationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules_cache WHERE trainNumber = :trainNumber ORDER BY stopNumber ASC")
    fun getScheduleForTrain(trainNumber: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules_cache WHERE trainNumber = :trainNumber ORDER BY stopNumber ASC")
    suspend fun getSchedulesForTrainSync(trainNumber: String): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules_cache WHERE trainNumber = :trainNumber")
    suspend fun deleteSchedulesForTrain(trainNumber: String)
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_cache ORDER BY timestamp DESC")
    fun getNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_cache ORDER BY timestamp DESC")
    suspend fun getAllNewsSync(): List<NewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<NewsEntity>)
}

@Dao
interface BlogDao {
    @Query("SELECT * FROM blogs_cache ORDER BY timestamp DESC")
    fun getBlogs(): Flow<List<BlogEntity>>

    @Query("SELECT * FROM blogs_cache ORDER BY timestamp DESC")
    suspend fun getAllBlogsSync(): List<BlogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlogs(blogs: List<BlogEntity>)
}

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE city = :city LIMIT 1")
    suspend fun getWeather(city: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)
}

@Dao
interface PrayerTimesCacheDao {
    @Query("SELECT * FROM prayer_times_cache WHERE city = :city LIMIT 1")
    suspend fun getPrayerTimes(city: String): PrayerTimesCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: PrayerTimesCacheEntity)
}

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AnalyticsEvent)

    @Query("SELECT * FROM analytics ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<AnalyticsEvent>>
}


package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false,
    val otpCode: String? = null,
    val isOtpVerified: Boolean = false,
    val profilePictureUrl: String? = null
)

@Entity(tableName = "favorite_trains")
data class FavoriteTrain(
    @PrimaryKey val trainNumber: String,
    val trainName: String,
    val source: String,
    val destination: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_stations")
data class FavoriteStation(
    @PrimaryKey val stationCode: String,
    val stationName: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val type: String, // "train" or "station"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val category: String, // "alert", "news", "update", "announcement"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "trains_cache")
data class TrainEntity(
    @PrimaryKey val trainNumber: String,
    val trainName: String,
    val source: String,
    val destination: String,
    val type: String, // "Express" or "Passenger"
    val departureTime: String,
    val arrivalTime: String,
    val routeDesc: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stations_cache")
data class StationEntity(
    @PrimaryKey val stationCode: String,
    val stationName: String,
    val locationDescription: String,
    val facilitiesList: String, // comma separated list
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedules_cache")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trainNumber: String,
    val stationCode: String,
    val arrivalTime: String,
    val departureTime: String,
    val stopMinutes: Int,
    val distanceKm: Int,
    val stopNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "news_cache")
data class NewsEntity(
    @PrimaryKey val newsId: String,
    val title: String,
    val content: String,
    val category: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blogs_cache")
data class BlogEntity(
    @PrimaryKey val blogId: String,
    val title: String,
    val content: String,
    val category: String,
    val author: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val city: String,
    val temperature: String,
    val condition: String,
    val humidity: String,
    val wind: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_times_cache")
data class PrayerTimesCacheEntity(
    @PrimaryKey val city: String,
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analytics")
data class AnalyticsEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventName: String,
    val eventData: String,
    val timestamp: Long = System.currentTimeMillis()
)


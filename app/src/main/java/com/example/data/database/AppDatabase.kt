package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        FavoriteTrain::class,
        FavoriteStation::class,
        RecentSearch::class,
        NotificationItem::class,
        TrainEntity::class,
        StationEntity::class,
        ScheduleEntity::class,
        NewsEntity::class,
        BlogEntity::class,
        WeatherCacheEntity::class,
        PrayerTimesCacheEntity::class,
        AnalyticsEvent::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchDao(): SearchDao
    abstract fun notificationDao(): NotificationDao
    abstract fun trainDao(): TrainDao
    abstract fun stationDao(): StationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun newsDao(): NewsDao
    abstract fun blogDao(): BlogDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun prayerTimesCacheDao(): PrayerTimesCacheDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.model.NotificationItem
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New registration token generated: $token")
        
        // Push the new token to our production backend if a logged-in user session exists
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val activeUser = db.userDao().getActiveUser()
                if (activeUser != null) {
                    val apiService = ApiClient.apiService
                    apiService.registerFcmToken(FcmTokenRequest(activeUser.email, token))
                    Log.d("FCM", "Successfully uploaded refreshed FCM token for ${activeUser.email}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Failed to register new FCM token on server: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Companion Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Dynamic transit status updated."
        val category = remoteMessage.data["category"] ?: remoteMessage.data["type"] ?: "alert"

        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.notificationDao().insertNotification(
                    NotificationItem(
                        title = title,
                        message = body,
                        category = category,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        sendNotification(title, body, category)
    }

    private fun sendNotification(title: String, messageBody: String, category: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Select correct channel based on the dynamic message category
        val channelId = when (category.lowercase()) {
            "delay", "delays" -> "delays_channel"
            "news", "blogs" -> "news_channel"
            else -> "alerts_channel"
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(
                if (channelId == "news_channel") NotificationCompat.PRIORITY_DEFAULT 
                else NotificationCompat.PRIORITY_HIGH
            )
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Modern Material 3 Notification Channels Configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "alerts_channel",
                    "Critical Security & Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "FCM Broadcasts for emergency advisories, schedule changes and user auth alerts"
                },
                NotificationChannel(
                    "delays_channel",
                    "Train Delays & Tracking Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time delay tracking, arrival projections, and active GPS telemetry updates"
                },
                NotificationChannel(
                    "news_channel",
                    "Railways News & Promos",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Weekly newsletters, local tourism blogs, and transit guides"
                }
            )
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

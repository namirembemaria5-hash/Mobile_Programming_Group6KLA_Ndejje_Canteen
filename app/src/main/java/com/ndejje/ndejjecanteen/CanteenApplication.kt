package com.ndejje.ndejjecanteen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.ndejje.ndejjecanteen.utils.CanteenFirebaseMessagingService

class CanteenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        saveFcmToken()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CanteenFirebaseMessagingService.CHANNEL_ID,
                CanteenFirebaseMessagingService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CanteenFirebaseMessagingService.CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                getSharedPreferences("canteen_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", token)
                    .apply()
            }
        }
    }
}

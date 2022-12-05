package com.ashomapp.presentation.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.ashomapp.database.SharedPrefrenceHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

class NotificationServices : FirebaseMessagingService() {
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    companion object {
        var badgescounter = 0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        badgescounter = badgescounter + 1

        try {
            SharedPrefrenceHelper.notification_badges =
                SharedPrefrenceHelper.notification_badges?.plus(
                    1
                )
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            showNotification()
        } catch (e: Exception) {
            Log.d("errornotification", e.localizedMessage)
        }


    }

    fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)

            notificationChannel.apply {
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        //  val notificationId: Long = System.currentTimeMillis()
        //  notificationManager.notify(notificationId.toInt(), builder.build())
    }
}
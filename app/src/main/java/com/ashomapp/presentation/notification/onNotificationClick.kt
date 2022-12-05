package com.ashomapp.presentation.notification

import com.ashomapp.network.response.NotificationDTO

interface onNotificationClick {
    fun onNotificationClick(notificationDTO: NotificationDTO)
}
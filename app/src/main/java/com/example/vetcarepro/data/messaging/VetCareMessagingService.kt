package com.example.vetcarepro.data.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class VetCareMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "VetCare Pro"
        val body = message.notification?.body ?: message.data["message"] ?: "New alert"
        val targetRoute = message.data["targetRoute"] ?: "dashboard"
        VetCareNotificationHelper.showActionableNotification(this, title, body, targetRoute)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}


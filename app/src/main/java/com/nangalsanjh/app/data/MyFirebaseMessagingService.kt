package com.nangalsanjh.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nangalsanjh.app.MainActivity
import com.nangalsanjh.app.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Nangal Seva"
        val body = remoteMessage.notification?.body ?: ""

        sendNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid ?: return

        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "nangal_seva_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nangal Seva Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Complaint updates and community posts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }
}

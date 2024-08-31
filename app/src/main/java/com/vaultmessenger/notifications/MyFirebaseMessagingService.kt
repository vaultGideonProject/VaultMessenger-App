package com.vaultmessenger.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vaultmessenger.MainActivity
import com.vaultmessenger.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        val notification = remoteMessage.notification
        if (notification != null) {
            sendNotification(this, notification.title, notification.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server or save it as needed
        Log.d("FCM DEBUG", "FCM Registration Token: $token")
    }

    private fun sendNotification(context: Context, title: String?, messageBody: String?) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, "VaultMessengerChannel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Get the NotificationManager system service
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}

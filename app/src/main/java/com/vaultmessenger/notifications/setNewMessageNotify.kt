package com.vaultmessenger.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.vaultmessenger.MainActivity
import com.vaultmessenger.R

class setNewMessageNotify(private val context: Context, private val CHANNEL_ID: String) {

    companion object {
        private const val REQUEST_CODE = 1001 // Define a unique request code
    }

    private val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    private val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    private val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("New Message")
        .setContentText("You have a new message!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)


    fun addReplyAction(replyLabel: String, replyIntent: Intent) {
        val remoteInput: RemoteInput = RemoteInput.Builder("KEY_TEXT_REPLY")
            .setLabel(replyLabel)
            .build()

        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.baseline_send_24,
            replyLabel,
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        builder.addAction(action)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun show(activity: ComponentActivity) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE
                )
                return@with
            }

            val NOTIFICATION_ID = 1234
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}

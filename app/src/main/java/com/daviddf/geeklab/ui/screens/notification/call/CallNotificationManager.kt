package com.daviddf.geeklab.ui.screens.notification.call

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.daviddf.geeklab.R

object CallNotificationManager {
    private const val CHANNEL_ID = "call_notifications"
    private const val NOTIFICATION_ID = 1001

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.call_notification_title)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = context.getString(R.string.call_notification_desc)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("FullScreenIntentPolicy")
    fun showIncomingCall(context: Context, callerName: String) {
        createNotificationChannel(context)
        
        val person = Person.Builder()
            .setName(callerName)
            .setUri("tel:123456789")
            .setImportant(true)
            .build()

        val fullScreenIntent = Intent(context, TelecomCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("caller_name", callerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            fullScreenIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val answerBroadcastIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_ANSWER"
            putExtra("caller_name", callerName)
        }
        val answerIntent = PendingIntent.getBroadcast(
            context, 
            1, 
            answerBroadcastIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val declineBroadcastIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_DECLINE"
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val declineIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineBroadcastIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    person,
                    declineIntent,
                    answerIntent
                )
            )
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setOnlyAlertOnce(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) TelecomCallRepository.getInstance(context).isActivityVisible else false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    @SuppressLint("FullScreenIntentPolicy")
    fun showOngoingCall(context: Context, callerName: String) {
        createNotificationChannel(context)
        
        val person = Person.Builder()
            .setName(callerName)
            .setUri("tel:123456789")
            .setImportant(true)
            .build()

        val fullScreenIntent = Intent(context, TelecomCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("caller_name", callerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val hangUpBroadcastIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_HANG_UP"
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val hangUpIntent = PendingIntent.getBroadcast(
            context,
            3,
            hangUpBroadcastIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    person,
                    hangUpIntent
                )
            )
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOnlyAlertOnce(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) TelecomCallRepository.getInstance(context).isActivityVisible else false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

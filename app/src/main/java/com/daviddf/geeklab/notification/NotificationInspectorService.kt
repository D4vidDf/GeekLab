package com.daviddf.geeklab.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.daviddf.geeklab.data.notification.NotificationDatabase
import com.daviddf.geeklab.data.notification.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationInspectorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val bigText = extras.getCharSequence("android.bigText")?.toString()
        
        val entity = NotificationEntity(
            packageName = sbn.packageName,
            title = title,
            text = text,
            bigText = bigText,
            timestamp = sbn.postTime,
            channelId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notification.channelId
            } else {
                null
            },
            isClearable = sbn.isClearable,
            key = sbn.key
        )

        serviceScope.launch {
            NotificationDatabase.getDatabase(applicationContext).notificationDao().insert(entity)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: track removal
    }
}

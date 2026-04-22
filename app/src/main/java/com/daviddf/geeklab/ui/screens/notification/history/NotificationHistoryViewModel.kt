package com.daviddf.geeklab.ui.screens.notification.history

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.daviddf.geeklab.data.notification.NotificationDatabase
import com.daviddf.geeklab.data.notification.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationHistoryViewModel(private val context: Context) : ViewModel() {
    private val database = NotificationDatabase.getDatabase(context)
    val notifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isServiceEnabled(): Boolean {
        val packageName = context.packageName
        val flat = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(packageName) == true
    }

    fun getNotificationById(id: Long): Flow<NotificationEntity?> {
        // I need to add this to the DAO
        return database.notificationDao().getNotificationById(id)
    }

    fun onToggleClicked(context: Context) {
        if (isServiceEnabled()) {
            // If it's already enabled, we have to send them to settings to disable it.
            // Android doesn't allow apps to disable their own Listener Service programmatically.
            openNotificationListenerSettings(context)
        } else {
            // If it's disabled, send them to settings to enable it.
            openNotificationListenerSettings(context)
        }
    }
}

class NotificationHistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationHistoryViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun getFormattedDate(timestamp: Long, locale: Locale): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val notificationDate = Calendar.getInstance().apply { time = date }

    val isSameYear = now.get(Calendar.YEAR) == notificationDate.get(Calendar.YEAR)

    // Check if it's "Today" to show just time, otherwise show date
    val isToday = now.get(Calendar.DAY_OF_YEAR) == notificationDate.get(Calendar.DAY_OF_YEAR) && isSameYear

    return if (isToday) {
        SimpleDateFormat("h:mm a", locale).format(date)
    } else {
        val pattern = if (isSameYear) "EEE. d MMM" else "EEE. d MMM yyyy"
        SimpleDateFormat(pattern, locale).format(date)
    }
}
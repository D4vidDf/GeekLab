package com.daviddf.geeklab.ui.screens.notification.history

import android.app.Application
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.service.notification.NotificationListenerService
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.daviddf.geeklab.R
import com.daviddf.geeklab.data.notification.NotificationDatabase
import com.daviddf.geeklab.data.notification.NotificationEntity
import com.daviddf.geeklab.notification.NotificationInspectorService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NotificationDetailState(
    val notification: NotificationEntity,
    val appLabel: String,
    val appIcon: Bitmap?
)

class NotificationHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val context get() = getApplication<Application>().applicationContext
    private val database = NotificationDatabase.getDatabase(context)
    private val packageManager = context.packageManager
    val notifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()

    init {
        // Try to rebind the service if it's already enabled but not running
        // This helps after app updates or code changes
        refreshServiceConnection()
    }

    private fun refreshServiceConnection() {
        if (isServiceEnabled()) {
            try {
                val componentName = ComponentName(context, NotificationInspectorService::class.java)
                
                // Toggle the component state to force a rebind
                // This is a common workaround for NotificationListenerService unbinding after app updates
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                NotificationListenerService.requestRebind(componentName)
            } catch (_: Exception) {
            }
        }
    }

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

    fun getNotificationDetail(id: Long): Flow<NotificationDetailState?> {
        return database.notificationDao().getNotificationById(id).map { notification ->
            notification?.let {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(it.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    it.packageName
                }

                val appIcon = try {
                    val drawable = packageManager.getApplicationIcon(it.packageName)
                    val bitmap = createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1)
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                } catch (_: Exception) {
                    null
                }

                NotificationDetailState(it, appLabel, appIcon)
            }
        }
    }

    fun openApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (_: Exception) {
            // Handle error
        }
    }

    fun saveMediaToGallery(path: String): Boolean {
        return try {
            val file = File(path)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GeekLab")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            
            uri?.let {
                resolver.openOutputStream(it).use { out ->
                    FileInputStream(file).use { input ->
                        input.copyTo(out!!)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                true
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun shareMedia(path: String) {
        try {
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, context.getString(R.string.share_media_title))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Handle error
        }
    }

    fun onToggleClicked(context: Context) {
        if (isServiceEnabled()) {
            openNotificationListenerSettings(context)
        } else {
            openNotificationListenerSettings(context)
        }
    }
}

class NotificationHistoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationHistoryViewModel(application) as T
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

package com.daviddf.geeklab.notification

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.createBitmap
import com.daviddf.geeklab.data.notification.NotificationDatabase
import com.daviddf.geeklab.data.notification.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NotificationInspectorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notificationChannel = Channel<StatusBarNotification>(Channel.UNLIMITED)

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            notificationChannel.receiveAsFlow().collect { sbn ->
                handleNotificationPosted(sbn)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Offload from system callback immediately to avoid blocking system server binder threads
        notificationChannel.trySend(sbn)
    }

    private suspend fun handleNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        
        val mediaPaths = mutableListOf<String>()
        val extrasMap = mutableMapOf<String, String>()

        extras.keySet().forEach { key ->
            @Suppress("DEPRECATION")
            val value = extras.get(key)
            if (value != null) {
                when (value) {
                    is Bitmap -> {
                        saveMedia(value, key)?.let { mediaPaths.add(it) }
                        extrasMap[key] = "[Bitmap saved]"
                    }
                    is Icon -> {
                        val bitmap = value.loadDrawable(this)?.let { drawable ->
                            val b = createBitmap(
                                drawable.intrinsicWidth.coerceAtLeast(1),
                                drawable.intrinsicHeight.coerceAtLeast(1)
                            )
                            val canvas = android.graphics.Canvas(b)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            b
                        }
                        bitmap?.let { saveMedia(it, key)?.let { path -> mediaPaths.add(path) } }
                        extrasMap[key] = "[Icon saved]"
                    }
                    else -> {
                        extrasMap[key] = value.toString()
                    }
                }
            }
        }
        val extrasJson = try { Json.encodeToString(extrasMap) } catch (_: Exception) { null }

        val actionsList = mutableListOf<String>()
        notification.actions?.forEach { action ->
            actionsList.add(action.title?.toString() ?: "Unnamed Action")
        }
        val actionsJson = try { Json.encodeToString(actionsList) } catch (_: Exception) { null }
        val mediaPathsJson = if (mediaPaths.isNotEmpty()) Json.encodeToString(mediaPaths) else null

        val entity = NotificationEntity(
            packageName = sbn.packageName,
            title = title,
            text = text,
            bigText = bigText,
            subText = subText,
            timestamp = sbn.postTime,
            channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.channelId
            } else {
                null
            },
            isClearable = sbn.isClearable,
            key = sbn.key,
            category = notification.category,
            priority = @Suppress("DEPRECATION") notification.priority,
            visibility = notification.visibility,
            color = notification.color,
            extrasJson = extrasJson,
            actionsJson = actionsJson,
            mediaPathsJson = mediaPathsJson
        )

        NotificationDatabase.getDatabase(applicationContext).notificationDao().insert(entity)
    }

    private fun saveMedia(bitmap: Bitmap, key: String): String? {
        return try {
            val mediaDir = File(filesDir, "notification_media")
            if (!mediaDir.exists()) mediaDir.mkdirs()
            
            val fileName = "media_${UUID.randomUUID()}_${key.replace(".", "_")}.png"
            val file = File(mediaDir, fileName)
            FileOutputStream(file).use { out ->
                // Use lower quality or faster compression if needed, but PNG quality param is ignored.
                // WEBP might be faster/smaller on modern devices.
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: track removal
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationChannel.close()
    }
}

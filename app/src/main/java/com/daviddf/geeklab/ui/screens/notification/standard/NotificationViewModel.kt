package com.daviddf.geeklab.ui.screens.notification.standard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.daviddf.geeklab.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _count = MutableStateFlow("1")
    val count = _count.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _messageError = MutableStateFlow<String?>(null)
    val messageError = _messageError.asStateFlow()

    private val _countError = MutableStateFlow<String?>(null)
    val countError = _countError.asStateFlow()

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        _titleError.value = null
    }

    fun updateMessage(newMessage: String) {
        _message.value = newMessage
        _messageError.value = null
    }

    fun updateCount(newCount: String) {
        _count.value = newCount
        _countError.value = null
    }

    fun updateImage(context: Context, uri: Uri?) {
        _imageUri.value = uri
        if (uri != null) {
            _bitmap.value = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (_: Exception) {
                null
            }
        } else {
            _bitmap.value = null
        }
    }

    fun generateNotifications(
        context: Context,
        errorTitleRequired: String,
        errorTitleTooLong: String,
        errorMessageRequired: String,
        errorInvalidNumber: String,
        onPermissionNeeded: () -> Unit
    ) {
        var hasError = false
        if (_title.value.isEmpty()) {
            _titleError.value = errorTitleRequired
            hasError = true
        } else if (_title.value.length > 30) {
            _titleError.value = errorTitleTooLong
            hasError = true
        }

        if (_message.value.isEmpty()) {
            _messageError.value = errorMessageRequired
            hasError = true
        }

        val n = _count.value.toLongOrNull() ?: 0L
        if (n <= 0) {
            _countError.value = errorInvalidNumber
            hasError = true
        }

        if (!hasError) {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                sendNotifications(context, _title.value, _message.value, n, _bitmap.value)
            } else {
                onPermissionNeeded()
            }
        }
    }

    fun sendNotifications(context: Context, title: String, message: String, count: Long, bitmap: Bitmap?) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val channelId = "GeekLab"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        for (i in 1..count) {
            val builder = NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle(title)
                setContentText(message)
                setPriority(NotificationCompat.PRIORITY_MAX)
                setDefaults(Notification.DEFAULT_ALL)
                setAutoCancel(true)
                if (bitmap != null) {
                    setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(title)
                            .setSummaryText(message)
                    )
                } else {
                    setStyle(NotificationCompat.BigTextStyle().bigText(message))
                }
            }

            try {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt() + i.toInt(),
                    builder.build()
                )
            } catch (_: SecurityException) {
            }
        }
    }
}

package com.daviddf.geeklab.ui.screens.notification.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.lifecycle.ViewModel
import com.daviddf.geeklab.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MessageData(
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean
)

class MessagingNotificationViewModel : ViewModel() {
    private val _userName = MutableStateFlow("David")
    val userName = _userName.asStateFlow()

    private val _partnerName = MutableStateFlow("John Doe")
    val partnerName = _partnerName.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageData>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _currentMessageText = MutableStateFlow("")
    val currentMessageText = _currentMessageText.asStateFlow()

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun updatePartnerName(name: String) {
        _partnerName.value = name
    }

    fun updateCurrentMessageText(text: String) {
        _currentMessageText.value = text
    }

    fun addMessage(isFromMe: Boolean) {
        if (_currentMessageText.value.isBlank()) return
        
        val newMessage = MessageData(
            text = _currentMessageText.value,
            timestamp = System.currentTimeMillis(),
            isFromMe = isFromMe
        )
        _messages.value = _messages.value + newMessage
        _currentMessageText.value = ""
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun sendNotification(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val channelId = "messaging_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Messaging Style Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val user = Person.Builder()
            .setName(_userName.value)
            .setImportant(true)
            .build()

        val partner = Person.Builder()
            .setName(_partnerName.value)
            .setImportant(true)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(user)
            .setConversationTitle(if (_partnerName.value.isNotEmpty()) "Chat with ${_partnerName.value}" else null)

        _messages.value.forEach { msg ->
            messagingStyle.addMessage(
                msg.text,
                msg.timestamp,
                if (msg.isFromMe) null else partner
            )
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(
                2001,
                builder.build()
            )
        } catch (_: SecurityException) {
        }
    }
}

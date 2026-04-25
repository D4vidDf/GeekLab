package com.daviddf.geeklab.ui.screens.notification.call

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.daviddf.geeklab.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class TelecomCallService : Service() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_URI = "extra_uri"
        const val ACTION_INCOMING_CALL = "action_incoming_call"
        private const val ONGOING_NOTIFICATION_ID = 1001
    }

    private lateinit var telecomRepository: TelecomCallRepository
    private val scope = CoroutineScope(SupervisorJob())
    private var audioJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        telecomRepository = TelecomCallRepository.getInstance(applicationContext)

        telecomRepository.currentCall
            .onEach { call ->
                updateServiceState(call)
            }
            .launchIn(scope)
    }

    private fun updateServiceState(call: TelecomCall) {
        when (call) {
            is TelecomCall.Registered -> {
                if (call.isActive && !call.isOnHold) {
                    startForegroundService()
                    if (audioJob == null || audioJob?.isActive == false) {
                        audioJob = scope.launch {
                            AudioLoopSource.openAudioLoop()
                        }
                    }
                } else {
                    audioJob?.cancel()
                    audioJob = null
                    // If not active, we might still want to be in foreground for the incoming call
                    // but Telecom delegation usually handles that.
                }
            }
            is TelecomCall.Unregistered -> {
                audioJob?.cancel()
                audioJob = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            is TelecomCall.None -> {
                audioJob?.cancel()
                audioJob = null
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    private fun startForegroundService() {
        // We use the notification already created by CallNotificationManager
        // But we need to call startForeground to tell the system we are doing phoneCall work.
        // On Android 14+ this is required for RECORD_AUDIO in background.
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = notificationManager.activeNotifications.find { it.id == ONGOING_NOTIFICATION_ID }?.notification
            ?: createPlaceholderNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ONGOING_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }
    }

    private fun createPlaceholderNotification(): Notification {
        val channelId = "call_notifications"
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(getString(R.string.ongoing_call))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_INCOMING_CALL -> {
                val name = intent.getStringExtra(EXTRA_NAME) ?: getString(R.string.unknown_caller)
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_URI, Uri::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_URI)!!
                }
                
                scope.launch {
                    telecomRepository.registerCall(applicationContext, name, uri, true)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? = null
}

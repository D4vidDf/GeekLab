package com.daviddf.geeklab.ui.screens.notification.live

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.ProgressStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddf.geeklab.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveUpdateViewModel : ViewModel() {
    private var notificationManager: NotificationManager? = null
    private var appContext: Context? = null
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    private val _config = MutableStateFlow(SimulationConfig())
    val config = _config.asStateFlow()

    private var simulationJob: Job? = null

    companion object {
        const val CHANNEL_ID = "live_updates_channel_id"
        private const val CHANNEL_NAME = "live_updates_channel_name"
        private const val NOTIFICATION_ID = 1234
    }

    enum class ChipMode {
        TIMER, TEXT
    }

    data class SimulationConfig(
        val steps: Int = 5,
        val intervalMs: Long = 3000,
        val useColors: Boolean = true,
        val selectedPointColor: Int = 0xFFECB7FF.toInt(),
        val selectedSegmentColor: Int = 0xFF86F7FA.toInt(),
        val chipMode: ChipMode = ChipMode.TIMER,
        val customShortCriticalText: String = ""
    )

    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun updateConfig(newConfig: SimulationConfig) {
        _config.value = newConfig
    }

    fun startSimulation() {
        val context = appContext ?: return
        val manager = notificationManager ?: return
        val currentConfig = _config.value

        stopSimulation()
        _isSimulating.value = true
        simulationJob = viewModelScope.launch {
            try {
                for (step in 1..currentConfig.steps) {
                    val progress = (step.toFloat() / currentConfig.steps * 100).toInt()
                    val notification = buildNotification(context, step, currentConfig, progress).build()
                    manager.notify(NOTIFICATION_ID, notification)

                    if (step < currentConfig.steps) {
                        delay(currentConfig.intervalMs)
                    } else {
                        delay(1000)
                        _isSimulating.value = false
                    }
                }
            } finally {
                _isSimulating.value = false
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        notificationManager?.cancel(NOTIFICATION_ID)
        _isSimulating.value = false
    }

    private fun buildNotification(
        context: Context,
        step: Int,
        config: SimulationConfig,
        progress: Int
    ): NotificationCompat.Builder {
        val deleteIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("com.daviddf.geeklab.ACTION_STOP_SIMULATION").setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.step_title_format, step)
        val text = context.getString(R.string.step_text_format, progress)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setDeleteIntent(deleteIntent)

        if (Build.VERSION.SDK_INT >= 35) {
            builder.setRequestPromotedOngoing(true)
            
            if (config.chipMode == ChipMode.TEXT) {
                builder.setShortCriticalText(config.customShortCriticalText.takeIf { it.isNotBlank() } ?: text)
            }

            val progressStyle = ProgressStyle()
            progressStyle.setProgress(progress)

            if (config.useColors) {
                val segments = mutableListOf<ProgressStyle.Segment>()
                for (i in 1..config.steps) {
                    segments.add(
                        ProgressStyle.Segment((i.toFloat() / config.steps * 100).toInt())
                            .setColor(config.selectedSegmentColor)
                    )
                }
                progressStyle.setProgressSegments(segments)

                val points = mutableListOf<ProgressStyle.Point>()
                for (i in 1..step) {
                    points.add(
                        ProgressStyle.Point((i.toFloat() / config.steps * 100).toInt())
                            .setColor(config.selectedPointColor)
                    )
                }
                progressStyle.setProgressPoints(points)
            }
            builder.setStyle(progressStyle)
        } else {
            builder.setProgress(100, progress, false)
        }

        if (config.chipMode == ChipMode.TIMER && step < config.steps) {
            builder.setWhen(System.currentTimeMillis() + 10 * 60 * 1000)
            builder.setUsesChronometer(true)
            builder.setChronometerCountDown(true)
        }

        if (progress in 51..99) {
            builder.addAction(
                NotificationCompat.Action.Builder(null, context.getString(R.string.live_update_action_got_it), null).build()
            )
        } else if (progress == 100) {
            builder.addAction(
                NotificationCompat.Action.Builder(null, context.getString(R.string.live_update_action_rate), null).build()
            )
        }

        return builder
    }

    @SuppressLint("NewApi")
    fun isPostPromotionsEnabled(): Boolean {
        val manager = notificationManager ?: return true
        return if (Build.VERSION.SDK_INT >= 35) {
            try {
                manager.canPostPromotedNotifications()
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }
}

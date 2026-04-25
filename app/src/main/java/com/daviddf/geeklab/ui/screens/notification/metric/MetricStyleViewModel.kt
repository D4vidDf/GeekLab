package com.daviddf.geeklab.ui.screens.notification.metric

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.notification.Not
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MetricStyleViewModel : ViewModel() {

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    private val _numMetrics = MutableStateFlow(3)
    val numMetrics = _numMetrics.asStateFlow()

    private val _metricsPromoted = MutableStateFlow(listOf(true, true, true))
    val metricsPromoted = _metricsPromoted.asStateFlow()

    fun updatePermissionStatus(context: Context) {
        _hasPermission.value = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun updateNumMetrics(count: Int) {
        _numMetrics.value = count
    }

    fun toggleMetricPromotion(index: Int, promoted: Boolean) {
        _metricsPromoted.update { current ->
            current.toMutableList().apply { this[index] = promoted }
        }
    }

    fun showMetricNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 37) {
            val startTime = SystemClock.elapsedRealtime() - 1000 * 60 * 5
            
            val metricStyle = Notification.MetricStyle()
            val promotedStates = _metricsPromoted.value

            if (_numMetrics.value >= 1) {
                val stepsMetric = Notification.Metric(
                    Notification.Metric.FixedInt(1979),
                    context.getString(R.string.metric_steps)
                )
                metricStyle.addMetric(stepsMetric).setCriticalMetric(0)
            }
            
            if (_numMetrics.value >= 2) {
                val timeMetric = Notification.Metric(
                    Notification.Metric.TimeDifference.forStopwatch(
                        startTime,
                        Notification.Metric.TimeDifference.FORMAT_CHRONOMETER
                    ),
                    context.getString(R.string.metric_active_time)
                )
                metricStyle.addMetric(timeMetric).setCriticalMetric(1)
            }
            
            if (_numMetrics.value >= 3) {
                val distanceMetric = Notification.Metric(
                    Notification.Metric.FixedFloat(1.4f),
                    context.getString(R.string.metric_distance)
                )
                metricStyle.addMetric(distanceMetric).setCriticalMetric(2)
            }

            val notification = Notification.Builder(context, Not.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.metric_workout_summary))
                .setContentText(context.getString(R.string.metric_workout_status))
                .setStyle(metricStyle)
                .setOngoing(true)
                .setRequestPromotedOngoing(true)
                .build()

            notificationManager.notify(1001, notification)
        } else {
            val notification = androidx.core.app.NotificationCompat.Builder(context, Not.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.metric_fallback_title))
                .setContentText(context.getString(R.string.metric_fallback_text))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        }
    }
}

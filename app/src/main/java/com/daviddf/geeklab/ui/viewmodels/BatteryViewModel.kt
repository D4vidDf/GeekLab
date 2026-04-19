package com.daviddf.geeklab.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.daviddf.geeklab.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

// --- Model ---
data class BatteryState(
    val percentage: Float = 0f,
    val statusResId: Int = R.string.unknown,
    val description: String = "",
    val capacity: String = "-- mAh",
    val temperature: String = "-- °C",
    val voltage: String = "-- V",
    val isCharging: Boolean = false,
    val chargeTimeRemaining: String = "",
    val technology: String = "Li-ion",
    val cycleCount: Int = 0,
    val pluggedTypeResId: Int = R.string.none,
    val healthType: HealthType = HealthType.UNKNOWN
)

enum class HealthType {
    GOOD, WARNING, CRITICAL, UNKNOWN
}

// --- Logic (ViewModel) ---
class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BatteryState())
    val uiState: StateFlow<BatteryState> = _uiState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = if (level != -1 && scale != -1) level.toFloat() / scale else 0f

            val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempValue = tempTenths / 10f
            val temperature = context.getString(R.string.battery_temp_format, tempValue)

            val voltMilli = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val voltValue = voltMilli / 1000f
            
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL

            val voltageStr = context.getString(R.string.battery_voltage_format, voltValue)

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val (pluggedTypeResId, _) = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> R.string.ac_adapter to "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_USB -> R.string.usb_port to "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> R.string.wireless_pad to "Wireless Pad"
                else -> R.string.none to "None"
            }

            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val (healthResId, healthType) = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good to HealthType.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat to HealthType.CRITICAL
                BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead to HealthType.CRITICAL
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage to HealthType.WARNING
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_failure to HealthType.CRITICAL
                BatteryManager.BATTERY_HEALTH_COLD -> R.string.battery_health_cold to HealthType.WARNING
                else -> R.string.unknown to HealthType.UNKNOWN
            }

            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            
            // Cycle count (Available on Android 14+)
            val cycles = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    bm?.getIntProperty(8) ?: 0
                } else 0
            } catch (_: SecurityException) {
                0
            }

            // Estimate charge time remaining (Available on Android 9+)
            var timeRemainingStr = ""
            if (isCharging && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val timeRemainingMs = try {
                    bm?.computeChargeTimeRemaining() ?: -1L
                } catch (_: SecurityException) {
                    -1L
                }
                if (timeRemainingMs > 0) {
                    val hours = TimeUnit.MILLISECONDS.toHours(timeRemainingMs)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemainingMs) % 60
                    timeRemainingStr = if (hours > 0) {
                        context.getString(R.string.hours_minutes_short, hours, minutes)
                    } else {
                        context.getString(R.string.minutes_short, minutes)
                    }
                }
            }

            val chargeCounter = try {
                bm?.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0L
            } catch (_: SecurityException) {
                0L
            }
            val estimatedTotal = if (percentage > 0) (chargeCounter / percentage / 1000).toInt() else 0
            val capacityStr = if (estimatedTotal > 0) {
                context.getString(R.string.battery_capacity_format, estimatedTotal)
            } else {
                context.getString(R.string.not_available)
            }

            val healthStatusText = context.getString(R.string.battery_health_status, context.getString(healthResId))
            val chargeStatusText = if (isCharging) {
                val chargingInfo = if (timeRemainingStr.isNotEmpty()) {
                    context.getString(R.string.charging_left, timeRemainingStr)
                } else {
                    context.getString(R.string.charging_via_short, context.getString(pluggedTypeResId))
                }
                "$chargingInfo\n$healthStatusText"
            } else {
                healthStatusText
            }

            _uiState.value = BatteryState(
                percentage = percentage,
                statusResId = if (isCharging) R.string.charging else healthResId,
                description = chargeStatusText,
                capacity = capacityStr,
                temperature = temperature,
                voltage = voltageStr,
                isCharging = isCharging,
                chargeTimeRemaining = timeRemainingStr,
                technology = technology,
                cycleCount = cycles,
                pluggedTypeResId = pluggedTypeResId,
                healthType = healthType
            )
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
            // Ignore
        }
    }
}

package com.daviddf.geeklab.ui.viewmodels

import android.annotation.SuppressLint
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
    val healthType: HealthType = HealthType.UNKNOWN,
    val currentNow: String = "-- mA",
    val currentAverage: String = "-- mA",
    val remainingEnergy: String = "-- mWh",
    val powerProfileExtras: Map<String, String> = emptyMap()
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

            // --- Improved Battery Capacity Calculation ---
            val totalCapacityMah = getBatteryCapacity(context, bm, percentage)
            val capacityStr = if (totalCapacityMah > 0) {
                context.getString(R.string.battery_capacity_format, totalCapacityMah)
            } else {
                context.getString(R.string.not_available)
            }

            // --- Additional Battery Metrics ---
            val currentNow = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
            val currentAverage = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) ?: 0
            val energyCounter = bm?.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) ?: 0L

            val currentNowStr = if (currentNow != Int.MIN_VALUE) "${currentNow / 1000} mA" else "-- mA"
            val currentAverageStr = if (currentAverage != Int.MIN_VALUE) "${currentAverage / 1000} mA" else "-- mA"
            val energyCounterStr = if (energyCounter > 0) "${energyCounter / 1_000_000} mWh" else "-- mWh"

            // --- PowerProfile Extras ---
            val extras = getPowerProfileExtras(context)

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
                healthType = healthType,
                currentNow = currentNowStr,
                currentAverage = currentAverageStr,
                remainingEnergy = energyCounterStr,
                powerProfileExtras = extras
            )
        }
    }

    @SuppressLint("PrivateApi", "DefaultLocale")
    private fun getPowerProfileExtras(context: Context): Map<String, String> {
        val extras = mutableMapOf<String, String>()
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfileConstructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = powerProfileConstructor.newInstance(context)
            
            val getAveragePower = powerProfileClass.getMethod("getAveragePower", String::class.java)
            
            // Commonly available PowerProfile constants
            val constants = mapOf(
                "battery.capacity" to "Capacidad de diseño",
                "cpu.active" to "CPU Activa",
                "wifi.active" to "Wi-Fi Activo",
                "gps.on" to "GPS Activo",
                "bluetooth.active" to "Bluetooth Activo",
                "screen.on" to "Pantalla Activa",
                "dsp.audio" to "Audio DSP",
                "dsp.video" to "Video DSP"
            )

            constants.forEach { (key, label) ->
                try {
                    val value = getAveragePower.invoke(powerProfileInstance, key) as Double
                    if (value > 0) {
                        extras[label] = String.format("%.2f mA", value)
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        return extras
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * Attempts to get the total battery capacity in mAh.
     * Uses PowerProfile reflection (most accurate for design capacity) 
     * with a fallback to the charge counter estimation.
     */
    @SuppressLint("PrivateApi")
    private fun getBatteryCapacity(context: Context, bm: BatteryManager?, percentage: Float): Int {
        // Method 1: PowerProfile (Design Capacity)
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfileConstructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = powerProfileConstructor.newInstance(context)
            val batteryCapacityMethod = powerProfileClass.getMethod("getBatteryCapacity")
            val capacity = batteryCapacityMethod.invoke(powerProfileInstance) as Double
            if (capacity > 0) return capacity.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Method 2: Charge Counter Fallback (Actual available capacity)
        val chargeCounter = try {
            bm?.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0L
        } catch (_: SecurityException) {
            0L
        }
        
        // If the counter is in micro-ampere-hours, convert to mAh
        if (chargeCounter > 0 && percentage > 0) {
            val estimated = (chargeCounter / percentage / 1000).toInt()
            if (estimated > 100) return estimated // Sanity check for realistic values
        }
        
        return 0
    }
}

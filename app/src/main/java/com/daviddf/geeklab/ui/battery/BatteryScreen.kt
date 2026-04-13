package com.daviddf.geeklab.ui.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.*
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
            val temperature = "${tempTenths / 10f} °C"

            val voltMilli = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL

            val voltageStr = "${voltMilli / 1000f} V"

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
                    timeRemainingStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                }
            }

            val chargeCounter = try {
                bm?.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0L
            } catch (_: SecurityException) {
                0L
            }
            val estimatedTotal = if (percentage > 0) (chargeCounter / percentage / 1000).toInt() else 0
            val capacityStr = if (estimatedTotal > 0) "$estimatedTotal mAh" else "-- mAh"

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

// --- UI ---
@Composable
fun BatteryScreen(
    onBackClick: () -> Unit,
    viewModel: BatteryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BatteryContent(
        state = state,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryContent(
    state: BatteryState,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.battery_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWide = this.maxWidth >= 600.dp
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column: Battery Status - Scrollable for smaller "wide" screens
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        BatteryStatusCard(
                            percentage = state.percentage,
                            status = stringResource(state.statusResId),
                            description = state.description,
                            isCharging = state.isCharging,
                            healthType = state.healthType,
                            isWideLayout = true
                        )
                        // Add some bottom padding to ensure content isn't cut off when scrolled
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Right Column: Scrollable Stats
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            StatCard(
                                value = state.capacity,
                                label = stringResource(R.string.estimated_capacity),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    value = state.temperature,
                                    label = stringResource(R.string.temperature),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    value = state.voltage,
                                    label = stringResource(R.string.voltage),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            InfoCard(
                                title = stringResource(R.string.battery_info),
                                content = buildString {
                                    if (state.isCharging) {
                                        append(stringResource(R.string.charging_via, stringResource(state.pluggedTypeResId)))
                                        append("\n")
                                        append(stringResource(R.string.charging_voltage, state.voltage))
                                        append("\n")
                                    } else {
                                        append(stringResource(R.string.voltage_label, state.voltage))
                                        append("\n")
                                    }
                                    append(stringResource(R.string.technology, state.technology))
                                    append("\n")
                                    if (state.cycleCount > 0) {
                                        append(stringResource(R.string.cycle_count, state.cycleCount))
                                        append("\n")
                                    }
                                    append("\n")
                                    append(stringResource(R.string.battery_monitoring_desc))
                                }
                            )
                        }
                    }
                }
            } else {
                // Standard Vertical Layout
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BatteryStatusCard(
                            percentage = state.percentage,
                            status = stringResource(state.statusResId),
                            description = state.description,
                            isCharging = state.isCharging,
                            healthType = state.healthType,
                            isWideLayout = false
                        )
                    }

                    item {
                        StatCard(
                            value = state.capacity,
                            label = stringResource(R.string.estimated_capacity),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                value = state.temperature,
                                label = stringResource(R.string.temperature),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.voltage,
                                label = stringResource(R.string.voltage),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        InfoCard(
                            title = stringResource(R.string.battery_info),
                            content = buildString {
                                if (state.isCharging) {
                                    append(stringResource(R.string.charging_via, stringResource(state.pluggedTypeResId)))
                                    append("\n")
                                    append(stringResource(R.string.charging_voltage, state.voltage))
                                    append("\n")
                                } else {
                                    append(stringResource(R.string.voltage_label, state.voltage))
                                    append("\n")
                                }
                                append(stringResource(R.string.technology, state.technology))
                                append("\n")
                                if (state.cycleCount > 0) {
                                    append(stringResource(R.string.cycle_count, state.cycleCount))
                                    append("\n")
                                }
                                append("\n")
                                append(stringResource(R.string.battery_monitoring_desc))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryStatusCard(
    percentage: Float,
    status: String,
    description: String,
    isCharging: Boolean,
    healthType: HealthType,
    isWideLayout: Boolean = false
) {
    val cardColor = when (healthType) {
        HealthType.GOOD -> CardBateriaGood
        HealthType.WARNING -> CardBateriaWarning
        HealthType.CRITICAL -> CardBateriaCritical
        HealthType.UNKNOWN -> CardBateriaUnknown
    }
    val textColor = when (healthType) {
        HealthType.GOOD -> TextBateriaGood
        HealthType.WARNING -> TextBateriaWarning
        HealthType.CRITICAL -> TextBateriaCritical
        HealthType.UNKNOWN -> TextBateriaUnknown
    }

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "percentage"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "chargingIcon")
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isWideLayout) Modifier.wrapContentHeight() else Modifier.height(160.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        if (isWideLayout) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    val strokeWidth = 12.dp
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = textColor.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = textColor,
                            startAngle = -90f,
                            sweepAngle = animatedPercentage * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    AnimatedContent(
                        targetState = isCharging,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "chargingIconContentWide"
                    ) { charging ->
                        if (charging) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.ElectricBolt,
                                    contentDescription = null,
                                    tint = textColor.copy(alpha = iconAlpha),
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    text = stringResource(R.string.charging),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.percentage_format, (animatedPercentage * 100).toInt()),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = isCharging to status,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "headlineContentWide"
                    ) { (charging, statusText) ->
                        Text(
                            text = if (charging) {
                                stringResource(R.string.status_percent_format, statusText, (animatedPercentage * 100).toInt())
                            } else {
                                statusText
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedContent(
                        targetState = description,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "descriptionContentWide"
                    ) { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    val strokeWidth = 10.dp
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = textColor.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = textColor,
                            startAngle = -90f,
                            sweepAngle = animatedPercentage * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    AnimatedContent(
                        targetState = isCharging,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "chargingIconContent"
                    ) { charging ->
                        if (charging) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.ElectricBolt,
                                    contentDescription = null,
                                    tint = textColor.copy(alpha = iconAlpha),
                                    modifier = Modifier.size(40.dp)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.charging),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.percentage_format, (animatedPercentage * 100).toInt()),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(textColor.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = isCharging to status,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "headlineContent"
                    ) { (charging, statusText) ->
                        Text(
                            text = if (charging) {
                                stringResource(R.string.status_percent_format, statusText, (animatedPercentage * 100).toInt())
                            } else {
                                statusText
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 2
                        )
                    }
                    AnimatedContent(
                        targetState = description,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "descriptionContent"
                    ) { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (MaterialTheme.colorScheme.background == BackgroundDark) {
        Color(0xFFE5E7EB)
    } else {
        Color(0xFFE8EAF6)
    }
    val contentColor = Color(0xFF4527A0)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: String
) {
    val containerColor = if (MaterialTheme.colorScheme.background == BackgroundDark) {
        Color(0xFFE5E7EB)
    } else {
        Color(0xFFE8EAF6)
    }
    val contentColor = Color(0xFF4527A0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryScreenPreview() {
    GeekLabTheme {
        BatteryContent(
            state = BatteryState(
                percentage = 0.9f, 
                statusResId = R.string.battery_health_good, 
                description = "Battery health: Good",
                isCharging = false,
                healthType = HealthType.GOOD
            ), 
            onBackClick = {}
        )
    }
}

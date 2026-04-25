package com.daviddf.geeklab.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.*

data class ToolItem(
    val titleResId: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val isXiaomiOnly: Boolean = false,
    val minApi: Int = 0,
    val action: (Context, ToolsActions) -> Unit
)

data class ToolCategory(
    val titleResId: Int,
    val items: List<ToolItem>
)

interface ToolsActions {
    fun onNotificationClick()
    fun onLiveUpdateClick()
    fun onMetricStyleClick()
    fun onBatteryClick()
    fun onInfoClick()
    fun onAppsClick()
    fun onNotificationHistoryClick()
    fun onCallNotificationClick()
    fun onBluetoothClick()
    fun onBluetoothBleClick()
    fun onNfcScannerClick()
    fun onWifiClick()
    fun onWifiScannerClick()
    fun onCameraXClick()
}

object ToolsData {
    private fun launchSettings(context: Context, packageName: String, className: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setClassName(packageName, className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(R.string.error_function_not_available), Toast.LENGTH_LONG).show()
        }
    }

    val categories = listOf(
        ToolCategory(
            titleResId = R.string.category_device,
            items = listOf(
                ToolItem(
                    titleResId = R.string.info_title,
                    icon = Icons.Rounded.Info,
                    containerColor = CardInformacion,
                    contentColor = TextInformacion,
                    action = { _, actions -> actions.onInfoClick() }
                ),
                ToolItem(
                    titleResId = R.string.battery_title,
                    icon = Icons.Rounded.BatteryFull,
                    containerColor = CardBateria,
                    contentColor = TextBateria,
                    action = { _, actions -> actions.onBatteryClick() }
                ),
                ToolItem(
                    titleResId = R.string.wifi_analyzer_title,
                    icon = Icons.Rounded.Wifi,
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF1A237E),
                    action = { _, actions -> actions.onWifiClick() }
                ),
                ToolItem(
                    titleResId = R.string.wifi_scanner_title,
                    icon = Icons.Rounded.WifiFind,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF7B1FA2),
                    action = { _, actions -> actions.onWifiScannerClick() }
                ),
                ToolItem(
                    titleResId = R.string.camera_title,
                    icon = Icons.Rounded.PhotoCamera,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    action = { _, actions -> actions.onCameraXClick() }
                ),
                ToolItem(
                    titleResId = R.string.data_usage,
                    icon = Icons.Rounded.PermDeviceInformation,
                    containerColor = Color(0xFFE1F5FE),
                    contentColor = Color(0xFF01579B),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.xiaomi.misettings", "com.xiaomi.misettings.usagestats.UsageStatsMainActivity") }
                )
            )
        ),
        ToolCategory(
            titleResId = R.string.category_apps,
            items = listOf(
                ToolItem(
                    titleResId = R.string.apps_title,
                    icon = Icons.Rounded.GridView,
                    containerColor = CardAplicaciones,
                    contentColor = TextAplicaciones,
                    action = { _, actions -> actions.onAppsClick() }
                ),
                ToolItem(
                    titleResId = R.string.multiaccount,
                    icon = Icons.Rounded.Group,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF4A148C),
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$UserSettingsActivity") }
                )
            )
        ),
        ToolCategory(
            titleResId = R.string.category_notifications,
            items = listOf(
                ToolItem(
                    titleResId = R.string.notification_history,
                    icon = Icons.Rounded.History,
                    containerColor = Color(0xFFE1F5FE),
                    contentColor = Color(0xFF01579B),
                    action = { _, actions -> actions.onNotificationHistoryClick() }
                ),
                ToolItem(
                    titleResId = R.string.call_notification_title,
                    icon = Icons.Rounded.Call,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    action = { _, actions -> actions.onCallNotificationClick() }
                ),
                ToolItem(
                    titleResId = R.string.notificaciones,
                    icon = Icons.Rounded.Notifications,
                    containerColor = CardNotificaciones,
                    contentColor = TextNotificaciones,
                    action = { _, actions -> actions.onNotificationClick() }
                ),
                ToolItem(
                    titleResId = R.string.live_update_title,
                    icon = Icons.Rounded.Sync,
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF1A237E),
                    minApi = 36,
                    action = { _, actions -> actions.onLiveUpdateClick() }
                ),
                ToolItem(
                    titleResId = R.string.metric_style_title,
                    icon = Icons.Rounded.BarChart,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    minApi = 37,
                    action = { _, actions -> actions.onMetricStyleClick() }
                )
            )
        ),
        ToolCategory(
            titleResId = R.string.category_developer,
            items = listOf(
                ToolItem(
                    titleResId = R.string.developer_options,
                    icon = Icons.Rounded.Code,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF1B5E20),
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$DevelopmentSettingsActivity") }
                ),
                ToolItem(
                    titleResId = R.string.bluetooth_title,
                    icon = Icons.Rounded.Bluetooth,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    action = { _, actions -> actions.onBluetoothClick() }
                ),
                ToolItem(
                    titleResId = R.string.bluetooth_ble_title,
                    icon = Icons.AutoMirrored.Rounded.BluetoothSearching,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF7B1FA2),
                    action = { _, actions -> actions.onBluetoothBleClick() }
                ),
                ToolItem(
                    titleResId = R.string.nfc_scanner_title,
                    icon = Icons.Rounded.Nfc,
                    containerColor = Color(0xFFEFEBE9),
                    contentColor = Color(0xFF4E342E),
                    action = { _, actions -> actions.onNfcScannerClick() }
                ),
                ToolItem(
                    titleResId = R.string.band_selector,
                    icon = Icons.Rounded.CellTower,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.MiuiBandMode") }
                ),
                ToolItem(
                    titleResId = R.string.hdr_enhance,
                    icon = Icons.Rounded.HdrOn,
                    containerColor = Color(0xFFFCE4EC),
                    contentColor = Color(0xFF880E4F),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity") }
                ),
                ToolItem(
                    titleResId = R.string.improve_speed_connection,
                    icon = Icons.Rounded.Speed,
                    containerColor = Color(0xFFE0F2F1),
                    contentColor = Color(0xFF004D40),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings") }
                ),
                ToolItem(
                    titleResId = R.string.performance_mode,
                    icon = Icons.Rounded.Memory,
                    containerColor = Color(0xFFEFEBE9),
                    contentColor = Color(0xFF3E2723),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.qualcomm.qti.performancemode", "com.qualcomm.qti.performancemode.PerformanceModeActivity") }
                ),
                ToolItem(
                    titleResId = R.string.qcolor,
                    icon = Icons.Rounded.ColorLens,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity") }
                )
            )
        )
    )
}

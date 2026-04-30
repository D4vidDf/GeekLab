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
    val id: String,
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
    fun onWidgetInspectorClick()
    fun onNotificationHistoryClick()
    fun onCallNotificationClick()
    fun onBluetoothClick()
    fun onBluetoothBleClick()
    fun onNfcScannerClick()
    fun onWifiClick()
    fun onWifiScannerClick()
    fun onCameraXClick()
    fun onUltraHdrClick()
    fun onWebAnalyzerClick()
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
                    id = "device_info",
                    titleResId = R.string.info_title,
                    icon = Icons.Rounded.Info,
                    containerColor = CardInformacion,
                    contentColor = TextInformacion,
                    action = { _, actions -> actions.onInfoClick() }
                ),
                ToolItem(
                    id = "battery_info",
                    titleResId = R.string.battery_title,
                    icon = Icons.Rounded.BatteryFull,
                    containerColor = CardBateria,
                    contentColor = TextBateria,
                    action = { _, actions -> actions.onBatteryClick() }
                ),
                ToolItem(
                    id = "wifi_analyzer",
                    titleResId = R.string.wifi_analyzer_title,
                    icon = Icons.Rounded.Wifi,
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF1A237E),
                    action = { _, actions -> actions.onWifiClick() }
                ),
                ToolItem(
                    id = "wifi_scanner",
                    titleResId = R.string.wifi_scanner_title,
                    icon = Icons.Rounded.WifiFind,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF7B1FA2),
                    action = { _, actions -> actions.onWifiScannerClick() }
                ),
                ToolItem(
                    id = "camera",
                    titleResId = R.string.camera_title,
                    icon = Icons.Rounded.PhotoCamera,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    action = { _, actions -> actions.onCameraXClick() }
                ),
                ToolItem(
                    id = "ultrahdr",
                    titleResId = R.string.ultrahdr_title,
                    icon = Icons.Rounded.HdrOn,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    minApi = 34,
                    action = { _, actions -> actions.onUltraHdrClick() }
                ),
                ToolItem(
                    id = "data_usage",
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
                    id = "apps_viewer",
                    titleResId = R.string.apps_title,
                    icon = Icons.Rounded.GridView,
                    containerColor = CardAplicaciones,
                    contentColor = TextAplicaciones,
                    action = { _, actions -> actions.onAppsClick() }
                ),
                ToolItem(
                    id = "widget_inspector",
                    titleResId = R.string.widget_inspector_title,
                    icon = Icons.Rounded.Widgets,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    action = { _, actions -> actions.onWidgetInspectorClick() }
                ),
                ToolItem(
                    id = "multiaccount",
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
                    id = "notification_history",
                    titleResId = R.string.notification_history,
                    icon = Icons.Rounded.History,
                    containerColor = Color(0xFFE1F5FE),
                    contentColor = Color(0xFF01579B),
                    action = { _, actions -> actions.onNotificationHistoryClick() }
                ),
                ToolItem(
                    id = "call_notification",
                    titleResId = R.string.call_notification_title,
                    icon = Icons.Rounded.Call,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    action = { _, actions -> actions.onCallNotificationClick() }
                ),
                ToolItem(
                    id = "standard_notification",
                    titleResId = R.string.notificaciones,
                    icon = Icons.Rounded.Notifications,
                    containerColor = CardNotificaciones,
                    contentColor = TextNotificaciones,
                    action = { _, actions -> actions.onNotificationClick() }
                ),
                ToolItem(
                    id = "live_update",
                    titleResId = R.string.live_update_title,
                    icon = Icons.Rounded.Sync,
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF1A237E),
                    minApi = 36,
                    action = { _, actions -> actions.onLiveUpdateClick() }
                ),
                ToolItem(
                    id = "metric_style",
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
                    id = "developer_options",
                    titleResId = R.string.developer_options,
                    icon = Icons.Rounded.Code,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF1B5E20),
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$DevelopmentSettingsActivity") }
                ),
                ToolItem(
                    id = "bluetooth_scanner",
                    titleResId = R.string.bluetooth_title,
                    icon = Icons.Rounded.Bluetooth,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    action = { _, actions -> actions.onBluetoothClick() }
                ),
                ToolItem(
                    id = "ble_scanner",
                    titleResId = R.string.bluetooth_ble_title,
                    icon = Icons.AutoMirrored.Rounded.BluetoothSearching,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF7B1FA2),
                    action = { _, actions -> actions.onBluetoothBleClick() }
                ),
                ToolItem(
                    id = "nfc_scanner",
                    titleResId = R.string.nfc_scanner_title,
                    icon = Icons.Rounded.Nfc,
                    containerColor = Color(0xFFEFEBE9),
                    contentColor = Color(0xFF4E342E),
                    action = { _, actions -> actions.onNfcScannerClick() }
                ),
                ToolItem(
                    id = "band_selector",
                    titleResId = R.string.band_selector,
                    icon = Icons.Rounded.CellTower,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.MiuiBandMode") }
                ),
                ToolItem(
                    id = "hdr_enhance",
                    titleResId = R.string.hdr_enhance,
                    icon = Icons.Rounded.HdrOn,
                    containerColor = Color(0xFFFCE4EC),
                    contentColor = Color(0xFF880E4F),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity") }
                ),
                ToolItem(
                    id = "improve_speed_connection",
                    titleResId = R.string.improve_speed_connection,
                    icon = Icons.Rounded.Speed,
                    containerColor = Color(0xFFE0F2F1),
                    contentColor = Color(0xFF004D40),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings") }
                ),
                ToolItem(
                    id = "performance_mode",
                    titleResId = R.string.performance_mode,
                    icon = Icons.Rounded.Memory,
                    containerColor = Color(0xFFEFEBE9),
                    contentColor = Color(0xFF3E2723),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.qualcomm.qti.performancemode", "com.qualcomm.qti.performancemode.PerformanceModeActivity") }
                ),
                ToolItem(
                    id = "qcolor",
                    titleResId = R.string.qcolor,
                    icon = Icons.Rounded.ColorLens,
                    containerColor = Color(0xFFF1F8E9),
                    contentColor = Color(0xFF33691E),
                    isXiaomiOnly = true,
                    action = { ctx, _ -> launchSettings(ctx, "com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity") }
                ),
                ToolItem(
                    id = "web_analyzer",
                    titleResId = R.string.web_analyzer_title,
                    icon = Icons.Rounded.Web,
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF3F51B5),
                    action = { _, actions -> actions.onWebAnalyzerClick() }
                )
            )
        )
    )
}

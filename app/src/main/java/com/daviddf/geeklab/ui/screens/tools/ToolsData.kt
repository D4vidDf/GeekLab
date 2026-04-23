package com.daviddf.geeklab.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
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
                ToolItem(R.string.info_title, Icons.Rounded.Info, CardInformacion, TextInformacion) { _, actions -> actions.onInfoClick() },
                ToolItem(R.string.battery_title, Icons.Rounded.BatteryFull, CardBateria, TextBateria) { _, actions -> actions.onBatteryClick() },
                ToolItem(R.string.data_usage, Icons.Rounded.PermDeviceInformation, Color(0xFFE1F5FE), Color(0xFF01579B), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.xiaomi.misettings", "com.xiaomi.misettings.usagestats.UsageStatsMainActivity")
                }
            )
        ),
        ToolCategory(
            titleResId = R.string.category_apps,
            items = listOf(
                ToolItem(R.string.apps_title, Icons.Rounded.GridView, CardAplicaciones, TextAplicaciones) { _, actions -> actions.onAppsClick() },
                ToolItem(R.string.multiaccount, Icons.Rounded.Group, Color(0xFFF3E5F5), Color(0xFF4A148C)) { ctx, _ ->
                    launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$UserSettingsActivity")
                }
            )
        ),
        ToolCategory(
            titleResId = R.string.category_notifications,
            items = listOf(
                ToolItem(R.string.notification_history, Icons.Rounded.History, Color(0xFFE1F5FE), Color(0xFF01579B)) { _, actions -> actions.onNotificationHistoryClick() },
                ToolItem(R.string.call_notification_title, Icons.Rounded.Call, Color(0xFFF1F8E9), Color(0xFF33691E)) { _, actions -> actions.onCallNotificationClick() },
                ToolItem(R.string.notificaciones, Icons.Rounded.Notifications, CardNotificaciones, TextNotificaciones) { _, actions -> actions.onNotificationClick() },
                ToolItem(R.string.live_update_title, Icons.Rounded.Sync, Color(0xFFE8EAF6), Color(0xFF1A237E), minApi = 36) { _, actions -> actions.onLiveUpdateClick() },
                ToolItem(R.string.metric_style_title, Icons.Rounded.BarChart, Color(0xFFF1F8E9), Color(0xFF33691E), minApi = 37) { _, actions -> actions.onMetricStyleClick() }
            )
        ),
        ToolCategory(
            titleResId = R.string.category_developer,
            items = listOf(
                ToolItem(R.string.developer_options, Icons.Rounded.Code, Color(0xFFE8F5E9), Color(0xFF1B5E20)) { ctx, _ ->
                    launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$DevelopmentSettingsActivity")
                },
                ToolItem(R.string.band_selector, Icons.Rounded.CellTower, Color(0xFFFFF3E0), Color(0xFFE65100), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.android.settings", "com.android.settings.MiuiBandMode")
                },
                ToolItem(R.string.hdr_enhance, Icons.Rounded.HdrOn, Color(0xFFFCE4EC), Color(0xFF880E4F), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity")
                },
                ToolItem(R.string.improve_speed_connection, Icons.Rounded.Speed, Color(0xFFE0F2F1), Color(0xFF004D40), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings")
                },
                ToolItem(R.string.performance_mode, Icons.Rounded.Memory, Color(0xFFEFEBE9), Color(0xFF3E2723), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.qualcomm.qti.performancemode", "com.qualcomm.qti.performancemode.PerformanceModeActivity")
                },
                ToolItem(R.string.qcolor, Icons.Rounded.ColorLens, Color(0xFFF1F8E9), Color(0xFF33691E), isXiaomiOnly = true) { ctx, _ ->
                    launchSettings(ctx, "com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity")
                }
            )
        )
    )
}

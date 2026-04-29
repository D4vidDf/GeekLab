package com.daviddf.geeklab.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface GeekLabKey : NavKey {
    @Serializable data object Home : GeekLabKey
    @Serializable data object News : GeekLabKey
    @Serializable data object Battery : GeekLabKey
    @Serializable data object Notifications : GeekLabKey
    @Serializable data object Info : GeekLabKey
    @Serializable data object Apps : GeekLabKey
    @Serializable data object WidgetInspector : GeekLabKey
    @Serializable data class WidgetDetail(val packageName: String, val className: String) : GeekLabKey
    @Serializable data object Tools : GeekLabKey
    @Serializable data class AppDetail(val packageName: String) : GeekLabKey
    @Serializable data class ManifestViewer(val packageName: String) : GeekLabKey
    @Serializable data object CustomNotification : GeekLabKey
    @Serializable data object LiveUpdate : GeekLabKey
    @Serializable data object MetricStyle : GeekLabKey
    @Serializable data object NotificationHistory : GeekLabKey
    @Serializable data class NotificationDetail(val notificationId: Long) : GeekLabKey
    @Serializable data object CallNotification : GeekLabKey
    @Serializable data object Bluetooth : GeekLabKey
    @Serializable data object BluetoothBle : GeekLabKey
    @Serializable data object NfcScanner : GeekLabKey
    @Serializable data object Wifi : GeekLabKey
    @Serializable data object WifiScanner : GeekLabKey
    @Serializable data object Camera : GeekLabKey
    @Serializable data object CameraX : GeekLabKey
    @Serializable data object UltraHdr : GeekLabKey
    @Serializable data object WebAnalyzer : GeekLabKey
    @Serializable data object Settings : GeekLabKey
    @Serializable data object Licenses : GeekLabKey
}
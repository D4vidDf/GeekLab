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
    @Serializable data object Tools : GeekLabKey
    @Serializable data class AppDetail(val packageName: String) : GeekLabKey
    @Serializable data class ManifestViewer(val packageName: String) : GeekLabKey
    @Serializable data object CustomNotification : GeekLabKey
}
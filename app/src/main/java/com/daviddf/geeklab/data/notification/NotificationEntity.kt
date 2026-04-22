package com.daviddf.geeklab.data.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val subText: String? = null,
    val timestamp: Long,
    val channelId: String?,
    val isClearable: Boolean,
    val key: String,
    val category: String? = null,
    val priority: Int = 0,
    val visibility: Int = 0,
    val color: Int = 0,
    val extrasJson: String? = null,
    val actionsJson: String? = null,
    val mediaPathsJson: String? = null
)

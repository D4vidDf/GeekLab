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
    val timestamp: Long,
    val channelId: String?,
    val isClearable: Boolean,
    val key: String
)

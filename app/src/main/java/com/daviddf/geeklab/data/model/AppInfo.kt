package com.daviddf.geeklab.data.model

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val applicationInfo: ApplicationInfo
)

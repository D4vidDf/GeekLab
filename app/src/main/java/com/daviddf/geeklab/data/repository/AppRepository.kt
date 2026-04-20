package com.daviddf.geeklab.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.daviddf.geeklab.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    name = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.loadIcon(packageManager),
                    applicationInfo = appInfo
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}

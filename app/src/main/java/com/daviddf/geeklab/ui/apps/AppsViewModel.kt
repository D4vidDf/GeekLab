package com.daviddf.geeklab.ui.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager = application.packageManager

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val installedApps: StateFlow<List<ApplicationInfo>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        try {
            val apps = packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
                .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
            _installedApps.value = apps
        } catch (e: Exception) {
            _installedApps.value = emptyList()
        }
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }
}

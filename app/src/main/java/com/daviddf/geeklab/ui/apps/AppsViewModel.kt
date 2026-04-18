package com.daviddf.geeklab.ui.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager = application.packageManager

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val installedApps: StateFlow<List<ApplicationInfo>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apps = withContext(Dispatchers.Default) {
                    packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
                        .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
                }
                _installedApps.value = apps
            } catch (_: Exception) {
                _installedApps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }
}

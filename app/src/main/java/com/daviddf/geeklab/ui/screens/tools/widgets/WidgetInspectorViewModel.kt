package com.daviddf.geeklab.ui.screens.tools.widgets

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppWithWidgets(
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val widgets: List<WidgetInfo>,
)

data class WidgetInfo(
    val label: String,
    val packageName: String,
    val className: String,
    val providerInfo: AppWidgetProviderInfo
)

data class WidgetInspectorUiState(
    val appsWithWidgets: List<AppWithWidgets> = emptyList(),
    val isLoading: Boolean = true
)

class WidgetInspectorViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WidgetInspectorUiState())
    val uiState: StateFlow<WidgetInspectorUiState> = _uiState.asStateFlow()

    private val packageManager = application.packageManager
    private val appWidgetManager = AppWidgetManager.getInstance(application)

    init {
        loadWidgets()
    }

    fun loadWidgets() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            val providers = appWidgetManager.installedProviders
            val groupedByPackage = providers.groupBy { it.provider.packageName }
            
            val appsList = groupedByPackage.asSequence().mapNotNull { (packageName, widgetProviders) ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appLabel = packageManager.getApplicationLabel(appInfo).toString()
                    val appIcon = packageManager.getApplicationIcon(appInfo)
                    
                    val widgets = widgetProviders.map { provider ->
                        WidgetInfo(
                            label = provider.loadLabel(packageManager),
                            packageName = provider.provider.packageName,
                            className = provider.provider.className,
                            providerInfo = provider
                        )
                    }
                    
                    AppWithWidgets(packageName, appLabel, appIcon, widgets)
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }.sortedBy { it.appLabel }.toList()
            
            _uiState.update { it.copy(appsWithWidgets = appsList, isLoading = false) }
        }
    }

    fun getWidgetDetail(packageName: String, className: String): WidgetInfo? {
        val providers = appWidgetManager.installedProviders
        return providers.find { (it.provider.packageName == packageName) && (it.provider.className == className) }?.let {
            WidgetInfo(
                label = it.loadLabel(packageManager),
                packageName = it.provider.packageName,
                className = it.provider.className,
                providerInfo = it
            )
        }
    }
}

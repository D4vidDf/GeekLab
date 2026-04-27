package com.daviddf.geeklab.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.daviddf.geeklab.ui.screens.tools.ToolItem
import com.daviddf.geeklab.ui.screens.tools.ToolsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("home_prefs", Context.MODE_PRIVATE)
    
    private val _favoriteTools = MutableStateFlow<List<ToolItem>>(emptyList())
    val favoriteTools = _favoriteTools.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val favoriteIds = prefs.getStringSet("favorite_tool_ids", setOf("standard_notification", "battery_info", "device_info", "apps_viewer")) ?: setOf("standard_notification", "battery_info", "device_info", "apps_viewer")
        
        val allTools = ToolsData.categories.flatMap { it.items }
        val favorites = favoriteIds.mapNotNull { id ->
            allTools.find { it.id == id }
        }
        
        _favoriteTools.value = favorites
    }

    fun toggleFavorite(toolId: String) {
        val currentIds = prefs.getStringSet("favorite_tool_ids", setOf("standard_notification", "battery_info", "device_info", "apps_viewer"))?.toMutableSet() ?: mutableSetOf()
        
        if (currentIds.contains(toolId)) {
            currentIds.remove(toolId)
        } else {
            currentIds.add(toolId)
        }
        
        prefs.edit {
            putStringSet("favorite_tool_ids", currentIds)
        }
        loadFavorites()
    }
}

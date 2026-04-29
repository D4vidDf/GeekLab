package com.daviddf.geeklab.ui.screens.tools

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    onLiveUpdateClick: () -> Unit = {},
    onMetricStyleClick: () -> Unit = {},
    onBatteryClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onWidgetInspectorClick: () -> Unit = {},
    onNotificationHistoryClick: () -> Unit = {},
    onCallNotificationClick: () -> Unit = {},
    onBluetoothClick: () -> Unit = {},
    onBluetoothBleClick: () -> Unit = {},
    onNfcScannerClick: () -> Unit = {},
    onWifiClick: () -> Unit = {},
    onWifiScannerClick: () -> Unit = {},
    onCameraXClick: () -> Unit = {},
    onUltraHdrClick: () -> Unit = {},
    onWebAnalyzerClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val favoriteTools by homeViewModel.favoriteTools.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }
    
    val actions = remember(onNotificationClick, onLiveUpdateClick, onMetricStyleClick, onBatteryClick, onInfoClick, onAppsClick, onWidgetInspectorClick, onNotificationHistoryClick, onCallNotificationClick, onBluetoothClick, onBluetoothBleClick, onNfcScannerClick, onWifiClick, onWifiScannerClick, onCameraXClick, onUltraHdrClick, onWebAnalyzerClick) {
        object : ToolsActions {
            override fun onNotificationClick() = onNotificationClick()
            override fun onLiveUpdateClick() = onLiveUpdateClick()
            override fun onMetricStyleClick() = onMetricStyleClick()
            override fun onBatteryClick() = onBatteryClick()
            override fun onInfoClick() = onInfoClick()
            override fun onAppsClick() = onAppsClick()
            override fun onWidgetInspectorClick() = onWidgetInspectorClick()
            override fun onNotificationHistoryClick() = onNotificationHistoryClick()
            override fun onCallNotificationClick() = onCallNotificationClick()
            override fun onBluetoothClick() = onBluetoothClick()
            override fun onBluetoothBleClick() = onBluetoothBleClick()
            override fun onNfcScannerClick() = onNfcScannerClick()
            override fun onWifiClick() = onWifiClick()
            override fun onWifiScannerClick() = onWifiScannerClick()
            override fun onCameraXClick() = onCameraXClick()
            override fun onUltraHdrClick() = onUltraHdrClick()
            override fun onWebAnalyzerClick() = onWebAnalyzerClick()
        }
    }

    val isXiaomiDevice = remember {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")
    }

    val columns = when {
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> GridCells.Adaptive(minSize = 130.dp)
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> GridCells.Adaptive(minSize = 110.dp)
        else -> GridCells.Fixed(3)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tools_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                            contentDescription = "Editar Favoritos",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditMode) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Toca para añadir o quitar de favoritos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            ToolsData.categories.forEach { category ->
                val visibleItems = category.items.filter { item ->
                    val xiaomiMatch = isXiaomiDevice || !item.isXiaomiOnly
                    val apiMatch = android.os.Build.VERSION.SDK_INT >= item.minApi
                    xiaomiMatch && apiMatch
                }

                if (visibleItems.isNotEmpty()) {
                    categorySection(
                        titleResId = category.titleResId,
                        items = visibleItems,
                        favoriteIds = favoriteTools.map { it.id }.toSet(),
                        isEditMode = isEditMode,
                        onToggleFavorite = { id -> homeViewModel.toggleFavorite(id) },
                        context = context,
                        actions = actions
                    )
                }
            }
            
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun LazyGridScope.categorySection(
    titleResId: Int,
    items: List<ToolItem>,
    favoriteIds: Set<String>,
    isEditMode: Boolean,
    onToggleFavorite: (String) -> Unit,
    context: Context,
    actions: ToolsActions
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
    }
    items(items) { item ->
        FavoriteCard(
            title = stringResource(item.titleResId),
            icon = item.icon,
            containerColor = item.containerColor,
            contentColor = item.contentColor,
            isFavorite = favoriteIds.contains(item.id),
            onClick = {
                if (isEditMode) {
                    onToggleFavorite(item.id)
                } else {
                    item.action(context, actions)
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@PreviewScreenSizes
@Composable
fun ToolsScreenPreview() {
    GeekLabTheme {
        ToolsScreen(onBackClick = {})
    }
}

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.theme.GeekLabTheme

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
    onNotificationHistoryClick: () -> Unit = {},
    onCallNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    
    val actions = remember(onNotificationClick, onLiveUpdateClick, onMetricStyleClick, onBatteryClick, onInfoClick, onAppsClick, onCallNotificationClick) {
        object : ToolsActions {
            override fun onNotificationClick() = onNotificationClick()
            override fun onLiveUpdateClick() = onLiveUpdateClick()
            override fun onMetricStyleClick() = onMetricStyleClick()
            override fun onBatteryClick() = onBatteryClick()
            override fun onInfoClick() = onInfoClick()
            override fun onAppsClick() = onAppsClick()
            override fun onNotificationHistoryClick() = onNotificationHistoryClick()
            override fun onCallNotificationClick() = onCallNotificationClick()
        }
    }

    val isXiaomiDevice = remember {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")
    }

    // Smaller minSize for smaller cards as requested
    val columns = when {
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> GridCells.Adaptive(minSize = 130.dp)
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> GridCells.Adaptive(minSize = 110.dp)
        else -> GridCells.Fixed(3) // 3 columns on compact for smaller cards
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
            onClick = { item.action(context, actions) }
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

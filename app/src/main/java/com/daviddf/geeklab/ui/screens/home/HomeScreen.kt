package com.daviddf.geeklab.ui.screens.home

import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.components.FeaturedCard
import com.daviddf.geeklab.ui.components.NewsItemCard
import com.daviddf.geeklab.ui.components.NewsItemShimmer
import com.daviddf.geeklab.ui.components.SectionHeader
import com.daviddf.geeklab.ui.screens.tools.ToolItem
import com.daviddf.geeklab.ui.screens.tools.ToolsActions
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.viewmodels.HomeViewModel
import com.daviddf.geeklab.ui.viewmodels.NewsViewModel

@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAppsClick: () -> Unit,
    onToolsClick: () -> Unit,
    onSeeMoreNewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLiveUpdateClick: () -> Unit = {},
    onMetricStyleClick: () -> Unit = {},
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
    newsViewModel: NewsViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val news by newsViewModel.news.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsState()
    val favoriteTools by homeViewModel.favoriteTools.collectAsState()

    val actions = remember {
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
        }
    }

    HomeScreenContent(
        news = news,
        isLoading = isLoading,
        error = error,
        favoriteTools = favoriteTools,
        onToolsClick = onToolsClick,
        onSeeMoreNewsClick = onSeeMoreNewsClick,
        onSettingsClick = onSettingsClick,
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    news: List<Experiments>,
    isLoading: Boolean,
    error: String?,
    favoriteTools: List<ToolItem>,
    onToolsClick: () -> Unit,
    onSeeMoreNewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    actions: ToolsActions
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val windowSizeClass = adaptiveInfo.windowSizeClass
    val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) && !isExpanded
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onBackground
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isExpanded) {
                // Tablet Layout: Side-by-Side
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Left Column: Tools
                        Column(
                            modifier = Modifier.weight(1.5f)
                        ) {
                            SectionHeader(
                                title = stringResource(R.string.tools_title),
                                onSeeAllClick = onToolsClick
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            if (favoriteTools.isEmpty()) {
                                EmptyFavoritesState(onToolsClick = onToolsClick)
                            } else {
                                favoriteTools.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            FavoriteCard(
                                                modifier = Modifier.weight(1f),
                                                title = stringResource(item.titleResId),
                                                icon = item.icon,
                                                containerColor = item.containerColor,
                                                contentColor = item.contentColor,
                                                onClick = { item.action(context, actions) }
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // Right Column: Featured News
                        Column(
                            modifier = Modifier.weight(2f)
                        ) {
                            SectionHeader(
                                title = stringResource(R.string.featured),
                                onSeeAllClick = onSeeMoreNewsClick
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            if (isLoading && news.isEmpty()) {
                                repeat(2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        NewsItemShimmer(Modifier.weight(1f))
                                        NewsItemShimmer(Modifier.weight(1f))
                                    }
                                }
                            } else {
                                news.take(4).chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            FeaturedCard(
                                                modifier = Modifier.weight(1f),
                                                title = item.titulo ?: "",
                                                tag = item.tag,
                                                imageUrl = item.imagen,
                                                onClick = {
                                                    item.url?.let { url ->
                                                        val intent = CustomTabsIntent.Builder().build()
                                                        intent.launchUrl(context, url.toUri())
                                                    }
                                                }
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Compact (Phone) or Medium (Fold)
                val totalCols = if (isMedium) 4 else 2
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(totalCols),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Tools Section
                    item(span = { GridItemSpan(totalCols) }) {
                        SectionHeader(
                            title = stringResource(R.string.tools_title),
                            onSeeAllClick = onToolsClick
                        )
                    }

                    if (favoriteTools.isEmpty()) {
                        item(span = { GridItemSpan(totalCols) }) {
                            EmptyFavoritesState(onToolsClick = onToolsClick)
                        }
                    } else {
                        items(favoriteTools) { item ->
                            FavoriteCard(
                                title = stringResource(item.titleResId),
                                icon = item.icon,
                                containerColor = item.containerColor,
                                contentColor = item.contentColor,
                                onClick = { item.action(context, actions) }
                            )
                        }
                    }

                    // News Section
                    item(span = { GridItemSpan(totalCols) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = stringResource(R.string.featured),
                            onSeeAllClick = onSeeMoreNewsClick
                        )
                    }

                    if (isLoading && news.isEmpty()) {
                        items(if (isMedium) 2 else 1, span = { GridItemSpan(if (isMedium) 2 else totalCols) }) {
                            NewsItemShimmer()
                        }
                    } else if (error != null && news.isEmpty()) {
                        item(span = { GridItemSpan(totalCols) }) {
                            ErrorState(message = stringResource(R.string.no_internet_connection), icon = Icons.Rounded.ErrorOutline)
                        }
                    } else if (news.isEmpty()) {
                        item(span = { GridItemSpan(totalCols) }) {
                            ErrorState(message = stringResource(R.string.news_not_found), icon = Icons.Rounded.SearchOff)
                        }
                    } else {
                        val newsItems = if (isMedium) news.take(4) else news.take(2)
                        val newsSpan = if (isMedium) 2 else totalCols
                        
                        items(newsItems, span = { GridItemSpan(newsSpan) }) { item ->
                            if (isMedium) {
                                FeaturedCard(
                                    title = item.titulo ?: "",
                                    tag = item.tag,
                                    imageUrl = item.imagen,
                                    onClick = {
                                        item.url?.let { url ->
                                            val intent = CustomTabsIntent.Builder().build()
                                            intent.launchUrl(context, url.toUri())
                                        }
                                    }
                                )
                            } else {
                                NewsItemCard(
                                    item = item,
                                    onClick = {
                                        item.url?.let { url ->
                                            val intent = CustomTabsIntent.Builder().build()
                                            intent.launchUrl(context, url.toUri())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(onToolsClick: () -> Unit) {
    Surface(
        onClick = onToolsClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Personaliza tu acceso rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Añade tus herramientas favoritas desde el menú de herramientas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    GeekLabTheme {
        HomeScreenContent(
            news = listOf(
                Experiments(titulo = "Noticia Destacada 1", tag = "TECNOLOGÍA"),
                Experiments(titulo = "Noticia Destacada 2", tag = "DISEÑO")
            ),
            isLoading = false,
            error = null,
            favoriteTools = emptyList(),
            onToolsClick = {},
            onSeeMoreNewsClick = {},
            onSettingsClick = {},
            actions = object : ToolsActions {
                override fun onNotificationClick() {}
                override fun onLiveUpdateClick() {}
                override fun onMetricStyleClick() {}
                override fun onBatteryClick() {}
                override fun onInfoClick() {}
                override fun onAppsClick() {}
                override fun onWidgetInspectorClick() {}
                override fun onNotificationHistoryClick() {}
                override fun onCallNotificationClick() {}
                override fun onBluetoothClick() {}
                override fun onBluetoothBleClick() {}
                override fun onNfcScannerClick() {}
                override fun onWifiClick() {}
                override fun onWifiScannerClick() {}
                override fun onCameraXClick() {}
                override fun onUltraHdrClick() {}
            }
        )
    }
}

package com.daviddf.geeklab.ui.screens.home

import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.components.NewsItemShimmer
import com.daviddf.geeklab.ui.components.SectionHeader
import com.daviddf.geeklab.ui.components.shimmerEffect
import com.daviddf.geeklab.ui.screens.tools.ToolItem
import com.daviddf.geeklab.ui.screens.tools.ToolsActions
import com.daviddf.geeklab.ui.screens.tools.ToolsData
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
    onWebAnalyzerClick: () -> Unit = {},
    newsViewModel: NewsViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val news by newsViewModel.news.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsState()
    val favoriteTools by homeViewModel.favoriteTools.collectAsState()

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
    
    val infiniteTransition = rememberInfiniteTransition(label = "tool_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Settings Button in scrollable area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Hero Title Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.geek_lab_hero_title),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            lineHeight = 64.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.software_validation_suite),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Big Action Button (Launch Tools)
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onToolsClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.RocketLaunch,
                            contentDescription = stringResource(R.string.tools_title),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Expressive Featured Section
            val allTools = ToolsData.categories.flatMap { it.items }
            val defaultHeroIds = listOf("standard_notification", "battery_info")
            val defaultRowIds = listOf("device_info", "apps_viewer")
            
            val heroTools = remember(favoriteTools) {
                if (favoriteTools.isEmpty()) {
                    defaultHeroIds.mapNotNull { id -> allTools.find { it.id == id } }
                } else if (favoriteTools.size == 1) {
                    val first = favoriteTools[0]
                    val secondId = defaultHeroIds.find { it != first.id } ?: defaultHeroIds[0]
                    listOfNotNull(first, allTools.find { it.id == secondId })
                } else {
                    favoriteTools.take(2)
                }
            }

            val displayRowTools = remember(favoriteTools) {
                if (favoriteTools.isEmpty()) {
                    defaultRowIds.mapNotNull { id -> allTools.find { it.id == id } }
                } else {
                    favoriteTools.drop(2)
                }
            }

            val firstTool = heroTools.getOrNull(0)
            val secondTool = heroTools.getOrNull(1)
            val lastNews = news.firstOrNull()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 24.dp)
            ) {
                // Middle: Last Featured New
                lastNews?.let { item ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.75f),
                        shape = MaterialShapes.Square.toShape(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { 
                            item.url?.let { url ->
                                val intent = CustomTabsIntent.Builder().build()
                                intent.launchUrl(context, url.toUri())
                            }
                        }
                    ) {
                        Box {
                            AsyncImage(
                                model = item.imagen,
                                contentDescription = item.titulo,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                // Top-Start Corner: First Tool
                firstTool?.let { tool ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(100.dp)
                            .graphicsLayer { rotationZ = rotation },
                        shape = MaterialShapes.Cookie6Sided.toShape(),
                        color = tool.containerColor,
                        onClick = { tool.action(context, actions) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.graphicsLayer { rotationZ = -rotation }
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = stringResource(tool.titleResId),
                                modifier = Modifier.size(32.dp),
                                tint = tool.contentColor
                            )
                        }
                    }
                }

                // Bottom-End Corner: Second Tool
                secondTool?.let { tool ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(120.dp),
                        shape = MaterialShapes.Cookie4Sided.toShape(),
                        color = tool.containerColor,
                        onClick = { tool.action(context, actions) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = stringResource(tool.titleResId),
                                modifier = Modifier.size(40.dp),
                                tint = tool.contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Tools Section (Horizontal Scroll)
            SectionHeader(
                title = stringResource(R.string.tools_title),
                onSeeAllClick = onToolsClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (displayRowTools.isEmpty() && favoriteTools.isNotEmpty()) {
                // If the user has 1-2 favorites, they are in the hero. 
                // Show a card to add more.
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    EmptyFavoritesState(onToolsClick = onToolsClick)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayRowTools) { item ->
                        FavoriteCard(
                            modifier = Modifier.width(140.dp),
                            title = stringResource(item.titleResId),
                            icon = item.icon,
                            containerColor = item.containerColor,
                            contentColor = item.contentColor,
                            onClick = { item.action(context, actions) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))

            // Featured News Section (Carousel)
            SectionHeader(
                title = stringResource(R.string.featured),
                onSeeAllClick = onSeeMoreNewsClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading && news.isEmpty()) {
                repeat(2) {
                    NewsItemShimmer(modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (error != null && news.isEmpty()) {
                ErrorState(
                    message = stringResource(R.string.no_internet_connection),
                    icon = Icons.Rounded.ErrorOutline,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else if (news.isNotEmpty()) {
                HorizontalMultiBrowseCarousel(
                    state = rememberCarouselState { news.size },
                    preferredItemWidth = 320.dp,
                    itemSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .height(160.dp) // Small height for Home
                ) { index ->
                    val item = news[index]
                    SubcomposeAsyncImage(
                        model = item.imagen,
                        contentDescription = item.titulo,
                        modifier = Modifier
                            .fillMaxSize()
                            .maskClip(MaterialTheme.shapes.extraLarge)
                            .clickable {
                                item.url?.let { url ->
                                    val intent = CustomTabsIntent.Builder().build()
                                    intent.launchUrl(context, url.toUri())
                                }
                            },
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmerEffect()
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                            )
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .maskClip(MaterialTheme.shapes.extraLarge),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun EmptyFavoritesState(onToolsClick: () -> Unit) {
    Surface(
        onClick = onToolsClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.personalize_tools),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.add_quick_access_here),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewScreenSizes
@Composable
fun HomeScreenPreview() {
    GeekLabTheme {
        HomeScreenContent(
            news = listOf(
                Experiments(titulo = "Noticia Destacada 1", tag = "TECNOLOGÍA"),
                Experiments(titulo = "Noticia Destacada 2", tag = "DISEÑO"),
                Experiments(titulo = "Noticia Destacada 3", tag = "ANDROID")
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
                override fun onWebAnalyzerClick() {}
            }
        )
    }
}

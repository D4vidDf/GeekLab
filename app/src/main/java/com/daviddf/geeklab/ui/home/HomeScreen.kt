package com.daviddf.geeklab.ui.home

import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.components.FeaturedCard
import com.daviddf.geeklab.ui.components.NewsItemCard
import com.daviddf.geeklab.ui.components.NewsItemShimmer
import com.daviddf.geeklab.ui.components.SectionHeader
import com.daviddf.geeklab.ui.feed.NewsViewModel
import com.daviddf.geeklab.ui.theme.CardAplicaciones
import com.daviddf.geeklab.ui.theme.CardBateria
import com.daviddf.geeklab.ui.theme.CardInformacion
import com.daviddf.geeklab.ui.theme.CardNotificaciones
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.theme.TextAplicaciones
import com.daviddf.geeklab.ui.theme.TextBateria
import com.daviddf.geeklab.ui.theme.TextInformacion
import com.daviddf.geeklab.ui.theme.TextNotificaciones

@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAppsClick: () -> Unit,
    onToolsClick: () -> Unit,
    onSeeMoreNewsClick: () -> Unit,
    newsViewModel: NewsViewModel = viewModel()
) {
    val news by newsViewModel.news.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsState()

    HomeScreen(
        news = news,
        isLoading = isLoading,
        error = error,
        onNotificationClick = onNotificationClick,
        onBatteryClick = onBatteryClick,
        onInfoClick = onInfoClick,
        onAppsClick = onAppsClick,
        onToolsClick = onToolsClick,
        onSeeMoreNewsClick = onSeeMoreNewsClick,
        onRefresh = { newsViewModel.refreshNews() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    news: List<Experiments>,
    isLoading: Boolean,
    error: String?,
    onNotificationClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAppsClick: () -> Unit,
    onToolsClick: () -> Unit,
    onSeeMoreNewsClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val widthClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass
    val isCompact = widthClass == WindowWidthSizeClass.COMPACT
    val isMedium = widthClass == WindowWidthSizeClass.MEDIUM
    val isExpanded = widthClass == WindowWidthSizeClass.EXPANDED

    val favorites = listOf(
        FavoriteItem(stringResource(R.string.notificaciones), Icons.Rounded.Notifications, CardNotificaciones, TextNotificaciones, onNotificationClick),
        FavoriteItem(stringResource(R.string.battery_title), Icons.Rounded.BatteryFull, CardBateria, TextBateria, onBatteryClick),
        FavoriteItem(stringResource(R.string.info_title), Icons.Rounded.Info, CardInformacion, TextInformacion, onInfoClick),
        FavoriteItem(stringResource(R.string.apps_title), Icons.Rounded.GridView, CardAplicaciones, TextAplicaciones, onAppsClick)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /* Profile Click */ },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.preset),
                            contentDescription = stringResource(R.string.news),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
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
        PullToRefreshBox(
            isRefreshing = isLoading && news.isNotEmpty(),
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isExpanded) {
                // Tablet Layout: Side-by-Side
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Left Column: Favorites (Vertical 1x4 to fill height)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.favorites),
                            onSeeAllClick = onToolsClick
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favorites) { item ->
                                FavoriteCard(
                                    title = item.title,
                                    icon = item.icon,
                                    containerColor = item.containerColor,
                                    contentColor = item.contentColor,
                                    onClick = item.onClick
                                )
                            }
                        }
                    }

                    // Right Column: Featured News (Grid 2x2)
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.featured),
                            onSeeAllClick = onSeeMoreNewsClick
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isLoading && news.isEmpty()) {
                                items(4) { NewsItemShimmer() }
                            } else {
                                items(news.take(4)) { item ->
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
                                }
                            }
                        }
                    }
                }
            } else {
                // Compact (Phone) or Medium (Fold)
                // Use a single grid with dynamic spans to eliminate empty side space
                val totalCols = if (isMedium) 4 else 2
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(totalCols),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Favorites Section
                    item(span = { GridItemSpan(totalCols) }) {
                        SectionHeader(
                            title = stringResource(R.string.favorites),
                            onSeeAllClick = onToolsClick
                        )
                    }

                    // Favorites: 1 row if Medium (4 cols), 2 rows if Compact (2 cols)
                    items(favorites) { item ->
                        FavoriteCard(
                            title = item.title,
                            icon = item.icon,
                            containerColor = item.containerColor,
                            contentColor = item.contentColor,
                            onClick = item.onClick
                        )
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

private data class FavoriteItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

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
        HomeScreen(
            news = listOf(
                Experiments(titulo = "Noticia Destacada 1", tag = "TECNOLOGÍA"),
                Experiments(titulo = "Noticia Destacada 2", tag = "DISEÑO")
            ),
            isLoading = false,
            error = null,
            onNotificationClick = {},
            onBatteryClick = {},
            onInfoClick = {},
            onAppsClick = {},
            onToolsClick = {},
            onSeeMoreNewsClick = {},
            onRefresh = {}
        )
    }
}

@Preview(name = "Tablet Light", showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Tablet Dark", showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenExpandedPreview() {
    GeekLabTheme {
        HomeScreen(
            news = listOf(
                Experiments(titulo = "Noticia Destacada 1", tag = "TECNOLOGÍA"),
                Experiments(titulo = "Noticia Destacada 2", tag = "DISEÑO"),
                Experiments(titulo = "Noticia Destacada 3", tag = "ANDROID"),
                Experiments(titulo = "Noticia Destacada 4", tag = "DEV")
            ),
            isLoading = false,
            error = null,
            onNotificationClick = {},
            onBatteryClick = {},
            onInfoClick = {},
            onAppsClick = {},
            onToolsClick = {},
            onSeeMoreNewsClick = {},
            onRefresh = {}
        )
    }
}

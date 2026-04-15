package com.daviddf.geeklab.ui.home

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import coil.compose.SubcomposeAsyncImage
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.components.NewsItemCard
import com.daviddf.geeklab.ui.components.NewsItemShimmer
import com.daviddf.geeklab.ui.components.shimmerEffect
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
    onSeeMoreNewsClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current

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
                            contentDescription = stringResource(R.string.news), // Using news as fallback for now or we should add profile_pic
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.favorites),
                            onSeeAllClick = {})

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            FavoriteCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.notificaciones),
                                icon = Icons.Rounded.Notifications,
                                containerColor = CardNotificaciones,
                                contentColor = TextNotificaciones,
                                onClick = onNotificationClick
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            FavoriteCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.battery_title),
                                icon = Icons.Rounded.BatteryFull,
                                containerColor = CardBateria,
                                contentColor = TextBateria,
                                onClick = onBatteryClick
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            FavoriteCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.info_title),
                                icon = Icons.Rounded.Info,
                                containerColor = CardInformacion,
                                contentColor = TextInformacion,
                                onClick = onInfoClick
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            FavoriteCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.apps_title),
                                icon = Icons.Rounded.GridView,
                                containerColor = CardAplicaciones,
                                contentColor = TextAplicaciones,
                                onClick = onAppsClick
                            )
                        }
                    }
                }

                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.featured),
                            onSeeAllClick = onSeeMoreNewsClick
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val featuredNews = news.take(2)
                        if (isLoading && news.isEmpty()) {
                            repeat(2) {
                                NewsItemShimmer()
                            }
                        } else if (error != null && news.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.no_internet_connection),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (news.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.news_not_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            featuredNews.forEach { item ->
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

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.see_all),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun FeaturedCard(
    title: String,
    tag: String? = null,
    imageRes: Int? = null,
    imageUrl: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = stringResource(R.string.imagen_portada),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmerEffect()
                            )
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                } else if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = stringResource(R.string.imagen_portada),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = tag ?: stringResource(R.string.app_name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 4.dp),
            maxLines = 2
        )
    }
}

@Preview(showBackground = true)
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
            onSeeMoreNewsClick = {},
            onRefresh = {}
        )
    }
}
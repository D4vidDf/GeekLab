package com.daviddf.geeklab.ui.screens.news

import androidx.browser.customtabs.CustomTabsIntent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import coil.compose.SubcomposeAsyncImage
import com.daviddf.geeklab.Experiments
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.NewsItemCard
import com.daviddf.geeklab.ui.components.NewsItemShimmer
import com.daviddf.geeklab.ui.components.shimmerEffect
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.viewmodels.NewsViewModel

@Composable
fun NewsScreen(
    onBackClick: () -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    NewsScreenContent(
        news = news,
        isLoading = isLoading,
        error = error,
        onBackClick = onBackClick,
        onRefresh = { viewModel.refreshNews() },
        onLoadMore = { viewModel.loadMoreNews() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreenContent(
    news: List<Experiments>,
    isLoading: Boolean,
    error: String? = null,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val windowSizeClass = adaptiveInfo.windowSizeClass
    
    val columns = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
        else -> 1
    }

    val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val gridState = rememberLazyGridState()
    val pullState = rememberPullToRefreshState()
    val isRefreshing = isLoading && news.isNotEmpty()

    // Detect when we reach the end of the list to load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading) {
            onLoadMore()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.news),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        ) {
            if (news.isEmpty()) {
                if (isLoading) {
                    // Shimmer Skeletons
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item(span = { GridItemSpan(columns) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .shimmerEffect()
                            )
                        }
                        items(6) {
                            NewsItemShimmer()
                        }
                    }
                } else if (error != null) {
                    // Connection Error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_internet_connection),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.news_not_found),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val carouselItems = news.take(4)
                val listItems = news.drop(4)

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (carouselItems.isNotEmpty()) {
                        item(span = { GridItemSpan(columns) }) {
                            HorizontalMultiBrowseCarousel(
                                state = rememberCarouselState { carouselItems.size },
                                preferredItemWidth = if (isCompact) 340.dp else 480.dp,
                                itemSpacing = 16.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isCompact) 220.dp else 320.dp)
                                    .padding(vertical = 8.dp)
                            ) { index ->
                                val item = carouselItems[index]
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
                    }

                    items(listItems) { item ->
                        NewsItemCard(item = item) {
                            item.url?.let { url ->
                                val intent = CustomTabsIntent.Builder().build()
                                intent.launchUrl(context, url.toUri())
                            }
                        }
                    }

                    if (isLoading) {
                        item(span = { GridItemSpan(columns) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet", showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun NewsScreenPreview() {
    GeekLabTheme {
        NewsScreenContent(
            news = listOf(
                Experiments(
                    titulo = "Pixel 9 Pro XL: El nuevo estandarte de Google",
                    tag = "Hardware",
                    imagen = "https://picsum.photos/id/1/800/600"
                ),
                Experiments(
                    titulo = "Android 15: Todas las novedades confirmadas",
                    tag = "Software",
                    imagen = "https://picsum.photos/id/2/800/600"
                ),
                Experiments(
                    titulo = "La IA llega a todos los rincones de Workspace",
                    tag = "IA",
                    imagen = "https://picsum.photos/id/3/800/600"
                ),
                Experiments(
                    titulo = "Nuevos avances en computación cuántica",
                    tag = "Ciencia",
                    imagen = "https://picsum.photos/id/4/800/600"
                ),
                Experiments(
                    titulo = "Resumen del Google I/O 2024",
                    tag = "Evento",
                    imagen = "https://picsum.photos/id/5/800/600"
                ),
                Experiments(
                    titulo = "Cómo optimizar tu app para pantallas grandes",
                    tag = "Desarrollo",
                    imagen = "https://picsum.photos/id/6/800/600"
                ),
                Experiments(
                    titulo = "Jetpack Compose: Mejores prácticas en 2024",
                    tag = "Desarrollo",
                    imagen = "https://picsum.photos/id/7/800/600"
                )
            ),
            isLoading = false,
            onBackClick = {},
            onRefresh = {},
            onLoadMore = {}
        )
    }
}

package com.daviddf.geeklab.ui.screens.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.daviddf.geeklab.R
import com.daviddf.geeklab.data.model.AppInfo
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.viewmodels.AppsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onBackClick: () -> Unit,
    onAppClick: (String) -> Unit,
    showNavigationIcon: Boolean = true,
    viewModel: AppsViewModel = viewModel()
) {
    val isGridView by viewModel.isGridView.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    AppsScreenContent(
        isGridView = isGridView,
        installedApps = installedApps,
        isLoading = isLoading,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        isSearching = isSearching,
        onSearchToggle = { isSearching = it },
        onBackClick = onBackClick,
        onAppClick = onAppClick,
        onToggleView = { viewModel.toggleViewMode() },
        showNavigationIcon = showNavigationIcon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreenContent(
    isGridView: Boolean,
    installedApps: List<AppInfo>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onToggleView: () -> Unit,
    showNavigationIcon: Boolean = true
) {
    val filteredApps = remember(searchQuery, installedApps) {
        val filtered = if (searchQuery.isEmpty()) installedApps
        else installedApps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
        filtered.sortedBy { it.name.lowercase() }
    }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    
    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    val columns = when {
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> GridCells.Adaptive(minSize = 130.dp)
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> GridCells.Adaptive(minSize = 110.dp)
        else -> GridCells.Fixed(3)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSearching) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text(stringResource(R.string.search_apps)) },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onSearchToggle(false); onSearchQueryChange("") }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.apps_title), fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        if (showNavigationIcon) {
                            FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { onSearchToggle(true) }) {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        }
                        IconButton(onClick = { onToggleView() }) {
                            Icon(
                                if (isGridView) Icons.AutoMirrored.Rounded.List else Icons.Rounded.GridView,
                                contentDescription = if (isGridView) stringResource(R.string.list_view) else stringResource(R.string.grid_view)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = columns,
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        AppGridItem(app, onAppClick)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        AppListItem(app, onAppClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AppGridItem(app: AppInfo, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(app.packageName) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon = remember(app.packageName) { 
                try { app.icon?.toBitmap()?.asImageBitmap() } catch (_: Exception) { null }
            }
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (icon != null) {
                        Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(app.packageName) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = remember(app.packageName) { 
                try { app.icon?.toBitmap()?.asImageBitmap() } catch (_: Exception) { null }
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (icon != null) {
                        Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppGridItemPreview() {
    GeekLabTheme {
        Box(modifier = Modifier.padding(16.dp).width(120.dp)) {
            AppGridItem(
                app = AppInfo(
                    packageName = "com.daviddf.geeklab",
                    name = "GeekLab",
                    icon = null,
                    applicationInfo = android.content.pm.ApplicationInfo()
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppListItemPreview() {
    GeekLabTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AppListItem(
                app = AppInfo(
                    packageName = "com.daviddf.geeklab",
                    name = "GeekLab",
                    icon = null,
                    applicationInfo = android.content.pm.ApplicationInfo()
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@PreviewScreenSizes
@Composable
fun AppsScreenPreview() {
    val mockApps = remember {
        listOf(
            "Android System",
            "Calculator",
            "Calendar",
            "Camera",
            "Chrome",
            "Clock",
            "Contacts",
            "Drive",
            "Files",
            "Gallery",
            "GeekLab",
            "Gmail",
            "Maps",
            "Messages",
            "Notes",
            "Phone",
            "Photos",
            "Play Store",
            "Settings",
            "YouTube"
        ).map { name ->
            AppInfo(
                packageName = "com.mock.${name.lowercase().replace(" ", ".")}",
                name = name,
                icon = null,
                applicationInfo = android.content.pm.ApplicationInfo()
            )
        }
    }

    GeekLabTheme {
        AppsScreenContent(
            isGridView = true,
            installedApps = mockApps,
            searchQuery = "",
            onSearchQueryChange = {},
            isSearching = false,
            onSearchToggle = {},
            onBackClick = {},
            onAppClick = {},
            onToggleView = {},
            isLoading = false
        )
    }
}

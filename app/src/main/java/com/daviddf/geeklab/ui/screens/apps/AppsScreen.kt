package com.daviddf.geeklab.ui.screens.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
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
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text(stringResource(R.string.search_apps)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(stringResource(R.string.apps_title), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (showNavigationIcon || isSearching) {
                        IconButton(onClick = if (isSearching) {
                            {
                                onSearchToggle(false); onSearchQueryChange("")
                            }
                        } else onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (isSearching) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    } else {
                        IconButton(onClick = { onSearchToggle(true) }) {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        }
                        IconButton(onClick = { onToggleView() }) {
                            Icon(
                                if (isGridView) Icons.AutoMirrored.Rounded.List else Icons.Rounded.GridView,
                                contentDescription = if (isGridView) stringResource(R.string.list_view) else stringResource(R.string.grid_view)
                            )
                        }
                    }
                }
            )
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
                    columns = GridCells.Adaptive(minSize = 100.dp),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(app.packageName) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = remember(app.packageName) { 
            try { app.icon?.toBitmap()?.asImageBitmap() } catch (_: Exception) { null }
        }
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(app.packageName) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember(app.packageName) { 
            try { app.icon?.toBitmap()?.asImageBitmap() } catch (_: Exception) { null }
        }
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@PreviewScreenSizes
@Composable
fun AppsScreenPreview() {
    GeekLabTheme {
        AppsScreenContent(
            isGridView = true,
            installedApps = emptyList(),
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

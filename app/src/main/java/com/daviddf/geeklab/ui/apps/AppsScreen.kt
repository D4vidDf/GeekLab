package com.daviddf.geeklab.ui.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
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
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onBackClick: () -> Unit,
    onAppClick: (String) -> Unit,
    viewModel: AppsViewModel = viewModel()
) {
    val isGridView by viewModel.isGridView.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    AppsScreenContent(
        isGridView = isGridView,
        installedApps = installedApps,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        isSearching = isSearching,
        onSearchToggle = { isSearching = it },
        onBackClick = onBackClick,
        onAppClick = onAppClick,
        onToggleView = { viewModel.toggleViewMode() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreenContent(
    isGridView: Boolean,
    installedApps: List<ApplicationInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onToggleView: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter {
            val label = try { it.loadLabel(packageManager).toString() } catch (e: Exception) { it.packageName }
            label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
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
                    IconButton(onClick = if (isSearching) { { onSearchToggle(false); onSearchQueryChange("") } } else onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
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
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredApps) { app ->
                    AppGridItem(app, packageManager, onAppClick)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    AppListItem(app, packageManager, onAppClick)
                }
            }
        }
    }
}

@Composable
fun AppGridItem(app: ApplicationInfo, pm: PackageManager, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(app.packageName) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = remember(app.packageName) { 
            try { app.loadIcon(pm).toBitmap().asImageBitmap() } catch (e: Exception) { null }
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
            text = try { app.loadLabel(pm).toString() } catch (e: Exception) { app.packageName },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppListItem(app: ApplicationInfo, pm: PackageManager, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(app.packageName) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember(app.packageName) { 
            try { app.loadIcon(pm).toBitmap().asImageBitmap() } catch (e: Exception) { null }
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
                text = try { app.loadLabel(pm).toString() } catch (e: Exception) { app.packageName },
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
            installedApps = listOf(
                createMockAppInfo("com.example.app1", "Alpha App"),
                createMockAppInfo("com.example.app2", "Beta Tool"),
                createMockAppInfo("com.example.app3", "Gamma Game")
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            isSearching = false,
            onSearchToggle = {},
            onBackClick = {},
            onAppClick = {},
            onToggleView = {}
        )
    }
}

private fun createMockAppInfo(packageName: String, label: String): ApplicationInfo {
    return ApplicationInfo().apply {
        this.packageName = packageName
        this.name = label
        this.flags = ApplicationInfo.FLAG_INSTALLED
    }
}

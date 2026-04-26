package com.daviddf.geeklab.ui.screens.tools.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetInspectorScreen(
    onBackClick: () -> Unit,
    onWidgetClick: (String, String) -> Unit,
    viewModel: WidgetInspectorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(uiState.appsWithWidgets, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.appsWithWidgets
        } else {
            uiState.appsWithWidgets.filter { app ->
                app.appLabel.contains(searchQuery, ignoreCase = true) ||
                app.widgets.any { it.label.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.widget_inspector_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        AppWidgetSection(app, onWidgetClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AppWidgetSection(
    app: AppWithWidgets,
    onWidgetClick: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            AsyncImage(
                model = app.appIcon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = app.appLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        app.widgets.forEach { widget ->
            WidgetCard(widget, onWidgetClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun WidgetCard(
    widget: WidgetInfo,
    onWidgetClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isInspectionMode = LocalInspectionMode.current
    val preview = remember(widget) {
        if (isInspectionMode) return@remember null
        try {
            widget.providerInfo.loadPreviewImage(context, density.density.toInt())
                ?: widget.providerInfo.loadIcon(context, density.density.toInt())
        } catch (_: Exception) {
            null
        }
    }

    Card(
        onClick = { onWidgetClick(widget.packageName, widget.className) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (preview != null) {
                        AsyncImage(
                            model = preview,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = widget.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = widget.className.substringAfterLast('.'),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun AppWidgetSectionPreview() {
    GeekLabTheme {
        AppWidgetSection(
            app = AppWithWidgets(
                packageName = "com.android.settings",
                appLabel = "Settings",
                appIcon = null,
                widgets = listOf(
                    WidgetInfo("Shortcut", "com.android.settings", "com.android.settings.SettingsShortcut", android.appwidget.AppWidgetProviderInfo()),
                    WidgetInfo("Data Usage", "com.android.settings", "com.android.settings.SettingsDataUsage", android.appwidget.AppWidgetProviderInfo())
                )
            ),
            onWidgetClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetCardPreview() {
    GeekLabTheme {
        WidgetCard(
            widget = WidgetInfo(
                label = "Battery Level",
                packageName = "com.daviddf.geeklab",
                className = "com.daviddf.geeklab.BatteryWidget",
                providerInfo = android.appwidget.AppWidgetProviderInfo()
            ),
            onWidgetClick = { _, _ -> }
        )
    }
}

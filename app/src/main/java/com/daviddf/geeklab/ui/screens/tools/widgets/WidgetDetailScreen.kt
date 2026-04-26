package com.daviddf.geeklab.ui.screens.tools.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetDetailScreen(
    packageName: String,
    className: String,
    onBackClick: () -> Unit,
    viewModel: WidgetInspectorViewModel = viewModel()
) {
    val widgetInfo = remember(packageName, className) {
        viewModel.getWidgetDetail(packageName, className)
    }
    
    WidgetDetailContent(
        widgetInfo = widgetInfo,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetDetailContent(
    widgetInfo: WidgetInfo?,
    onBackClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = with(density) { windowInfo.containerSize.width.toDp() }.value
    val screenHeightDp = with(density) { windowInfo.containerSize.height.toDp() }.value

    // AppWidgetHost management for live preview
    // Using applicationContext to avoid leaking Activity and to ensure host stability
    val appWidgetHost = remember { AppWidgetHost(context.applicationContext, 1024) }
    var appWidgetId by rememberSaveable { mutableIntStateOf(-1) }
    var isBound by rememberSaveable { mutableStateOf(false) }
    var hostView by remember { mutableStateOf<AppWidgetHostView?>(null) }

    // Resize state
    var isGridMode by rememberSaveable { mutableStateOf(true) }
    var gridWidth by rememberSaveable { 
        mutableIntStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (widgetInfo?.providerInfo?.targetCellWidth ?: 0) > 0) widgetInfo!!.providerInfo.targetCellWidth else 2) 
    }
    var gridHeight by rememberSaveable { 
        mutableIntStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (widgetInfo?.providerInfo?.targetCellHeight ?: 0) > 0) widgetInfo!!.providerInfo.targetCellHeight else 1) 
    }
    var widgetWidth by rememberSaveable { mutableFloatStateOf((gridWidth * 73 - 16).toFloat()) }
    var widgetHeight by rememberSaveable { mutableFloatStateOf((gridHeight * 73 - 16).toFloat()) }

    fun updateSizesFromGrid() {
        // Official Android 12+ formula: (Cells * 73) - 16
        // This is more accurate for modern devices (targetSdk 31+)
        widgetWidth = (gridWidth * 73 - 16).toFloat()
        widgetHeight = (gridHeight * 73 - 16).toFloat()
    }

    val bindLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && widgetInfo != null) {
            isBound = true
            hostView = appWidgetHost.createView(context, appWidgetId, widgetInfo.providerInfo)
            if (isGridMode) {
                updateSizesFromGrid()
            }
        }
    }

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && widgetInfo != null) {
            hostView = appWidgetHost.createView(context, appWidgetId, widgetInfo.providerInfo)
        }
    }

    LaunchedEffect(gridWidth, gridHeight, isGridMode) {
        if (isGridMode) {
            updateSizesFromGrid()
        }
    }

    LaunchedEffect(Unit) {
        appWidgetHost.startListening()
    }

    DisposableEffect(Unit) {
        onDispose {
            appWidgetHost.stopListening()
            // We don't delete the ID here to allow it to persist through permission flows
        }
    }

    LaunchedEffect(widgetInfo, isBound) {
        if (widgetInfo != null) {
            if (!isBound) {
                try {
                    if (appWidgetId == -1) {
                        appWidgetId = appWidgetHost.allocateAppWidgetId()
                    }
                    
                    val bound = AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(appWidgetId, widgetInfo.providerInfo.provider)
                    if (bound) {
                        isBound = true
                        hostView = appWidgetHost.createView(context, appWidgetId, widgetInfo.providerInfo)
                    } else {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetInfo.providerInfo.provider)
                        }
                        bindLauncher.launch(intent)
                    }
                } catch (_: Exception) {}
            } else if (hostView == null) {
                // Restore hostView after recreation if already bound
                hostView = appWidgetHost.createView(context, appWidgetId, widgetInfo.providerInfo)
            }
        }
    }

    // Update widget options when resized to trigger responsive layouts
    LaunchedEffect(widgetWidth, widgetHeight, isBound) {
        if (isBound && appWidgetId != -1) {
            val options = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widgetWidth.toInt())
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widgetHeight.toInt())
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widgetWidth.toInt())
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, widgetHeight.toInt())
            }
            AppWidgetManager.getInstance(context).updateAppWidgetOptions(appWidgetId, options)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.widget_detail_title), fontWeight = FontWeight.Bold) },
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
        if (widgetInfo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.widget_not_found))
            }
        } else {
            val provider = widgetInfo.providerInfo
            val isInspectionMode = LocalInspectionMode.current
            val previewDrawable = remember(provider) {
                if (isInspectionMode) return@remember null
                try {
                    provider.loadPreviewImage(context, config.densityDpi) 
                        ?: provider.loadIcon(context, config.densityDpi)
                } catch (_: Exception) {
                    null
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Preview Card
                WidgetPreviewCard(
                    hostView = hostView,
                    previewDrawable = previewDrawable,
                    widgetWidth = widgetWidth,
                    widgetHeight = widgetHeight
                )

                // Size Controls
                if (hostView != null) {
                    WidgetSizeControlsCard(
                        isGridMode = isGridMode,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                        widgetWidth = widgetWidth,
                        widgetHeight = widgetHeight,
                        onGridModeChange = { isGridMode = it },
                        onGridWidthChange = { gridWidth = it },
                        onGridHeightChange = { gridHeight = it },
                        onWidgetWidthChange = { widgetWidth = it },
                        onWidgetHeightChange = { widgetHeight = it },
                        screenWidthDp = screenWidthDp,
                        screenHeightDp = screenHeightDp,
                        widgetInfo = widgetInfo
                    )
                }

                // Configuration Activity
                if (provider.configure != null) {
                    Button(
                        onClick = {
                            if (appWidgetId != -1) {
                                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                                    component = provider.configure
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                }
                                configureLauncher.launch(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Rounded.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.widget_configure))
                    }
                }

                // Technical Info Card
                WidgetTechnicalInfoCard(widgetInfo = widgetInfo)
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(
    hostView: AppWidgetHostView?,
    previewDrawable: android.graphics.drawable.Drawable?,
    widgetWidth: Float,
    widgetHeight: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .horizontalScroll(rememberScrollState()), 
            contentAlignment = Alignment.Center
        ) {
            if (hostView != null) {
                AndroidView(
                    factory = { hostView },
                    modifier = Modifier
                        .width(widgetWidth.dp)
                        .height(widgetHeight.dp)
                )
            } else if (previewDrawable != null) {
                AsyncImage(
                    model = previewDrawable,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Widgets, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.widget_preview_not_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetSizeControlsCard(
    isGridMode: Boolean,
    gridWidth: Int,
    gridHeight: Int,
    widgetWidth: Float,
    widgetHeight: Float,
    onGridModeChange: (Boolean) -> Unit,
    onGridWidthChange: (Int) -> Unit,
    onGridHeightChange: (Int) -> Unit,
    onWidgetWidthChange: (Float) -> Unit,
    onWidgetHeightChange: (Float) -> Unit,
    screenWidthDp: Float,
    screenHeightDp: Float,
    widgetInfo: WidgetInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.widget_simulate_resizing), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = isGridMode,
                        onClick = { onGridModeChange(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(stringResource(R.string.widget_grid))
                    }
                    SegmentedButton(
                        selected = !isGridMode,
                        onClick = { onGridModeChange(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(stringResource(R.string.widget_dp))
                    }
                }
            }
            
            if (isGridMode) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                        ((widgetInfo.providerInfo.targetCellWidth > 0) || 
                         (widgetInfo.providerInfo.targetCellHeight > 0))) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.widget_target_size, 
                                    widgetInfo.providerInfo.targetCellWidth, 
                                    widgetInfo.providerInfo.targetCellHeight
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    val maxCols = (screenWidthDp.toInt() / 73).coerceAtLeast(4)
                    val maxRows = (screenHeightDp.toInt() / 100).coerceAtLeast(5) // Estimation

                    Column {
                        Text(
                            text = stringResource(R.string.widget_width_cells, gridWidth), 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = gridWidth.toFloat(),
                            onValueChange = { onGridWidthChange(it.toInt()) },
                            valueRange = 1f..maxCols.toFloat(),
                            steps = maxCols - 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.widget_height_cells, gridHeight), 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = gridHeight.toFloat(),
                            onValueChange = { onGridHeightChange(it.toInt()) },
                            valueRange = 1f..maxRows.toFloat(),
                            steps = maxRows - 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = stringResource(R.string.widget_width_dp, widgetWidth.toInt()), 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = widgetWidth,
                            onValueChange = { onWidgetWidthChange(it) },
                            valueRange = 40f..screenWidthDp.coerceAtLeast(41f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.widget_height_dp, widgetHeight.toInt()), 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = widgetHeight,
                            onValueChange = { onWidgetHeightChange(it) },
                            valueRange = 40f..screenHeightDp.coerceAtLeast(41f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetTechnicalInfoCard(widgetInfo: WidgetInfo) {
    val provider = widgetInfo.providerInfo
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.widget_info),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            DetailItem(Icons.Rounded.Widgets, stringResource(R.string.widget_label), widgetInfo.label)
            DetailItem(Icons.Rounded.Tag, stringResource(R.string.package_name), widgetInfo.packageName, multiline = true)
            DetailItem(Icons.Rounded.Class, stringResource(R.string.widget_class), widgetInfo.className, multiline = true)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DetailItem(Icons.Rounded.Straighten, stringResource(R.string.widget_min_width), "${provider.minWidth} dp")
            DetailItem(Icons.Rounded.Height, stringResource(R.string.widget_min_height), "${provider.minHeight} dp")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DetailItem(Icons.Rounded.Straighten, stringResource(R.string.widget_max_resize_width), "${provider.maxResizeWidth} dp")
                DetailItem(Icons.Rounded.Height, stringResource(R.string.widget_max_resize_height), "${provider.maxResizeHeight} dp")
                val targetW = provider.targetCellWidth
                val targetH = provider.targetCellHeight
                DetailItem(
                    Icons.Rounded.ViewColumn, 
                    stringResource(R.string.widget_target_width), 
                    if (targetW > 0) "$targetW cells (${targetW * 73 - 16} dp)" else stringResource(R.string.widget_none)
                )
                DetailItem(
                    Icons.Rounded.TableRows,
                    stringResource(R.string.widget_target_height), 
                    if (targetH > 0) "$targetH cells (${targetH * 73 - 16} dp)" else stringResource(R.string.widget_none)
                )
            }

            DetailItem(
                Icons.Rounded.Sync, 
                stringResource(R.string.widget_update_period), 
                if (provider.updatePeriodMillis == 0) stringResource(R.string.widget_none) else "${provider.updatePeriodMillis / 1000} s"
            )

            val resizeMode = buildString {
                val mode = provider.resizeMode
                val horizontal = (mode and AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0
                val vertical = (mode and AppWidgetProviderInfo.RESIZE_VERTICAL) != 0
                if (horizontal && vertical) append(stringResource(R.string.widget_both))
                else if (horizontal) append(stringResource(R.string.widget_horizontal))
                else if (vertical) append(stringResource(R.string.widget_vertical))
                else append(stringResource(R.string.widget_none))
            }
            DetailItem(Icons.Rounded.OpenInFull, stringResource(R.string.widget_resize_mode), resizeMode, multiline = true)

            if (provider.configure != null) {
                DetailItem(Icons.Rounded.Settings, stringResource(R.string.widget_configure), provider.configure.className, multiline = true)
            }
        }
    }
}

@Preview(showBackground = true, name = "Preview Card")
@Composable
fun WidgetPreviewCardPreview() {
    GeekLabTheme {
        WidgetPreviewCard(
            hostView = null,
            previewDrawable = null,
            widgetWidth = 200f,
            widgetHeight = 100f
        )
    }
}

@Preview(showBackground = true, name = "Size Controls")
@Composable
fun WidgetSizeControlsCardPreview() {
    GeekLabTheme {
        WidgetSizeControlsCard(
            isGridMode = true,
            gridWidth = 2,
            gridHeight = 1,
            widgetWidth = 110f,
            widgetHeight = 40f,
            onGridModeChange = {},
            onGridWidthChange = {},
            onGridHeightChange = {},
            onWidgetWidthChange = {},
            onWidgetHeightChange = {},
            screenWidthDp = 360f,
            screenHeightDp = 640f,
            widgetInfo = WidgetInfo("Test", "test.pkg", "test.Class", AppWidgetProviderInfo())
        )
    }
}

@Preview(showBackground = true, name = "Technical Info")
@Composable
fun WidgetTechnicalInfoCardPreview() {
    GeekLabTheme {
        WidgetTechnicalInfoCard(
            widgetInfo = WidgetInfo(
                label = "GeekLab Widget",
                packageName = "com.daviddf.geeklab",
                className = "com.daviddf.geeklab.MyWidget",
                providerInfo = AppWidgetProviderInfo().apply {
                    minWidth = 110
                    minHeight = 40
                    updatePeriodMillis = 86400000
                    resizeMode = AppWidgetProviderInfo.RESIZE_HORIZONTAL or AppWidgetProviderInfo.RESIZE_VERTICAL
                }
            )
        )
    }
}

@Preview(showBackground = true, name = "Configure Button")
@Composable
fun WidgetConfigButtonPreview() {
    GeekLabTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Rounded.Edit, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.widget_configure))
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun WidgetDetailScreenPreview() {
    GeekLabTheme {
        WidgetDetailContent(
            widgetInfo = WidgetInfo(
                label = "System Clock",
                packageName = "com.android.deskclock",
                className = "com.android.alarmclock.DigitalAppWidgetProvider",
                providerInfo = AppWidgetProviderInfo().apply {
                    minWidth = 180
                    minHeight = 110
                }
            ),
            onBackClick = {}
        )
    }
}

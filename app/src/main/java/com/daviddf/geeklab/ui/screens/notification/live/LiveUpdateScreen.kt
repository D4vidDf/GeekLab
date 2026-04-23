package com.daviddf.geeklab.ui.screens.notification.live

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun LiveUpdateScreenPreview() {
    MaterialTheme {
        LiveUpdateScreen(onBackClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveUpdateScreen(
    onBackClick: () -> Unit,
    viewModel: LiveUpdateViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Initialize manager
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val isSimulating by viewModel.isSimulating.collectAsState()
    val config by viewModel.config.collectAsState()

    val startedMessage = stringResource(R.string.live_update_started)
    val stoppedMessage = stringResource(R.string.live_update_stopped)
    
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.daviddf.geeklab.ACTION_STOP_SIMULATION") {
                    viewModel.stopSimulation()
                }
            }
        }
        val filter = IntentFilter("com.daviddf.geeklab.ACTION_STOP_SIMULATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.live_update_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            item {
                NotificationPermissionSection()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (Build.VERSION.SDK_INT >= 35) {
                item {
                    NotificationPostPromotedPermission(viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Text(
                    text = stringResource(R.string.live_update_summary_text),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Behavior Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = stringResource(R.string.custom_simulation_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.steps_label, config.steps),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = config.steps.toFloat(),
                            onValueChange = { viewModel.updateConfig(config.copy(steps = it.toInt())) },
                            valueRange = 2f..10f,
                            steps = 7,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.interval_label, config.intervalMs),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        val intervals = listOf(1000L, 2000L, 5000L, 10000L)
                        val selectedIndex = intervals.indexOf(config.intervalMs).coerceAtLeast(0)
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            intervals.forEachIndexed { index, interval ->
                                SegmentedButton(
                                    selected = index == selectedIndex,
                                    onClick = { viewModel.updateConfig(config.copy(intervalMs = interval)) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = intervals.size)
                                ) {
                                    Text(stringResource(R.string.interval_seconds_format, interval / 1000))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                // Color Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.use_colors_label)) },
                            trailingContent = {
                                Switch(
                                    checked = config.useColors,
                                    onCheckedChange = { viewModel.updateConfig(config.copy(useColors = it)) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )

                        if (config.useColors) {
                            ColorPickerSection(
                                label = stringResource(R.string.point_color_label),
                                selectedColor = Color(config.selectedPointColor),
                                onColorSelected = { viewModel.updateConfig(config.copy(selectedPointColor = it.toArgb())) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ColorPickerSection(
                                label = stringResource(R.string.segment_color_label),
                                selectedColor = Color(config.selectedSegmentColor),
                                onColorSelected = { viewModel.updateConfig(config.copy(selectedSegmentColor = it.toArgb())) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                // Pill Mode Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = stringResource(R.string.chip_mode_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val modes = listOf(
                                LiveUpdateViewModel.ChipMode.TIMER to R.string.chip_mode_timer,
                                LiveUpdateViewModel.ChipMode.TEXT to R.string.chip_mode_text
                            )
                            modes.forEachIndexed { index, (mode, labelRes) ->
                                SegmentedButton(
                                    selected = config.chipMode == mode,
                                    onClick = { viewModel.updateConfig(config.copy(chipMode = mode)) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                                ) {
                                    Text(stringResource(labelRes))
                                }
                            }
                        }

                        if (config.chipMode == LiveUpdateViewModel.ChipMode.TEXT) {
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = config.customShortCriticalText,
                                onValueChange = { viewModel.updateConfig(config.copy(customShortCriticalText = it)) },
                                label = { Text(stringResource(R.string.short_critical_text_label)) },
                                placeholder = { Text(stringResource(R.string.short_critical_text_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            
            item {
                if (!isSimulating) {
                    Button(
                        onClick = {
                            viewModel.startSimulation()
                            scope.launch {
                                snackbarHostState.showSnackbar(startedMessage)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = stringResource(R.string.start_live_update),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.stopSimulation()
                            scope.launch {
                                snackbarHostState.showSnackbar(stoppedMessage)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.stop_live_update),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerSection(
    label: String,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFECB7FF), // Purple
        Color(0xFF86F7FA), // Cyan
        Color(0xFFFFB7B7), // Red/Pink
        Color(0xFFB7FFB7), // Green
        Color(0xFFFFF6B7), // Yellow
        Color(0xFFB7CFFF), // Blue
        Color(0xFFFFFFFF), // White
        Color(0xFF000000)  // Black
    )

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(if (isSelected) RoundedCornerShape(12.dp) else CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun NotificationPermissionSection() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationPermissionCard(
            shouldShowRationale = false, // Simplified
            onGrantClick = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            modifier = Modifier.fillMaxWidth(),
            permissionStringResourceId = R.string.permission_message,
            permissionRationalStringResourceId = R.string.permission_rationale,
        )
    }
}

@Composable
fun NotificationPostPromotedPermission(viewModel: LiveUpdateViewModel) {
    val context = LocalContext.current
    var isPostPromotionsEnabled by remember { mutableStateOf(viewModel.isPostPromotionsEnabled()) }
    
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isPostPromotionsEnabled = viewModel.isPostPromotionsEnabled()
    }
    
    if (!isPostPromotionsEnabled) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.post_promoted_permission_message),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 35) {
                            val intent = Intent("android.settings.APP_NOTIFICATION_PROMOTION_SETTINGS").apply {
                                putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = stringResource(R.string.to_settings))
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    shouldShowRationale: Boolean,
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier,
    permissionStringResourceId: Int,
    permissionRationalStringResourceId: Int,
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(permissionStringResourceId),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (shouldShowRationale) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(permissionRationalStringResourceId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onGrantClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = stringResource(R.string.permission_grant))
            }
        }
    }
}

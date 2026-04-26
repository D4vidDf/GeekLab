package com.daviddf.geeklab.ui.screens.tools.wifi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsEthernet
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material.icons.rounded.SignalWifiStatusbar4Bar
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Wifi1Bar
import androidx.compose.material.icons.rounded.Wifi2Bar
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreen(
    onBackClick: () -> Unit,
    viewModel: WifiViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isPreview = LocalInspectionMode.current
    
    val permissions = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        list
    }

    var hasPermissions by remember {
        mutableStateOf(
            if (isPreview) true else permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        if (hasPermissions) {
            viewModel.startMonitoring(context)
        }
    }

    DisposableEffect(Unit) {
        if (!isPreview) {
            // Always start monitoring to get hardware state, even if location permissions are missing
            viewModel.startMonitoring(context)
        }

        onDispose {
            if (!isPreview) viewModel.stopMonitoring(context)
        }
    }

    // Automatically trigger permission prompt if missing and WiFi is enabled (better UX)
    LaunchedEffect(uiState.isWifiEnabled, hasPermissions) {
        if (uiState.isWifiEnabled && !hasPermissions && !isPreview) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    WifiScreenContent(
        uiState = uiState,
        hasPermissions = hasPermissions,
        isLocationEnabled = uiState.isLocationEnabled,
        onBackClick = onBackClick,
        onPermissionRequest = { permissionLauncher.launch(permissions.toTypedArray()) },
        onTestClick = viewModel::runConnectionTest,
        onSpeedTestClick = viewModel::runSpeedTest,
        onServerSelect = viewModel::setStabilityServer,
        onCustomServerUpdate = viewModel::updateCustomServer,
        estimateDistance = viewModel::estimateDistance
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreenContent(
    uiState: WifiUiState,
    hasPermissions: Boolean,
    isLocationEnabled: Boolean,
    onBackClick: () -> Unit,
    onPermissionRequest: () -> Unit,
    onTestClick: () -> Unit,
    onSpeedTestClick: () -> Unit,
    onServerSelect: (StabilityServer) -> Unit,
    onCustomServerUpdate: (String, String) -> Unit,
    estimateDistance: (Int, Int) -> Double,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showServerPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.wifi_analyzer_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Visual
            WifiSignalVisualizer(
                rssi = uiState.currentWifi?.signalStrength ?: -100,
                isConnected = uiState.isConnected && uiState.currentWifi != null
            )

            if (!uiState.isWifiEnabled) {
                NoConnectionCard(
                    isEnabled = false,
                    onActionClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Intent(Settings.Panel.ACTION_WIFI)
                        } else {
                            Intent(Settings.ACTION_WIFI_SETTINGS)
                        }
                        context.startActivity(intent)
                    }
                )
            } else if (!hasPermissions) {
                NoConnectionCard(
                    isEnabled = true,
                    isPermissionMissing = true,
                    onActionClick = onPermissionRequest
                )
            } else if (!isLocationEnabled) {
                NoConnectionCard(
                    isEnabled = true,
                    isLocationDisabled = true,
                    onActionClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                )
            } else if (uiState.isConnected && uiState.currentWifi != null) {
                WifiConnectionCard(uiState.currentWifi, estimateDistance)
                
                ConnectionTestCard(
                    isTesting = uiState.isTestingConnection,
                    result = uiState.testResult,
                    selectedServer = uiState.selectedServer,
                    customAddress = uiState.customServerAddress,
                    onTestClick = onTestClick,
                    onSelectServerClick = { showServerPicker = true }
                )

                SpeedTestCard(
                    isTesting = uiState.isTestingSpeed,
                    downloadSpeed = uiState.downloadSpeed,
                    uploadSpeed = uiState.uploadSpeed,
                    onTestClick = onSpeedTestClick
                )

                NetworkDetailsCard(uiState.currentWifi)
            } else {
                NoConnectionCard(isEnabled = true)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showServerPicker) {
        ModalBottomSheet(
            onDismissRequest = { showServerPicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.wifi_select_server),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                StabilityServer.entries.forEach { server ->
                    ServerOption(
                        server = server,
                        selected = server == uiState.selectedServer,
                        customAddress = if (server == StabilityServer.CUSTOM) uiState.customServerAddress else null,
                        onClick = {
                            onServerSelect(server)
                            if (server != StabilityServer.CUSTOM) showServerPicker = false
                        }
                    )
                }

                if (uiState.selectedServer == StabilityServer.CUSTOM) {
                    CustomServerFields(
                        address = uiState.customServerAddress,
                        port = uiState.customServerPort,
                        onUpdate = onCustomServerUpdate
                    )
                    
                    Button(
                        onClick = { showServerPicker = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(stringResource(R.string.aceptar))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomServerFields(
    address: String,
    port: String,
    onUpdate: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = address,
            onValueChange = { onUpdate(it, port) },
            label = { Text(stringResource(R.string.wifi_ip_host)) },
            placeholder = { Text(stringResource(R.string.wifi_ip_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )
        OutlinedTextField(
            value = port,
            onValueChange = { if (it.all { char -> char.isDigit() }) onUpdate(address, it) },
            label = { Text(stringResource(R.string.wifi_port)) },
            placeholder = { Text(stringResource(R.string.wifi_port_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )
    }
}

@Composable
fun WifiSignalVisualizer(rssi: Int, isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        // Rings - Only show if connected to a network
        if (isConnected) {
            Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.2f)))
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.4f)))
        }
        
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isConnected) {
                        when {
                            rssi > -50 -> Icons.Rounded.Wifi
                            rssi > -70 -> Icons.Rounded.Wifi2Bar
                            else -> Icons.Rounded.Wifi1Bar
                        }
                    } else {
                        Icons.Rounded.WifiOff
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WifiConnectionCard(details: WifiDetails, estimateDistance: (Int, Int) -> Double) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = details.ssid,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = details.bssid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoChip(
                    icon = Icons.Rounded.Speed,
                    label = stringResource(R.string.wifi_speed),
                    value = stringResource(R.string.wifi_mbps_format, details.linkSpeed)
                )
                InfoChip(
                    icon = Icons.Rounded.Router,
                    label = stringResource(R.string.wifi_dist_est),
                    value = stringResource(R.string.wifi_m_format, estimateDistance(details.signalStrength, details.frequency))
                )
                InfoChip(
                    icon = Icons.Rounded.RssFeed,
                    label = stringResource(R.string.wifi_signal),
                    value = stringResource(R.string.wifi_dbm_format, details.signalStrength)
                )
            }
        }
    }
}

@Composable
fun NetworkDetailsCard(details: WifiDetails) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.wifi_network_details), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            
            DetailItem(Icons.Rounded.Numbers, stringResource(R.string.wifi_ip_address), details.ipAddress)
            DetailItem(Icons.Rounded.SettingsEthernet, stringResource(R.string.wifi_gateway), details.gateway)
            DetailItem(Icons.Rounded.Dns, stringResource(R.string.wifi_dns), details.dns1)
            if (details.dns2 != "0.0.0.0" && details.dns2 != "N/A") {
                DetailItem(Icons.Rounded.Dns, "${stringResource(R.string.wifi_dns)} 2", details.dns2)
            }
            DetailItem(Icons.Rounded.Router, stringResource(R.string.wifi_standard), details.standard)
            DetailItem(Icons.Rounded.SettingsInputAntenna, stringResource(R.string.wifi_channel), if (details.channel > 0) details.channel.toString() else "N/A")
            DetailItem(Icons.Rounded.SignalWifiStatusbar4Bar, stringResource(R.string.wifi_frequency), stringResource(R.string.wifi_mhz_format, details.frequency))
            DetailItem(Icons.Rounded.Security, stringResource(R.string.wifi_security), details.security, multiline = true)
        }
    }
}

@Composable
fun ConnectionTestCard(
    isTesting: Boolean,
    result: ConnectionTestResult?,
    selectedServer: StabilityServer,
    customAddress: String,
    onTestClick: () -> Unit,
    onSelectServerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.wifi_stability_test),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.wifi_current_server, 
                            when(selectedServer) {
                                StabilityServer.GOOGLE -> stringResource(R.string.wifi_server_google)
                                StabilityServer.CLOUDFLARE -> stringResource(R.string.wifi_server_cloudflare)
                                StabilityServer.OPENDNS -> stringResource(R.string.wifi_server_opendns)
                                StabilityServer.QUAD9 -> stringResource(R.string.wifi_server_quad9)
                                StabilityServer.CUSTOM -> customAddress.ifBlank { stringResource(R.string.wifi_custom_target) }
                            }
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (!isTesting) {
                    IconButton(onClick = onSelectServerClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (result != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ResultBox(
                        label = stringResource(R.string.wifi_latency),
                        value = if (result.latency >= 0) stringResource(R.string.wifi_ms_format, result.latency.toInt()) else stringResource(R.string.wifi_fail),
                        color = if (result.latency in 0..100) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ResultBox(
                        label = stringResource(R.string.wifi_status),
                        value = if (result.isStable) stringResource(R.string.wifi_stable) else stringResource(R.string.wifi_unstable),
                        color = if (result.isStable) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onTestClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTesting,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.wifi_start_test))
            }
        }
    }
}

@Composable
fun SpeedTestCard(
    isTesting: Boolean,
    downloadSpeed: Double?,
    uploadSpeed: Double?,
    onTestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.wifi_speed_test),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if ((downloadSpeed != null) || (uploadSpeed != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (downloadSpeed != null) {
                        ResultBox(
                            label = stringResource(R.string.wifi_download_speed),
                            value = stringResource(R.string.wifi_mbps_float_format, downloadSpeed),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (uploadSpeed != null) {
                        ResultBox(
                            label = stringResource(R.string.wifi_upload_speed),
                            value = stringResource(R.string.wifi_mbps_float_format, uploadSpeed),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onTestClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTesting,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(stringResource(R.string.wifi_start_speed_test))
            }
        }
    }
}

@Composable
fun NoConnectionCard(
    isEnabled: Boolean,
    isPermissionMissing: Boolean = false,
    isLocationDisabled: Boolean = false,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (!isEnabled || isPermissionMissing || isLocationDisabled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (!isEnabled) Icons.Rounded.WifiOff 
                              else if (isPermissionMissing) Icons.Rounded.Security
                              else if (isLocationDisabled) Icons.Rounded.LocationOff
                              else Icons.Rounded.SignalWifiOff, 
                contentDescription = null, 
                modifier = Modifier.size(48.dp), 
                tint = if (!isEnabled || isPermissionMissing || isLocationDisabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (!isEnabled) stringResource(R.string.wifi_disabled)
                       else if (isPermissionMissing) stringResource(R.string.wifi_grant_permissions)
                       else if (isLocationDisabled) stringResource(R.string.wifi_location_required)
                       else stringResource(R.string.wifi_not_connected),
                style = MaterialTheme.typography.titleMedium,
                color = if (!isEnabled || isPermissionMissing || isLocationDisabled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (onActionClick != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onActionClick,
                    shape = MaterialTheme.shapes.large,
                    colors = if (isPermissionMissing || isLocationDisabled) ButtonDefaults.buttonColors() 
                             else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        if (isPermissionMissing) stringResource(R.string.wifi_grant_permissions) 
                        else if (isLocationDisabled) stringResource(R.string.wifi_turn_on_location)
                        else stringResource(R.string.wifi_enable)
                    )
                }
            }
        }
    }
}

@Composable
fun ServerOption(
    server: StabilityServer,
    selected: Boolean,
    customAddress: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when(server) {
                        StabilityServer.GOOGLE -> stringResource(R.string.wifi_server_google)
                        StabilityServer.CLOUDFLARE -> stringResource(R.string.wifi_server_cloudflare)
                        StabilityServer.OPENDNS -> stringResource(R.string.wifi_server_opendns)
                        StabilityServer.QUAD9 -> stringResource(R.string.wifi_server_quad9)
                        StabilityServer.CUSTOM -> stringResource(R.string.wifi_custom_target)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (server == StabilityServer.CUSTOM) {
                        if (customAddress.isNullOrBlank()) stringResource(R.string.wifi_specify_manually) else customAddress
                    } else {
                        "${server.address}:${server.port}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (selected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Connected - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Connected - Dark")
@Composable
fun WifiScreenConnectedPreview() {
    GeekLabTheme {
        WifiScreenContent(
            uiState = WifiUiState(
                isWifiEnabled = true,
                isConnected = true,
                currentWifi = WifiDetails(
                    ssid = "GeekLab_HighSpeed",
                    bssid = "00:11:22:33:44:55",
                    signalStrength = -45,
                    linkSpeed = 1200,
                    frequency = 5240,
                    channel = 48,
                    standard = "Wi-Fi 6 (ax)",
                    security = "WPA3-SAE",
                    ipAddress = "192.168.1.128",
                    gateway = "192.168.1.1",
                    dns1 = "8.8.8.8"
                ),
                testResult = ConnectionTestResult(
                    latency = 24,
                    jitter = 4,
                    isStable = true
                ),
                downloadSpeed = 450.5,
                uploadSpeed = 85.2
            ),
            hasPermissions = true,
            isLocationEnabled = true,
            onBackClick = {},
            onPermissionRequest = {},
            onTestClick = {},
            onSpeedTestClick = {},
            onServerSelect = {},
            onCustomServerUpdate = { _, _ -> },
            estimateDistance = { _, _ -> 2.5 }
        )
    }
}

@Preview(showBackground = true, name = "WiFi Disabled")
@Composable
fun WifiScreenDisabledPreview() {
    GeekLabTheme {
        WifiScreenContent(
            uiState = WifiUiState(isWifiEnabled = false),
            hasPermissions = true,
            isLocationEnabled = true,
            onBackClick = {},
            onPermissionRequest = {},
            onTestClick = {},
            onSpeedTestClick = {},
            onServerSelect = {},
            onCustomServerUpdate = { _, _ -> },
            estimateDistance = { _, _ -> 0.0 }
        )
    }
}

@Preview(showBackground = true, name = "Permissions Missing")
@Composable
fun WifiScreenPermissionsPreview() {
    GeekLabTheme {
        WifiScreenContent(
            uiState = WifiUiState(isWifiEnabled = true),
            hasPermissions = false,
            isLocationEnabled = true,
            onBackClick = {},
            onPermissionRequest = {},
            onTestClick = {},
            onSpeedTestClick = {},
            onServerSelect = {},
            onCustomServerUpdate = { _, _ -> },
            estimateDistance = { _, _ -> 0.0 }
        )
    }
}

@Preview(showBackground = true, name = "Location Disabled")
@Composable
fun WifiScreenLocationPreview() {
    GeekLabTheme {
        WifiScreenContent(
            uiState = WifiUiState(isWifiEnabled = true),
            hasPermissions = true,
            isLocationEnabled = false,
            onBackClick = {},
            onPermissionRequest = {},
            onTestClick = {},
            onSpeedTestClick = {},
            onServerSelect = {},
            onCustomServerUpdate = { _, _ -> },
            estimateDistance = { _, _ -> 0.0 }
        )
    }
}

@Preview(showBackground = true, name = "Not Connected")
@Composable
fun WifiScreenNotConnectedPreview() {
    GeekLabTheme {
        WifiScreenContent(
            uiState = WifiUiState(isWifiEnabled = true, isConnected = false),
            hasPermissions = true,
            isLocationEnabled = true,
            onBackClick = {},
            onPermissionRequest = {},
            onTestClick = {},
            onSpeedTestClick = {},
            onServerSelect = {},
            onCustomServerUpdate = { _, _ -> },
            estimateDistance = { _, _ -> 0.0 }
        )
    }
}


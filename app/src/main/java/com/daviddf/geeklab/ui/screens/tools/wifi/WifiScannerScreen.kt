package com.daviddf.geeklab.ui.screens.tools.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@Composable
fun WifiScannerScreen(
    onBackClick: () -> Unit,
    viewModel: WifiScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    WifiScannerContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onScanClick = { viewModel.startScan(context) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerContent(
    uiState: WifiScannerUiState,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedResult by remember { mutableStateOf<WifiScanResult?>(null) }

    val permissions = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        list
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onScanClick()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.wifi_scanner_title), fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon
            Surface(
                modifier = Modifier.size(96.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.WifiFind,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.wifi_scanner_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            // Primary Action Button
            Button(
                onClick = {
                    val allGranted = permissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) {
                        onScanClick()
                    } else {
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = !uiState.isScanning
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = if (uiState.isScanning) stringResource(R.string.wifi_scanning) else stringResource(R.string.scan_networks),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Results Section
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.available_networks),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (uiState.scanResults.isEmpty() && !uiState.isScanning) {
                    Text(
                        text = stringResource(R.string.no_devices_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.scanResults.forEach { result ->
                    WifiResultCard(
                        result = result,
                        onClick = { selectedResult = result }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (selectedResult != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedResult = null },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            WifiScanDetailsSheet(result = selectedResult!!)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WifiResultCard(
    result: WifiScanResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            result.level > -50 -> Icons.Rounded.Wifi
                            result.level > -70 -> Icons.Rounded.Wifi2Bar
                            else -> Icons.Rounded.Wifi1Bar
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                val ssid = if (result.ssid == "Hidden Network") stringResource(R.string.wifi_hidden_network) else result.ssid
                Text(
                    text = ssid,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.bssid,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val level = stringResource(R.string.wifi_dbm_format, result.level)
                Text(
                    text = level,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        result.level > -50 -> Color(0xFF4CAF50)
                        result.level > -70 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WifiScanDetailsSheet(result: WifiScanResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            val ssid = if (result.ssid == "Hidden Network") stringResource(R.string.wifi_hidden_network) else result.ssid
            Text(
                text = ssid,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = result.bssid,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TechBadge(
                text = if (result.standard != "Unknown") result.standard else if (result.frequency > 5900) "Wi-Fi 6E" else if (result.frequency > 4900) "Wi-Fi 5" else "Wi-Fi 4",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            TechBadge(
                text = stringResource(R.string.wifi_ch_format, result.channel) + " (" + stringResource(R.string.wifi_mhz_format, result.frequency) + ")",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            TechBadge(
                text = if (result.frequency > 5900) "6GHz" else if (result.frequency > 4900) "5GHz" else "2.4GHz",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.wifi_network_details),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                DetailItem(Icons.Rounded.SignalWifiStatusbar4Bar, stringResource(R.string.wifi_frequency), stringResource(R.string.wifi_mhz_format, result.frequency))
                DetailItem(Icons.Rounded.SettingsInputAntenna, stringResource(R.string.wifi_channel), result.channel.toString())
                DetailItem(Icons.Rounded.Router, stringResource(R.string.wifi_standard), result.standard)
                DetailItem(Icons.Rounded.Security, stringResource(R.string.wifi_security), result.capabilities, multiline = true)
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun WifiScannerScreenPreview() {
    GeekLabTheme {
        WifiScannerContent(
            uiState = WifiScannerUiState(
                isScanning = false,
                isWifiEnabled = true,
                scanResults = listOf(
                    WifiScanResult("Home_WiFi", "00:11:22:33:44:55", -45, 5240, 48, "Wi-Fi 6", "[WPA2-PSK-CCMP][RSN-PSK-CCMP][ESS]"),
                    WifiScanResult("Office_Guest", "AA:BB:CC:DD:EE:FF", -65, 2437, 6, "Wi-Fi 4", "[WPA2-PSK-CCMP][ESS]"),
                    WifiScanResult("Public_Library", "11:22:33:44:55:66", -80, 5745, 149, "Wi-Fi 5", "[OPEN]")
                )
            ),
            onBackClick = {},
            onScanClick = {}
        )
    }
}

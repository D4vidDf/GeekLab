package com.daviddf.geeklab.ui.screens.info

import android.content.Context
import android.content.res.Configuration
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onBackClick: () -> Unit,
    viewModel: InfoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    InfoScreenContent(
        uiState = uiState,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreenContent(
    uiState: InfoUiState,
    onBackClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.info_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = uiState.commercialName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            Text(
                text = stringResource(R.string.battery_monitoring_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            // Device Info Card
            InfoDeviceCard(
                title = stringResource(R.string.device_info),
                icon = Icons.Rounded.Devices,
                content = buildString {
                    append(stringResource(R.string.commercial_name, uiState.commercialName))
                    append("\n")
                    append(stringResource(R.string.device_type, uiState.deviceType))
                    append("\n")
                    append(stringResource(R.string.manufacturer, uiState.manufacturer))
                    append("\n")
                    append(stringResource(R.string.brand, uiState.brand))
                    append("\n")
                    append(stringResource(R.string.model, uiState.model))
                    append("\n")
                    append(stringResource(R.string.bluetooth_version, uiState.bluetoothVersion))
                }
            )

            // System Info Card
            InfoDeviceCard(
                title = stringResource(R.string.system_info),
                icon = Icons.Rounded.SettingsSuggest,
                content = buildString {
                    append(stringResource(R.string.android_version, uiState.androidVersion))
                    append("\n")
                    append(stringResource(R.string.sdk_int, uiState.sdkInt))
                    append("\n")
                    append(stringResource(R.string.build_id, uiState.buildId))
                    append("\n")
                    append(stringResource(R.string.build_fingerprint, uiState.buildFingerprint))
                    append("\n")
                    append(stringResource(R.string.base_os, uiState.baseOs ?: stringResource(R.string.not_available)))
                    append("\n")
                    append(stringResource(R.string.security_patch, uiState.securityPatch))
                }
            )

            // RAM & Memory Card
            InfoDeviceCard(
                title = stringResource(R.string.ram_info),
                icon = Icons.Rounded.Memory,
                content = buildString {
                    append(stringResource(R.string.installed_ram, formatBytes(context, uiState.totalRam)))
                    append("\n")
                    append(stringResource(R.string.used_ram_label, formatBytes(context, uiState.usedRam)))
                    append("\n")
                    append(stringResource(R.string.total_memory, formatBytes(context, uiState.totalRam))) // Common request to show both as Memory/RAM
                    append("\n")
                    append(stringResource(R.string.used_memory, formatBytes(context, uiState.usedRam)))
                }
            )

            // Storage Card
            InfoDeviceCard(
                title = stringResource(R.string.storage_info),
                icon = Icons.Rounded.Storage,
                content = buildString {
                    append(stringResource(R.string.total_storage, formatBytes(context, uiState.totalStorage)))
                    append("\n")
                    append(stringResource(R.string.available_storage_label, formatBytes(context, uiState.availableStorage)))
                }
            )

            // CPU Info Card
            InfoDeviceCard(
                title = stringResource(R.string.cpu_info_title),
                icon = Icons.Rounded.SettingsSuggest, // Using SettingsSuggest as a placeholder for CPU if Icons.Rounded.Cpu is not available
                content = buildString {
                    append(stringResource(R.string.soc_model, uiState.socModel))
                    append("\n")
                    append(stringResource(R.string.cpu_architecture, uiState.cpuArch))
                    append("\n")
                    append(stringResource(R.string.fabrication_process, uiState.fabricationProcess))
                    append("\n")
                    append(stringResource(R.string.instructions, uiState.instructionSet))
                    append("\n")
                    append(stringResource(R.string.cpu_revision, uiState.cpuRevision))
                    append("\n")
                    append(stringResource(R.string.cpu_cores, uiState.cpuCores))
                    append("\n")
                    append(stringResource(R.string.cpu_clock_range, uiState.cpuClockRange))
                }
            )

            // Real-time Core Clocks
            if (uiState.coreClocks.isNotEmpty()) {
                InfoDeviceCard(
                    title = stringResource(R.string.real_time_clocks),
                    icon = Icons.Rounded.SettingsSuggest,
                    content = uiState.coreClocks.mapIndexed { index, freq ->
                        "Core $index: $freq MHz"
                    }.joinToString("\n")
                )
            }

            // ABI & Features
            InfoDeviceCard(
                title = stringResource(R.string.features_title),
                icon = Icons.Rounded.SettingsSuggest,
                content = buildString {
                    append(stringResource(R.string.compatible_abi, uiState.supportedAbis.joinToString(", ")))
                    append("\n")
                    append(stringResource(R.string.compatible_abi_32, uiState.supported32Abis.joinToString(", ").ifEmpty { stringResource(R.string.none) }))
                    append("\n")
                    append(stringResource(R.string.compatible_abi_64, uiState.supported64Abis.joinToString(", ").ifEmpty { stringResource(R.string.none) }))
                    append("\n\n")
                    append(stringResource(R.string.aes, if(uiState.hasAes) stringResource(R.string.supported) else stringResource(R.string.not_supported)))
                    append("\n")
                    append(stringResource(R.string.neon, if(uiState.hasNeon) stringResource(R.string.supported) else stringResource(R.string.not_supported)))
                    append("\n")
                    append(stringResource(R.string.pmull, if(uiState.hasPmull) stringResource(R.string.supported) else stringResource(R.string.not_supported)))
                    append("\n")
                    append(stringResource(R.string.sha1, if(uiState.hasSha1) stringResource(R.string.supported) else stringResource(R.string.not_supported)))
                    append("\n")
                    append(stringResource(R.string.sha2, if(uiState.hasSha2) stringResource(R.string.supported) else stringResource(R.string.not_supported)))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun formatBytes(context: Context, bytes: Long): String {
    return Formatter.formatFileSize(context, bytes)
}

@Composable
fun InfoDeviceCard(title: String, content: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun InfoScreenPreview() {
    GeekLabTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            InfoScreenContent(
                uiState = InfoUiState(
                    commercialName = "Google Pixel 7 Pro",
                    totalRam = 12 * 1024 * 1024 * 1024L,
                    usedRam = 4 * 1024 * 1024 * 1024L,
                    totalStorage = 256 * 1024 * 1024 * 1024L,
                    availableStorage = 180 * 1024 * 1024 * 1024L,
                    socModel = "Tensor G2",
                    cpuCores = 8,
                    coreClocks = listOf(2850, 2850, 2350, 2350, 1800, 1800, 1800, 1800),
                    hasAes = true,
                    hasNeon = true
                ),
                onBackClick = {}
            )
        }
    }
}

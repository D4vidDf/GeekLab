package com.daviddf.geeklab.ui.screens.tools.nfc

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.screens.tools.wifi.TechBadge
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import androidx.compose.material3.Divider as HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScannerScreen(
    onBackClick: () -> Unit,
    viewModel: NfcScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val activity = context as? Activity

    DisposableEffect(Unit) {
        if (activity != null && uiState.isNfcSupported && uiState.isNfcEnabled) {
            viewModel.startScanning(activity)
        }
        onDispose {
            if (activity != null) {
                viewModel.stopScanning(activity)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.nfc_scanner_title), fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!uiState.isNfcSupported) {
                NfcStatusCard(
                    icon = Icons.Rounded.Nfc,
                    message = stringResource(R.string.nfc_not_supported),
                    isError = true
                )
            } else if (!uiState.isNfcEnabled) {
                NfcStatusCard(
                    icon = Icons.Rounded.Nfc,
                    message = stringResource(R.string.nfc_disabled),
                    isError = true,
                    actionLabel = stringResource(R.string.enable_nfc),
                    onAction = {
                        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                    }
                )
            } else {
                ScanningVisualizer(isScanning = uiState.isScanning)

                AnimatedVisibility(
                    visible = uiState.lastScannedTag != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    uiState.lastScannedTag?.let { tag ->
                        NfcTagDetails(tag)
                    }
                }
                
                if (uiState.lastScannedTag == null && uiState.isScanning) {
                    Text(
                        text = stringResource(R.string.nfc_scanning),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!uiState.isScanning) {
                    Button(
                        onClick = { activity?.let { viewModel.startScanning(it) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.nfc_ready_to_scan))
                    }
                }
            }
        }
    }
}

@Composable
fun NfcStatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    isError: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ScanningVisualizer(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_scan")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        if (isScanning) {
            Surface(
                modifier = Modifier.size(100.dp * scale),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {}
        }
        Surface(
            modifier = Modifier.size(80.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (isScanning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isScanning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NfcTagDetails(tag: NfcTagInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.nfc_tag_detected),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                DetailItem(Icons.Rounded.Fingerprint, stringResource(R.string.nfc_id), tag.id)
                
                Text(
                    text = stringResource(R.string.nfc_tech_list),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tag.techList.forEach { tech ->
                        TechBadge(
                            text = tech,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        tag.ndefInfo?.let { ndef ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.nfc_ndef_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    DetailItem(Icons.Rounded.Tag, stringResource(R.string.nfc_tag_type), ndef.type)
                    DetailItem(Icons.Rounded.Storage, stringResource(R.string.nfc_max_size), "${ndef.maxSize} bytes")
                    DetailItem(
                        Icons.Rounded.Edit, 
                        stringResource(R.string.nfc_is_writable), 
                        if (ndef.isWritable) stringResource(R.string.yes) else stringResource(R.string.no)
                    )
                    
                    if (ndef.records.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.nfc_messages),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        ndef.records.forEachIndexed { index, record ->
                            val msg = record.payload
                            val icon = when {
                                msg.startsWith("http") -> Icons.Rounded.Link
                                msg.contains("@") -> Icons.Rounded.Email
                                msg.startsWith("tel:") -> Icons.Rounded.Phone
                                else -> Icons.Rounded.Article
                            }
                            
                            val typeLabel = when (record.tnf) {
                                android.nfc.NdefRecord.TNF_WELL_KNOWN -> stringResource(R.string.nfc_well_known, record.type)
                                android.nfc.NdefRecord.TNF_MIME_MEDIA -> stringResource(R.string.nfc_mime_type, record.type)
                                android.nfc.NdefRecord.TNF_ABSOLUTE_URI -> stringResource(R.string.nfc_absolute_uri, record.type)
                                else -> stringResource(R.string.nfc_unknown_tnf, record.tnf.toInt())
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                DetailItem(
                                    icon = icon,
                                    label = stringResource(R.string.nfc_record_label, index + 1),
                                    value = typeLabel,
                                    multiline = true
                                )
                                Text(
                                    text = stringResource(R.string.nfc_payload, msg),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 28.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.nfc_no_messages),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NfcStatusCardPreview() {
    GeekLabTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NfcStatusCard(
                icon = Icons.Rounded.Nfc,
                message = "NFC is ready",
                isError = false
            )
            NfcStatusCard(
                icon = Icons.Rounded.Nfc,
                message = "NFC is disabled",
                isError = true,
                actionLabel = "Enable NFC",
                onAction = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanningVisualizerPreview() {
    GeekLabTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ScanningVisualizer(isScanning = true)
            ScanningVisualizer(isScanning = false)
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun NfcScannerScreenPreview() {
    GeekLabTheme {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            NfcTagDetails(
                tag = NfcTagInfo(
                    id = "04:A1:B2:C3:D4:E5:F6",
                    techList = listOf("NfcA", "MifareUltraLight", "Ndef"),
                    ndefInfo = NdefInfo(
                        type = "NFC Forum Type 2",
                        maxSize = 137,
                        isWritable = true,
                        canMakeReadOnly = true,
                        records = listOf(
                            NdefRecordData(android.nfc.NdefRecord.TNF_WELL_KNOWN, "U", "https://geeklab.d4viddf.com"),
                            NdefRecordData(android.nfc.NdefRecord.TNF_WELL_KNOWN, "T", "Welcome to GeekLab NFC Scan")
                        )
                    )
                )
            )
        }
    }
}

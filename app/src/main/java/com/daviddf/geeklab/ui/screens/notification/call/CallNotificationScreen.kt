package com.daviddf.geeklab.ui.screens.notification.call

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.SettingsPhone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallNotificationScreen(
    onBackClick: () -> Unit,
    viewModel: CallNotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val hasPermission by viewModel.hasPermission.collectAsState()
    val callerName by viewModel.callerName.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.updatePermissionStatus(context)
    }

    LaunchedEffect(Unit) {
        viewModel.updatePermissionStatus(context)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.call_notification_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
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
            // Icon header
            Surface(
                modifier = Modifier.size(96.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.call_notification_summary),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.metric_configuration_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = callerName,
                        onValueChange = { viewModel.updateCallerName(it) },
                        label = { Text(stringResource(R.string.caller_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    )
                }
            }

            // Normal Calls Section
            Text(
                text = stringResource(R.string.normal_call),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CallTestButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.incoming_call_test),
                    icon = Icons.Rounded.PhoneInTalk,
                    onClick = {
                        val needsNotification = Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        val needsAudio = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        if (needsNotification || needsAudio) {
                            val permissions = mutableListOf<String>()
                            if (needsNotification) permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                            if (needsAudio) permissions.add(android.Manifest.permission.RECORD_AUDIO)
                            launcher.launch(permissions.toTypedArray())
                        } else {
                            viewModel.showNormalIncomingCall(context)
                        }
                    }
                )
                CallTestButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.ongoing_call_test),
                    icon = Icons.Rounded.SettingsPhone,
                    onClick = {
                        val needsNotification = Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        val needsAudio = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (needsNotification || needsAudio) {
                            val permissions = mutableListOf<String>()
                            if (needsNotification) permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                            if (needsAudio) permissions.add(android.Manifest.permission.RECORD_AUDIO)
                            launcher.launch(permissions.toTypedArray())
                        } else {
                            viewModel.showNormalOngoingCall(context)
                        }
                    }
                )
            }

            // Telecom Section
            Text(
                text = stringResource(R.string.telecom_call),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            CallTestButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.test_call),
                icon = Icons.Rounded.Call,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    val needsNotification = Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    val needsAudio = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (needsNotification || needsAudio) {
                        val permissions = mutableListOf<String>()
                        if (needsNotification) permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        if (needsAudio) permissions.add(android.Manifest.permission.RECORD_AUDIO)
                        launcher.launch(permissions.toTypedArray())
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewModel.startTelecomCall(context)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CallTestButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)

@Composable
fun CallNotificationScreenPreview() {
    MaterialTheme {
        CallNotificationScreen(onBackClick = {})
    }
}

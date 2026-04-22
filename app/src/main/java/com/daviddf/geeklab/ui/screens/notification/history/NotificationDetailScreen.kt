package com.daviddf.geeklab.ui.screens.notification.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: NotificationHistoryViewModel = viewModel(
        factory = NotificationHistoryViewModelFactory(context)
    )
    val notification by viewModel.getNotificationById(notificationId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_details)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        notification?.let { n ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DetailItem(stringResource(R.string.tittle), n.title ?: "N/A")
                DetailItem(stringResource(R.string.message), n.text ?: "N/A")
                if (n.bigText != null) {
                    DetailItem("Big Text", n.bigText)
                }
                DetailItem(stringResource(R.string.package_name), n.packageName)
                DetailItem(stringResource(R.string.channel_id), n.channelId ?: "N/A")
                DetailItem(stringResource(R.string.post_time), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LocalLocale.current.platformLocale).format(Date(n.timestamp)))
                DetailItem(stringResource(R.string.clearable), n.isClearable.toString())
                DetailItem(stringResource(R.string.key), n.key)
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

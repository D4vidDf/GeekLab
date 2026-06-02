package com.daviddf.geeklab.ui.screens.notification.messaging

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingNotificationScreen(
    onBackClick: () -> Unit,
    viewModel: MessagingNotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val userName by viewModel.userName.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentMessageText by viewModel.currentMessageText.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendNotification(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.messaging_style_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBackClick,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.messaging_style_summary),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.metric_configuration_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { viewModel.updateUserName(it) },
                        label = { Text(stringResource(R.string.user_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { viewModel.updatePartnerName(it) },
                        label = { Text(stringResource(R.string.chat_partner_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.PersonOutline, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )
                }
            }

            // Message List Card
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.message_list_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (messages.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = { viewModel.clearMessages() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ClearAll,
                                    contentDescription = stringResource(R.string.clear_messages),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    if (messages.isEmpty()) {
                        Text(
                            text = "No messages added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            messages.forEach { msg ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = if (msg.isFromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(
                                            text = msg.text,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = if (msg.isFromMe) userName else partnerName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = currentMessageText,
                        onValueChange = { viewModel.updateCurrentMessageText(it) },
                        placeholder = { Text(stringResource(R.string.message_text_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.addMessage(isFromMe = false) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(stringResource(R.string.send_as_partner))
                        }
                        FilledTonalButton(
                            onClick = { viewModel.addMessage(isFromMe = true) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(stringResource(R.string.send_as_user))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.sendNotification(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.messaging_notification_test),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

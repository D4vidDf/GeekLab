package com.daviddf.geeklab.ui.screens.notification.standard

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("1") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var countError by remember { mutableStateOf<String?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendNotifications(context, title, message, count.toLongOrNull() ?: 0L, bitmap)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notificaciones),
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
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.Unspecified
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
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.title_notifications),
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
                        text = stringResource(R.string.custom_notification_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            titleError = null
                        },
                        label = { Text(stringResource(R.string.notification_title_input)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError != null,
                        supportingText = titleError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Rounded.Title, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { 
                            message = it
                            messageError = null
                        },
                        label = { Text(stringResource(R.string.notification_message_input)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = messageError != null,
                        supportingText = messageError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = count,
                        onValueChange = { 
                            count = it
                            countError = null
                        },
                        label = { Text(stringResource(R.string.notification_count_input)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = countError != null,
                        supportingText = countError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Rounded.Numbers, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )
                }
            }

            // Image Section Card
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
                    Text(
                        text = stringResource(R.string.imagen_opcional),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    FilledTonalButton(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUri == null) stringResource(R.string.select_image) else stringResource(R.string.change_image))
                    }

                    bitmap?.let { btm ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Image(
                                bitmap = btm.asImageBitmap(),
                                contentDescription = stringResource(R.string.selected_image_desc),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            val errorTitleRequired = stringResource(R.string.error_title_required)
            val errorTitleTooLong = stringResource(R.string.error_title_too_long)
            val errorMessageRequired = stringResource(R.string.error_message_required)
            val errorInvalidNumber = stringResource(R.string.error_invalid_number)

            Button(
                onClick = {
                    var hasError = false
                    if (title.isEmpty()) {
                        titleError = errorTitleRequired
                        hasError = true
                    } else if (title.length > 30) {
                        titleError = errorTitleTooLong
                        hasError = true
                    }

                    if (message.isEmpty()) {
                        messageError = errorMessageRequired
                        hasError = true
                    }

                    val n = count.toLongOrNull() ?: 0L
                    if (n <= 0) {
                        countError = errorInvalidNumber
                        hasError = true
                    }

                    if (!hasError) {
                        val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                        
                        if (areNotificationsEnabled) {
                            sendNotifications(context, title, message, n, bitmap)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.generate_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun sendNotifications(context: Context, title: String, message: String, count: Long, bitmap: Bitmap?) {
    // Final check before sending each batch
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

    val channelId = "GeekLab"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    for (i in 1..count) {
        val builder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(message)
            setPriority(NotificationCompat.PRIORITY_MAX)
            setDefaults(Notification.DEFAULT_ALL)
            setAutoCancel(true)
            if (bitmap != null) {
                setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(message))
            } else {
                setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
        }

        try {
            // Using a unique ID for each notification in the loop
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt() + i.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Handle or log
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun NotificationScreenPreview() {
    GeekLabTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NotificationScreen(onBackClick = {})
        }
    }
}

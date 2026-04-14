package com.daviddf.geeklab.ui.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daviddf.geeklab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
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
            // Permission granted, we can send notifications
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notificaciones),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        titleError = null
                    },
                    label = { Text(stringResource(R.string.notification_title_input)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } }
                )
            }

            item {
                OutlinedTextField(
                    value = message,
                    onValueChange = { 
                        message = it
                        messageError = null
                    },
                    label = { Text(stringResource(R.string.notification_message_input)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = messageError != null,
                    supportingText = messageError?.let { { Text(it) } }
                )
            }

            item {
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
                    supportingText = countError?.let { { Text(it) } }
                )
            }

            item {
                Button(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageUri == null) stringResource(R.string.select_image) else stringResource(R.string.change_image))
                }
            }

            bitmap?.let { btm ->
                item {
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = stringResource(R.string.selected_image_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            
                            sendNotifications(context, title, message, n, bitmap)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.generate_notifications))
                }
            }
        }
    }
}

private fun sendNotifications(context: Context, title: String, message: String, count: Long, bitmap: Bitmap?) {
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
        } catch (e: SecurityException) {
            // Handle or log
        }
    }
}

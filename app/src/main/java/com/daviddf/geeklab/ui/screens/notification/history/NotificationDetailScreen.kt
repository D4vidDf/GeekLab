package com.daviddf.geeklab.ui.screens.notification.history

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.data.notification.NotificationEntity
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.text.SimpleDateFormat
import java.util.*

private val prettyJson = Json { prettyPrint = true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: NotificationHistoryViewModel = viewModel(
        factory = NotificationHistoryViewModelFactory(context.applicationContext as Application)
    )
    val state by viewModel.getNotificationDetail(notificationId).collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_details)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        state?.let { s ->
            NotificationDetailContent(
                state = s, 
                padding = padding, 
                onOpenApp = { viewModel.openApp(s.notification.packageName) },
                onSaveMedia = { viewModel.saveMediaToGallery(it) },
                onShareMedia = { viewModel.shareMedia(it) }
            )
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailContent(
    state: NotificationDetailState, 
    padding: PaddingValues,
    onOpenApp: () -> Unit,
    onSaveMedia: (String) -> Boolean,
    onShareMedia: (String) -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        stringResource(R.string.tab_info),
        stringResource(R.string.tab_content),
        stringResource(R.string.tab_media),
        stringResource(R.string.tab_extras),
        stringResource(R.string.tab_actions)
    )
    val n = state.notification

    val packageNameLabel = stringResource(R.string.package_name)
    val postTimeLabel = stringResource(R.string.post_time)
    val channelIdLabel = stringResource(R.string.channel_id)
    val clearableLabel = stringResource(R.string.clearable)
    val keyLabel = stringResource(R.string.key)
    val titleLabel = stringResource(R.string.tittle)
    val messageLabel = stringResource(R.string.message)
    val categoryLabel = stringResource(R.string.category)
    val priorityLabel = stringResource(R.string.priority)
    val visibilityLabel = stringResource(R.string.visibility)
    val notApplicable = stringResource(R.string.not_applicable)
    val bigTextLabel = stringResource(R.string.big_text)
    val subTextLabel = stringResource(R.string.sub_text)
    val actionLabel = stringResource(R.string.action_item_label)
    val availableActionsTitle = stringResource(R.string.available_actions)
    val bundleExtrasTitle = stringResource(R.string.bundle_extras)
    val noExtrasFound = stringResource(R.string.no_extras_found)
    val noActionsFound = stringResource(R.string.no_actions_found)
    val noMediaFound = stringResource(R.string.no_media_found)
    val generalInfoTitle = stringResource(R.string.general_information)
    val contentTitle = stringResource(R.string.notification_content_title)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                HeaderSection(state.appLabel, n.packageName, state.appIcon)
                
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onOpenApp,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.open_app))
                    }
                }
            }
        }

        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> { // Info
                item { InfoSectionTitle(generalInfoTitle) }
                val infoItems = listOf(
                    packageNameLabel to n.packageName,
                    postTimeLabel to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(n.timestamp)),
                    channelIdLabel to (n.channelId ?: notApplicable),
                    categoryLabel to (n.category ?: notApplicable),
                    priorityLabel to n.priority.toString(),
                    visibilityLabel to n.visibility.toString(),
                    clearableLabel to n.isClearable.toString(),
                    keyLabel to n.key
                )
                itemsIndexed(infoItems) { index, item ->
                    InfoItem(item.first, item.second, index, infoItems.size)
                }
            }
            1 -> { // Content
                item { InfoSectionTitle(contentTitle) }
                val contentItems = listOfNotNull(
                    titleLabel to (n.title ?: notApplicable),
                    messageLabel to (n.text ?: notApplicable),
                    n.bigText?.let { bigTextLabel to it },
                    n.subText?.let { subTextLabel to it }
                )
                itemsIndexed(contentItems) { index, item ->
                    InfoItem(item.first, item.second, index, contentItems.size)
                }
            }
            2 -> { // Media
                val mediaPaths = try {
                    n.mediaPathsJson?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                if (mediaPaths.isNotEmpty()) {
                    item {
                        MediaSection(mediaPaths, onSaveMedia, onShareMedia)
                    }
                } else {
                    item { EmptyState(noMediaFound) }
                }
            }
            3 -> { // Extras
                val extrasMap = try {
                    n.extrasJson?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap()
                } catch (_: Exception) {
                    emptyMap()
                }
                if (extrasMap.isNotEmpty()) {
                    item { InfoSectionTitle(bundleExtrasTitle) }
                    val sortedExtras = extrasMap.toList().sortedBy { it.first }
                    itemsIndexed(sortedExtras) { index, extra ->
                        val value = extra.second
                        val formattedValue = try {
                            val jsonElement = Json.parseToJsonElement(value)
                            prettyJson.encodeToString(JsonElement.serializer(), jsonElement)
                        } catch (_: Exception) {
                            value
                        }
                        InfoItem(extra.first, formattedValue, index, sortedExtras.size)
                    }
                } else {
                    item { EmptyState(noExtrasFound) }
                }
            }
            4 -> { // Actions
                val actionsList = try {
                    n.actionsJson?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                if (actionsList.isNotEmpty()) {
                    item { InfoSectionTitle(availableActionsTitle) }
                    itemsIndexed(actionsList) { index, action ->
                        InfoItem(String.format(actionLabel, index + 1), action, index, actionsList.size)
                    }
                } else {
                    item { EmptyState(noActionsFound) }
                }
            }
        }
    }
}

@Composable
fun MediaSection(paths: List<String>, onSave: (String) -> Boolean, onShare: (String) -> Unit) {
    var selectedPath by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        InfoSectionTitle(stringResource(R.string.notification_media_title))
        Spacer(modifier = Modifier.height(8.dp))
        
        paths.chunked(2).forEach { rowPaths ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowPaths.forEach { path ->
                    MediaItem(path, modifier = Modifier.weight(1f)) {
                        selectedPath = path
                    }
                }
                if (rowPaths.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (selectedPath != null) {
        MediaFullScreenDialog(
            path = selectedPath!!,
            onDismiss = { selectedPath = null },
            onSave = onSave,
            onShare = onShare
        )
    }
}

@Composable
fun MediaItem(path: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ImageNotSupported, contentDescription = null)
            }
        }
    }
}

@Composable
fun MediaFullScreenDialog(
    path: String, 
    onDismiss: () -> Unit,
    onSave: (String) -> Boolean,
    onShare: (String) -> Unit
) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveSuccessMsg = stringResource(R.string.media_saved_success)
    val saveFailedMsg = stringResource(R.string.media_save_failed)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close_desc), tint = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { 
                                val success = onSave(path)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (success) saveSuccessMsg else saveFailedMsg
                                    )
                                }
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = stringResource(R.string.save_desc), tint = Color.White)
                        }
                        IconButton(
                            onClick = { onShare(path) },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.share_desc), tint = Color.White)
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418, name = "General Info")
@Composable
fun NotificationDetailPreview() {
    val mockNotification = NotificationEntity(
        id = 1,
        packageName = "com.whatsapp",
        title = "John Doe",
        text = "Hey, how are you? I'm checking the notification details.",
        bigText = "This is a much longer message that would appear when you expand the notification. It can contain multiple lines of text.",
        subText = "WhatsApp • 2 messages",
        timestamp = System.currentTimeMillis(),
        channelId = "messages",
        isClearable = true,
        key = "whatsapp_key_1",
        category = "msg",
        priority = 1,
        visibility = 1,
        extrasJson = "{\"android.title\":\"John Doe\",\"android.text\":\"Hey, how are you?\",\"android.subText\":\"WhatsApp\",\"android.remoteInputHistory\":\"[\\\"Fine, thanks!\\\", \\\"What about you?\\\"]\",\"complex.data\":\"{\\\"id\\\": 123, \\\"status\\\": \\\"active\\\", \\\"tags\\\": [\\\"important\\\", \\\"social\\\"]}\"}",
        actionsJson = "[\"Reply\",\"Mark as read\",\"Mute\"]",
        mediaPathsJson = "[]"
    )

    com.daviddf.geeklab.ui.theme.GeekLabTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NotificationDetailContent(
                state = NotificationDetailState(
                    notification = mockNotification,
                    appLabel = "WhatsApp",
                    appIcon = null
                ),
                padding = PaddingValues(0.dp),
                onOpenApp = {},
                onSaveMedia = { true },
                onShareMedia = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418, name = "Extras Tab")
@Composable
fun NotificationExtrasPreview() {
    val mockNotification = NotificationEntity(
        id = 1,
        packageName = "com.whatsapp",
        title = "John Doe",
        text = "Hey, how are you?",
        bigText = null,
        timestamp = System.currentTimeMillis(),
        channelId = "messages",
        isClearable = true,
        key = "whatsapp_key_1",
        extrasJson = "{\"android.title\":\"John Doe\",\"android.text\":\"Hey, how are you?\",\"complex.json\":\"{\\\"user\\\": {\\\"name\\\": \\\"David\\\", \\\"id\\\": 99}, \\\"settings\\\": {\\\"notifications\\\": true}}\"}",
        actionsJson = "[]"
    )

    com.daviddf.geeklab.ui.theme.GeekLabTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NotificationDetailContent(
                state = NotificationDetailState(mockNotification, "WhatsApp", null),
                padding = PaddingValues(0.dp),
                onOpenApp = {},
                onSaveMedia = { true },
                onShareMedia = {},
                initialTab = 3
            )
        }
    }
}

@Composable
fun HeaderSection(label: String, packageName: String, icon: Bitmap?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                bitmap = icon.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun InfoItem(label: String, value: String, index: Int, size: Int) {
    val shape = getShape(index, size)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getShape(index: Int, size: Int): RoundedCornerShape {
    return when {
        size == 1 -> RoundedCornerShape(24.dp)
        index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        index == size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(4.dp)
    }
}

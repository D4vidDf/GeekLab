package com.daviddf.geeklab.ui.screens.notification.history

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.data.notification.NotificationEntity
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = remember(configuration) { configuration.locales[0] }
    val timeFormat = remember(locale) { SimpleDateFormat("h:mm a", locale) }

    val viewModel: NotificationHistoryViewModel = viewModel(
        factory = NotificationHistoryViewModelFactory(context.applicationContext as Application)
    )
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val isServiceEnabled = remember(refreshTrigger) { viewModel.isServiceEnabled() }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.notification_history),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { padding ->
        val groupedNotifications = remember(notifications) {
            notifications.groupBy { it.packageName }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding
        ) {
            // 1. Toggle Switch
            item(key = "toggle_service") {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.use_notification_history),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isServiceEnabled,
                            onCheckedChange = { viewModel.onToggleClicked(context) }
                        )
                    }
                }
            }

            if (!isServiceEnabled) {
                item { EmptyState(stringResource(R.string.notification_history_off), stringResource(R.string.notification_history_off_desc)) }
            } else if (notifications.isEmpty()) {
                item { EmptyState(stringResource(R.string.no_recent_notifications), stringResource(R.string.no_recent_notifications_desc)) }
            } else {
                // 2. Recently Dismissed Section
                item(key = "header_recent") {
                    Text(
                        text = stringResource(R.string.recently_dismissed),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }

                val recent = notifications.take(3)
                items(recent, key = { "recent_${it.id}" }) { notification ->
                    RecentNotificationItem(
                        notification = notification,
                        timeFormat = timeFormat,
                        locale = locale,
                        onClick = { onNotificationClick(notification.id) }
                    )
                }

                // 3. Last 24 Hours Section (The Sticky/Fixed Header)
                item(key = "header_24h") {
                    Text(
                        text = stringResource(R.string.last_24_hours),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
                        color = Color.White
                    )
                }

                // 4. App Groups (Unlimited)
                groupedNotifications.forEach { (packageName, appNotifications) ->
                    item(key = packageName) {
                        AppNotificationGroup(
                            packageName = packageName,
                            notifications = appNotifications,
                            locale = locale,
                            onNotificationClick = onNotificationClick,
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    if (icon != null) {
        val painter = remember(icon) {
            BitmapPainter(icon.toBitmap().asImageBitmap())
        }
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun RecentNotificationItem(
    notification: NotificationEntity,
    timeFormat: SimpleDateFormat,
    locale: Locale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2128)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    packageName = notification.packageName,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.packageName.split(".").last().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray
                )
                    if (!notification.title.isNullOrBlank()) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = timeFormat.format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
            if (!notification.text.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    maxLines = 2,
                    modifier = Modifier.padding(start = 44.dp)
                )
            }
        }
    }
}

@Composable
fun AppNotificationGroup(
    modifier: Modifier = Modifier,
    packageName: String,
    notifications: List<NotificationEntity>,
    locale: Locale,
    onNotificationClick: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Animate the bottom corners specifically for the header surface
    val cornerAnim by animateDpAsState(
        targetValue = if (expanded) 8.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cornerFade"
    )

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = cornerAnim, bottomEnd = cornerAnim),
            color = Color(0xFF1E2128),
            contentColor = Color.White
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(packageName = packageName, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packageName.split(".").last().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${notifications.size} notifications",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Crossfade(targetState = expanded, animationSpec = spring(stiffness = Spring.StiffnessHigh)) { isExpanded ->
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Gray
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
        ) {
            Column {
                notifications.forEachIndexed { index, notification ->
                    Spacer(Modifier.height(4.dp))
                    SubNotificationItem(
                        notification = notification,
                        dateString = remember(notification.timestamp) { getFormattedDate(notification.timestamp, locale) },
                        isLast = index == notifications.size - 1,
                        onClick = { onNotificationClick(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubNotificationItem(
    notification: NotificationEntity,
    dateString: String,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val bottomCorner by animateDpAsState(
        targetValue = if (isLast) 24.dp else 8.dp,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 8.dp, topEnd = 8.dp,
            bottomStart = bottomCorner, bottomEnd = bottomCorner
        ),
        color = Color(0xFF1E2128),
        contentColor = Color.White
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (!notification.title.isNullOrBlank()) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!notification.text.isNullOrBlank()) {
                        Text(
                            text = notification.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            maxLines = 3 // Increased for long lists
                        )
                    }
                }
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(title: String, description: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(148.dp),
                color = Color(0xFF1E2128),
                shape = MaterialShapes.Cookie6Sided.toShape()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                title, 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun NotificationHistoryScreenPreview() {
    GeekLabTheme(darkTheme = true) {
        // Mock data for preview
        val mockNotifications = listOf(
            NotificationEntity(
                id = 1,
                packageName = "com.whatsapp",
                title = "John Doe",
                text = "Hey, how are you?",
                bigText = null,
                timestamp = System.currentTimeMillis(),
                channelId = "msg",
                isClearable = true,
                key = "key1"
            ),
            NotificationEntity(
                id = 2,
                packageName = "com.instagram.android",
                title = "Instagram",
                text = "Someone liked your photo",
                bigText = null,
                timestamp = System.currentTimeMillis() - 3600000,
                channelId = "notif",
                isClearable = true,
                key = "key2"
            ),
            NotificationEntity(
                id = 3,
                packageName = "com.android.settings",
                title = "System",
                text = "Battery low",
                bigText = null,
                timestamp = System.currentTimeMillis() - 7200000,
                channelId = "sys",
                isClearable = true,
                key = "key3"
            )
        )
        
        Scaffold(
            topBar = {
                OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Notification history") },
                    navigationIcon = {
                        FilledTonalIconButton(onClick = {  }, shapes = IconButtonDefaults.shapes()) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color(0xFF121418)
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Simplified version for preview
                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                val timeFormat = remember { SimpleDateFormat("h:mm a", locale) }
                LazyColumn {
                    item {
                        Text(
                            text = "Recently dismissed",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White
                        )
                    }
                    items(mockNotifications) { notification ->
                        RecentNotificationItem(notification, timeFormat, locale) {}
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun RecentNotificationItemPreview() {
    GeekLabTheme(darkTheme = true) {
        val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
        val timeFormat = remember { SimpleDateFormat("h:mm a", locale) }
        RecentNotificationItem(
            notification = NotificationEntity(
                id = 1,
                packageName = "com.whatsapp",
                title = "John Doe",
                text = "This is a preview message to test the design.",
                bigText = null,
                timestamp = System.currentTimeMillis(),
                channelId = "msg",
                isClearable = true,
                key = "key1"
            ),
            timeFormat = timeFormat,
            locale = locale,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun AppNotificationGroupPreview() {
    GeekLabTheme(darkTheme = true) {
        val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
        AppNotificationGroup(
            packageName = "com.whatsapp",
            notifications = listOf(
                NotificationEntity(
                    id = 1,
                    packageName = "com.whatsapp",
                    title = "John Doe",
                    text = "Message 1",
                    bigText = null,
                    timestamp = System.currentTimeMillis(),
                    channelId = "msg",
                    isClearable = true,
                    key = "key1"
                ),
                NotificationEntity(
                    id = 2,
                    packageName = "com.whatsapp",
                    title = "Jane Smith",
                    text = "Message 2",
                    bigText = null,
                    timestamp = System.currentTimeMillis() - 60000,
                    channelId = "msg",
                    isClearable = true,
                    key = "key2"
                )
            ),
            locale = locale,
            onNotificationClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121418)
@Composable
fun EmptyStatePreview() {
    GeekLabTheme(darkTheme = true) {
        EmptyState(
            title = "Notification history is off",
            description = "Turn on notification history to see recent and snoozed notifications"
        )
    }
}

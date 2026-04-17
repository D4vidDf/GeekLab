package com.daviddf.geeklab.ui.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

data class PermissionDetail(
    val name: String,
    val colloquialName: String,
    val description: String,
    val docUrl: String
)

@Composable
fun AppDetailScreen(
    packageName: String,
    onBackClick: () -> Unit,
    viewModel: AppDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val notAvailable = stringResource(R.string.not_available)
    val sideloadedText = stringResource(R.string.sideloaded_system)
    val datePattern = stringResource(R.string.date_time_pattern)

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName, notAvailable, sideloadedText, datePattern)
    }

    AppDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onLaunchApp = { viewModel.launchApp(packageName) },
        onShareApk = { viewModel.shareApk(packageName, uiState.label) },
        onLaunchActivity = { activityName -> viewModel.launchActivity(packageName, activityName) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDetailContent(
    uiState: AppDetailState,
    onBackClick: () -> Unit,
    onLaunchApp: () -> Unit,
    onShareApk: () -> Unit,
    onLaunchActivity: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedPermission by remember { mutableStateOf<PermissionDetail?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_info)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.packageInfo != null) {
            val packageInfo = uiState.packageInfo!!
            val appInfo = packageInfo.applicationInfo
            val label = uiState.label
            val icon = uiState.icon?.asImageBitmap()

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf(
                stringResource(R.string.permissions),
                stringResource(R.string.activities),
                stringResource(R.string.services),
                stringResource(R.string.providers),
                stringResource(R.string.shared_libraries)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (icon != null) {
                                Image(
                                    bitmap = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(packageInfo.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column {
                            InfoRow(stringResource(R.string.version), uiState.version)
                            InfoRow(stringResource(R.string.size), uiState.size)
                            InfoRow(stringResource(R.string.installed), uiState.installTime)
                            InfoRow(stringResource(R.string.origin), uiState.origin)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SplitButton(
                                onPrimaryClick = onLaunchApp,
                                onSecondaryClick = onShareApk,
                                primaryText = stringResource(R.string.open),
                                primaryIcon = Icons.Rounded.PlayArrow,
                                secondaryIcon = Icons.Rounded.Share,
                                secondaryContentDescription = stringResource(R.string.share_apk)
                            )
                        }
                    }
                }

                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        ScrollableTabRow(
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
                    0 -> {
                        val permissions = packageInfo.requestedPermissions?.toList()
                        if (permissions != null && permissions.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(permissions) { index, permission ->
                                val detail = getPermissionDetail(permission, context)
                                val shape = getShape(index, permissions.size)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    onClick = {
                                        selectedPermission = detail
                                        showBottomSheet = true
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            detail.colloquialName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            permission,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_permissions_requested)) }
                        }
                    }
                    1 -> {
                        val activities = packageInfo.activities?.toList()
                        if (activities != null && activities.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(activities) { index, activity ->
                                val shape = getShape(index, activities.size)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    ActivityRow(activity, packageInfo.packageName, onLaunchActivity)
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_activities_found)) }
                        }
                    }
                    2 -> {
                        val services = uiState.services
                        if (services.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(services) { index, service ->
                                val shape = getShape(index, services.size)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(
                                            service.name.removePrefix(packageInfo.packageName),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_services_found)) }
                        }
                    }
                    3 -> {
                        val providers = packageInfo.providers?.toList()
                        if (providers != null && providers.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(providers) { index, provider ->
                                val shape = getShape(index, providers.size)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(
                                            provider.name.removePrefix(packageInfo.packageName),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_providers_found)) }
                        }
                    }
                    4 -> {
                        val sharedLibs = uiState.sharedLibraries
                        if (sharedLibs.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(sharedLibs) { index, lib ->
                                val shape = getShape(index, sharedLibs.size)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            lib,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_native_libs_found)) }
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.app_not_found))
            }
        }

        if (showBottomSheet && selectedPermission != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                PermissionBottomSheetContent(
                    detail = selectedPermission!!,
                    onOpenDoc = {
                        val intent = Intent(Intent.ACTION_VIEW, selectedPermission!!.docUrl.toUri())
                        context.startActivity(intent)
                    }
                )
            }
        }
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

@Composable
fun PermissionBottomSheetContent(detail: PermissionDetail, onOpenDoc: () -> Unit) {
    val isAndroidPermission = detail.name.startsWith("android.permission.")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                stringResource(R.string.permission_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                detail.colloquialName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                detail.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.description),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
            Text(
                detail.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        Button(
            onClick = onOpenDoc,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isAndroidPermission) stringResource(R.string.open_documentation) else stringResource(R.string.search_online),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getPermissionDetail(permission: String, context: Context): PermissionDetail {
    val pm = context.packageManager
    val label = try {
        val info = pm.getPermissionInfo(permission, 0)
        info.loadLabel(pm).toString()
    } catch (e: Exception) {
        permission.split(".").last().replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    val description = try {
        val info = pm.getPermissionInfo(permission, 0)
        info.loadDescription(pm)?.toString() ?: context.getString(R.string.no_description_available)
    } catch (e: Exception) {
        context.getString(R.string.default_permission_description)
    }

    val docUrl = if (permission.startsWith("android.permission.")) {
        "https://developer.android.com/reference/android/Manifest.permission#${permission.substringAfterLast(".")}"
    } else {
        "https://www.google.com/search?q=${Uri.encode(permission)}"
    }

    return PermissionDetail(
        name = permission,
        colloquialName = label,
        description = description,
        docUrl = docUrl
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButton(
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    primaryText: String,
    primaryIcon: ImageVector,
    secondaryIcon: ImageVector,
    secondaryContentDescription: String,
    modifier: Modifier = Modifier
) {
    SplitButtonLayout(
        modifier = modifier,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = onPrimaryClick,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(primaryIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    primaryText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                onClick = onSecondaryClick,
                modifier = Modifier.width(72.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        secondaryIcon,
                        contentDescription = secondaryContentDescription,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActivityRow(activity: ActivityInfo, packageName: String, onLaunch: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            activity.name.removePrefix(packageName),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        IconButton(
            onClick = { onLaunch(activity.name) },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = stringResource(R.string.launch),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@PreviewScreenSizes
@Composable
fun AppDetailPreview() {
    GeekLabTheme {
        AppDetailContent(
            uiState = AppDetailState(
                packageInfo = PackageInfo().apply {
                    packageName = "com.example.mockapp"
                    requestedPermissions = arrayOf("android.permission.INTERNET", "android.permission.CAMERA")
                    activities = arrayOf(ActivityInfo().apply { name = "com.example.mockapp.MainActivity" })
                    applicationInfo = ApplicationInfo().apply {
                        packageName = "com.example.mockapp"
                        nativeLibraryDir = "/data/app/com.example.mockapp/lib/arm64"
                    }
                },
                label = "Mock Application",
                version = "1.0.0 (Preview)",
                size = "42 MB",
                installTime = "01/01/2024 12:00",
                origin = "Google Play Store",
                services = listOf(ServiceInfo().apply { name = "com.example.mockapp.BackgroundService" }),
                isLoading = false
            ),
            onBackClick = {},
            onLaunchApp = {},
            onShareApk = {},
            onLaunchActivity = {}
        )
    }
}

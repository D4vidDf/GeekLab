package com.daviddf.geeklab.ui.screens.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Expand
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LowPriority
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Source
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.viewmodels.AppDetailState
import com.daviddf.geeklab.ui.viewmodels.AppDetailViewModel

data class PermissionDetail(
    val name: String,
    val colloquialName: String,
    val description: String,
    val docUrl: String
)

data class ActivityDetail(
    val name: String,
    val label: String,
    val icon: android.graphics.Bitmap?,
    val exported: Boolean,
    val enabled: Boolean,
    val processName: String,
    val launchMode: String,
    val orientation: String,
    val theme: String,
    val packageName: String
)

@Composable
fun AppDetailScreen(
    packageName: String,
    onBackClick: () -> Unit,
    onViewManifest: (String) -> Unit,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    showExpandToggle: Boolean = false,
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
        onShareApk = { viewModel.shareApk(packageName, uiState.label, uiState.version) },
        onLaunchActivity = { activityName -> viewModel.launchActivity(packageName, activityName) },
        onViewManifest = { onViewManifest(packageName) },
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand,
        showExpandToggle = showExpandToggle,
        isDetailPane = showExpandToggle && !isExpanded
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDetailContent(
    uiState: AppDetailState,
    onBackClick: () -> Unit,
    onLaunchApp: () -> Unit,
    onShareApk: () -> Unit,
    onLaunchActivity: (String) -> Unit,
    onViewManifest: () -> Unit,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    showExpandToggle: Boolean = false,
    isDetailPane: Boolean = false
) {
    val context = LocalContext.current
    val viewModel: AppDetailViewModel = viewModel()
    var selectedPermission by remember { mutableStateOf<PermissionDetail?>(null) }
    var selectedActivity by remember { mutableStateOf<ActivityDetail?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetType by remember { mutableStateOf("permission") } // "permission" or "activity"

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_info), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (!isDetailPane) {
                        FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (showExpandToggle) {
                        IconButton(onClick = onToggleExpand) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.CloseFullscreen else Icons.Rounded.OpenInFull,
                                contentDescription = if (isExpanded) stringResource(R.string.exit_fullscreen) else stringResource(R.string.expand_fullscreen)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.packageInfo != null) {
            val packageInfo = uiState.packageInfo
            val label = uiState.label
            val icon = uiState.icon?.asImageBitmap()

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf(
                stringResource(R.string.info_tab),
                stringResource(R.string.permissions),
                stringResource(R.string.activities),
                stringResource(R.string.services),
                stringResource(R.string.providers),
                stringResource(R.string.shared_libraries),
                stringResource(R.string.receivers),
                stringResource(R.string.signatures),
                stringResource(R.string.overlays)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(80.dp),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (icon != null) {
                                            Image(
                                                bitmap = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp)
                                            )
                                        } else {
                                            Icon(Icons.Rounded.Apps, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column {
                                    Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                                    Text(packageInfo.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailItem(Icons.Rounded.Numbers, stringResource(R.string.version), uiState.version)
                                DetailItem(Icons.Rounded.SdStorage, stringResource(R.string.size), uiState.size)
                                DetailItem(Icons.Rounded.History, stringResource(R.string.installed), uiState.installTime)
                                DetailItem(Icons.Rounded.Source, stringResource(R.string.origin), uiState.origin)
                            }

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
                }

                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 0.dp
                    ) {
                        SecondaryScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 16.dp,
                            containerColor = MaterialTheme.colorScheme.background,
                            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { 
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    }
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val routes = listOfNotNull(
                                    Icons.Rounded.Folder to (R.string.source_dir to uiState.sourceDir),
                                    Icons.Rounded.Storage to (R.string.data_dir to uiState.dataDir),
                                    uiState.deviceProtectedDataDir?.let { Icons.Rounded.Security to (R.string.device_protected_data_dir to it) },
                                    uiState.nativeLibraryDir?.let { Icons.Rounded.Code to (R.string.jni_library_dir to it) }
                                )
                                val usage = listOf(
                                    Icons.Rounded.FileUpload to (R.string.data_transmitted to uiState.txBytes),
                                    Icons.Rounded.FileDownload to (R.string.data_received to uiState.rxBytes)
                                )
                                val storage = listOf(
                                    Icons.Rounded.Apps to (R.string.app_size to uiState.appSize),
                                    Icons.Rounded.DataObject to (R.string.data_size to uiState.dataSize),
                                    Icons.Rounded.Cached to (R.string.cache_size to uiState.cacheSize),
                                    Icons.Rounded.Summarize to (R.string.total_size to uiState.totalSize)
                                )
                                val moreInfo = listOfNotNull(
                                    Icons.Rounded.AdsClick to (R.string.target_sdk to uiState.targetSdk.toString()),
                                    Icons.Rounded.LowPriority to (R.string.min_sdk to uiState.minSdk.toString()),
                                    Icons.Rounded.Flag to (R.string.flags to uiState.flags.joinToString(", ")),
                                    Icons.Rounded.CalendarMonth to (R.string.last_update_time to uiState.updateTime),
                                    Icons.Rounded.Badge to (R.string.user_id to uiState.uid.toString()),
                                    uiState.sharedUserId?.let { Icons.Rounded.Group to (R.string.shared_user_id to it) },
                                    uiState.primaryCpuAbi?.let { Icons.Rounded.Memory to (R.string.primary_abi to it) },
                                    uiState.selinux?.let { Icons.Rounded.AdminPanelSettings to (R.string.selinux to it) }
                                )

                                InfoSectionTitle(stringResource(R.string.routes_directories))
                                routes.forEachIndexed { index, (icon, info) ->
                                    GroupedDetailItem(icon, stringResource(info.first), info.second, index, routes.size)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                InfoSectionTitle(stringResource(R.string.data_usage_tab))
                                usage.forEachIndexed { index, (icon, info) ->
                                    GroupedDetailItem(icon, stringResource(info.first), info.second, index, usage.size)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                InfoSectionTitle(stringResource(R.string.storage_cache))
                                storage.forEachIndexed { index, (icon, info) ->
                                    GroupedDetailItem(icon, stringResource(info.first), info.second, index, storage.size)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                InfoSectionTitle(stringResource(R.string.more_info))
                                moreInfo.forEachIndexed { index, (icon, info) ->
                                    GroupedDetailItem(icon, stringResource(info.first), info.second, index, moreInfo.size)
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = onViewManifest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Rounded.Code, null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(R.string.view_manifest), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    1 -> {
                        val permissions = uiState.packageInfo.requestedPermissions?.toList() ?: emptyList()
                        if (permissions.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(
                                items = permissions,
                                key = { _, permission -> permission }
                            ) { index, permission ->
                                GroupedSurfaceItem(
                                    index = index,
                                    size = permissions.size,
                                    onClick = {
                                        selectedPermission = getPermissionDetail(permission, context)
                                        bottomSheetType = "permission"
                                        showBottomSheet = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            permission.removePrefix("android.permission."),
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_permissions_requested)) }
                        }
                    }
                    2 -> {
                        val activities = uiState.packageInfo.activities?.toList() ?: emptyList()
                        if (activities.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(
                                items = activities,
                                key = { _, activity -> activity.name }
                            ) { index, activity ->
                                GroupedSurfaceItem(index = index, size = activities.size) {
                                    ActivityRow(
                                        activity = activity, 
                                        packageName = uiState.packageInfo.packageName, 
                                        onClick = {
                                            selectedActivity = getActivityDetail(activity, context)
                                            bottomSheetType = "activity"
                                            showBottomSheet = true
                                        },
                                        onLaunch = { onLaunchActivity(activity.name) }
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_activities_found)) }
                        }
                    }
                    3 -> {
                        val services = uiState.services
                        if (services.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(services) { index, service ->
                                GroupedSurfaceItem(index = index, size = services.size) {
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
                    4 -> {
                        val providers = packageInfo.providers?.toList()
                        if (!providers.isNullOrEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(providers) { index, provider ->
                                GroupedSurfaceItem(index = index, size = providers.size) {
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
                    5 -> {
                        val sharedLibs = uiState.sharedLibraries
                        if (sharedLibs.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(sharedLibs) { index, lib ->
                                GroupedSurfaceItem(index = index, size = sharedLibs.size) {
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
                    6 -> {
                        val receivers = uiState.receivers
                        if (receivers.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(receivers) { index, receiver ->
                                GroupedSurfaceItem(index = index, size = receivers.size) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(
                                            receiver.name.removePrefix(packageInfo.packageName),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        } else {
                            item { EmptyState(stringResource(R.string.no_receivers_found)) }
                        }
                    }
                    7 -> {
                        val signatures = uiState.signatures
                        if (signatures.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            
                            item {
                                InfoSectionTitle(stringResource(R.string.signing_system))
                                if (uiState.isVerified) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = Color(0xFFE8F5E9) // Light green background
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32) // Dark green
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                stringResource(R.string.verified),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }

                            signatures.forEach { sig ->
                                item {
                                    val certItems = listOfNotNull(
                                        Icons.Rounded.Badge to (R.string.subject to sig.subject),
                                        Icons.Rounded.Business to (R.string.issuer to sig.issuer),
                                        Icons.Rounded.EventAvailable to (R.string.issue_date to sig.issueDate),
                                        Icons.Rounded.EventBusy to (R.string.expiry_date to sig.expiryDate),
                                        Icons.Rounded.Fingerprint to (R.string.type to sig.type),
                                        Icons.Rounded.Layers to (R.string.version_label to sig.version),
                                        Icons.Rounded.Numbers to (R.string.serial_number to sig.serialNumber)
                                    )
                                    val checksums = listOfNotNull(
                                        "MD5" to sig.md5,
                                        "SHA-1" to sig.sha1,
                                        "SHA-256" to sig.sha256,
                                        "SHA-384" to sig.sha384,
                                        "SHA-512" to sig.sha512
                                    )
                                    val signatureData = listOfNotNull(
                                        Icons.Rounded.Code to (R.string.algorithm to sig.algorithm),
                                        Icons.Rounded.Key to (R.string.oid to sig.oid),
                                        Icons.Rounded.Gesture to (R.string.signature_label to sig.signature)
                                    )
                                    val publicKeyData = listOfNotNull(
                                        Icons.Rounded.Hardware to (R.string.algorithm to sig.publicKeyAlgorithm),
                                        Icons.Rounded.Code to (R.string.format to sig.publicKeyFormat),
                                        Icons.Rounded.Expand to (R.string.exponent to sig.exponent),
                                        Icons.Rounded.FitScreen to (R.string.modulus to sig.modulus)
                                    )

                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        InfoSectionTitle(stringResource(R.string.signatures))
                                        certItems.forEachIndexed { index, (icon, pair) ->
                                            pair.second?.let { GroupedDetailItem(icon, stringResource(pair.first), it, index, certItems.size) }
                                        }

                                        val filteredChecksums = checksums.filter { it.second != null }
                                        if (filteredChecksums.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InfoSectionTitle(stringResource(R.string.checksums))
                                            filteredChecksums.forEachIndexed { index, (label, value) ->
                                                GroupedDetailItem(Icons.Rounded.Security, label, value!!, index, filteredChecksums.size)
                                            }
                                        }

                                        val filteredSigData = signatureData.filter { it.second.second != null }
                                        if (filteredSigData.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InfoSectionTitle(stringResource(R.string.signature_label))
                                            filteredSigData.forEachIndexed { index, (icon, pair) ->
                                                GroupedDetailItem(icon, stringResource(pair.first), pair.second!!, index, filteredSigData.size)
                                            }
                                        }

                                        val filteredPubKeyData = publicKeyData.filter { it.second.second != null }
                                        if (filteredPubKeyData.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InfoSectionTitle(stringResource(R.string.public_key))
                                            filteredPubKeyData.forEachIndexed { index, (icon, pair) ->
                                                GroupedDetailItem(icon, stringResource(pair.first), pair.second!!, index, filteredPubKeyData.size)
                                            }
                                        }

                                        if (sig.extensions.isNotEmpty()) {
                                            val extensionsList = sig.extensions.toList()
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InfoSectionTitle(stringResource(R.string.non_critical_extensions))
                                            extensionsList.forEachIndexed { index, (oid, value) ->
                                                GroupedDetailItem(Icons.Rounded.Extension, oid, value, index, extensionsList.size)
                                            }
                                        }
                                    }
                                }
                                
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        } else {
                            item { EmptyState(stringResource(R.string.no_signatures_found)) }
                        }
                    }
                    8 -> {
                        if (uiState.overlayTarget != null) {
                            item {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    InfoSectionTitle(stringResource(R.string.overlays))
                                    GroupedDetailItem(
                                        icon = Icons.Rounded.Layers,
                                        label = stringResource(R.string.target_package),
                                        value = uiState.overlayTarget,
                                        index = 0,
                                        size = 1
                                    )
                                }
                            }
                        } else {
                            item { EmptyState(stringResource(R.string.no_overlays_found)) }
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.app_not_found))
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                if (bottomSheetType == "permission" && selectedPermission != null) {
                    PermissionBottomSheetContent(
                        detail = selectedPermission!!,
                        onOpenDoc = {
                            val intent = Intent(Intent.ACTION_VIEW, selectedPermission!!.docUrl.toUri())
                            context.startActivity(intent)
                        }
                    )
                } else if (bottomSheetType == "activity" && selectedActivity != null) {
                    ActivityBottomSheetContent(
                        detail = selectedActivity!!,
                        onLaunch = { onLaunchActivity(selectedActivity!!.name) },
                        onAddShortcut = { name, icon ->
                            viewModel.createShortcut(
                                packageName = selectedActivity!!.packageName,
                                className = selectedActivity!!.name,
                                label = name,
                                icon = icon
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityBottomSheetContent(
    detail: ActivityDetail,
    onLaunch: () -> Unit,
    onAddShortcut: (String, android.graphics.Bitmap) -> Unit
) {
    var shortcutName by remember { mutableStateOf(detail.label) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 48.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Layers,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                stringResource(R.string.activity_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val bitmap = detail.icon?.asImageBitmap()
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    detail.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    detail.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoSectionTitle(stringResource(R.string.technical_info))
            DetailItem(Icons.Rounded.Output, stringResource(R.string.exported), detail.exported.toString())
            DetailItem(Icons.Rounded.CheckCircle, stringResource(R.string.enabled), detail.enabled.toString())
            DetailItem(Icons.Rounded.Settings, stringResource(R.string.launch_mode), detail.launchMode)
            DetailItem(Icons.Rounded.ScreenRotation, stringResource(R.string.orientation), detail.orientation)
            DetailItem(Icons.Rounded.Palette, stringResource(R.string.theme), detail.theme)
            DetailItem(Icons.Rounded.Memory, stringResource(R.string.process), detail.processName)
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.personalize_shortcut),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = shortcutName,
                onValueChange = { shortcutName = it },
                label = { Text(stringResource(R.string.shortcut_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SplitButton(
                    onPrimaryClick = { 
                        if (detail.icon != null) {
                            onAddShortcut(shortcutName, detail.icon)
                        }
                    },
                    onSecondaryClick = onLaunch,
                    primaryText = stringResource(R.string.add_to_home),
                    primaryIcon = Icons.Rounded.Add,
                    secondaryIcon = Icons.Rounded.PlayArrow,
                    secondaryContentDescription = stringResource(R.string.launch)
                )
            }
        }
    }
}

fun getActivityDetail(activity: ActivityInfo, context: Context): ActivityDetail {
    val pm = context.packageManager
    val label = activity.loadLabel(pm).toString()
    val icon = activity.loadIcon(pm).toBitmap()
    
    val launchMode = when (activity.launchMode) {
        ActivityInfo.LAUNCH_MULTIPLE -> context.getString(R.string.launch_mode_standard)
        ActivityInfo.LAUNCH_SINGLE_TOP -> context.getString(R.string.launch_mode_single_top)
        ActivityInfo.LAUNCH_SINGLE_TASK -> context.getString(R.string.launch_mode_single_task)
        ActivityInfo.LAUNCH_SINGLE_INSTANCE -> context.getString(R.string.launch_mode_single_instance)
        else -> context.getString(R.string.unknown)
    }

    val orientation = when (activity.screenOrientation) {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> context.getString(R.string.orientation_unspecified)
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> context.getString(R.string.orientation_landscape)
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> context.getString(R.string.orientation_portrait)
        ActivityInfo.SCREEN_ORIENTATION_USER -> context.getString(R.string.orientation_user)
        ActivityInfo.SCREEN_ORIENTATION_BEHIND -> context.getString(R.string.orientation_behind)
        ActivityInfo.SCREEN_ORIENTATION_SENSOR -> context.getString(R.string.orientation_sensor)
        ActivityInfo.SCREEN_ORIENTATION_NOSENSOR -> context.getString(R.string.orientation_nosensor)
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> context.getString(R.string.orientation_sensor_landscape)
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> context.getString(R.string.orientation_sensor_portrait)
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> context.getString(R.string.orientation_reverse_landscape)
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> context.getString(R.string.orientation_reverse_portrait)
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> context.getString(R.string.orientation_full_sensor)
        else -> context.getString(R.string.activity_other)
    }

    return ActivityDetail(
        name = activity.name,
        label = label,
        icon = icon,
        exported = activity.exported,
        enabled = activity.enabled,
        processName = activity.processName,
        launchMode = launchMode,
        orientation = orientation,
        theme = if (activity.theme != 0) "0x${activity.theme.toString(16)}" else context.getString(R.string.activity_default_theme),
        packageName = activity.packageName
    )
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
    } catch (_: Exception) {
        permission.split(".").last().replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    val description = try {
        val info = pm.getPermissionInfo(permission, 0)
        info.loadDescription(pm)?.toString() ?: context.getString(R.string.no_description_available)
    } catch (_: Exception) {
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
fun GroupedDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    index: Int,
    size: Int
) {
    val shape = getGroupedShape(index, size)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding( vertical = 1.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            DetailItem(icon, label, value)
        }
    }
}

@Composable
fun GroupedSurfaceItem(
    index: Int,
    size: Int,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = getGroupedShape(index, size)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding( vertical = 2.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick ?: {}
    ) {
        content()
    }
}

private fun getGroupedShape(index: Int, size: Int): RoundedCornerShape {
    val cornerLarge = 24.dp
    val cornerSmall = 4.dp
    return when {
        size == 1 -> RoundedCornerShape(cornerLarge)
        index == 0 -> RoundedCornerShape(topStart = cornerLarge, topEnd = cornerLarge, bottomStart = cornerSmall, bottomEnd = cornerSmall)
        index == size - 1 -> RoundedCornerShape(topStart = cornerSmall, topEnd = cornerSmall, bottomStart = cornerLarge, bottomEnd = cornerLarge)
        else -> RoundedCornerShape(cornerSmall)
    }
}

@Composable
fun InfoSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding( end = 24.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ActivityRow(
    activity: ActivityInfo, 
    packageName: String, 
    onClick: () -> Unit,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val label = remember(activity) { activity.loadLabel(pm).toString() }
    val icon = remember(activity) { 
        activity.loadIcon(pm).toBitmap().asImageBitmap() 
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = activity.name.removePrefix(packageName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onLaunch) {
            Icon(
                Icons.Rounded.PlayArrow, 
                contentDescription = stringResource(R.string.launch),
                tint = MaterialTheme.colorScheme.primary
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
                    packageName = "com.daviddf.geeklab"
                    requestedPermissions = arrayOf("android.permission.INTERNET", "android.permission.CAMERA")
                    activities = arrayOf(ActivityInfo().apply { name = "com.daviddf.geeklab.MainActivity" })
                    applicationInfo = android.content.pm.ApplicationInfo().apply {
                        packageName = "com.daviddf.geeklab"
                        nativeLibraryDir = "/data/app/com.daviddf.geeklab/lib/arm64"
                    }
                },
                label = "GeekLab Technical",
                version = "2.4.0 (Stable)",
                size = "64.2 MB",
                installTime = "15/05/2024 14:30",
                origin = "Google Play Store",
                services = listOf(android.content.pm.ServiceInfo().apply { name = "com.daviddf.geeklab.InspectorService" }),
                sourceDir = "/data/app/~~base.apk",
                dataDir = "/data/user/0/com.daviddf.geeklab",
                targetSdk = 35,
                minSdk = 26,
                isLoading = false
            ),
            onBackClick = {},
            onLaunchApp = {},
            onShareApk = {},
            onLaunchActivity = {},
            onViewManifest = {}
        )
    }
}

package com.daviddf.geeklab.ui.screens.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
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
import androidx.compose.ui.graphics.Color
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
import com.daviddf.geeklab.ui.viewmodels.AppDetailState
import com.daviddf.geeklab.ui.viewmodels.AppDetailViewModel

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
    var selectedPermission by remember { mutableStateOf<PermissionDetail?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_info)) },
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
                                contentDescription = if (isExpanded) "Exit Fullscreen" else "Expand to Fullscreen"
                            )
                        }
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
                    0 -> {
                        val routes = listOfNotNull(
                            R.string.source_dir to uiState.sourceDir,
                            R.string.data_dir to uiState.dataDir,
                            uiState.deviceProtectedDataDir?.let { R.string.device_protected_data_dir to it },
                            uiState.nativeLibraryDir?.let { R.string.jni_library_dir to it }
                        )
                        val usage = listOf(
                            R.string.data_transmitted to uiState.txBytes,
                            R.string.data_received to uiState.rxBytes
                        )
                        val storage = listOf(
                            R.string.app_size to uiState.appSize,
                            R.string.data_size to uiState.dataSize,
                            R.string.cache_size to uiState.cacheSize,
                            R.string.total_size to uiState.totalSize
                        )
                        val moreInfo = listOfNotNull(
                            R.string.target_sdk to uiState.targetSdk.toString(),
                            R.string.min_sdk to uiState.minSdk.toString(),
                            R.string.flags to uiState.flags.joinToString(", "),
                            R.string.installed to uiState.installTime,
                            R.string.last_update_time to uiState.updateTime,
                            R.string.user_id to uiState.uid.toString(),
                            uiState.sharedUserId?.let { R.string.shared_user_id to it },
                            uiState.primaryCpuAbi?.let { R.string.primary_abi to it },
                            uiState.selinux?.let { R.string.selinux to it }
                        )

                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // Routes \u0026 Directories
                        item { InfoSectionTitle(stringResource(R.string.routes_directories)) }
                        itemsIndexed(routes) { index, (labelRes, value) ->
                            InfoItem(stringResource(labelRes), value, index, routes.size)
                        }

                        // Data Usage
                        item { InfoSectionTitle(stringResource(R.string.data_usage_tab)) }
                        itemsIndexed(usage) { index, (labelRes, value) ->
                            InfoItem(stringResource(labelRes), value, index, usage.size)
                        }

                        // Storage
                        item { InfoSectionTitle(stringResource(R.string.storage_cache)) }
                        itemsIndexed(storage) { index, (labelRes, value) ->
                            InfoItem(stringResource(labelRes), value, index, storage.size)
                        }

                        // More Info
                        item { InfoSectionTitle(stringResource(R.string.more_info)) }
                        itemsIndexed(moreInfo) { index, (labelRes, value) ->
                            InfoItem(stringResource(labelRes), value, index, moreInfo.size)
                        }

                        item {
                            Button(
                                onClick = onViewManifest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(stringResource(R.string.view_manifest))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    1 -> {
                        val permissions = uiState.packageInfo.requestedPermissions?.toList() ?: emptyList()
                        if (permissions.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(
                                items = permissions,
                                key = { _, permission -> permission }
                            ) { index, permission ->
                                val shape = remember(index, permissions.size) { getShape(index, permissions.size) }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    onClick = {
                                        selectedPermission = getPermissionDetail(permission, context)
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
                                val shape = remember(index, activities.size) { getShape(index, activities.size) }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    ActivityRow(activity, uiState.packageInfo.packageName, onLaunchActivity)
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
                    4 -> {
                        val providers = packageInfo.providers?.toList()
                        if (!providers.isNullOrEmpty()) {
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
                    5 -> {
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
                    6 -> {
                        val receivers = uiState.receivers
                        if (receivers.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            itemsIndexed(receivers) { index, receiver ->
                                val shape = getShape(index, receivers.size)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 3.dp),
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
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
                                            .padding(horizontal = 16.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(24.dp),
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
                                item { InfoSectionTitle(stringResource(R.string.signatures)) }
                                
                                val certItems = listOfNotNull(
                                    sig.subject?.let { R.string.subject to it },
                                    sig.issuer?.let { R.string.issuer to it },
                                    sig.issueDate?.let { R.string.issue_date to it },
                                    sig.expiryDate?.let { R.string.expiry_date to it },
                                    sig.type?.let { R.string.type to it },
                                    sig.version?.let { R.string.version_label to it },
                                    sig.serialNumber?.let { R.string.serial_number to it }
                                )
                                itemsIndexed(certItems) { index, (labelRes, value) ->
                                    InfoItem(stringResource(labelRes), value, index, certItems.size)
                                }

                                val checksums = listOfNotNull(
                                    sig.md5?.let { "MD5" to it },
                                    sig.sha1?.let { "SHA-1" to it },
                                    sig.sha256?.let { "SHA-256" to it },
                                    sig.sha384?.let { "SHA-384" to it },
                                    sig.sha512?.let { "SHA-512" to it }
                                )
                                if (checksums.isNotEmpty()) {
                                    item { InfoSectionTitle(stringResource(R.string.checksums)) }
                                    itemsIndexed(checksums) { index, (label, value) ->
                                        InfoItem(label, value, index, checksums.size)
                                    }
                                }

                                val signatureData = listOfNotNull(
                                    sig.algorithm?.let { R.string.algorithm to it },
                                    sig.oid?.let { R.string.oid to it },
                                    sig.signature?.let { R.string.signature_label to it }
                                )
                                if (signatureData.isNotEmpty()) {
                                    item { InfoSectionTitle(stringResource(R.string.signature_label)) }
                                    itemsIndexed(signatureData) { index, (labelRes, value) ->
                                        InfoItem(stringResource(labelRes), value, index, signatureData.size)
                                    }
                                }

                                val publicKeyData = listOfNotNull(
                                    sig.publicKeyAlgorithm?.let { R.string.algorithm to it },
                                    sig.publicKeyFormat?.let { R.string.format to it },
                                    sig.exponent?.let { R.string.exponent to it },
                                    sig.modulus?.let { R.string.modulus to it }
                                )
                                if (publicKeyData.isNotEmpty()) {
                                    item { InfoSectionTitle(stringResource(R.string.public_key)) }
                                    itemsIndexed(publicKeyData) { index, (labelRes, value) ->
                                        InfoItem(stringResource(labelRes), value, index, publicKeyData.size)
                                    }
                                }

                                if (sig.extensions.isNotEmpty()) {
                                    item { InfoSectionTitle(stringResource(R.string.non_critical_extensions)) }
                                    val extensionsList = sig.extensions.toList()
                                    itemsIndexed(extensionsList) { index, (oid, value) ->
                                        InfoItem(oid, value, index, extensionsList.size)
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
                                InfoSectionTitle(stringResource(R.string.overlays))
                                InfoItem(
                                    label = "Target Package",
                                    value = uiState.overlayTarget,
                                    index = 0,
                                    size = 1
                                )
                            }
                        } else {
                            item { EmptyState(stringResource(R.string.no_overlays_found)) }
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
fun InfoItem(
    label: String,
    value: String,
    index: Int,
    size: Int
) {
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
                    applicationInfo = android.content.pm.ApplicationInfo().apply {
                        packageName = "com.example.mockapp"
                        nativeLibraryDir = "/data/app/com.example.mockapp/lib/arm64"
                    }
                },
                label = "Mock Application",
                version = "1.0.0 (Preview)",
                size = "42 MB",
                installTime = "01/01/2024 12:00",
                origin = "Google Play Store",
                services = listOf(android.content.pm.ServiceInfo().apply { name = "com.example.mockapp.BackgroundService" }),
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

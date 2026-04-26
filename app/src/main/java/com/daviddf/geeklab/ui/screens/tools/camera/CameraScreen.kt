package com.daviddf.geeklab.ui.screens.tools.camera

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLocale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onTryCameraXClick: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCameraInfo(context)
    }

    CameraScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTryCameraXClick = onTryCameraXClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenContent(
    uiState: CameraUiState,
    onBackClick: () -> Unit,
    onTryCameraXClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.camera_info_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.camera_info_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            Button(
                onClick = onTryCameraXClick,
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.camera_try_camerax),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                uiState.cameras.forEach { camera ->
                    CameraSensorCard(camera)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CameraSensorCard(camera: CameraInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (camera.isPhysical) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val facingText = stringResource(camera.facingResId)
                    Icon(
                        imageVector = if (facingText == stringResource(R.string.camera_facing_back)) Icons.Rounded.CameraRear else Icons.Rounded.CameraFront,
                        contentDescription = null,
                        tint = if (camera.isPhysical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${stringResource(R.string.camera_sensor_info)} (ID: ${camera.id})",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (camera.isPhysical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = (if (camera.isPhysical) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer).copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (camera.isPhysical) stringResource(R.string.camera_tag_physical) else stringResource(R.string.camera_tag_logical),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CameraInfoItem(stringResource(R.string.camera_facing), stringResource(camera.facingResId), Icons.Rounded.FlipCameraAndroid)
            CameraInfoItem(stringResource(R.string.camera_hardware_level), stringResource(camera.hardwareLevelResId), Icons.Rounded.DeveloperBoard)
            
            val locale = LocalLocale.current.platformLocale
            val binnedText = camera.megapixels?.let { 
                String.format(locale, "%.1f MP (%s)", it, camera.resolution)
            } ?: camera.resolution
            CameraInfoItem(stringResource(R.string.camera_resolution), binnedText, Icons.Rounded.AspectRatio)

            camera.physicalMegapixels?.let { pMP ->
                // Only show if it's actually different from the binned resolution
                if (camera.megapixels == null || Math.abs(pMP - camera.megapixels) > 0.5) {
                    val pText = String.format(locale, "%.1f MP (%s)", pMP, camera.physicalResolution ?: "")
                    CameraInfoItem(stringResource(R.string.camera_physical_resolution), pText, Icons.Rounded.GridGoldenratio)
                }
            }

            camera.sensorSize?.let { 
                CameraInfoItem(stringResource(R.string.camera_sensor_size), "${it.width} x ${it.height} mm", Icons.Rounded.Straighten)
            }
            if (camera.focalLengths.isNotEmpty()) {
                CameraInfoItem(stringResource(R.string.camera_focal_length), "${camera.focalLengths.joinToString(", ")} mm", Icons.Rounded.Lens)
            }
            camera.aperture?.let {
                CameraInfoItem(stringResource(R.string.camera_aperture), "f/$it", Icons.Rounded.Camera)
            }
            CameraInfoItem(stringResource(R.string.camera_iso_range), camera.isoRange, Icons.Rounded.BrightnessMedium)
            CameraInfoItem(stringResource(R.string.camera_flash), if (camera.hasFlash) stringResource(R.string.yes) else stringResource(R.string.no), Icons.Rounded.FlashOn)
            
            if (camera.capabilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.camera_capabilities),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                camera.capabilities.forEach { capability ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(capability, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraInfoItem(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    MaterialTheme {
        CameraScreenContent(
            uiState = CameraUiState(
                isLoading = false,
                cameras = listOf(
                    CameraInfo(
                        id = "0",
                        facingResId = R.string.camera_facing_back,
                        sensorSize = android.util.SizeF(6.4f, 4.8f),
                        resolution = "4032x3024",
                        megapixels = 12.2,
                        physicalResolution = "8192x6144",
                        physicalMegapixels = 50.3,
                        focalLengths = listOf(4.25f),
                        aperture = 1.8f,
                        isoRange = "100 - 3200",
                        hasFlash = true,
                        capabilities = listOf("RAW", "Manual Sensor", "Backward Compatible"),
                        hardwareLevelResId = R.string.camera_level_level3
                    ),
                    CameraInfo(
                        id = "1",
                        facingResId = R.string.camera_facing_front,
                        sensorSize = android.util.SizeF(3.2f, 2.4f),
                        resolution = "3264x2448",
                        megapixels = 8.0,
                        physicalResolution = "3264x2448",
                        physicalMegapixels = 8.0,
                        focalLengths = listOf(3.1f),
                        aperture = 2.2f,
                        isoRange = "100 - 1600",
                        hasFlash = false,
                        capabilities = listOf("Backward Compatible"),
                        hardwareLevelResId = R.string.camera_level_full
                    )
                )
            ),
            onBackClick = {},
            onTryCameraXClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CameraSensorCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CameraSensorCard(
                camera = CameraInfo(
                    id = "0",
                    facingResId = R.string.camera_facing_back,
                    sensorSize = android.util.SizeF(6.4f, 4.8f),
                    resolution = "4000x3000",
                    megapixels = 12.0,
                    physicalResolution = "8000x6000",
                    physicalMegapixels = 48.0,
                    focalLengths = listOf(4.25f, 26f),
                    aperture = 1.8f,
                    isoRange = "100 - 3200",
                    hasFlash = true,
                    capabilities = listOf("RAW", "Manual Sensor", "Logical Multi-camera"),
                    hardwareLevelResId = R.string.camera_level_full
                )
            )
        }
    }
}

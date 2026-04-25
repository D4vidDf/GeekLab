package com.daviddf.geeklab.ui.screens.tools.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.extensions.ExtensionMode
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.FilterBAndW
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.HdrOn
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Portrait
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraXScreen(
    onBackClick: () -> Unit,
    viewModel: CameraXViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    var permissionsGranted by remember {
        mutableStateOf(
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsGranted = permissions.values.all { it }
        }
    )

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.camera_pro_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (permissionsGranted) {
            CameraXContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onSetupCamera = { previewView -> 
                    viewModel.setupCamera(context, lifecycleOwner, previewView)
                },
                onToggleCamera = { previewView ->
                    viewModel.toggleCamera(lifecycleOwner, previewView)
                },
                onZoomChange = viewModel::setZoom,
                onEffectChange = { effect, previewView -> 
                    viewModel.setEffect(effect, lifecycleOwner, previewView)
                },
                onExtensionChange = { mode, previewView ->
                    viewModel.setExtension(mode, lifecycleOwner, previewView)
                },
                onAspectRatioChange = { ratio, previewView ->
                    viewModel.setAspectRatio(ratio, lifecycleOwner, previewView)
                },
                onCapture = { viewModel.capturePhoto(context) },
                onToggleRecording = { viewModel.toggleRecording(context) },
                onCycleEffect = { previewView -> 
                    viewModel.cycleEffect(lifecycleOwner, previewView)
                },
                onModeChange = viewModel::setCameraMode
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Camera, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.camera_permission_required),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }) {
                        Text(stringResource(R.string.camera_grant_permission))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraXContent(
    modifier: Modifier = Modifier,
    uiState: CameraXUiState,
    onSetupCamera: (PreviewView) -> Unit,
    onToggleCamera: (PreviewView) -> Unit,
    onZoomChange: (Float) -> Unit,
    onEffectChange: (Int, PreviewView) -> Unit,
    onExtensionChange: (Int, PreviewView) -> Unit,
    onAspectRatioChange: (Int, PreviewView) -> Unit,
    onCapture: () -> Unit,
    onToggleRecording: () -> Unit,
    onCycleEffect: (PreviewView) -> Unit,
    onModeChange: (CameraMode) -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        onSetupCamera(previewView)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val rotation by animateFloatAsState(
            targetValue = if (isLandscape) -90f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "uiRotation"
        )

        // Immersive Preview
        AndroidView(
            factory = { 
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }
            },
            update = { view ->
                view.scaleType = PreviewView.ScaleType.FIT_CENTER
            },
            modifier = Modifier.fillMaxSize()
        )

        // Right Side Zoom Slider (Expressive Vertical)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .height(320.dp)
                .width(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { onZoomChange((uiState.zoomRatio + 0.5f).coerceAtMost(uiState.maxZoom)) }) {
                Icon(
                    Icons.Rounded.ZoomIn, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = rotation }
                )
            }
            
            val sliderState = remember(uiState.minZoom, uiState.maxZoom) {
                SliderState(
                    value = uiState.zoomRatio,
                    valueRange = uiState.minZoom..uiState.maxZoom.coerceAtLeast(uiState.minZoom + 0.1f)
                )
            }
            
            LaunchedEffect(uiState.zoomRatio) {
                sliderState.value = uiState.zoomRatio
            }
            
            LaunchedEffect(sliderState.value) {
                onZoomChange(sliderState.value)
            }

            VerticalSlider(
                state = sliderState,
                reverseDirection = true,
                modifier = Modifier
                    .weight(1f)
                    .width(40.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )

            IconButton(onClick = { onZoomChange((uiState.zoomRatio - 0.5f).coerceAtLeast(uiState.minZoom)) }) {
                Icon(
                    Icons.Rounded.ZoomOut, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = rotation }
                )
            }
        }

        // Bottom Controls Container (Glassmorphic)
        Column(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
                .then(
                    if (isLandscape) Modifier.width(320.dp).fillMaxHeight()
                    else Modifier.fillMaxWidth()
                )
                .background(
                    if (isLandscape) {
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    }
                )
                .padding(if (isLandscape) 16.dp else 0.dp)
                .navigationBarsPadding()
                .padding(bottom = if (isLandscape) 0.dp else 24.dp, start = if (isLandscape) 24.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Re-orient content for landscape if needed
            val contentModifier = Modifier.graphicsLayer { rotationZ = rotation }

            // Mode Selector (Expressive Toggles)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CameraModeTab(
                    selected = uiState.cameraMode == CameraMode.PHOTO,
                    onClick = { onModeChange(CameraMode.PHOTO) },
                    label = stringResource(R.string.camera_mode_photo),
                    modifier = contentModifier
                )
                CameraModeTab(
                    selected = uiState.cameraMode == CameraMode.VIDEO,
                    onClick = { onModeChange(CameraMode.VIDEO) },
                    label = stringResource(R.string.camera_mode_video),
                    modifier = contentModifier
                )
            }

            // Extension & Ratio Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    ExpressiveRatioChip(
                        selected = uiState.aspectRatio == AspectRatio.RATIO_4_3,
                        onClick = { onAspectRatioChange(AspectRatio.RATIO_4_3, previewView) },
                        label = "4:3",
                        modifier = contentModifier
                    )
                }
                item {
                    ExpressiveRatioChip(
                        selected = uiState.aspectRatio == AspectRatio.RATIO_16_9,
                        onClick = { onAspectRatioChange(AspectRatio.RATIO_16_9, previewView) },
                        label = "16:9",
                        modifier = contentModifier
                    )
                }
                
                item { Spacer(modifier = Modifier.width(4.dp)) }
                
                item {
                    ExpressiveExtensionChip(
                        selected = uiState.extensionMode == ExtensionMode.NONE,
                        onClick = { onExtensionChange(ExtensionMode.NONE, previewView) },
                        label = stringResource(R.string.camera_extension_normal),
                        icon = Icons.Rounded.AutoAwesomeMotion,
                        modifier = contentModifier
                    )
                }

                item {
                    ExpressiveExtensionChip(
                        selected = uiState.extensionMode == ExtensionMode.HDR,
                        onClick = { onExtensionChange(ExtensionMode.HDR, previewView) },
                        label = stringResource(R.string.camera_extension_hdr),
                        icon = Icons.Rounded.HdrOn,
                        modifier = contentModifier
                    )
                }

                item {
                    ExpressiveExtensionChip(
                        selected = uiState.extensionMode == ExtensionMode.BOKEH,
                        onClick = { onExtensionChange(ExtensionMode.BOKEH, previewView) },
                        label = stringResource(R.string.camera_extension_portrait),
                        icon = Icons.Rounded.Portrait,
                        modifier = contentModifier
                    )
                }
                
                if (uiState.supportedExtensions.isNotEmpty()) {
                    items(uiState.supportedExtensions.filter { it != ExtensionMode.HDR && it != ExtensionMode.BOKEH }) { mode ->
                        ExpressiveExtensionChip(
                            selected = uiState.extensionMode == mode,
                            onClick = { onExtensionChange(mode, previewView) },
                            label = when(mode) {
                                ExtensionMode.AUTO -> stringResource(R.string.camera_extension_auto)
                                ExtensionMode.NIGHT -> stringResource(R.string.camera_extension_night)
                                ExtensionMode.FACE_RETOUCH -> stringResource(R.string.camera_extension_retouch)
                                else -> "Ext"
                            },
                            icon = when(mode) {
                                ExtensionMode.NIGHT -> Icons.Rounded.Nightlight
                                else -> Icons.Rounded.AutoAwesome
                            },
                            modifier = contentModifier
                        )
                    }
                }
            }

            // Zoom Indicator
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = contentModifier
            ) {
                Text(
                    text = String.format(LocalLocale.current.platformLocale, "%.1fx", uiState.zoomRatio),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Main Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveSmallAction(
                    icon = Icons.Rounded.FlipCameraAndroid,
                    onClick = { onToggleCamera(previewView) },
                    modifier = contentModifier
                )

                ExpressiveCaptureButton(
                    isCapturing = uiState.isCapturing,
                    isRecording = uiState.isRecording,
                    onClick = {
                        if (uiState.cameraMode == CameraMode.PHOTO) onCapture()
                        else onToggleRecording()
                    },
                    onLongClick = {
                        if (uiState.cameraMode == CameraMode.PHOTO) onToggleRecording()
                    },
                    modifier = contentModifier
                )

                ExpressiveSmallAction(
                    icon = Icons.Rounded.FilterBAndW,
                    onClick = { onCycleEffect(previewView) },
                    modifier = contentModifier
                )
            }
            
            // Mode Indicator
            Text(
                text = if (uiState.isRecording) stringResource(R.string.camera_status_recording) else if (uiState.cameraMode == CameraMode.VIDEO) stringResource(R.string.camera_mode_video) else stringResource(R.string.camera_mode_photo),
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.isRecording) Color.Red else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = contentModifier
            )
        }
    }
}

@Composable
fun CameraModeTab(
    selected: Boolean, 
    onClick: () -> Unit, 
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tabBackground"
    )
    val contentColor by animateColorAsState(
        if (selected) Color.White else Color.White.copy(alpha = 0.6f),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpressiveCaptureButton(
    isCapturing: Boolean,
    isRecording: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val outerRingColor by animateColorAsState(
        targetValue = if (isRecording) Color.Red else Color.White,
        animationSpec = tween(500),
        label = "outerRingColor"
    )

    Box(
        modifier = modifier
            .size(92.dp)
            .scale(scale)
            .border(4.dp, outerRingColor.copy(alpha = 0.5f), CircleShape)
            .padding(8.dp)
            .clip(CircleShape)
            .background(if (isRecording) Color.Red else Color.White)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCapturing) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        } else if (isRecording) {
            Box(modifier = Modifier.size(24.dp).background(Color.White, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun ExpressiveSmallAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ExpressiveRatioChip(
    selected: Boolean, 
    onClick: () -> Unit, 
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.4f),
        contentColor = Color.White,
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpressiveExtensionChip(
    selected: Boolean, 
    onClick: () -> Unit, 
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.tertiary else Color.Black.copy(alpha = 0.4f),
        contentColor = Color.White,
        modifier = modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraXScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            CameraXContent(
                uiState = CameraXUiState(
                    zoomRatio = 1.0f,
                    minZoom = 1.0f,
                    maxZoom = 10.0f,
                    cameraMode = CameraMode.PHOTO,
                    supportedExtensions = listOf(
                    ExtensionMode.AUTO,
                    ExtensionMode.HDR,
                    ExtensionMode.NIGHT,
                    ExtensionMode.BOKEH
                )
                ),
                onSetupCamera = {},
                onToggleCamera = {},
                onZoomChange = {},
                onEffectChange = { _, _ -> },
                onExtensionChange = { _, _ -> },
                onAspectRatioChange = { _, _ -> },
                onCapture = {},
                onToggleRecording = {},
                onCycleEffect = {},
                onModeChange = {}
            )
        }
    }
}

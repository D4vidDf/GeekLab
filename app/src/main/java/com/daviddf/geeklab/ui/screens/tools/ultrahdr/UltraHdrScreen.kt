package com.daviddf.geeklab.ui.screens.tools.ultrahdr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Exposure
import androidx.compose.material.icons.rounded.HdrOff
import androidx.compose.material.icons.rounded.HdrOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.screens.apps.InfoSectionTitle
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import kotlin.math.ln

// AGSL Shader code for Ultra HDR simulation based on Android platform samples
private const val ULTRAHDR_SHADER = """
    uniform shader image;
    uniform shader gainmap;
    uniform float3 logRatioMin;
    uniform float3 logRatioMax;
    uniform float3 gamma;
    uniform float3 epsilonSdr;
    uniform float3 epsilonHdr;
    uniform float weight;

    half4 main(float2 fragCoord) {
        float4 sdrColor = image.eval(fragCoord);
        float4 g = gainmap.eval(fragCoord);
        
        // Simplified Ultra HDR application math
        float3 res;
        for (int i = 0; i < 3; i++) {
            float g_val = pow(g[i], gamma[i]);
            float log_m = mix(logRatioMin[i], logRatioMax[i], g_val);
            res[i] = (sdrColor[i] + epsilonSdr[i]) * exp(weight * log_m) - epsilonHdr[i];
        }
        
        return half4(res, sdrColor.a);
    }
"""

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraHdrScreen(
    onBackClick: () -> Unit,
    viewModel: UltraHdrViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImageFromUri(it) }
    }

    // Enable HDR mode for the window if a gainmap is detected and preview is enabled
    LaunchedEffect(uiState.hasGainmap, uiState.isHdrPreviewEnabled, uiState.viewMode, activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val window = activity?.window
            if (window != null) {
                // Toggle Hardware HDR Mode
                val targetMode = if (uiState.hasGainmap && uiState.isHdrPreviewEnabled && uiState.viewMode == UltraHdrViewMode.HDR) {
                    ActivityInfo.COLOR_MODE_HDR
                } else {
                    ActivityInfo.COLOR_MODE_DEFAULT
                }
                
                if (window.colorMode != targetMode) {
                    window.colorMode = targetMode
                }

                // If in HDR mode, increase brightness to help visualize the effect
                val layoutParams = window.attributes
                if (targetMode == ActivityInfo.COLOR_MODE_HDR) {
                    layoutParams.screenBrightness = 1.0f
                } else {
                    layoutParams.screenBrightness = -1.0f // Reset to system default
                }
                window.attributes = layoutParams
            }
        }
    }

    // Ensure reset when leaving the screen
    DisposableEffect(activity) {
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val window = activity?.window
                if (window != null) {
                    window.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                    val layoutParams = window.attributes
                    layoutParams.screenBrightness = -1.0f
                    window.attributes = layoutParams
                }
            }
        }
    }

    UltraHdrContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onPickImage = { launcher.launch("image/*") },
        onLoadExample = { viewModel.loadExampleImage() },
        onToggleHdr = { viewModel.toggleHdrPreview(it) },
        onViewModeChange = { viewModel.setViewMode(it) },
        onSimulationRatioChange = { viewModel.setSimulationRatio(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraHdrContent(
    uiState: UltraHdrState,
    onBackClick: () -> Unit,
    onPickImage: () -> Unit,
    onLoadExample: () -> Unit,
    onToggleHdr: (Boolean) -> Unit,
    onViewModeChange: (UltraHdrViewMode) -> Unit,
    onSimulationRatioChange: (Float) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.ultrahdr_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    val docUrl = stringResource(R.string.ultrahdr_doc_url)
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, docUrl.toUri())
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Rounded.Info, contentDescription = stringResource(R.string.ultrahdr_info))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        imageVector = Icons.Rounded.HdrOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.ultrahdr_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.ultrahdr_pick),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = onLoadExample,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Rounded.Image, null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.ultrahdr_example),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            uiState.imageBitmap?.let { bitmap ->
                val adaptiveInfo = currentWindowAdaptiveInfoV2()
                val isCompact = !adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
                val labelStyle = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge

                // View Mode Selection
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
                ) {
                    SegmentedButton(
                        selected = uiState.viewMode == UltraHdrViewMode.HDR,
                        onClick = { onViewModeChange(UltraHdrViewMode.HDR) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                        label = { Text(stringResource(R.string.ultrahdr_mode_hdr), style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    SegmentedButton(
                        selected = uiState.viewMode == UltraHdrViewMode.SDR,
                        onClick = { onViewModeChange(UltraHdrViewMode.SDR) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                        label = { Text(stringResource(R.string.ultrahdr_mode_sdr), style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    SegmentedButton(
                        selected = uiState.viewMode == UltraHdrViewMode.GAINMAP,
                        onClick = { onViewModeChange(UltraHdrViewMode.GAINMAP) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                        label = { Text(stringResource(R.string.ultrahdr_mode_gainmap), style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    SegmentedButton(
                        selected = uiState.viewMode == UltraHdrViewMode.SHADER_SIMULATION,
                        onClick = { onViewModeChange(UltraHdrViewMode.SHADER_SIMULATION) },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                        label = { Text(stringResource(R.string.ultrahdr_mode_shader), style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        when (uiState.viewMode) {
                            UltraHdrViewMode.HDR, UltraHdrViewMode.SDR -> {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                                )
                            }
                            UltraHdrViewMode.GAINMAP -> {
                                uiState.gainmapBitmap?.let { gBitmap ->
                                    Image(
                                        bitmap = gBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                                    )
                                } ?: Text(stringResource(R.string.ultrahdr_no_gainmap_content), modifier = Modifier.padding(32.dp))
                            }
                            UltraHdrViewMode.SHADER_SIMULATION -> {
                                UltraHdrShaderSimulation(
                                    sdrBitmap = bitmap,
                                    gainmapBitmap = uiState.gainmapBitmap,
                                    metadata = uiState.gainmapMetadata,
                                    simulationRatio = uiState.simulationRatio,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.viewMode == UltraHdrViewMode.SHADER_SIMULATION) {
                    Column(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.ultrahdr_shader_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = uiState.simulationRatio,
                            onValueChange = onSimulationRatioChange,
                            valueRange = 1.0f..(uiState.gainmapMetadata?.displayRatioHdr?.coerceAtLeast(1.1f) ?: 8.0f)
                        )
                        Text(
                            stringResource(R.string.hdr_simulation_ratio, uiState.simulationRatio),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.End),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.hasGainmap && uiState.viewMode == UltraHdrViewMode.HDR) {
                    // HDR Hardware Mode Note
                    Text(
                        stringResource(R.string.hdr_hardware_warning),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // HDR Toggle Switch
                    Surface(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.hdr_preview),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (uiState.isHdrPreviewEnabled) stringResource(R.string.hdr_on) else stringResource(R.string.hdr_off),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (uiState.isHdrPreviewEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isHdrPreviewEnabled,
                                onCheckedChange = onToggleHdr,
                                thumbContent = {
                                    Icon(
                                        imageVector = if (uiState.isHdrPreviewEnabled) Icons.Rounded.HdrOn else Icons.Rounded.HdrOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoSectionTitle(stringResource(R.string.gainmap_info))
                    if (uiState.hasGainmap) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.HdrOn, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    stringResource(R.string.hdr_active),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.HdrOff, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    stringResource(R.string.gainmap_not_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                uiState.gainmapMetadata?.let { metadata ->
                    val specItems = listOf(
                        Icons.Rounded.BrightnessHigh to (stringResource(R.string.hdr_capacity_label) to "%.2f".format(uiState.currentDisplayRatio)),
                        Icons.Rounded.Exposure to (stringResource(R.string.gainmap_ratio) to "%.2f".format(metadata.displayRatioHdr)),
                        Icons.Rounded.Compare to (stringResource(R.string.gainmap_min_display) to "%.2f".format(metadata.displayRatioSdr)),
                        Icons.Rounded.Analytics to (stringResource(R.string.gainmap_gamma) to metadata.gamma.joinToString(", ")),
                        Icons.Rounded.BrightnessLow to (stringResource(R.string.gainmap_epsilon_sdr) to metadata.epsilonSdr.joinToString(", ")),
                        Icons.Rounded.BrightnessHigh to (stringResource(R.string.gainmap_epsilon_hdr) to metadata.epsilonHdr.joinToString(", "))
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoSectionTitle(stringResource(R.string.ultrahdr_specs))
                        specItems.forEachIndexed { index, (icon, info) ->
                            UltraHdrDetailItem(
                                icon = icon,
                                label = info.first,
                                value = info.second,
                                index = index,
                                size = specItems.size
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.ultrahdr_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UltraHdrShaderSimulation(
    sdrBitmap: Bitmap,
    gainmapBitmap: Bitmap?,
    metadata: GainmapMetadata?,
    simulationRatio: Float,
    modifier: Modifier = Modifier
) {
    if (gainmapBitmap == null || metadata == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.ultrahdr_shader_requirement))
        }
        return
    }

    val shader = remember { RuntimeShader(ULTRAHDR_SHADER) }

    LaunchedEffect(sdrBitmap, gainmapBitmap, metadata, simulationRatio) {
        // Prepare shader inputs
        val weight = (ln(simulationRatio) - ln(metadata.displayRatioSdr)) / 
                     (ln(metadata.displayRatioHdr) - ln(metadata.displayRatioSdr))
        
        shader.setFloatUniform("logRatioMin", 
            ln(metadata.ratioMin[0]), ln(metadata.ratioMin[1]), ln(metadata.ratioMin[2]))
        shader.setFloatUniform("logRatioMax", 
            ln(metadata.ratioMax[0]), ln(metadata.ratioMax[1]), ln(metadata.ratioMax[2]))
        shader.setFloatUniform("gamma", 
            1.0f / metadata.gamma[0], 1.0f / metadata.gamma[1], 1.0f / metadata.gamma[2])
        shader.setFloatUniform("epsilonSdr", 
            metadata.epsilonSdr[0], metadata.epsilonSdr[1], metadata.epsilonSdr[2])
        shader.setFloatUniform("epsilonHdr", 
            metadata.epsilonHdr[0], metadata.epsilonHdr[1], metadata.epsilonHdr[2])
        shader.setFloatUniform("weight", weight.coerceIn(0.0f, 1.0f))
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                ShaderBrush(shader)
            )
    ) {
        DisposableEffect(sdrBitmap, gainmapBitmap) {
            shader.setInputBuffer("image", BitmapShader(sdrBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
            shader.setInputBuffer("gainmap", BitmapShader(gainmapBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
            onDispose {}
        }
    }
}

@Composable
fun UltraHdrDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    index: Int,
    size: Int
) {
    val cornerLarge = 24.dp
    val cornerSmall = 4.dp
    val shape = when {
        size == 1 -> RoundedCornerShape(cornerLarge)
        index == 0 -> RoundedCornerShape(topStart = cornerLarge, topEnd = cornerLarge, bottomStart = cornerSmall, bottomEnd = cornerSmall)
        index == size - 1 -> RoundedCornerShape(topStart = cornerSmall, topEnd = cornerSmall, bottomStart = cornerLarge, bottomEnd = cornerLarge)
        else -> RoundedCornerShape(cornerSmall)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            DetailItem(icon, label, value)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UltraHdrPreview() {
    val mockBitmap = remember {
        createBitmap(800, 600, Bitmap.Config.ARGB_8888).apply {
            val canvas = android.graphics.Canvas(this)
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            canvas.drawRect(0f, 0f, 800f, 600f, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textAlign = android.graphics.Paint.Align.CENTER
            paint.textSize = 40f
            canvas.drawText("ULTRA HDR IMAGE PREVIEW", 400f, 300f, paint)
        }
    }

    GeekLabTheme {
        UltraHdrContent(
            uiState = UltraHdrState(
                imageBitmap = mockBitmap,
                hasGainmap = true,
                gainmapMetadata = GainmapMetadata(
                    ratioMin = floatArrayOf(1.0f, 1.0f, 1.0f),
                    ratioMax = floatArrayOf(8.0f, 8.0f, 8.0f),
                    gamma = floatArrayOf(1.0f, 1.0f, 1.0f),
                    epsilonSdr = floatArrayOf(0.01f, 0.01f, 0.01f),
                    epsilonHdr = floatArrayOf(0.01f, 0.01f, 0.01f),
                    displayRatioSdr = 1.0f,
                    displayRatioHdr = 8.0f
                ),
                currentDisplayRatio = 5.2f,
                isHdrPreviewEnabled = true
            ),
            onBackClick = {},
            onPickImage = {},
            onLoadExample = {},
            onToggleHdr = {},
            onViewModeChange = {},
            onSimulationRatioChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Technical Components Only")
@Composable
fun UltraHdrKeyComponentsPreview() {
    GeekLabTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HDR Toggle Switch Component
            Surface(
                modifier = Modifier.widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("HDR Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("HDR ON", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Switch(checked = true, onCheckedChange = {})
                }
            }

            // Gainmap Active Status Component
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoSectionTitle("Gainmap Information")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.HdrOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("HDR Viewing Active", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Technical Specs Component
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoSectionTitle("Ultra HDR Specs")
                val specItems = listOf(
                    Icons.Rounded.BrightnessHigh to ("Display HDR Capacity" to "5.20"),
                    Icons.Rounded.Exposure to ("Gainmap Ratio" to "8.00"),
                    Icons.Rounded.Analytics to ("Gamma" to "1.0, 1.0, 1.0")
                )
                specItems.forEachIndexed { index, (icon, info) ->
                    UltraHdrDetailItem(
                        icon = icon,
                        label = info.first,
                        value = info.second,
                        index = index,
                        size = specItems.size
                    )
                }
            }
        }
    }
}


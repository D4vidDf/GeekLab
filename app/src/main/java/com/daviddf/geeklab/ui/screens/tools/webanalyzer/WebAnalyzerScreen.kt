package com.daviddf.geeklab.ui.screens.tools.webanalyzer

import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.DetailItem
import com.daviddf.geeklab.ui.screens.tools.wifi.InfoChip
import com.daviddf.geeklab.ui.screens.tools.wifi.ResultBox
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAnalyzerScreen(
    onBackClick: () -> Unit,
    viewModel: WebAnalyzerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    WebAnalyzerContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onUrlChange = viewModel::onUrlChange,
        onApiKeyChange = viewModel::onApiKeyChange,
        onMethodSelect = viewModel::onMethodSelect,
        onAnalyzeClick = viewModel::analyzeUrl
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAnalyzerContent(
    uiState: WebAnalyzerUiState,
    onBackClick: () -> Unit,
    onUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onMethodSelect: (AnalysisMethod) -> Unit,
    onAnalyzeClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDetailSheet by remember { mutableStateOf<CategoryInfo?>(null) }
    var showConfigSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.web_analyzer_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showConfigSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Configurar análisis",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Visual
            WebAnalyzerVisualizer(isLoading = uiState.isLoading)

            if (uiState.isLoading) {
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.animateContentSize()
                )
            }

            // URL Input Card
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.urlInput,
                        onValueChange = onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("URL") },
                        placeholder = { Text(stringResource(R.string.web_analyzer_url_placeholder)) },
                        leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.urlInput.isNotEmpty()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = null)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onAnalyzeClick()
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true,
                        isError = uiState.error != null,
                        supportingText = uiState.error?.let { { Text(it) } },
                        shape = MaterialTheme.shapes.large
                    )

                    Button(
                        onClick = {
                            onAnalyzeClick()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.Analytics, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.web_analyzer_analyze))
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = uiState.result,
                transitionSpec = {
                    fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                },
                label = "analysis_result"
            ) { result ->
                if (result != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        WebSummaryCard(result)

                        WebAuditCard(
                            result = result,
                            onCategoryClick = { category -> showDetailSheet = category }
                        )
                        
                        WebTechnicalDetailsCard(result)

                        WebOpportunitiesCard(result.opportunities)

                        WebFilesCard(result.fileDetails)

                        // Screenshot Card
                        if (result.screenshotBase64 != null) {
                            Text(
                                text = "Lighthouse Screenshot",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            WebScreenshotCard(base64Data = result.screenshotBase64)
                        }
                    }
                }
            }
        }
    }

    if (showDetailSheet != null) {
        CategoryDetailSheet(
            category = showDetailSheet!!,
            onDismiss = { showDetailSheet = null }
        )
    }

    if (showConfigSheet) {
        WebConfigSheet(
            uiState = uiState,
            onDismiss = { showConfigSheet = false },
            onMethodSelect = onMethodSelect,
            onApiKeyChange = onApiKeyChange
        )
    }
}

@Composable
fun WebAnalyzerVisualizer(isLoading: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        if (isLoading) {
            Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.2f)))
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.4f)))
        }
        
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = if (isLoading) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isLoading) Icons.Rounded.Loop else Icons.Rounded.Language,
                    contentDescription = null,
                    modifier = if (isLoading) Modifier.size(40.dp).rotateModifier(rotation) else Modifier.size(40.dp),
                    tint = if (isLoading) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WebSummaryCard(result: WebAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (result.isLocalAnalysis) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.url.substringAfter("://").substringBefore("/"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (result.isLocalAnalysis) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (result.isLocalAnalysis) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "LOCAL", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebAuditCard(
    result: WebAnalysisResult,
    onCategoryClick: (CategoryInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Resultados Lighthouse", 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultBox(
                        label = stringResource(R.string.web_analyzer_performance),
                        value = result.performance.score.toString(),
                        color = getScoreColor(result.performance.score),
                        modifier = Modifier.weight(1f).clickable { onCategoryClick(result.performance) }
                    )
                    ResultBox(
                        label = stringResource(R.string.web_analyzer_accessibility),
                        value = result.accessibility.score.toString(),
                        color = getScoreColor(result.accessibility.score),
                        modifier = Modifier.weight(1f).clickable { onCategoryClick(result.accessibility) }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultBox(
                        label = stringResource(R.string.web_analyzer_best_practices),
                        value = result.bestPractices.score.toString(),
                        color = getScoreColor(result.bestPractices.score),
                        modifier = Modifier.weight(1f).clickable { onCategoryClick(result.bestPractices) }
                    )
                    ResultBox(
                        label = stringResource(R.string.web_analyzer_seo),
                        value = result.seo.score.toString(),
                        color = getScoreColor(result.seo.score),
                        modifier = Modifier.weight(1f).clickable { onCategoryClick(result.seo) }
                    )
                }
            }
            
            Text(
                text = "Toca un puntaje para ver detalles completos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WebTechnicalDetailsCard(result: WebAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.web_analyzer_technical_details), 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultBox(
                    label = stringResource(R.string.web_analyzer_load_time),
                    value = result.loadTime,
                    color = getScoreColor(result.performance.score),
                    modifier = Modifier.weight(1f)
                )
                ResultBox(
                    label = stringResource(R.string.web_analyzer_requests),
                    value = result.requests.toString(),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            DetailItem(Icons.Rounded.DataUsage, stringResource(R.string.web_analyzer_total_size), result.totalSize)
        }
    }
}

@Composable
fun WebOpportunitiesCard(opportunities: List<WebOpportunity>) {
    if (opportunities.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.web_analyzer_opportunities), 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold
            )
            
            opportunities.forEach { opp ->
                OpportunityItem(opp)
            }
        }
    }
}

@Composable
fun OpportunityItem(opportunity: WebOpportunity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(getOpportunityColor(opportunity.score))
        )
        Column {
            Text(opportunity.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (opportunity.displayValue.isNotEmpty()) {
                Text(
                    opportunity.displayValue, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = getOpportunityColor(opportunity.score),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WebFilesCard(files: List<WebFileDetail>) {
    if (files.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.web_analyzer_files), 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.secondary, 
                fontWeight = FontWeight.Bold
            )
            
            files.forEach { file ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(file.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(file.size, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun WebScreenshotCard(base64Data: String?) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (base64Data != null) {
                val bitmap = remember(base64Data) {
                    try {
                        val pureBase64 = base64Data.substringAfter("base64,")
                        val decodedString = Base64.decode(pureBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Lighthouse Screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    MockupPreview(isMobile = true)
                }
            } else {
                MockupPreview(isMobile = true)
            }
        }
    }
}

@Composable
fun MockupPreview(isMobile: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (isMobile) Icons.Rounded.Smartphone else Icons.Rounded.DesktopWindows,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            "Visualización no disponible",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailSheet(
    category: CategoryInfo,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { category.score / 100f },
                        modifier = Modifier.size(56.dp),
                        color = getScoreColor(category.score),
                        strokeWidth = 4.dp
                    )
                    Text(category.score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(category.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Detalle de auditorías", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                val sortedAudits = category.audits.sortedBy { it.score ?: 1.1f }
                items(sortedAudits) { audit ->
                    AuditItem(audit)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebConfigSheet(
    uiState: WebAnalyzerUiState,
    onDismiss: () -> Unit,
    onMethodSelect: (AnalysisMethod) -> Unit,
    onApiKeyChange: (String) -> Unit
) {
    val context = LocalContext.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                stringResource(R.string.web_analyzer_method_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            AnalysisMethodOption(
                method = AnalysisMethod.LOCAL,
                selected = uiState.selectedMethod == AnalysisMethod.LOCAL,
                title = stringResource(R.string.web_analyzer_method_local),
                description = stringResource(R.string.web_analyzer_method_local_desc),
                icon = Icons.Rounded.Phonelink,
                onClick = { onMethodSelect(AnalysisMethod.LOCAL); onDismiss() }
            )

            AnalysisMethodOption(
                method = AnalysisMethod.ANONYMOUS,
                selected = uiState.selectedMethod == AnalysisMethod.ANONYMOUS,
                title = stringResource(R.string.web_analyzer_method_anonymous),
                description = stringResource(R.string.web_analyzer_method_anonymous_desc),
                icon = Icons.Rounded.CloudQueue,
                onClick = { onMethodSelect(AnalysisMethod.ANONYMOUS) }
            )

            AnalysisMethodOption(
                method = AnalysisMethod.API_KEY,
                selected = uiState.selectedMethod == AnalysisMethod.API_KEY,
                title = stringResource(R.string.web_analyzer_method_api),
                description = stringResource(R.string.web_analyzer_method_api_desc),
                icon = Icons.Rounded.VpnKey,
                onClick = { onMethodSelect(AnalysisMethod.API_KEY) }
            )

            AnimatedVisibility(visible = uiState.selectedMethod == AnalysisMethod.API_KEY) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.apiKey,
                        onValueChange = onApiKeyChange,
                        label = { Text(stringResource(R.string.web_analyzer_api_key_label)) },
                        placeholder = { Text(stringResource(R.string.web_analyzer_api_key_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { 
                                val intent = Intent(Intent.ACTION_VIEW,
                                    "https://developers.google.com/speed/docs/insights/v5/get-started".toUri())
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Rounded.Help, contentDescription = "Obtener API Key")
                            }
                        }
                    )
                    Text(
                        "Usa una API Key para evitar límites de cuota (25,000 req/día gratis).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(R.string.aceptar))
            }
        }
    }
}

@Composable
fun AnalysisMethodOption(
    method: AnalysisMethod,
    selected: Boolean,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AuditItem(audit: WebAudit) {
    val score = audit.score
    val statusColor = when {
        score == null -> MaterialTheme.colorScheme.outline
        score >= 0.9f -> Color(0xFF4CAF50)
        score >= 0.5f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    val icon = when {
        score == null -> Icons.Rounded.Info
        score >= 0.9f -> Icons.Rounded.CheckCircle
        score >= 0.5f -> Icons.Rounded.Warning
        else -> Icons.Rounded.Error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column {
            Text(audit.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (audit.displayValue.isNotEmpty()) {
                Text(audit.displayValue, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Text(audit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (audit.group.isNotEmpty()) {
                TechBadge(
                    text = audit.group.uppercase(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TechBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun getScoreColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

fun getOpportunityColor(score: Float): Color {
    return when {
        score >= 0.9f -> Color(0xFF4CAF50)
        score >= 0.5f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun WebAnalyzerScreenPreview() {
    GeekLabTheme {
        WebAnalyzerContent(
            uiState = WebAnalyzerUiState(
                urlInput = "https://google.com",
                result = WebAnalysisResult(
                    url = "https://google.com",
                    performance = CategoryInfo("Performance", 92, listOf(
                        WebAudit("lcp", "Largest Contentful Paint", "Indicates the time when the largest text or image is painted.", 0.92f, "1.2s", "metrics"),
                        WebAudit("fid", "First Input Delay", "Measures the time from when a user first interacts with your site.", 0.45f, "300ms", "metrics")
                    )),
                    accessibility = CategoryInfo("Accessibility", 85, listOf(
                        WebAudit("color-contrast", "Background and foreground colors do not have a sufficient contrast ratio.", "Low-contrast text is difficult or impossible to read.", 0.0f, "", "accessibility")
                    )),
                    bestPractices = CategoryInfo("Best Practices", 100, emptyList()),
                    seo = CategoryInfo("SEO", 98, emptyList()),
                    totalSize = "2.4 MB",
                    loadTime = "0.85s",
                    requests = 45,
                    opportunities = listOf(
                        WebOpportunity("Optimizar imágenes", "Ahorra 1.2MB usando WebP", 0.4f, "Ahorro de 1.2s"),
                        WebOpportunity("Minificar JS", "Reduce el tamaño de los scripts", 0.9f, "Ahorro de 50KB")
                    ),
                    fileDetails = listOf(
                        WebFileDetail("bundle.js", "1.2 MB", "JS"),
                        WebFileDetail("styles.css", "200 KB", "CSS")
                    )
                )
            ),
            onBackClick = {},
            onUrlChange = {},
            onApiKeyChange = {},
            onMethodSelect = {},
            onAnalyzeClick = {}
        )
    }
}

@Composable
fun Modifier.rotateModifier(degrees: Float): Modifier = this.then(
    Modifier.rotate(degrees)
)

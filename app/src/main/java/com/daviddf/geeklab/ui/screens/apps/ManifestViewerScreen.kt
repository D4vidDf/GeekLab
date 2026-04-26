package com.daviddf.geeklab.ui.screens.apps

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.daviddf.geeklab.BuildConfig
import com.daviddf.geeklab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestViewerScreen(
    packageName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var manifestContent by remember { mutableStateOf<List<String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val searchResults = remember(manifestContent, searchQuery) {
        if (searchQuery.length < 2) emptyList()
        else {
            manifestContent?.mapIndexedNotNull { index, line ->
                if (line.contains(searchQuery, ignoreCase = true)) index else null
            } ?: emptyList()
        }
    }
    var currentResultIndex by remember(searchQuery) { mutableIntStateOf(0) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val saveMessage = stringResource(R.string.save)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                manifestContent?.forEach { line ->
                                    outputStream.write((line + "\n").toByteArray())
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, saveMessage, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error saving file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                val uri = "content://com.daviddf.geeklab.manifest/$packageName".toUri()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    manifestContent = inputStream.bufferedReader().readLines()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it 
                                    currentResultIndex = 0
                                },
                                placeholder = { Text(stringResource(R.string.buscar)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            )
                            
                            if (searchResults.isNotEmpty()) {
                                Text(
                                    text = "${currentResultIndex + 1}/${searchResults.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = {
                                    if (currentResultIndex > 0) currentResultIndex--
                                    else currentResultIndex = searchResults.size - 1
                                }) {
                                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Previous")
                                }
                                IconButton(onClick = {
                                    if (currentResultIndex < searchResults.size - 1) currentResultIndex++
                                    else currentResultIndex = 0
                                }) {
                                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Next")
                                }
                            }

                            IconButton(onClick = { 
                                if (searchQuery.isNotEmpty()) searchQuery = "" else isSearchActive = false 
                            }) {
                                Icon(Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.manifest)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        TooltipIconButton(
                            onClick = { isSearchActive = true },
                            icon = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.buscar),
                            tooltipText = stringResource(R.string.buscar)
                        )
                        TooltipIconButton(
                            onClick = { 
                                manifestContent?.let { content ->
                                    shareManifest(context, packageName, content.joinToString("\n"))
                                }
                            },
                            icon = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.share),
                            tooltipText = stringResource(R.string.share)
                        )
                        TooltipIconButton(
                            onClick = { 
                                createDocumentLauncher.launch("AndroidManifest_$packageName.xml")
                            },
                            icon = Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.save),
                            tooltipText = stringResource(R.string.save)
                        )
                    }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (manifestContent != null) {
            ManifestContent(
                lines = manifestContent!!, 
                searchQuery = searchQuery,
                scrollToIndex = if (searchResults.isNotEmpty()) searchResults[currentResultIndex] else null,
                modifier = Modifier.padding(padding)
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.error_loading_manifest))
            }
        }
    }
}

fun shareManifest(context: Context, packageName: String, content: String) {
    try {
        val cachePath = File(context.cacheDir, "shared_manifests")
        if (!cachePath.exists()) cachePath.mkdirs()
        val file = File(cachePath, "${packageName}_manifest.xml")
        file.writeText(content)

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/xml"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TITLE, "${packageName}_manifest.xml")
            // Add ClipData to ensure the URI permission is granted correctly and show a preview on Android 10+
            clipData = ClipData.newRawUri(null, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing manifest", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tooltipText: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below
        ),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = tooltipState,
        modifier = modifier
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = tint)
        }
    }
}

@Composable
fun ManifestContent(
    lines: List<String>,
    searchQuery: String,
    modifier: Modifier = Modifier,
    scrollToIndex: Int? = null
) {
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    val textColor = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var highlightedLines by remember { mutableStateOf<List<AnnotatedString>>(emptyList()) }
    var codeMaxWidth by remember { mutableStateOf(0.dp) }

    LaunchedEffect(lines, searchQuery, textColor) {
        withContext(Dispatchers.Default) {
            val highlighted = lines.map { highlightXml(it, textColor, searchQuery) }
            
            // Optimization for Monospace: width = charWidth * maxChars
            val style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            val charWidth = textMeasurer.measure("M", style).size.width
            val maxChars = lines.maxOfOrNull { it.length } ?: 0
            val maxW = charWidth * maxChars

            withContext(Dispatchers.Main) {
                highlightedLines = highlighted
                codeMaxWidth = with(density) { maxW.toDp() } + 32.dp
            }
        }
    }

    LaunchedEffect(scrollToIndex) {
        scrollToIndex?.let {
            listState.animateScrollToItem(it)
        }
    }

    SelectionContainer {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = highlightedLines,
                    key = { index, _ -> index }
                ) { index, line ->
                    Row(
                        modifier = Modifier.height(20.dp)
                    ) {
                        // Sticky Line Numbers (Fixed horizontally)
                        DisableSelection {
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    modifier = Modifier.padding(end = 12.dp),
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        textAlign = TextAlign.End,
                                        lineHeight = 20.sp
                                    )
                                )
                                // Vertical divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                        .align(Alignment.CenterEnd)
                                )
                            }
                        }

                        // Code content (Horizontally Scrollable)
                        // Shared horizontalScrollState ensures all rows stay in sync
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            Text(
                                text = line,
                                modifier = Modifier
                                    .width(codeMaxWidth)
                                    .padding(horizontal = 12.dp),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                ),
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

fun highlightXml(line: String, textColor: Color, searchQuery: String = "") = buildAnnotatedString {
    val tagColor = Color(0xFFE06C75) // Soft red
    val attrNameColor = Color(0xFFD19A66) // Orange/Brown
    val attrValueColor = Color(0xFF98C379) // Green
    val searchHighlightColor = Color(0xFFFFCC00).copy(alpha = 0.4f)

    val xmlHighlighted = buildAnnotatedString {
        var i = 0
        while (i < line.length) {
            when {
                line[i] == '<' -> {
                    val end = line.indexOfAny(charArrayOf(' ', '>', '/'), i + 1)
                    val tagEnd = if (end == -1) line.length else end
                    withStyle(SpanStyle(color = tagColor)) {
                        append(line.substring(i, tagEnd))
                    }
                    i = tagEnd
                }
                line[i] == '>' || line[i] == '/' -> {
                    withStyle(SpanStyle(color = tagColor)) {
                        append(line[i])
                    }
                    i++
                }
                line[i] == '\"' -> {
                    val end = line.indexOf('\"', i + 1)
                    val valueEnd = if (end == -1) line.length else end + 1
                    withStyle(SpanStyle(color = attrValueColor)) {
                        append(line.substring(i, valueEnd))
                    }
                    i = valueEnd
                }
                line[i] == '=' -> {
                    append('=')
                    i++
                }
                line[i].isWhitespace() -> {
                    append(line[i])
                    i++
                }
                else -> {
                    val nextSpecial = line.indexOfAny(charArrayOf('=', ' ', '>', '/', '\"', '<'), i)
                    val segmentEnd = if (nextSpecial == -1) line.length else nextSpecial
                    val segment = line.substring(i, segmentEnd)
                    
                    if (segmentEnd < line.length && line[segmentEnd] == '=') {
                        withStyle(SpanStyle(color = attrNameColor)) {
                            append(segment)
                        }
                    } else {
                        withStyle(SpanStyle(color = textColor)) {
                            append(segment)
                        }
                    }
                    i = segmentEnd
                }
            }
        }
    }

    if (searchQuery.isEmpty()) {
        append(xmlHighlighted)
    } else {
        var lastMatchEnd = 0
        val lowerLine = line.lowercase()
        val lowerQuery = searchQuery.lowercase()
        var matchIndex = lowerLine.indexOf(lowerQuery)
        
        while (matchIndex != -1) {
            append(xmlHighlighted.subSequence(lastMatchEnd, matchIndex))
            withStyle(SpanStyle(background = searchHighlightColor, fontWeight = FontWeight.Bold)) {
                append(xmlHighlighted.subSequence(matchIndex, matchIndex + searchQuery.length))
            }
            lastMatchEnd = matchIndex + searchQuery.length
            matchIndex = lowerLine.indexOf(lowerQuery, lastMatchEnd)
        }
        append(xmlHighlighted.subSequence(lastMatchEnd, line.length))
    }
}

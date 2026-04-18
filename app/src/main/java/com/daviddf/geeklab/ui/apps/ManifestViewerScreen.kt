package com.daviddf.geeklab.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.daviddf.geeklab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    var isFilterEnabled by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.buscar)) },
                            modifier = Modifier
                                .fillMaxWidth()
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
                            trailingIcon = {
                                IconButton(onClick = { 
                                    if (searchQuery.isNotEmpty()) searchQuery = "" else isSearchActive = false 
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = null)
                                }
                            }
                        )
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
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.buscar))
                        }
                        IconButton(onClick = { isFilterEnabled = !isFilterEnabled }) {
                            Icon(
                                Icons.Rounded.FilterList, 
                                contentDescription = "Filter",
                                tint = if (isFilterEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
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
                isFilterEnabled = isFilterEnabled,
                modifier = Modifier.padding(padding)
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.error_loading_manifest))
            }
        }
    }
}

@Composable
fun ManifestContent(
    lines: List<String>, 
    searchQuery: String,
    isFilterEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    val filteredLines = remember(lines, searchQuery, isFilterEnabled) {
        if (isFilterEnabled && searchQuery.isNotEmpty()) {
            lines.mapIndexed { index, s -> index to s }.filter { it.second.contains(searchQuery, ignoreCase = true) }
        } else {
            lines.mapIndexed { index, s -> index to s }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        itemsIndexed(filteredLines) { _, (originalIndex, line) ->
            val textColor = MaterialTheme.colorScheme.onSurface
            val highlightedLine = remember(line, textColor, searchQuery) {
                highlightXml(line, textColor, searchQuery)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Line number
                Text(
                    text = (originalIndex + 1).toString(),
                    modifier = Modifier
                        .width(48.dp)
                        .padding(horizontal = 8.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.End
                    )
                )

                // Vertical divider for line numbers
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // XML Content with syntax highlighting
                Text(
                    text = highlightedLine,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )
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

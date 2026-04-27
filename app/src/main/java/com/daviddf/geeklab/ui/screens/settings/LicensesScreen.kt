package com.daviddf.geeklab.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.screens.apps.GroupedSurfaceItem
import com.daviddf.geeklab.ui.theme.GeekLabTheme

data class LibraryLicense(
    val name: String,
    val author: String,
    val license: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBackClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val libraries = listOf(
        LibraryLicense(
            "Android Jetpack (Compose, Room, CameraX, etc.)",
            "Google",
            "Apache License 2.0",
            "https://developer.android.com/jetpack"
        ),
        LibraryLicense(
            "Kotlin Standard Library & Coroutines",
            "JetBrains",
            "Apache License 2.0",
            "https://kotlinlang.org/"
        ),
        LibraryLicense(
            "Kotlinx Serialization",
            "JetBrains",
            "Apache License 2.0",
            "https://github.com/Kotlin/kotlinx.serialization"
        ),
        LibraryLicense(
            "Firebase SDK",
            "Google",
            "Apache License 2.0",
            "https://firebase.google.com/"
        ),
        LibraryLicense(
            "Coil (Image Loading)",
            "Coil Contributors",
            "Apache License 2.0",
            "https://github.com/coil-kt/coil"
        ),
        LibraryLicense(
            "Material Components for Android",
            "Google",
            "Apache License 2.0",
            "https://github.com/material-components/material-components-android"
        ),
        LibraryLicense(
            "Material Icons",
            "Google",
            "Apache License 2.0",
            "https://fonts.google.com/icons"
        ),
        LibraryLicense(
            "Google Guava & ListenableFuture",
            "Google",
            "Apache License 2.0",
            "https://github.com/google/guava"
        ),
        LibraryLicense(
            "JUnit 4",
            "JUnit.org",
            "Eclipse Public License 1.0",
            "https://junit.org/junit4/"
        ),
        LibraryLicense(
            "AndroidX Test (Espresso, JUnit)",
            "Google",
            "Apache License 2.0",
            "https://developer.android.com/training/testing"
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.open_source_licenses), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(libraries) { index, library ->
                GroupedSurfaceItem(
                    index = index,
                    size = libraries.size,
                    modifier = Modifier.widthIn(max = 600.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, library.url.toUri())
                        context.startActivity(intent)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = library.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = library.license,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LicensesScreenPreview() {
    GeekLabTheme {
        LicensesScreen(onBackClick = {})
    }
}

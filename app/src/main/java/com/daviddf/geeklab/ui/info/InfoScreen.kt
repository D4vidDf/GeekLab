package com.daviddf.geeklab.ui.info

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.CardInformacion
import com.daviddf.geeklab.ui.theme.TextInformacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBackClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.info_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InfoDeviceCard(
                    title = stringResource(R.string.device_info),
                    content = buildString {
                        append(stringResource(R.string.manufacturer, Build.MANUFACTURER))
                        append("\n")
                        append(stringResource(R.string.brand, Build.BRAND))
                        append("\n")
                        append(stringResource(R.string.model, Build.MODEL))
                        append("\n")
                        append(stringResource(R.string.board, Build.BOARD))
                        append("\n")
                        append(stringResource(R.string.hardware, Build.HARDWARE))
                        append("\n")
                        append(stringResource(R.string.device, Build.DEVICE))
                        append("\n")
                        append(stringResource(R.string.product, Build.PRODUCT))
                    }
                )
            }

            item {
                InfoDeviceCard(
                    title = stringResource(R.string.system_info),
                    content = buildString {
                        append(stringResource(R.string.android_version, Build.VERSION.RELEASE))
                        append("\n")
                        append(stringResource(R.string.sdk_int, Build.VERSION.SDK_INT))
                        append("\n")
                        append(stringResource(R.string.build_id, Build.ID))
                        append("\n")
                        append(stringResource(R.string.build_fingerprint, Build.FINGERPRINT))
                        append("\n")
                        append(stringResource(R.string.base_os, Build.VERSION.BASE_OS ?: stringResource(R.string.not_available)))
                        append("\n")
                        append(stringResource(R.string.security_patch, Build.VERSION.SECURITY_PATCH))
                    }
                )
            }
        }
    }
}

@Composable
fun InfoDeviceCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardInformacion)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextInformacion
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextInformacion.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )
        }
    }
}

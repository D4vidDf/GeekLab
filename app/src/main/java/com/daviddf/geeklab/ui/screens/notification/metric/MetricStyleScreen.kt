package com.daviddf.geeklab.ui.screens.notification.metric

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricStyleScreen(
    onBackClick: () -> Unit,
    viewModel: MetricStyleViewModel = viewModel()
) {
    val context = LocalContext.current
    val hasPermission by viewModel.hasPermission.collectAsState()
    val numMetrics by viewModel.numMetrics.collectAsState()
    val metricsPromoted by viewModel.metricsPromoted.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updatePermissionStatus(context)
        if (isGranted) {
            viewModel.showMetricNotification(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updatePermissionStatus(context)
    }

    MetricStyleContent(
        numMetrics = numMetrics,
        metricsPromoted = metricsPromoted,
        onBackClick = onBackClick,
        onNumMetricsChange = { viewModel.updateNumMetrics(it) },
        onMetricPromotionToggle = { index, promoted -> viewModel.toggleMetricPromotion(index, promoted) },
        onShowNotificationClick = {
            if (Build.VERSION.SDK_INT >= 33 && !hasPermission) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.showMetricNotification(context)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricStyleContent(
    numMetrics: Int,
    metricsPromoted: List<Boolean>,
    onBackClick: () -> Unit,
    onNumMetricsChange: (Int) -> Unit,
    onMetricPromotionToggle: (Int, Boolean) -> Unit,
    onShowNotificationClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.metric_style_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
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
            Surface(
                modifier = Modifier.size(88.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color(0xFFF1F8E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color(0xFF33691E)
                    )
                }
            }

            Text(
                text = stringResource(R.string.metric_style_summary),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onShowNotificationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(Icons.Rounded.BarChart, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.show_metric_notification),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (Build.VERSION.SDK_INT < 38) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.metric_android_17_warning),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.metric_android_17_capabilities),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    MetricFeatureItem(
                        stringResource(R.string.metric_multi_metric_title),
                        stringResource(R.string.metric_multi_metric_desc)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    MetricFeatureItem(
                        stringResource(R.string.metric_adaptive_design_title),
                        stringResource(R.string.metric_adaptive_design_desc)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.metric_configuration_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.metric_num_metrics_label), style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3).forEach { count ->
                            FilterChip(
                                selected = numMetrics == count,
                                onClick = { onNumMetricsChange(count) },
                                label = { Text(count.toString()) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(stringResource(R.string.metric_configure_individual_label), style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val metricLabels = listOf(
                        stringResource(R.string.metric_steps),
                        stringResource(R.string.metric_active_time),
                        stringResource(R.string.metric_distance)
                    )
                    for (i in 0 until numMetrics) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(metricLabels[i], style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    stringResource(R.string.metric_set_promoted_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = metricsPromoted[i],
                                onCheckedChange = { onMetricPromotionToggle(i, it) }
                            )
                        }
                        if (i < numMetrics - 1) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.metric_available_types_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MetricFeatureItem(stringResource(R.string.metric_type_fixed_int), stringResource(R.string.metric_fixed_int_desc))
                    Spacer(Modifier.height(12.dp))
                    MetricFeatureItem(stringResource(R.string.metric_type_fixed_float), stringResource(R.string.metric_fixed_float_desc))
                    Spacer(Modifier.height(12.dp))
                    MetricFeatureItem(stringResource(R.string.metric_type_fixed_time), stringResource(R.string.metric_fixed_time_desc))
                    Spacer(Modifier.height(12.dp))
                    MetricFeatureItem(stringResource(R.string.metric_type_time_diff), stringResource(R.string.metric_time_diff_desc))
                }
            }
        }
    }
}

@Composable
private fun MetricFeatureItem(title: String, description: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 22.dp, top = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MetricStyleScreenPreview() {
    GeekLabTheme {
        MetricStyleContent(
            numMetrics = 3,
            metricsPromoted = listOf(true, false, true),
            onBackClick = {},
            onNumMetricsChange = {},
            onMetricPromotionToggle = { _, _ -> },
            onShowNotificationClick = {}
        )
    }
}

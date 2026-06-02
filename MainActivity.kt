package com.daviddf.geeklab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.daviddf.geeklab.navigation.GeekLabKey
import com.daviddf.geeklab.navigation.Navigator
import com.daviddf.geeklab.navigation.rememberNavigationState
import com.daviddf.geeklab.ui.screens.apps.AppDetailScreen
import com.daviddf.geeklab.ui.screens.apps.AppsScreen
import com.daviddf.geeklab.ui.screens.apps.ManifestViewerScreen
import com.daviddf.geeklab.ui.screens.apps.ToolsScreen
import com.daviddf.geeklab.ui.screens.battery.BatteryScreen
import com.daviddf.geeklab.ui.screens.news.NewsScreen
import com.daviddf.geeklab.ui.screens.home.HomeScreen
import com.daviddf.geeklab.ui.screens.info.InfoScreen
import com.daviddf.geeklab.ui.screens.notification.CustomNotificationScreen
import com.daviddf.geeklab.ui.screens.notification.NotificationScreen
import com.daviddf.geeklab.ui.screens.notification.call.CallNotificationScreen
import com.daviddf.geeklab.ui.screens.notification.history.NotificationDetailScreen
import com.daviddf.geeklab.ui.screens.notification.history.NotificationHistoryScreen
import com.daviddf.geeklab.ui.screens.notification.live.LiveUpdateScreen
import com.daviddf.geeklab.ui.screens.notification.messaging.MessagingNotificationScreen
import com.daviddf.geeklab.ui.screens.notification.metric.MetricStyleScreen
import com.daviddf.geeklab.ui.screens.settings.LicensesScreen
import com.daviddf.geeklab.ui.screens.settings.SettingsScreen
import com.daviddf.geeklab.ui.screens.tools.ToolsScreen
import com.daviddf.geeklab.ui.screens.tools.bluetooth.BleScreen
import com.daviddf.geeklab.ui.screens.tools.bluetooth.BluetoothScreen
import com.daviddf.geeklab.ui.screens.tools.camera.CameraScreen
import com.daviddf.geeklab.ui.screens.tools.nfc.NfcScannerScreen
import com.daviddf.geeklab.ui.screens.tools.ultrahdr.UltraHdrScreen
import com.daviddf.geeklab.ui.screens.tools.webanalyzer.WebAnalyzerScreen
import com.daviddf.geeklab.ui.screens.tools.widgets.WidgetDetailScreen
import com.daviddf.geeklab.ui.screens.tools.widgets.WidgetInspectorScreen
import com.daviddf.geeklab.ui.screens.tools.wifi.WifiScannerScreen
import com.daviddf.geeklab.ui.screens.tools.wifi.WifiScreen
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val _intentFlow: MutableState<Intent?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _intentFlow.value = intent
        
        setContent {
            GeekLabTheme {
                val state = rememberNavigationState(
                    startRoute = GeekLabKey.Home,
                    topLevelRoutes = setOf(GeekLabKey.Home)
                )
                val navigator = remember(state) { Navigator(state) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(_intentFlow.value) {
                    _intentFlow.value?.let { 
                        handleIntent(it, navigator)
                        _intentFlow.value = null // Reset after handling
                    }
                }

                val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
                    when (key) {
                        is GeekLabKey.Home -> NavEntry(key) {
                            HomeScreen(
                                onNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.Notifications) } },
                                onBatteryClick = { scope.launch { navigator.navigate(GeekLabKey.Battery) } },
                                onInfoClick = { scope.launch { navigator.navigate(GeekLabKey.Info) } },
                                onAppsClick = { scope.launch { navigator.navigate(GeekLabKey.Apps) } },
                                onToolsClick = { scope.launch { navigator.navigate(GeekLabKey.Tools) } },
                                onSeeMoreNewsClick = { scope.launch { navigator.navigate(GeekLabKey.News) } },
                                onSettingsClick = { scope.launch { navigator.navigate(GeekLabKey.Settings) } },
                                onLiveUpdateClick = { scope.launch { navigator.navigate(GeekLabKey.LiveUpdate) } },
                                onMetricStyleClick = { scope.launch { navigator.navigate(GeekLabKey.MetricStyle) } },
                                onWidgetInspectorClick = { scope.launch { navigator.navigate(GeekLabKey.WidgetInspector) } },
                                onNotificationHistoryClick = { scope.launch { navigator.navigate(GeekLabKey.NotificationHistory) } },
                                onCallNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.CallNotification) } },
                                onMessagingNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.MessagingNotification) } },
                                onBluetoothClick = { scope.launch { navigator.navigate(GeekLabKey.Bluetooth) } },
                                onBluetoothBleClick = { scope.launch { navigator.navigate(GeekLabKey.BluetoothBle) } },
                                onNfcScannerClick = { scope.launch { navigator.navigate(GeekLabKey.NfcScanner) } },
                                onWifiClick = { scope.launch { navigator.navigate(GeekLabKey.Wifi) } },
                                onWifiScannerClick = { scope.launch { navigator.navigate(GeekLabKey.WifiScanner) } },
                                onCameraXClick = { scope.launch { navigator.navigate(GeekLabKey.CameraX) } },
                                onUltraHdrClick = { scope.launch { navigator.navigate(GeekLabKey.UltraHdr) } },
                                onWebAnalyzerClick = { scope.launch { navigator.navigate(GeekLabKey.WebAnalyzer) } }
                            )
                        }

                        is GeekLabKey.News -> NavEntry(key) {
                            NewsScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Battery -> NavEntry(key) {
                            BatteryScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Notifications -> NavEntry(key) {
                            NotificationScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Info -> NavEntry(key) {
                            InfoScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Apps -> NavEntry(key) {
                            AppsListDetailScreen(
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onViewManifest = { pkg: String -> scope.launch { navigator.navigate(GeekLabKey.ManifestViewer(pkg)) } }
                            )
                        }

                        is GeekLabKey.AppDetail -> NavEntry(key) {
                            AppDetailScreen(
                                packageName = key.packageName,
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onViewManifest = { pkg: String -> scope.launch { navigator.navigate(GeekLabKey.ManifestViewer(pkg)) } }
                            )
                        }

                        is GeekLabKey.ManifestViewer -> NavEntry(key) {
                            ManifestViewerScreen(
                                packageName = key.packageName,
                                onBackClick = { scope.launch { navigator.goBack() } }
                            )
                        }

                        is GeekLabKey.Tools -> NavEntry(key) {
                            ToolsScreen(
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.Notifications) } },
                                onLiveUpdateClick = { scope.launch { navigator.navigate(GeekLabKey.LiveUpdate) } },
                                onMetricStyleClick = { scope.launch { navigator.navigate(GeekLabKey.MetricStyle) } },
                                onBatteryClick = { scope.launch { navigator.navigate(GeekLabKey.Battery) } },
                                onInfoClick = { scope.launch { navigator.navigate(GeekLabKey.Info) } },
                                onAppsClick = { scope.launch { navigator.navigate(GeekLabKey.Apps) } },
                                onWidgetInspectorClick = { scope.launch { navigator.navigate(GeekLabKey.WidgetInspector) } },
                                onNotificationHistoryClick = { scope.launch { navigator.navigate(GeekLabKey.NotificationHistory) } },
                                onCallNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.CallNotification) } },
                                onMessagingNotificationClick = { scope.launch { navigator.navigate(GeekLabKey.MessagingNotification) } },
                                onBluetoothClick = { scope.launch { navigator.navigate(GeekLabKey.Bluetooth) } },
                                onBluetoothBleClick = { scope.launch { navigator.navigate(GeekLabKey.BluetoothBle) } },
                                onNfcScannerClick = { scope.launch { navigator.navigate(GeekLabKey.NfcScanner) } },
                                onWifiClick = { scope.launch { navigator.navigate(GeekLabKey.Wifi) } },
                                onWifiScannerClick = { scope.launch { navigator.navigate(GeekLabKey.WifiScanner) } },
                                onCameraXClick = { scope.launch { navigator.navigate(GeekLabKey.CameraX) } },
                                onUltraHdrClick = { scope.launch { navigator.navigate(GeekLabKey.UltraHdr) } },
                                onWebAnalyzerClick = { scope.launch { navigator.navigate(GeekLabKey.WebAnalyzer) } }
                            )
                        }

                        is GeekLabKey.CustomNotification -> NavEntry(key) {
                            CustomNotificationScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.LiveUpdate -> NavEntry(key) {
                            LiveUpdateScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.MetricStyle -> NavEntry(key) {
                            MetricStyleScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.NotificationHistory -> NavEntry(key) {
                            NotificationHistoryScreen(
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onNotificationClick = { id -> scope.launch { navigator.navigate(GeekLabKey.NotificationDetail(id)) } }
                            )
                        }

                        is GeekLabKey.NotificationDetail -> NavEntry(key) {
                            NotificationDetailScreen(
                                notificationId = key.notificationId,
                                onBackClick = { scope.launch { navigator.goBack() } }
                            )
                        }

                        is GeekLabKey.CallNotification -> NavEntry(key) {
                            CallNotificationScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Bluetooth -> NavEntry(key) {
                            BluetoothScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.BluetoothBle -> NavEntry(key) {
                            BleScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.NfcScanner -> NavEntry(key) {
                            NfcScannerScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.Wifi -> NavEntry(key) {
                            WifiScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.WifiScanner -> NavEntry(key) {
                            WifiScannerScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.CameraX -> NavEntry(key) {
                            CameraScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.UltraHdr -> NavEntry(key) {
                            UltraHdrScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.WebAnalyzer -> NavEntry(key) {
                            WebAnalyzerScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.WidgetInspector -> NavEntry(key) {
                            WidgetInspectorScreen(
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onWidgetClick = { pkg, cls -> scope.launch { navigator.navigate(GeekLabKey.WidgetDetail(pkg, cls)) } }
                            )
                        }

                        is GeekLabKey.WidgetDetail -> NavEntry(key) {
                            WidgetDetailScreen(
                                packageName = key.packageName,
                                className = key.className,
                                onBackClick = { scope.launch { navigator.goBack() } }
                            )
                        }

                        is GeekLabKey.Settings -> NavEntry(key) {
                            SettingsScreen(
                                onBackClick = { scope.launch { navigator.goBack() } },
                                onLicensesClick = { scope.launch { navigator.navigate(GeekLabKey.Licenses) } }
                            )
                        }

                        is GeekLabKey.Licenses -> NavEntry(key) {
                            LicensesScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        is GeekLabKey.MessagingNotification -> NavEntry(key) {
                            MessagingNotificationScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }
                            MessagingNotificationScreen(onBackClick = { scope.launch { navigator.goBack() } })
                        }

                        else -> NavEntry(key) {
                            Text("Not implemented yet")
                        }
                    }
                }

                NavDisplay(
                    backStack = state.backStacks[state.topLevelRoute]!!,
                    entryProvider = entryProvider
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _intentFlow.value = intent
    }

    private suspend fun handleIntent(intent: Intent?, navigator: Navigator) {
        val data = intent?.data ?: return
        when (data.scheme) {
            "geeklab" -> {
                when (data.host) {
                    "notification" -> {
                        navigator.navigate(GeekLabKey.CustomNotification)
                    }
                }
            }
            "https", "http" -> {
                if (data.host == "geeklab.d4viddf.com") {
                    when (data.path) {
                        "/notification" -> navigator.navigate(GeekLabKey.CustomNotification)
                        "/functions" -> navigator.navigate(GeekLabKey.Tools)
                        "/apps" -> navigator.navigate(GeekLabKey.Apps)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppsListDetailScreen(
    onBackClick: () -> Unit,
    onViewManifest: (String) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val scaffoldDirective = calculatePaneScaffoldDirective(adaptiveInfo)
    
    var isExpanded by remember { mutableStateOf(false) }

    // Define a directive that changes based on expansion state to trigger built-in animations
    val currentDirective = remember(scaffoldDirective, isExpanded) {
        if (isExpanded) {
            scaffoldDirective.copy(
                maxHorizontalPartitions = 1,
                horizontalPartitionSpacerSize = 0.dp
            )
        } else {
            // Ensure we use 2 partitions on large screens by default
            scaffoldDirective.copy(
                maxHorizontalPartitions = if (scaffoldDirective.maxHorizontalPartitions > 1) 2 else 1,
                horizontalPartitionSpacerSize = 0.dp,
                defaultPanePreferredWidth = 400.dp // Set a reasonable fixed width for the list
            )
        }
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = currentDirective
    )

    val scope = rememberCoroutineScope()

    ListDetailPaneScaffold(
        modifier = Modifier.fillMaxSize(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppsScreen(
                        onBackClick = onBackClick,
                        onAppClick = { packageName: String ->
                            if (navigator.currentDestination?.contentKey != packageName) {
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, packageName)
                                }
                            }
                        },
                        showNavigationIcon = true
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val packageName = navigator.currentDestination?.contentKey
                if (packageName != null) {
                    Row(Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = navigator.scaffoldDirective.maxHorizontalPartitions > 1 && !isExpanded,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            VerticalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        AppDetailScreen(
                            packageName = packageName,
                            onBackClick = {
                                if (isExpanded) {
                                    isExpanded = false
                                } else {
                                    // Clear selection instead of navigating back in multi-pane
                                    scope.launch {
                                        navigator.navigateBack()
                                    }
                                }
                            },
                            onViewManifest = onViewManifest,
                            isExpanded = isExpanded,
                            onToggleExpand = { isExpanded = !isExpanded },
                            showExpandToggle = scaffoldDirective.maxHorizontalPartitions > 1
                        )
                    }
                } else if (scaffoldDirective.maxHorizontalPartitions > 1) {
                    // Placeholder when no app is selected in multi-pane mode
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.select_app_info),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 300.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

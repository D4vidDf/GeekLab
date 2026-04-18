package com.daviddf.geeklab

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daviddf.geeklab.ui.apps.AppDetailScreen
import com.daviddf.geeklab.ui.apps.AppsScreen
import com.daviddf.geeklab.ui.apps.ManifestViewerScreen
import com.daviddf.geeklab.ui.apps.ToolsScreen
import com.daviddf.geeklab.ui.battery.BatteryScreen
import com.daviddf.geeklab.ui.feed.NewsScreen
import com.daviddf.geeklab.ui.home.HomeScreen
import com.daviddf.geeklab.ui.info.InfoScreen
import com.daviddf.geeklab.ui.notification.NotificationScreen
import com.daviddf.geeklab.ui.theme.GeekLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            GeekLabTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNotificationClick = { navController.navigate("notifications") },
                            onBatteryClick = { navController.navigate("battery") },
                            onInfoClick = { navController.navigate("info") },
                            onAppsClick = { navController.navigate("apps") },
                            onToolsClick = { navController.navigate("tools") },
                            onSeeMoreNewsClick = { navController.navigate("news") }
                        )
                    }

                    composable("news") {
                        NewsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    
                    composable("battery") {
                        BatteryScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("notifications") {
                        NotificationScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("info") {
                        InfoScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("apps") {
                        AppsListDetailScreen(
                            onBackClick = { navController.popBackStack() },
                            onViewManifest = { pkg -> navController.navigate("manifest_viewer/$pkg") }
                        )
                    }

                    composable("app_detail/{packageName}") { backStackEntry ->
                        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                        AppDetailScreen(
                            packageName = packageName,
                            onBackClick = { navController.popBackStack() },
                            onViewManifest = { pkg -> navController.navigate("manifest_viewer/$pkg") }
                        )
                    }

                    composable("manifest_viewer/{packageName}") { backStackEntry ->
                        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                        ManifestViewerScreen(
                            packageName = packageName,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("tools") {
                        ToolsScreen(onBackClick = { navController.popBackStack() })
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
    val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    
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
                        onAppClick = { packageName ->
                            if (navigator.currentDestination?.content != packageName) {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, packageName)
                            }
                        },
                        showNavigationIcon = true
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val packageName = navigator.currentDestination?.content
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
                                    navigator.navigateBack()
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

package com.daviddf.geeklab.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daviddf.geeklab.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.theme.CardAplicaciones
import com.daviddf.geeklab.ui.theme.TextAplicaciones

data class ToolShortcut(
    val titleResId: Int,
    val icon: ImageVector,
    val action: (Context) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    
    val shortcuts = listOf(
        ToolShortcut(R.string.developer_options, Icons.Rounded.Code) { ctx ->
            launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity")
        },
        ToolShortcut(R.string.band_selector, Icons.Rounded.CellTower) { ctx ->
            launchSettings(ctx, "com.android.settings", "com.android.settings.MiuiBandMode")
        },
        ToolShortcut(R.string.hdr_enhance, Icons.Rounded.HdrOn) { ctx ->
            launchSettings(ctx, "com.android.settings", "com.android.settings.display.ScreenEnhanceEngineS2hActivity")
        },
        ToolShortcut(R.string.improve_speed_connection, Icons.Rounded.Speed) { ctx ->
            launchSettings(ctx, "com.android.settings", "com.android.settings.wifi.linkturbo.WifiLinkTurboSettings")
        },
        ToolShortcut(R.string.multiaccount, Icons.Rounded.Group) { ctx ->
            launchSettings(ctx, "com.android.settings", "com.android.settings.Settings\$UserSettingsActivity")
        },
        ToolShortcut(R.string.data_usage, Icons.Rounded.DataUsage) { ctx ->
            launchSettings(ctx, "com.xiaomi.misettings", "com.xiaomi.misettings.usagestats.UsageStatsMainActivity")
        },
        ToolShortcut(R.string.performance_mode, Icons.Rounded.Memory) { ctx ->
            launchSettings(ctx, "com.qualcomm.qti.performancemode", "com.qualcomm.qti.performancemode.PerformanceModeActivity")
        },
        ToolShortcut(R.string.qcolor, Icons.Rounded.ColorLens) { ctx ->
            launchSettings(ctx, "com.qualcomm.qti.qcolor", "com.qualcomm.qti.qcolor.QColorActivity")
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tools_title),
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shortcuts) { shortcut ->
                FavoriteCard(
                    title = stringResource(shortcut.titleResId),
                    icon = shortcut.icon,
                    containerColor = CardAplicaciones,
                    contentColor = TextAplicaciones,
                    onClick = { shortcut.action(context) },
                    modifier = Modifier.height(140.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@PreviewScreenSizes
@Composable
fun ToolsScreenPreview() {
    GeekLabTheme {
        ToolsScreen(onBackClick = {})
    }
}

private fun launchSettings(context: Context, packageName: String, className: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setClassName(packageName, className)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_function_not_available), Toast.LENGTH_LONG).show()
    }
}

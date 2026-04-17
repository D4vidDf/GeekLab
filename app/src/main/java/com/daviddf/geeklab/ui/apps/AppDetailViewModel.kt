package com.daviddf.geeklab.ui.apps

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.text.format.Formatter
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

data class AppDetailState(
    val packageInfo: PackageInfo? = null,
    val label: String = "",
    val icon: android.graphics.Bitmap? = null,
    val version: String = "",
    val size: String = "",
    val installTime: String = "",
    val origin: String = "",
    val services: List<ServiceInfo> = emptyList(),
    val sharedLibraries: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager = application.packageManager
    private val context get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(AppDetailState())
    val uiState: StateFlow<AppDetailState> = _uiState.asStateFlow()

    fun loadAppDetails(packageName: String, notAvailable: String, sideloadedText: String, datePattern: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val flags = PackageManager.GET_ACTIVITIES or 
                        PackageManager.GET_SERVICES or 
                        PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_SHARED_LIBRARY_FILES

            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, flags)
            }

            val appInfo = packageInfo.applicationInfo ?: throw Exception("Application info not available")
            val label = appInfo.loadLabel(packageManager).toString()
            val icon = appInfo.loadIcon(packageManager).let { drawable ->
                val bitmap = createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1)
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            val version = packageInfo.versionName ?: notAvailable
            
            val file = File(appInfo.sourceDir)
            val size = if (file.exists()) Formatter.formatShortFileSize(context, file.length()) else notAvailable

            val locale = context.resources.configuration.locales[0]
            val installTime = SimpleDateFormat(datePattern, locale).format(Date(packageInfo.firstInstallTime))

            val origin = try {
                val installerPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    packageManager.getInstallSourceInfo(packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstallerPackageName(packageName)
                }

                installerPackage?.let { pkg ->
                    try {
                        packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString()
                    } catch (e: Exception) {
                        pkg
                    }
                } ?: sideloadedText
            } catch (e: Exception) {
                notAvailable
            }

            _uiState.value = AppDetailState(
                packageInfo = packageInfo,
                label = label,
                icon = icon,
                version = version,
                size = size,
                installTime = installTime,
                origin = origin,
                services = packageInfo.services?.toList() ?: emptyList(),
                sharedLibraries = appInfo.sharedLibraryFiles?.toList() ?: emptyList(),
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = AppDetailState(isLoading = false, error = e.message)
        }
    }

    fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun shareApk(packageName: String, label: String) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkFile = File(appInfo.sourceDir)
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, label)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun launchActivity(packageName: String, activityName: String) {
        try {
            val intent = Intent()
            intent.component = ComponentName(packageName, activityName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }
}

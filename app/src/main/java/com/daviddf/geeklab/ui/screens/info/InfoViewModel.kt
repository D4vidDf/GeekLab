package com.daviddf.geeklab.ui.screens.info

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile

data class InfoUiState(
    // Device Info
    val manufacturer: String = Build.MANUFACTURER,
    val brand: String = Build.BRAND,
    val model: String = Build.MODEL,
    val commercialName: String = "${Build.BRAND} ${Build.MODEL}",
    val deviceType: String = "Smartphone", // Default
    val board: String = Build.BOARD,
    val hardware: String = Build.HARDWARE,
    val device: String = Build.DEVICE,
    val product: String = Build.PRODUCT,
    
    // RAM & Storage
    val totalRam: Long = 0,
    val usedRam: Long = 0,
    val totalStorage: Long = 0,
    val availableStorage: Long = 0,
    
    // System Info
    val androidVersion: String = Build.VERSION.RELEASE,
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val buildId: String = Build.ID,
    val buildFingerprint: String = Build.FINGERPRINT,
    val baseOs: String? = Build.VERSION.BASE_OS,
    val securityPatch: String = Build.VERSION.SECURITY_PATCH,
    val bluetoothVersion: String = "N/A",

    // CPU Info
    val socModel: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else Build.HARDWARE,
    val cpuArch: String = System.getProperty("os.arch") ?: "Unknown",
    val fabricationProcess: String = "Unknown", // Usually not available via API
    val instructionSet: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
    val cpuRevision: String = "Unknown",
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    val cpuClockRange: String = "Unknown",
    val coreClocks: List<Long> = emptyList(),
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS.toList(),
    val supported32Abis: List<String> = Build.SUPPORTED_32_BIT_ABIS.toList(),
    val supported64Abis: List<String> = Build.SUPPORTED_64_BIT_ABIS.toList(),
    
    // Features
    val hasAes: Boolean = false,
    val hasNeon: Boolean = false,
    val hasPmull: Boolean = false,
    val hasSha1: Boolean = false,
    val hasSha2: Boolean = false
)

class InfoViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        updateStaticInfo()
        startRealtimeUpdates()
    }

    private fun updateStaticInfo() {
        val context = getApplication<Application>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.totalBytes
        val availableStorage = stat.availableBytes

        val cpuInfo = readCpuInfo()
        val isTablet = context.resources.configuration.smallestScreenWidthDp >= 600
        
        _uiState.update { state ->
            state.copy(
                deviceType = if (isTablet) "Tablet" else "Smartphone",
                totalRam = memoryInfo.totalMem,
                usedRam = memoryInfo.totalMem - memoryInfo.availMem,
                totalStorage = totalStorage,
                availableStorage = availableStorage,
                bluetoothVersion = getBluetoothVersion(),
                cpuRevision = cpuInfo["revision"] ?: "Unknown",
                hasAes = cpuInfo["features"]?.contains("aes", ignoreCase = true) == true,
                hasNeon = cpuInfo["features"]?.contains("neon", ignoreCase = true) == true || 
                          cpuInfo["features"]?.contains("asimd", ignoreCase = true) == true,
                hasPmull = cpuInfo["features"]?.contains("pmull", ignoreCase = true) == true,
                hasSha1 = cpuInfo["features"]?.contains("sha1", ignoreCase = true) == true,
                hasSha2 = cpuInfo["features"]?.contains("sha2", ignoreCase = true) == true,
                cpuClockRange = getCpuClockRange()
            )
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            while (true) {
                val context = getApplication<Application>()
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)

                val clocks = mutableListOf<Long>()
                for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                    clocks.add(getCpuCoreFreq(i))
                }

                _uiState.update { it.copy(
                    usedRam = memoryInfo.totalMem - memoryInfo.availMem,
                    coreClocks = clocks
                ) }
                delay(1000)
            }
        }
    }

    private fun getBluetoothVersion(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "5.3+"
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "5.2"
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "5.1"
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "5.0"
        else "4.x"
    }

    private fun readCpuInfo(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            File("/proc/cpuinfo").forEachLine { line ->
                val parts = line.split(":")
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    result[key] = value
                }
            }
        } catch (_: Exception) { }
        return result
    }

    private fun getCpuCoreFreq(core: Int): Long {
        return try {
            val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
            val reader = RandomAccessFile(path, "r")
            val line = reader.readLine()
            val freq = line?.toLong()?.div(1000) ?: 0L // Convert to MHz
            reader.close()
            freq
        } catch (_: Exception) {
            0L
        }
    }

    private fun getCpuClockRange(): String {
        return try {
            val minPath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"
            val maxPath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
            val minFreq = RandomAccessFile(minPath, "r").readLine().toLong() / 1000
            val maxFreq = RandomAccessFile(maxPath, "r").readLine().toLong() / 1000
            "$minFreq MHz - $maxFreq MHz"
        } catch (_: Exception) {
            "Unknown"
        }
    }
}

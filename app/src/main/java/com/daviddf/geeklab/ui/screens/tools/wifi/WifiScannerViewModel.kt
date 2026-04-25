package com.daviddf.geeklab.ui.screens.tools.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WifiScannerUiState(
    val isScanning: Boolean = false,
    val scanResults: List<WifiScanResult> = emptyList(),
    val isWifiEnabled: Boolean = false,
)

class WifiScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WifiScannerUiState())
    val uiState: StateFlow<WifiScannerUiState> = _uiState.asStateFlow()

    private var wifiScanReceiver: BroadcastReceiver? = null

    fun startScan(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        if (!wifiManager.isWifiEnabled) {
            _uiState.update { it.copy(isWifiEnabled = false) }
            return
        }

        _uiState.update { it.copy(isScanning = true, isWifiEnabled = true) }

        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    updateScanResults(wifiManager)
                }
                _uiState.update { it.copy(isScanning = false) }
                unregisterReceiver(context)
            }
        }

        context.registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure: old results may be available
            updateScanResults(wifiManager)
            _uiState.update { it.copy(isScanning = false) }
            unregisterReceiver(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateScanResults(wifiManager: WifiManager) {
        @Suppress("DEPRECATION")
        val results = wifiManager.scanResults.asSequence().map { result ->
            val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.wifiSsid?.toString()?.replace("\"", "") ?: "Hidden Network"
            } else {
                if (result.SSID.isNullOrBlank()) "Hidden Network" else result.SSID
            }

            val standard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                when (result.wifiStandard) {
                    android.net.wifi.ScanResult.WIFI_STANDARD_LEGACY -> "Wi-Fi 1/2/3"
                    android.net.wifi.ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
                    android.net.wifi.ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
                    android.net.wifi.ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
                    7 -> "Wi-Fi 6E"
                    8 -> "Wi-Fi 7"
                    else -> "Legacy"
                }
            } else {
                "Unknown"
            }

            WifiScanResult(
                ssid = ssid,
                bssid = result.BSSID,
                level = result.level,
                frequency = result.frequency,
                channel = convertFrequencyToChannel(result.frequency),
                standard = standard,
                capabilities = result.capabilities
            )
        }.sortedByDescending { it.level }.toList()
        
        _uiState.update { it.copy(scanResults = results) }
    }

    private fun convertFrequencyToChannel(freq: Int): Int {
        return if (freq >= 2412 && freq <= 2484) {
            (freq - 2412) / 5 + 1
        } else if (freq >= 5170 && freq <= 5825) {
            (freq - 5170) / 5 + 34
        } else if (freq >= 5945 && freq <= 7125) {
            (freq - 5945) / 5 + 1
        } else {
            0
        }
    }

    private fun unregisterReceiver(context: Context) {
        try {
            wifiScanReceiver?.let { context.unregisterReceiver(it) }
        } catch (_: Exception) {}
        wifiScanReceiver = null
    }

    override fun onCleared() {
        super.onCleared()
        // Receiver is usually short-lived and unregistered in onReceive, 
        // but we should ideally ensure it's gone if the VM is cleared.
    }
}

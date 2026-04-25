package com.daviddf.geeklab.ui.screens.tools.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BleDeviceDomain(
    val name: String?,
    val address: String,
    val rssi: Int,
    val services: List<BleServiceDomain> = emptyList()
)

data class BleServiceDomain(
    val uuid: String,
    val characteristics: List<BleCharacteristicDomain> = emptyList()
)

data class BleCharacteristicDomain(
    val uuid: String,
    val properties: Int
)

data class BleUiState(
    val isScanning: Boolean = false,
    val foundDevices: List<BleDeviceDomain> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isSupported: Boolean = true
)

@SuppressLint("MissingPermission")
class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private val _uiState = MutableStateFlow(BleUiState())
    val uiState: StateFlow<BleUiState> = _uiState.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val domain = BleDeviceDomain(device.name, device.address, result.rssi)
            _uiState.update { state ->
                if (state.foundDevices.any { it.address == domain.address }) {
                    state.copy(foundDevices = state.foundDevices.map { 
                        if (it.address == domain.address) it.copy(rssi = domain.rssi) else it 
                    })
                } else {
                    state.copy(foundDevices = state.foundDevices + domain)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    init {
        val isSupported = bluetoothAdapter != null
        val isEnabled = bluetoothAdapter?.isEnabled == true
        _uiState.update { it.copy(isSupported = isSupported, isBluetoothEnabled = isEnabled) }
    }

    fun startScan() {
        if (bleScanner == null || !bluetoothAdapter!!.isEnabled) return
        _uiState.update { it.copy(isScanning = true, foundDevices = emptyList()) }
        bleScanner.startScan(scanCallback)
    }

    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        _uiState.update { it.copy(isScanning = false) }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}

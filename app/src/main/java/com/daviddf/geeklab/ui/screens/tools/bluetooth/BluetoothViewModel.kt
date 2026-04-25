package com.daviddf.geeklab.ui.screens.tools.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.Manifest

data class BluetoothDeviceDomain(
    val name: String?,
    val address: String,
    val bondState: Int,
    val deviceClass: Int
)

data class BluetoothUiState(
    val isScanning: Boolean = false,
    val availableDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isSupported: Boolean = true
)

@SuppressLint("MissingPermission")
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    private val deviceFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { addFoundDevice(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _uiState.update { it.copy(isScanning = true) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _uiState.update { it.copy(isScanning = false) }
                }
            }
        }
    }

    init {
        val isSupported = bluetoothAdapter != null
        val isEnabled = bluetoothAdapter?.isEnabled == true
        _uiState.update { it.copy(isSupported = isSupported, isBluetoothEnabled = isEnabled) }
        
        if (isEnabled) {
            updatePairedDevices()
        }
    }

    fun startScan(context: Context) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        _uiState.update { it.copy(availableDevices = emptyList()) }
        
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(deviceFoundReceiver, filter)
        
        bluetoothAdapter.startDiscovery()
    }

    fun stopScan() {
        bluetoothAdapter?.cancelDiscovery()
    }

    fun updatePairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.BLUETOOTH_CONNECT) 
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            val paired = bluetoothAdapter?.bondedDevices?.map {
                BluetoothDeviceDomain(it.name, it.address, it.bondState, it.bluetoothClass.deviceClass)
            } ?: emptyList()
            _uiState.update { it.copy(pairedDevices = paired) }
        } catch (_: SecurityException) {
            _uiState.update { it.copy(pairedDevices = emptyList()) }
        }
    }

    private fun addFoundDevice(device: BluetoothDevice) {
        val domain = BluetoothDeviceDomain(device.name, device.address, device.bondState, device.bluetoothClass.deviceClass)
        _uiState.update { state ->
            if (state.availableDevices.any { it.address == domain.address }) {
                state
            } else {
                state.copy(availableDevices = state.availableDevices + domain)
            }
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(deviceFoundReceiver)
        } catch (_: Exception) {}
        stopScan()
    }
}

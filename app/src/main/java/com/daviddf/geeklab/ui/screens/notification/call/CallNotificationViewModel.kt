package com.daviddf.geeklab.ui.screens.notification.call

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallNotificationViewModel : ViewModel() {
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    private val _callerName = MutableStateFlow("John Doe")
    val callerName = _callerName.asStateFlow()

    fun updatePermissionStatus(context: Context) {
        if (Build.VERSION.SDK_INT >= 33) {
            _hasPermission.value = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            _hasPermission.value = true
        }
    }

    fun updateCallerName(name: String) {
        _callerName.value = name
    }

    fun showNormalIncomingCall(context: Context) {
        CallNotificationManager.showIncomingCall(context, _callerName.value)
    }

    fun showNormalOngoingCall(context: Context) {
        CallNotificationManager.showOngoingCall(context, _callerName.value)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startTelecomCall(context: Context) {
        val intent = Intent(context, TelecomCallService::class.java).apply {
            action = TelecomCallService.ACTION_INCOMING_CALL
            putExtra(TelecomCallService.EXTRA_NAME, _callerName.value)
            putExtra(TelecomCallService.EXTRA_URI, "tel:123456789".toUri())
        }
        context.startService(intent)
    }
}

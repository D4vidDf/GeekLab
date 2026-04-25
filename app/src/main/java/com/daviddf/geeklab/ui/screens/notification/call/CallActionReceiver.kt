package com.daviddf.geeklab.ui.screens.notification.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.daviddf.geeklab.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CallActionReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val callerName = intent.getStringExtra("caller_name") ?: context.getString(R.string.unknown_caller)

        if (intent.action == "ACTION_ANSWER") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val repository = TelecomCallRepository.getInstance(context)
                val currentCall = repository.currentCall.value
                
                if (currentCall is TelecomCall.Registered) {
                    GlobalScope.launch {
                        currentCall.processAction(TelecomCallAction.Answer)
                    }
                } else {
                    // Normal call answer simulation
                    CallNotificationManager.showOngoingCall(context, callerName)
                }
            } else {
                CallNotificationManager.showOngoingCall(context, callerName)
            }
            return
        }

        // Handle Decline / End / Hang up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val repository = TelecomCallRepository.getInstance(context)
            val currentCall = repository.currentCall.value
            if (currentCall is TelecomCall.Registered) {
                GlobalScope.launch {
                    currentCall.processAction(
                        TelecomCallAction.Disconnect(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
                    )
                }
            } else {
                CallNotificationManager.cancelNotification(context)
            }
        } else {
            CallNotificationManager.cancelNotification(context)
        }
    }
}

package com.daviddf.geeklab.ui.screens.notification.call

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallControlResult
import androidx.core.telecom.CallControlScope
import androidx.core.telecom.CallsManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class TelecomCallRepository(private val callsManager: CallsManager) {

    companion object {
        @Volatile
        private var instance: TelecomCallRepository? = null

        fun getInstance(context: Context): TelecomCallRepository {
            return instance ?: synchronized(this) {
                instance ?: create(context).also { instance = it }
            }
        }

        private fun create(context: Context): TelecomCallRepository {
            val callsManager = CallsManager(context).apply {
                registerAppWithTelecom(
                    capabilities = CallsManager.CAPABILITY_BASELINE
                )
            }
            return TelecomCallRepository(callsManager)
        }
    }

    private val _currentCall: MutableStateFlow<TelecomCall> = MutableStateFlow(TelecomCall.None)
    val currentCall = _currentCall.asStateFlow()

    var isActivityVisible = false

    suspend fun registerCall(
        context: Context,
        displayName: String,
        address: Uri,
        isIncoming: Boolean
    ) {
        if (_currentCall.value is TelecomCall.Registered) return

        val attributes = CallAttributesCompat(
            displayName = displayName,
            address = address,
            direction = if (isIncoming) CallAttributesCompat.DIRECTION_INCOMING else CallAttributesCompat.DIRECTION_OUTGOING,
            callType = CallAttributesCompat.CALL_TYPE_AUDIO_CALL,
            callCapabilities = CallAttributesCompat.SUPPORTS_SET_INACTIVE
        )

        val actionSource = Channel<TelecomCallAction>()

        try {
            callsManager.addCall(
                attributes,
                onAnswer = { _ ->
                    updateCurrentCall { copy(isActive = true, isOnHold = false) }
                    CallNotificationManager.showOngoingCall(context, attributes.displayName.toString())
                },
                onDisconnect = { cause -> 
                    _currentCall.value = TelecomCall.Unregistered("1", attributes, cause)
                    CallNotificationManager.cancelNotification(context)
                },
                onSetActive = { updateCurrentCall { copy(isActive = true, isOnHold = false) } },
                onSetInactive = { updateCurrentCall { copy(isOnHold = true) } }
            ) {
                launch { processCallActions(context, actionSource.consumeAsFlow()) }

                _currentCall.value = TelecomCall.Registered(
                    id = "1",
                    isActive = false,
                    isOnHold = false,
                    callAttributes = attributes,
                    isMuted = false,
                    errorCode = null,
                    currentCallEndpoint = null,
                    availableCallEndpoints = emptyList(),
                    actionSource = actionSource
                )

                // Show incoming call notification when registered
                CallNotificationManager.showIncomingCall(context, attributes.displayName.toString())

                launch {
                    currentCallEndpoint.collect { endpoint ->
                        updateCurrentCall { copy(currentCallEndpoint = endpoint) }
                    }
                }
                launch {
                    availableEndpoints.collect { endpoints ->
                        updateCurrentCall { copy(availableCallEndpoints = endpoints) }
                    }
                }
                launch {
                    isMuted.collect { muted ->
                        updateCurrentCall { copy(isMuted = muted) }
                    }
                }
            }
        } finally {
            _currentCall.value = TelecomCall.None
        }
    }

    private suspend fun CallControlScope.processCallActions(
        context: Context,
        actionSource: Flow<TelecomCallAction>
    ) {
        actionSource.collect { action ->
            val call = _currentCall.value as? TelecomCall.Registered
            when (action) {
                is TelecomCallAction.Answer -> {
                    val result = answer(CallAttributesCompat.CALL_TYPE_AUDIO_CALL)
                    if (result is CallControlResult.Success) {
                        updateCurrentCall { copy(isActive = true, isOnHold = false) }
                        if (call != null) {
                            CallNotificationManager.showOngoingCall(context, call.callAttributes.displayName.toString())
                        }
                    }
                }
                is TelecomCallAction.Disconnect -> {
                    disconnect(action.cause)
                    if (call != null) {
                        _currentCall.value = TelecomCall.Unregistered(call.id, call.callAttributes, action.cause)
                    }
                    CallNotificationManager.cancelNotification(context)
                }
                is TelecomCallAction.Hold -> {
                    setInactive()
                }
                is TelecomCallAction.Activate -> {
                    setActive()
                }
                else -> {}
            }
        }
    }

    private fun updateCurrentCall(transform: TelecomCall.Registered.() -> TelecomCall) {
        _currentCall.update { call ->
            if (call is TelecomCall.Registered) call.transform() else call
        }
    }
}

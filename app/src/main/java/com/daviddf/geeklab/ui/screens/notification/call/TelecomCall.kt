package com.daviddf.geeklab.ui.screens.notification.call

import android.telecom.DisconnectCause
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallEndpointCompat
import kotlinx.coroutines.channels.Channel

sealed interface TelecomCall {
    data object None : TelecomCall

    data class Registered(
        val id: String,
        val isActive: Boolean,
        val isOnHold: Boolean,
        val callAttributes: CallAttributesCompat,
        val isMuted: Boolean,
        val errorCode: Int?,
        val currentCallEndpoint: CallEndpointCompat?,
        val availableCallEndpoints: List<CallEndpointCompat>,
        val actionSource: Channel<TelecomCallAction>,
    ) : TelecomCall {
        suspend fun processAction(action: TelecomCallAction) {
            actionSource.send(action)
        }
    }

    data class Unregistered(
        val id: String,
        val callAttributes: CallAttributesCompat,
        val cause: DisconnectCause,
    ) : TelecomCall
}

sealed interface TelecomCallAction {
    data object Answer : TelecomCallAction
    data class Disconnect(val cause: DisconnectCause) : TelecomCallAction
    data class SwitchAudioEndpoint(val endpointId: String) : TelecomCallAction
    data object Hold : TelecomCallAction
    data object Activate : TelecomCallAction
    data object ToggleMute : TelecomCallAction
}

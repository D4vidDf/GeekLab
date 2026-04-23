package com.daviddf.geeklab.ui.screens.notification.call

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class TelecomCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupCallActivity()

        val repository = TelecomCallRepository.getInstance(applicationContext)

        setContent {
            GeekLabTheme {
                val currentCall by repository.currentCall.collectAsState()
                
                LaunchedEffect(currentCall) {
                    if (currentCall is TelecomCall.Unregistered || currentCall is TelecomCall.None) {
                        finishAndRemoveTask()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TelecomCallContent(currentCall)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TelecomCallRepository.getInstance(applicationContext).isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        TelecomCallRepository.getInstance(applicationContext).isActivityVisible = false
    }

    private fun setupCallActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val keyguardManager = getSystemService<KeyguardManager>()
        keyguardManager?.requestDismissKeyguard(this, null)
    }
}

@Composable
fun TelecomCallContent(call: TelecomCall) {
    val scope = rememberCoroutineScope()
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    
    val isActive = (call as? TelecomCall.Registered)?.isActive == true

    LaunchedEffect(isActive) {
        if (isActive) {
            val startTime = System.currentTimeMillis()
            while (true) {
                secondsElapsed = (System.currentTimeMillis() - startTime) / 1000
                kotlinx.coroutines.delay(1000)
            }
        } else {
            secondsElapsed = 0L
        }
    }

    val timerText = remember(secondsElapsed) {
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60
        String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Decorative background element for "expressiveness"
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(400.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.extraLarge.copy(
                topStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                topEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
            )
        ) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Larger, more expressive avatar
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                val displayName = when (call) {
                    is TelecomCall.Registered -> call.callAttributes.displayName.toString()
                    else -> stringResource(R.string.unknown_caller)
                }
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = when (call) {
                            is TelecomCall.Registered -> if (call.isActive) stringResource(R.string.ongoing_call) else stringResource(R.string.incoming_call)
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Expressive Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (call is TelecomCall.Registered) {
                    if (!call.isActive) {
                        // Answer Button
                        CallActionButton(
                            icon = Icons.Default.Call,
                            label = stringResource(R.string.answer),
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            onClick = {
                                scope.launch { call.processAction(TelecomCallAction.Answer) }
                            }
                        )
                    }

                    // Disconnect/End Button
                    CallActionButton(
                        icon = Icons.Default.CallEnd,
                        label = stringResource(R.string.end),
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        onClick = {
                            scope.launch { 
                                call.processAction(
                                    TelecomCallAction.Disconnect(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
                                ) 
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.size(88.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(38.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Incoming Call")
@Composable
fun TelecomCallIncomingPreview() {
    GeekLabTheme {
        TelecomCallContent(
            call = TelecomCall.Registered(
                id = "1",
                isActive = false,
                isOnHold = false,
                callAttributes = androidx.core.telecom.CallAttributesCompat(
                    displayName = "John Doe",
                    address = "tel:123456789".toUri(),
                    direction = androidx.core.telecom.CallAttributesCompat.DIRECTION_INCOMING,
                    callType = androidx.core.telecom.CallAttributesCompat.CALL_TYPE_AUDIO_CALL,
                    callCapabilities = androidx.core.telecom.CallAttributesCompat.SUPPORTS_SET_INACTIVE
                ),
                isMuted = false,
                errorCode = null,
                currentCallEndpoint = null,
                availableCallEndpoints = emptyList(),
                actionSource = Channel()
            )
        )
    }
}

@Preview(showBackground = true, name = "Ongoing Call")
@Composable
fun TelecomCallOngoingPreview() {
    GeekLabTheme {
        TelecomCallContent(
            call = TelecomCall.Registered(
                id = "1",
                isActive = true,
                isOnHold = false,
                callAttributes = androidx.core.telecom.CallAttributesCompat(
                    displayName = "John Doe",
                    address = "tel:123456789".toUri(),
                    direction = androidx.core.telecom.CallAttributesCompat.DIRECTION_INCOMING,
                    callType = androidx.core.telecom.CallAttributesCompat.CALL_TYPE_AUDIO_CALL,
                    callCapabilities = androidx.core.telecom.CallAttributesCompat.SUPPORTS_SET_INACTIVE
                ),
                isMuted = false,
                errorCode = null,
                currentCallEndpoint = null,
                availableCallEndpoints = emptyList(),
                actionSource = Channel()
            )
        )
    }
}

package com.daviddf.geeklab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.daviddf.geeklab.notification.Notifiaction
import com.daviddf.geeklab.ui.home.HomeScreen
import com.daviddf.geeklab.ui.theme.GeekLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val onNotificationClick = remember { 
                { startActivity(Intent(this, Notifiaction::class.java)) } 
            }
            val onBatteryClick = remember { { /* TODO */ } }
            val onInfoClick = remember { { /* TODO */ } }
            val onAppsClick = remember { { /* TODO */ } }

            GeekLabTheme {
                HomeScreen(
                    onNotificationClick = onNotificationClick,
                    onBatteryClick = onBatteryClick,
                    onInfoClick = onInfoClick,
                    onAppsClick = onAppsClick
                )
            }
        }
    }
}

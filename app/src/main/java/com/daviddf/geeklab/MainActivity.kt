package com.daviddf.geeklab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daviddf.geeklab.ui.apps.AppsScreen
import com.daviddf.geeklab.ui.battery.BatteryScreen
import com.daviddf.geeklab.ui.feed.NewsScreen
import com.daviddf.geeklab.ui.home.HomeScreen
import com.daviddf.geeklab.ui.info.InfoScreen
import com.daviddf.geeklab.ui.notification.NotificationScreen
import com.daviddf.geeklab.ui.theme.GeekLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            GeekLabTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNotificationClick = { navController.navigate("notifications") },
                            onBatteryClick = { navController.navigate("battery") },
                            onInfoClick = { navController.navigate("info") },
                            onAppsClick = { navController.navigate("apps") },
                            onSeeMoreNewsClick = { navController.navigate("news") }
                        )
                    }

                    composable("news") {
                        NewsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    
                    composable("battery") {
                        BatteryScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("notifications") {
                        NotificationScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("info") {
                        InfoScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("apps") {
                        AppsScreen(onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

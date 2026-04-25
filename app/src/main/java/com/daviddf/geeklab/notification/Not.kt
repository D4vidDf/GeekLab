package com.daviddf.geeklab.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class Not : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val channel1 = NotificationChannel(
            CHANNEL_1_ID,
            "GeekLab",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones GeekLab"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel1)
    }

    companion object {
        const val CHANNEL_1_ID = "GeekLab"
    }
}

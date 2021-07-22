package com.daviddf.geeklabtest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Not extends Application {
    public static final String CHANNEL_1_ID = "GeekLab";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }
    private void createNotificationChannels() {
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_1_ID,
                "GeekLab",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel1.setDescription("Notifiaciones GeekLab");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);

    }
}

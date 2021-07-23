package com.daviddf.geeklabtest;

import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.atomic.AtomicInteger;

import static androidx.core.content.ContextCompat.startActivities;

/**
 * Implementation of App Widget functionality.
 */
public class NotiWidg extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.noti_widg);
        Intent configIntent = new Intent(context, Notifiaction.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.widg, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
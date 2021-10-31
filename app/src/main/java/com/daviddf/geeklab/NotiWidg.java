package com.daviddf.geeklab;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationManagerCompat;

/**
 * Implementation of App Widget functionality.
 */
public class NotiWidg extends AppWidgetProvider {
    int NOTIFICACION_ID=1;

    private NotificationManagerCompat notificationManager;
    private final static String CHANNEL_ID = "Widget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.noti_widg);
        Intent configIntent = new Intent(context, Notifiaction.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.widg, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
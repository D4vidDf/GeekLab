package com.daviddf.geeklabtest;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class Google extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.google);

        Intent Google = new Intent(Intent.ACTION_WEB_SEARCH);
        Intent Assistant = new Intent(Intent.ACTION_VOICE_COMMAND);
        Intent Test = new Intent(Intent.ACTION_CALL_BUTTON);

        PendingIntent search = PendingIntent.getActivity(context, 0, Google, 0);
        PendingIntent assistant = PendingIntent.getActivity(context, 0, Assistant, 0);
        PendingIntent test = PendingIntent.getActivity(context, 0, Test, 0);

        remoteViews.setOnClickPendingIntent(R.id.search, search);
        remoteViews.setOnClickPendingIntent(R.id.asistente, assistant);
        remoteViews.setOnClickPendingIntent(R.id.test,test);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
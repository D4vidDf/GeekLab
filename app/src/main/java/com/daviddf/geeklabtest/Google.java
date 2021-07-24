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

        Intent Google = new Intent(Intent.ACTION_VIEW);
        Google.setClassName("com.google.android.googlequicksearchbox","com.google.android.apps.gsa.queryentry.QueryEntryActivity");
        Intent Assistant = new Intent(Intent.ACTION_VOICE_COMMAND);
        Intent Lens = new Intent(Intent.ACTION_VIEW);
        Lens.setClassName("com.google.ar.lens","com.google.vr.apps.ornament.app.lens.LensLauncherActivity");
        Intent Mail = new Intent(Intent.ACTION_VIEW);
        Mail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmailExternal");

        PendingIntent search = PendingIntent.getActivity(context, 0, Google, 0);
        PendingIntent assistant = PendingIntent.getActivity(context, 0, Assistant, 0);
        PendingIntent lens = PendingIntent.getActivity(context, 0, Lens, 0);
        PendingIntent mail = PendingIntent.getActivity(context, 0, Mail, 0);

        remoteViews.setOnClickPendingIntent(R.id.search, search);
        remoteViews.setOnClickPendingIntent(R.id.asistente, assistant);
        remoteViews.setOnClickPendingIntent(R.id.lens,lens);
        remoteViews.setOnClickPendingIntent(R.id.mail, mail);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
package com.daviddf.geeklab.widgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.daviddf.geeklab.R

/**
 * Implementation of App Widget functionality.
 */
class Google : AppWidgetProvider() {

    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val remoteViews = RemoteViews(context.packageName, R.layout.google)

        val intentGoogle = Intent(Intent.ACTION_VIEW).apply {
            setClassName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.queryentry.QueryEntryActivity")
        }
        val intentAssistant = Intent(Intent.ACTION_VOICE_COMMAND)
        val intentLens = Intent(Intent.ACTION_VIEW).apply {
            setClassName("com.google.ar.lens", "com.google.vr.apps.ornament.app.lens.LensLauncherActivity")
        }
        val intentMail = Intent(Intent.ACTION_VIEW).apply {
            setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmailExternal")
        }

        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val search = PendingIntent.getActivity(context, 0, intentGoogle, flags)
        val assistant = PendingIntent.getActivity(context, 0, intentAssistant, flags)
        val lens = PendingIntent.getActivity(context, 0, intentLens, flags)
        val mail = PendingIntent.getActivity(context, 0, intentMail, flags)

        remoteViews.setOnClickPendingIntent(R.id.search, search)
        remoteViews.setOnClickPendingIntent(R.id.asistente, assistant)
        remoteViews.setOnClickPendingIntent(R.id.lens, lens)
        remoteViews.setOnClickPendingIntent(R.id.mail, mail)

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }
}

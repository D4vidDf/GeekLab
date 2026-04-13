package com.daviddf.geeklab.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.daviddf.geeklab.R
import com.daviddf.geeklab.loop.Countdown

/**
 * Implementation of App Widget functionality.
 */
class LoopWidgt : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val remoteViews = RemoteViews(context.packageName, R.layout.loop_widg)
        val configIntent = Intent(context, Countdown::class.java)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, flags)

        remoteViews.setOnClickPendingIntent(R.id.loop_widg, configPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }
}

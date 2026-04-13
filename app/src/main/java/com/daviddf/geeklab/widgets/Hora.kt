package com.daviddf.geeklab.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.daviddf.geeklab.R
import java.text.DateFormat
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 */
class Hora : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_AUTO_UPDATE_WIDGET = "ACTION_AUTO_UPDATE_WIDGET"

        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val currentTime = Calendar.getInstance().time
            val views = RemoteViews(context.packageName, R.layout.hora)

            val hora = DateFormat.getTimeInstance(DateFormat.SHORT).format(currentTime)
            val splithora = hora.split(":".toRegex()).toTypedArray()

            // Instruct the widget manager to update the widget
            if (splithora.size >= 2) {
                views.setTextViewText(R.id.hora, splithora[0])
                views.setTextViewText(R.id.mm, splithora[1])
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

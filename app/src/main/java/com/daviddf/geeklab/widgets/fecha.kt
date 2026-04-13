package com.daviddf.geeklab.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.daviddf.geeklab.R
import java.text.DateFormat
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 */
class fecha : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val currentTime = Calendar.getInstance().time
            val formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(currentTime)

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.fecha)
            views.setTextViewText(R.id.fecha, formattedDate)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

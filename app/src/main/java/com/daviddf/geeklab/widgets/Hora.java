package com.daviddf.geeklab.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.daviddf.geeklab.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class Hora extends AppWidgetProvider {
    public static String ACTION_AUTO_UPDATE_WIDGET = "ACTION_AUTO_UPDATE_WIDGET";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Date currentTime = Calendar.getInstance().getTime();

        String formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(currentTime);


        CharSequence widgetText = context.getString(R.string.hh);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hora);


        String hora = DateFormat.getTimeInstance(DateFormat.SHORT).format(currentTime);
        String[] splithora = hora.split(":");

        // Instruct the widget manager to update the widget
        views.setTextViewText(R.id.hora, splithora[0]);
        views.setTextViewText(R.id.mm, splithora[1]);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them


        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        }
    }



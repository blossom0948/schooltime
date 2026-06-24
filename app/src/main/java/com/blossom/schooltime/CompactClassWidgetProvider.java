package com.blossom.schooltime;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public final class CompactClassWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, TimetableWidgetProvider.buildCompactWidget(context, id));
        }
        ScheduleNotifier.scheduleNext(context);
    }
}

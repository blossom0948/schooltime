package com.blossom.schooltime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public final class WeeklyCalendarWidgetProvider extends AppWidgetProvider {
    private static final int[][] CELL_IDS = {
            {R.id.week_cell_0_0, R.id.week_cell_0_1, R.id.week_cell_0_2, R.id.week_cell_0_3, R.id.week_cell_0_4},
            {R.id.week_cell_1_0, R.id.week_cell_1_1, R.id.week_cell_1_2, R.id.week_cell_1_3, R.id.week_cell_1_4},
            {R.id.week_cell_2_0, R.id.week_cell_2_1, R.id.week_cell_2_2, R.id.week_cell_2_3, R.id.week_cell_2_4},
            {R.id.week_cell_3_0, R.id.week_cell_3_1, R.id.week_cell_3_2, R.id.week_cell_3_3, R.id.week_cell_3_4},
            {R.id.week_cell_4_0, R.id.week_cell_4_1, R.id.week_cell_4_2, R.id.week_cell_4_3, R.id.week_cell_4_4},
            {R.id.week_cell_5_0, R.id.week_cell_5_1, R.id.week_cell_5_2, R.id.week_cell_5_3, R.id.week_cell_5_4},
            {R.id.week_cell_6_0, R.id.week_cell_6_1, R.id.week_cell_6_2, R.id.week_cell_6_3, R.id.week_cell_6_4}
    };
    private static final int[] DAY_IDS = {
            R.id.week_day_0,
            R.id.week_day_1,
            R.id.week_day_2,
            R.id.week_day_3,
            R.id.week_day_4
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, buildWidget(context, id));
        }
        ScheduleNotifier.scheduleNext(context);
    }

    static RemoteViews buildWidget(Context context, int appWidgetId) {
        ScheduleStore store = new ScheduleStore(context);
        ScheduleSnapshot snapshot = ScheduleSnapshot.from(store, java.util.Calendar.getInstance());
        WidgetPrefs prefs = WidgetPrefs.load(context, appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weekly_calendar);
        views.setInt(R.id.week_widget_root, "setBackgroundResource", prefs.dark ? R.drawable.widget_background_dark : R.drawable.widget_background);
        views.setTextViewText(R.id.week_widget_title, "주간 시간표");
        views.setTextViewText(R.id.week_widget_summary, prefs.compactTitle(snapshot));
        for (int day = 0; day < ScheduleStore.DAYS; day++) {
            views.setTextViewText(DAY_IDS[day], ScheduleStore.DAY_NAMES[day]);
        }
        for (int period = 0; period < ScheduleStore.PERIODS; period++) {
            for (int day = 0; day < ScheduleStore.DAYS; day++) {
                Period item = store.getPeriod(day, period);
                views.setTextViewText(CELL_IDS[period][day], item.subject);
                boolean current = snapshot.current != null && snapshot.current.day == item.day && snapshot.current.index == item.index;
                views.setInt(CELL_IDS[period][day], "setBackgroundResource", current ? R.drawable.widget_row_active : R.drawable.widget_row_normal);
            }
        }
        views.setOnClickPendingIntent(R.id.week_widget_root, openAppIntent(context));
        return views;
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

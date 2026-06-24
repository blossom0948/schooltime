package com.blossom.schooltime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

public final class TimetableWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, buildFullWidget(context));
        }
        ScheduleNotifier.scheduleNext(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        updateAll(context);
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName full = new ComponentName(context, TimetableWidgetProvider.class);
        manager.updateAppWidget(full, buildFullWidget(context));
        ComponentName compact = new ComponentName(context, CompactClassWidgetProvider.class);
        manager.updateAppWidget(compact, buildCompactWidget(context));
    }

    static RemoteViews buildCompactWidget(Context context) {
        ScheduleSnapshot snapshot = ScheduleSnapshot.from(new ScheduleStore(context), Calendar.getInstance());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_compact_class);
        views.setTextViewText(R.id.widget_compact_title, snapshot.current == null ? snapshot.title : snapshot.current.subject);
        views.setTextViewText(R.id.widget_compact_subtitle, snapshot.current == null ? snapshot.subtitle : snapshot.remainingMinutes + "분 남음");
        views.setOnClickPendingIntent(R.id.widget_compact_root, openAppIntent(context));
        return views;
    }

    private static RemoteViews buildFullWidget(Context context) {
        ScheduleStore store = new ScheduleStore(context);
        Calendar now = Calendar.getInstance();
        int today = Math.max(0, store.getTodaySchoolDay(now));
        ScheduleSnapshot snapshot = ScheduleSnapshot.from(store, now);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today_schedule);
        views.setTextViewText(R.id.widget_today_title, "오늘 시간표");
        views.setTextViewText(R.id.widget_today_next, snapshot.compact);

        int[] subjectIds = {
                R.id.widget_period_1_subject,
                R.id.widget_period_2_subject,
                R.id.widget_period_3_subject,
                R.id.widget_period_4_subject,
                R.id.widget_period_5_subject,
                R.id.widget_period_6_subject,
                R.id.widget_period_7_subject
        };
        int[] timeIds = {
                R.id.widget_period_1_time,
                R.id.widget_period_2_time,
                R.id.widget_period_3_time,
                R.id.widget_period_4_time,
                R.id.widget_period_5_time,
                R.id.widget_period_6_time,
                R.id.widget_period_7_time
        };
        int[] rowIds = {
                R.id.widget_period_1,
                R.id.widget_period_2,
                R.id.widget_period_3,
                R.id.widget_period_4,
                R.id.widget_period_5,
                R.id.widget_period_6,
                R.id.widget_period_7
        };

        List<Period> periods = store.getDay(today);
        for (int i = 0; i < ScheduleStore.PERIODS; i++) {
            Period period = periods.get(i);
            views.setTextViewText(subjectIds[i], period.subject);
            views.setTextViewText(timeIds[i], period.timeText());
            boolean current = snapshot.current != null && snapshot.current.day == period.day && snapshot.current.index == period.index;
            views.setInt(rowIds[i], "setBackgroundResource", current ? R.drawable.widget_row_active : R.drawable.widget_row_normal);
        }
        views.setOnClickPendingIntent(R.id.widget_today_root, openAppIntent(context));
        return views;
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

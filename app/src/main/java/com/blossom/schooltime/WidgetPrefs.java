package com.blossom.schooltime;

import android.content.Context;
import android.content.SharedPreferences;

final class WidgetPrefs {
    static final int MODE_NEXT = 0;
    static final int MODE_SUBJECT_ONLY = 1;
    static final int MODE_PERIOD_SUBJECT = 2;

    final boolean dark;
    final int mode;
    final boolean showWeekday;
    final boolean showPeriod;
    final boolean showTime;

    private WidgetPrefs(boolean dark, int mode, boolean showWeekday, boolean showPeriod, boolean showTime) {
        this.dark = dark;
        this.mode = mode;
        this.showWeekday = showWeekday;
        this.showPeriod = showPeriod;
        this.showTime = showTime;
    }

    static WidgetPrefs load(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("school_time_widget_" + appWidgetId, Context.MODE_PRIVATE);
        return new WidgetPrefs(
                prefs.getBoolean("dark", false),
                prefs.getInt("mode", MODE_NEXT),
                prefs.getBoolean("show_weekday", true),
                prefs.getBoolean("show_period", true),
                prefs.getBoolean("show_time", true)
        );
    }

    static void save(Context context, int appWidgetId, boolean dark, int mode, boolean showWeekday, boolean showPeriod, boolean showTime) {
        context.getSharedPreferences("school_time_widget_" + appWidgetId, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("dark", dark)
                .putInt("mode", mode)
                .putBoolean("show_weekday", showWeekday)
                .putBoolean("show_period", showPeriod)
                .putBoolean("show_time", showTime)
                .apply();
    }

    String compactTitle(ScheduleSnapshot snapshot) {
        Period target = snapshot.current != null ? snapshot.current : snapshot.next;
        if (target == null) {
            return "수업 없음";
        }
        if (mode == MODE_SUBJECT_ONLY) {
            return target.subject;
        }
        if (mode == MODE_PERIOD_SUBJECT) {
            return (target.index + 1) + "교시: " + target.subject;
        }
        return snapshot.current == null ? "다음 교시: " + target.subject : target.subject;
    }

    String compactSubtitle(ScheduleSnapshot snapshot) {
        Period target = snapshot.current != null ? snapshot.current : snapshot.next;
        if (target == null || mode == MODE_SUBJECT_ONLY) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (showWeekday) {
            builder.append(ScheduleStore.DAY_NAMES[target.day]).append("요일");
        }
        if (showPeriod) {
            appendDivider(builder);
            builder.append(target.index + 1).append("교시");
        }
        if (showTime) {
            appendDivider(builder);
            builder.append(target.timeText());
        }
        if (snapshot.current != null) {
            appendDivider(builder);
            builder.append(snapshot.remainingMinutes).append("분 남음");
        }
        return builder.toString();
    }

    private void appendDivider(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(" · ");
        }
    }
}

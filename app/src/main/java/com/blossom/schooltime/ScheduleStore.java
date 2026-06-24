package com.blossom.schooltime;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

final class ScheduleStore {
    static final int DAYS = 5;
    static final int PERIODS = 7;
    static final String[] DAY_NAMES = {"월", "화", "수", "목", "금"};

    private static final String PREFS = "school_time_schedule";
    private static final String[] DEFAULT_SUBJECTS = {
            "국어", "수학", "영어", "과학", "사회", "체육", "자습"
    };
    private static final int[][] DEFAULT_TIMES = {
            {8 * 60 + 50, 9 * 60 + 35},
            {9 * 60 + 45, 10 * 60 + 30},
            {10 * 60 + 40, 11 * 60 + 25},
            {11 * 60 + 35, 12 * 60 + 20},
            {13 * 60 + 10, 13 * 60 + 55},
            {14 * 60 + 5, 14 * 60 + 50},
            {15 * 60, 15 * 60 + 45}
    };

    private final SharedPreferences prefs;

    ScheduleStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("seeded", false)) {
            seed();
        }
    }

    List<Period> getDay(int day) {
        ArrayList<Period> periods = new ArrayList<>();
        for (int i = 0; i < PERIODS; i++) {
            periods.add(getPeriod(day, i));
        }
        return periods;
    }

    Period getPeriod(int day, int index) {
        int start = prefs.getInt(key(day, index, "start"), DEFAULT_TIMES[index][0]);
        int end = prefs.getInt(key(day, index, "end"), DEFAULT_TIMES[index][1]);
        String subject = prefs.getString(key(day, index, "subject"), DEFAULT_SUBJECTS[(index + day) % DEFAULT_SUBJECTS.length]);
        String room = prefs.getString(key(day, index, "room"), "");
        return new Period(day, index, start, end, subject == null ? "" : subject, room == null ? "" : room);
    }

    void savePeriod(Period period) {
        prefs.edit()
                .putInt(key(period.day, period.index, "start"), period.startMinutes)
                .putInt(key(period.day, period.index, "end"), period.endMinutes)
                .putString(key(period.day, period.index, "subject"), period.subject)
                .putString(key(period.day, period.index, "room"), period.room)
                .apply();
    }

    Period findCurrentOrNext(Calendar now) {
        int day = calendarToSchoolDay(now);
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (day >= 0) {
            for (Period period : getDay(day)) {
                if (!period.isEmpty() && period.endMinutes >= minutes) {
                    return period;
                }
            }
        }
        for (int offset = 1; offset <= 7; offset++) {
            Calendar next = (Calendar) now.clone();
            next.add(Calendar.DAY_OF_YEAR, offset);
            int nextDay = calendarToSchoolDay(next);
            if (nextDay < 0) {
                continue;
            }
            for (Period period : getDay(nextDay)) {
                if (!period.isEmpty()) {
                    return period;
                }
            }
        }
        return null;
    }

    int getTodaySchoolDay(Calendar calendar) {
        return calendarToSchoolDay(calendar);
    }

    String formatDayLine(int day) {
        if (day < 0 || day >= DAYS) {
            return "오늘은 등록된 수업이 없습니다.";
        }
        StringBuilder builder = new StringBuilder();
        for (Period period : getDay(day)) {
            if (period.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(period.index + 1)
                    .append("교시 ")
                    .append(period.subject)
                    .append("  ")
                    .append(period.timeText());
            if (!period.room.trim().isEmpty()) {
                builder.append("  ").append(period.room);
            }
        }
        return builder.length() == 0 ? "오늘은 등록된 수업이 없습니다." : builder.toString();
    }

    void resetDefaults() {
        prefs.edit().clear().apply();
        seed();
    }

    private void seed() {
        SharedPreferences.Editor editor = prefs.edit();
        for (int day = 0; day < DAYS; day++) {
            for (int index = 0; index < PERIODS; index++) {
                editor.putInt(key(day, index, "start"), DEFAULT_TIMES[index][0]);
                editor.putInt(key(day, index, "end"), DEFAULT_TIMES[index][1]);
                editor.putString(key(day, index, "subject"), DEFAULT_SUBJECTS[(index + day) % DEFAULT_SUBJECTS.length]);
                editor.putString(key(day, index, "room"), "");
            }
        }
        editor.putBoolean("seeded", true).apply();
    }

    private static int calendarToSchoolDay(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.MONDAY) return 0;
        if (day == Calendar.TUESDAY) return 1;
        if (day == Calendar.WEDNESDAY) return 2;
        if (day == Calendar.THURSDAY) return 3;
        if (day == Calendar.FRIDAY) return 4;
        return -1;
    }

    private static String key(int day, int index, String field) {
        return "d" + day + "_p" + index + "_" + field;
    }
}

package com.blossom.schooltime;

import java.util.Calendar;

final class ScheduleSnapshot {
    final Period current;
    final Period next;
    final int remainingMinutes;
    final int progressPercent;
    final String title;
    final String subtitle;
    final String compact;

    private ScheduleSnapshot(Period current, Period next, int remainingMinutes, int progressPercent, String title, String subtitle, String compact) {
        this.current = current;
        this.next = next;
        this.remainingMinutes = remainingMinutes;
        this.progressPercent = progressPercent;
        this.title = title;
        this.subtitle = subtitle;
        this.compact = compact;
    }

    static ScheduleSnapshot from(ScheduleStore store, Calendar now) {
        int day = store.getTodaySchoolDay(now);
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        Period current = null;
        if (day >= 0) {
            for (Period period : store.getDay(day)) {
                if (!period.isEmpty() && minutes >= period.startMinutes && minutes < period.endMinutes) {
                    current = period;
                    break;
                }
            }
        }

        Period next = store.findCurrentOrNext(now);
        if (current != null) {
            int remaining = Math.max(0, current.endMinutes - minutes);
            int total = Math.max(1, current.endMinutes - current.startMinutes);
            int elapsed = Math.max(0, minutes - current.startMinutes);
            int progress = Math.min(100, Math.max(0, elapsed * 100 / total));
            String subtitle = (current.index + 1) + "교시 · " + current.timeText();
            if (!current.room.trim().isEmpty()) {
                subtitle += " · " + current.room;
            }
            return new ScheduleSnapshot(
                    current,
                    next,
                    remaining,
                    progress,
                    current.subject,
                    subtitle,
                    current.subject + " | " + remaining + "분 남음"
            );
        }

        if (next != null) {
            String subtitle = ScheduleStore.DAY_NAMES[next.day] + "요일 " + (next.index + 1) + "교시 · " + next.timeText();
            if (!next.room.trim().isEmpty()) {
                subtitle += " · " + next.room;
            }
            return new ScheduleSnapshot(
                    null,
                    next,
                    0,
                    0,
                    "다음 교시: " + next.subject,
                    subtitle,
                    next.subject + " 준비"
            );
        }

        return new ScheduleSnapshot(
                null,
                null,
                0,
                0,
                "등록된 시간표가 없습니다",
                "앱에서 시간표를 입력해 주세요",
                "수업 없음"
        );
    }
}

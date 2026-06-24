package com.blossom.schooltime;

final class Period {
    final int day;
    final int index;
    final int startMinutes;
    final int endMinutes;
    final String subject;
    final String room;

    Period(int day, int index, int startMinutes, int endMinutes, String subject, String room) {
        this.day = day;
        this.index = index;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.subject = subject;
        this.room = room;
    }

    boolean isEmpty() {
        return subject.trim().isEmpty();
    }

    String timeText() {
        return formatMinutes(startMinutes) + "-" + formatMinutes(endMinutes);
    }

    static String formatMinutes(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    static int parseMinutes(String value, int fallback) {
        try {
            String[] parts = value.trim().split(":");
            if (parts.length != 2) {
                return fallback;
            }
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return fallback;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}

package com.blossom.schooltime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;

final class ScheduleNotifier {
    private static final String CHANNEL_ID = "school_time_next_period";
    private static final int NOTIFICATION_ID = 240624;

    private ScheduleNotifier() {
    }

    static void update(Context context) {
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        ensureChannel(manager);
        manager.notify(NOTIFICATION_ID, buildNotification(context));
        scheduleNext(context);
    }

    static void cancel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.cancel(NOTIFICATION_ID);
    }

    static void scheduleNext(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = alarmIntent(context);
        long triggerAt = System.currentTimeMillis() + 60_000L;
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, intent);
        }
    }

    private static Notification buildNotification(Context context) {
        ScheduleStore store = new ScheduleStore(context);
        Calendar now = Calendar.getInstance();
        Period next = store.findCurrentOrNext(now);
        int today = store.getTodaySchoolDay(now);
        String title;
        String text;
        if (next == null) {
            title = "등록된 시간표가 없습니다";
            text = "앱에서 시간표를 입력해 주세요";
        } else {
            String dayName = ScheduleStore.DAY_NAMES[next.day];
            title = "다음 교시: " + next.subject;
            text = dayName + " " + (next.index + 1) + "교시 · " + next.timeText();
            if (!next.room.trim().isEmpty()) {
                text += " · " + next.room;
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_schedule)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(store.formatDayLine(today)))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_STATUS)
                .build();
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void ensureChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "다음 교시",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("잠금화면에서 다음 교시와 오늘 시간표를 보여줍니다.");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);
    }
}

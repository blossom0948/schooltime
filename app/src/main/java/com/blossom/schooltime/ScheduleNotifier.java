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
import android.graphics.Color;
import android.os.Build;

import java.util.Calendar;

final class ScheduleNotifier {
    private static final String CHANNEL_ID = "school_time_next_period";
    static final int NOTIFICATION_ID = 240624;

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

    static Notification buildNotification(Context context) {
        ScheduleStore store = new ScheduleStore(context);
        Calendar now = Calendar.getInstance();
        ScheduleSnapshot snapshot = ScheduleSnapshot.from(store, now);
        int today = store.getTodaySchoolDay(now);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_schedule)
                .setContentTitle(snapshot.title)
                .setContentText(snapshot.current == null ? snapshot.subtitle : snapshot.compact)
                .setStyle(new Notification.BigTextStyle().bigText(store.formatDayLine(today)))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(Color.rgb(255, 122, 0))
                .setCategory(Notification.CATEGORY_STATUS);

        if (snapshot.current != null) {
            builder.setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(System.currentTimeMillis() + snapshot.remainingMinutes * 60_000L);
            if (Build.VERSION.SDK_INT >= 36) {
                builder.setStyle(new Notification.ProgressStyle()
                        .setProgress(snapshot.progressPercent)
                        .setProgressTrackerIcon(android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_stat_schedule))
                        .setStyledByProgress(false));
            }
        }
        return builder.build();
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

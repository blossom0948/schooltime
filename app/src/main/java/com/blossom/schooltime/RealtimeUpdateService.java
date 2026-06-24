package com.blossom.schooltime;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public final class RealtimeUpdateService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            ScheduleNotifier.update(RealtimeUpdateService.this);
            TimetableWidgetProvider.updateAll(RealtimeUpdateService.this);
            handler.postDelayed(this, nextMinuteDelay());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ScheduleNotifier.NOTIFICATION_ID, ScheduleNotifier.buildNotification(this));
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long nextMinuteDelay() {
        long now = System.currentTimeMillis();
        return 60_000L - (now % 60_000L) + 250L;
    }
}

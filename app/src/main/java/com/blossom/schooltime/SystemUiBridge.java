package com.blossom.schooltime;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

final class SystemUiBridge {
    private SystemUiBridge() {
    }

    static void start(Context context) {
        Intent intent = new Intent(context, RealtimeUpdateService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}

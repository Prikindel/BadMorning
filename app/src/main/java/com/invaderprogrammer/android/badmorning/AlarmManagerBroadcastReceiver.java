package com.invaderprogrammer.android.badmorning;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint({"LongLogTag", "WakelockTimeout"})
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        wl.acquire();



        if (!PreferenceSettings.getActivityOpen(context)) {
            context.startActivity(new Intent(context, AlarmManagerActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        wl.release();
    }
}

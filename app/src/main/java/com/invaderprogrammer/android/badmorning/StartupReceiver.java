package com.invaderprogrammer.android.badmorning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SetAlarmManager.setAlarmManager(context, false);
            DayOfTheWeek dayOfTheWeek = new DayOfTheWeek(context, SetAlarmManager.isCalendarOfDay());
            List<DayAlarm> alarms = dayOfTheWeek.getAlarms();
            for (DayAlarm dayAlarm : alarms) {
                if (dayAlarm.isEnable()) {
                    NotificationAlarm.notificationAlarm(context, dayAlarm);
                }
            }
        }
    }
}

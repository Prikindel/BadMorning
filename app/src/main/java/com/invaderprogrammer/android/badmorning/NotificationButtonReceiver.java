package com.invaderprogrammer.android.badmorning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;

public class NotificationButtonReceiver extends BroadcastReceiver {
    public static final String UUIDDAY = "com.invaderprogrammer.android.badmorning.UUIDDay";

    public NotificationButtonReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DayOfTheWeek dayOfTheWeek = new DayOfTheWeek(context, 0);
        DayAlarm dayAlarm = dayOfTheWeek.getAlarm(UUID.fromString(intent.getStringExtra(UUIDDAY)));
        NotificationAlarm.getNotificationChannel(context, dayAlarm);
        if (dayAlarm.isEnable()) {
            NotificationAlarm.clearNotificationEnable(dayAlarm.getKeyId());
            dayOfTheWeek.deleteAlarm(dayAlarm);
        } else {
            NotificationAlarm.clearNotification(dayAlarm.getKeyId());
            dayAlarm.setModeAlarm(false);
            dayOfTheWeek.updateAlarmInDataBase(dayAlarm);
        }
        SetAlarmManager.setAlarmManager(context, true);
    }
}

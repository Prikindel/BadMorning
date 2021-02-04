package com.invaderprogrammer.android.badmorning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DayAlarm dayAlarm;
        String stringUuid = PreferenceSettings.getUuidAlarm(context);
        if (stringUuid != null) {
            dayAlarm = new DayOfTheWeek(context, 1).getAlarm(UUID.fromString(stringUuid));
            NotificationAlarm.notificationAlarm(context, dayAlarm);
        }
    }
}

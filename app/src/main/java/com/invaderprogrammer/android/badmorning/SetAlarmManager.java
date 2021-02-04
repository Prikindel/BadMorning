package com.invaderprogrammer.android.badmorning;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

public class SetAlarmManager {

    public static DayAlarm getAlarmNear(DayOfTheWeek dayOfTheWeek, int day) {

        List<DayAlarm> alarms = dayOfTheWeek.getAlarms();
        for (DayAlarm dayAlarm : alarms) {
            int differenceDays = 0;
            if (dayOfTheWeek.getDAYOFTHEWEEK() < isCalendarOfDay()) {
                differenceDays = 7 - isCalendarOfDay() + dayOfTheWeek.getDAYOFTHEWEEK();
            } else {
                differenceDays = dayOfTheWeek.getDAYOFTHEWEEK() - isCalendarOfDay();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, dayAlarm.getTime().get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, dayAlarm.getTime().get(Calendar.MINUTE));
            dayAlarm.setTime(calendar);

            if (dayOfTheWeek.getDAYOFTHEWEEK() != isCalendarOfDay() && dayAlarm.isModeAlarm()) {
                calendar.setTimeInMillis(dayAlarm.getTime().getTimeInMillis() + differenceDays * 24 * 3600 * 1000);
                dayAlarm.setTime(calendar);
                return dayAlarm;
            } else if (dayAlarm.getTime().getTimeInMillis() / 1000 - System.currentTimeMillis() / 1000 > 0 && dayAlarm.isModeAlarm()) {
                return dayAlarm;
            } else if (day == 7 && dayAlarm.isModeAlarm()) {
                calendar.setTimeInMillis(dayAlarm.getTime().getTimeInMillis() + 7 * 24 * 3600 * 1000);
                dayAlarm.setTime(calendar);
                return dayAlarm;
            }
        }
        return null;
    }
    
    public static boolean setAlarmManager(Context context, boolean delete) {
        DayOfTheWeek dayOfTheWeek = new DayOfTheWeek(context, isCalendarOfDay());
        int dayoftheweek = dayOfTheWeek.getDAYOFTHEWEEK();
        DayAlarm dayAlarm = null;
        for (int day = 0; day < 8; day++) {
            dayAlarm = getAlarmNear(dayOfTheWeek, day);
            if (dayAlarm != null) {
                break;
            } else {
                if (dayOfTheWeek.getDAYOFTHEWEEK() == 6) {
                    dayoftheweek = 0;
                } else {
                    dayoftheweek++;
                }
                dayOfTheWeek = new DayOfTheWeek(context, dayoftheweek);
            }
        }

        if(dayAlarm != null) {
            setOnetimeTimer(context, dayAlarm);
            if (dayAlarm.isEnable()) {
                return true;
            } else {
                return false;
            }
        } else if (delete){
            cancelOnetimeTimer(context);
        } else {
        }
        return false;
    }

    public static int isCalendarOfDay() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayOfWeek = 0;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = 1;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = 2;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = 3;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = 4;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = 5;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = 6;
                break;
        }
        return dayOfWeek;
    }


    private static void setOnetimeTimer(Context context, DayAlarm dayAlarm){
        if (dayAlarm.getTime().getTimeInMillis() - Calendar.getInstance().getTimeInMillis() <= 1800000) {
            NotificationAlarm.notificationAlarm(context, dayAlarm);
        } else {
            AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pi = PendingIntent.getBroadcast(context, 1, new Intent(context, NotificationAlarmReceiver.class), 0);
            am.setExact(AlarmManager.RTC_WAKEUP, dayAlarm.getTime().getTime().getTime() - 1800000, pi);
        }

        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PreferenceSettings.setUuidAlarm(context, dayAlarm.getId());

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmManagerBroadcastReceiver.class), 0);
        am.setExact(AlarmManager.RTC_WAKEUP, dayAlarm.getTime().getTime().getTime(), pi);
    }

    private static void  cancelOnetimeTimer(Context context) {
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmManagerBroadcastReceiver.class), 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PreferenceSettings.clearUuidAlarm(context);
        am.cancel(pi);
    }


}

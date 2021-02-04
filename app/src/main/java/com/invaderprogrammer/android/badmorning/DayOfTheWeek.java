package com.invaderprogrammer.android.badmorning;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.invaderprogrammer.android.badmorning.database.AlarmBaseHelper;
import com.invaderprogrammer.android.badmorning.database.AlarmCursorWrapper;
import com.invaderprogrammer.android.badmorning.database.AlarmDbSchema.AlarmTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DayOfTheWeek {

    private int DAYOFTHEWEEK;
    private List<DayAlarm> mAlarms;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public DayOfTheWeek (Context context, int day) {
        mContext = context.getApplicationContext();
        mDatabase = new AlarmBaseHelper(mContext)
                .getWritableDatabase();
        DAYOFTHEWEEK = day;
        mAlarms = new ArrayList<>();
    }

    public DayAlarm getAlarm(int i) {
        return mAlarms.get(i);
    }

    public List<DayAlarm> getAlarmNotEnable() {
        List<DayAlarm> alarms = new ArrayList<>();
        for (int k = 0; k < mAlarms.size(); k++){
            if (!mAlarms.get(k).isEnable()) {
                alarms.add(mAlarms.get(k));
                break;
            }
        }
        for (int k = mAlarms.size() - 1; k >= 0; k++) {
            if (!mAlarms.get(k).isEnable()) {
                alarms.add(mAlarms.get(k));
                break;
            }
        }
        return alarms;
    }

    public int getPosition(UUID id) {
        int i = 0;
        for (DayAlarm dayAlarm : mAlarms) {
            if(dayAlarm.getId().compareTo(id) == 0) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public DayAlarm getAlarm(UUID id) {
        AlarmCursorWrapper cursor = queryAlarms(
                AlarmTable.Cols.UUID + " = ?",
                new String[]{id.toString()});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getAlarm();
        } finally {
            cursor.close();
        }
    }

    public List<DayAlarm> getAlarms() {
        mAlarms.clear();
        AlarmCursorWrapper cursor = queryAlarms(
                AlarmTable.Cols.DAY + " = ?",
                new String[]{String.valueOf(DAYOFTHEWEEK)});
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mAlarms.add(cursor.getAlarm());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        Collections.sort(mAlarms, new Comparator<DayAlarm>() {
            @Override
            public int compare(DayAlarm o1, DayAlarm o2) {
                if (o1.getTime().get(Calendar.HOUR_OF_DAY) == o2.getTime().get(Calendar.HOUR_OF_DAY)) {
                    return o1.getTime().get(Calendar.MINUTE) > o2.getTime().get(Calendar.MINUTE) ? 1 : -1;
                } else {
                    return o1.getTime().get(Calendar.HOUR_OF_DAY) > o2.getTime().get(Calendar.HOUR_OF_DAY) ? 1 : -1;
                }
            }
        });

        return mAlarms;
    }

    public List<DayAlarm> getAlarmsWithoutRepiting() {
        List<DayAlarm> alarms = getAlarms();
        List<DayAlarm> alrms = new ArrayList<>();
        for (DayAlarm dayAlarm : alarms) {
            if(!dayAlarm.isEnable()) {
                alrms.add(dayAlarm);
            }
        }
        return alrms;
    }

    public int size() {
        return mAlarms.size();
    }

    public void setAlarm(DayAlarm alarm) {
        ContentValues values = getContentValues(alarm, false);

        mDatabase.insert(AlarmTable.NAME, null, values);
    }

    public void updateAlarm(DayAlarm alarm) {
        updateAlarmInDataBase(alarm);

        getAlarms();
    }

    public void updateAlarmInDataBase(DayAlarm alarm) {
        String uuidString = alarm.getId().toString();
        ContentValues values = getContentValues(alarm, true);

        updateNotifications(alarm);

        mDatabase.update(AlarmTable.NAME, values,
                AlarmTable.Cols.UUID + " = ?",
                new String[] {uuidString});
    }

    private void updateNotifications(DayAlarm dayAlarm) {
        if (dayAlarm.isEnable()) {
            NotificationAlarm.clearNotificationEnable(dayAlarm.getKeyId());
        } else {
            NotificationAlarm.clearNotification(dayAlarm.getKeyId());
        }
    }

    private AlarmCursorWrapper queryAlarms(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                AlarmTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new AlarmCursorWrapper(cursor);
    }

    public void deleteAlarm(DayAlarm alarm) {
        updateNotifications(alarm);
        mDatabase.delete(AlarmTable.NAME,
                AlarmTable.Cols.UUID + "= ?",
                new String[] { alarm.getId().toString() }
        );
        getAlarms();
    }

    public void clearAlarm() {
        for (DayAlarm dayAlarm : mAlarms) {
            updateNotifications(dayAlarm);
        }

        mDatabase.delete(AlarmTable.NAME,
                AlarmTable.Cols.DAY + "= ?",
                new String[]{String.valueOf(DAYOFTHEWEEK)}
        );
        getAlarms();
    }

    public int getDAYOFTHEWEEK() {
        return DAYOFTHEWEEK;
    }

    public void setDAYOFTHEWEEK(int value) {
        DAYOFTHEWEEK = value;
    }

    public DayAlarm setAlarm(Calendar time) {
        DayAlarm dayAlarm = new DayAlarm(time, true);
        dayAlarm.setVibration(true);
        setAlarm(dayAlarm);
        return dayAlarm;
    }

    private ContentValues getContentValues(DayAlarm dayAlarm, boolean update) {
        ContentValues values = new ContentValues();
        values.put(AlarmTable.Cols.UUID, dayAlarm.getId().toString());
        if (!update) {
            values.put(AlarmTable.Cols.DAY, String.valueOf(DAYOFTHEWEEK));
        }
        values.put(AlarmTable.Cols.TIME, dayAlarm.getTime().getTime().getTime());
        values.put(AlarmTable.Cols.MODE, dayAlarm.isModeAlarm() ? 1 : 0);
        values.put(AlarmTable.Cols.VIBRATION, dayAlarm.isVibration() ? 1 : 0);
        values.put(AlarmTable.Cols.DELETED, dayAlarm.isDelete() ? 1 : 0);
        values.put(AlarmTable.Cols.MUSIC, dayAlarm.getMusic().toString());
        values.put(AlarmTable.Cols.DESCRIPTION, dayAlarm.getDescription());
        values.put(AlarmTable.Cols.ENABLED, dayAlarm.isEnable() ? 1 : 0);
        return values;
    }
}

package com.invaderprogrammer.android.badmorning.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import com.invaderprogrammer.android.badmorning.DayAlarm;
import com.invaderprogrammer.android.badmorning.database.AlarmDbSchema.AlarmTable;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AlarmCursorWrapper extends CursorWrapper {
    public AlarmCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public DayAlarm getAlarm() {
        String uuidString = getString(getColumnIndex(AlarmTable.Cols.UUID));
        long time = getLong(getColumnIndex(AlarmTable.Cols.TIME));
        int isMode = getInt(getColumnIndex(AlarmTable.Cols.MODE));
        int isVibration = getInt(getColumnIndex(AlarmTable.Cols.VIBRATION));
        int isDelete = getInt(getColumnIndex(AlarmTable.Cols.DELETED));
        Uri music = Uri.parse(getString(getColumnIndex(AlarmTable.Cols.MUSIC)));
        String description = getString(getColumnIndex(AlarmTable.Cols.DESCRIPTION));
        int isEnabled = getInt(getColumnIndex(AlarmTable.Cols.ENABLED));
        int keyId = getInt(getColumnIndex("_id"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));

        DayAlarm dayAlarm = new DayAlarm(UUID.fromString(uuidString));
        dayAlarm.setTime(calendar);
        dayAlarm.setModeAlarm(isMode != 0);
        dayAlarm.setVibration(isVibration != 0);
        dayAlarm.setDelete(isDelete != 0);
        dayAlarm.setMusic(music);
        dayAlarm.setDescription(description);
        dayAlarm.setEnable(isEnabled != 0);
        dayAlarm.setKeyId(keyId);

        return dayAlarm;
    }
}

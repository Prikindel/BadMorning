package com.invaderprogrammer.android.badmorning;

import android.media.RingtoneManager;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class DayAlarm {
    private int mKeyId;
    private UUID mId;
    private Calendar mTime;
    private boolean mModeAlarm;
    private boolean mVibration;
    private boolean mDelete;
    private Uri mMusic;
    private String mDescription;
    private boolean mEnable;

    public DayAlarm() {
        mId = UUID.randomUUID();
        mTime = Calendar.getInstance();
        mTime.set(Calendar.SECOND, 0);
        mModeAlarm = false;
        setDefaultSettingsOfAlarmDay();
    }

    public DayAlarm(Calendar time) {
        mId = UUID.randomUUID();
        mTime = time;
        mTime.set(Calendar.SECOND, 0);
        mModeAlarm = false;
        setDefaultSettingsOfAlarmDay();
    }

    public DayAlarm(Calendar time, boolean modeAlarm) {
        mId = UUID.randomUUID();
        mTime = time;
        mTime.set(Calendar.SECOND, 0);
        mModeAlarm = modeAlarm;
        setDefaultSettingsOfAlarmDay();
    }

    private void setDefaultSettingsOfAlarmDay() {
        mVibration = false;
        mDelete = false;
        mMusic = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mDescription = "";
        mEnable = false;
        mKeyId = -1;
    }

    public DayAlarm(UUID uuid) {
        mId = uuid;
    }

    public String toString() {
        return new SimpleDateFormat("HH:mm").format(mTime.getTime());
    }

    public Calendar getTime() {
        return mTime;
    }

    public void setTime(Calendar time) {
        mTime = time;
        mTime.set(Calendar.SECOND, 0);
    }

    public boolean isModeAlarm() {
        return mModeAlarm;
    }

    public void setModeAlarm(boolean modeAlarm) {
        mModeAlarm = modeAlarm;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public boolean isVibration() {
        return mVibration;
    }

    public void setVibration(boolean vibration) {
        mVibration = vibration;
    }

    public boolean isDelete() {
        return mDelete;
    }

    public void setDelete(boolean delete) {
        mDelete = delete;
    }

    public Uri getMusic() {
        return mMusic;
    }

    public void setMusic(Uri music) {
        mMusic = music;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public int getKeyId() {
        return mKeyId;
    }

    public void setKeyId(int keyId) {
        mKeyId = keyId;
    }

    public static DayAlarm assign (DayAlarm alarm) {
        DayAlarm dayAlarm = new DayAlarm(alarm.getTime(), alarm.isModeAlarm());
        dayAlarm.setVibration(alarm.isVibration());
        dayAlarm.setDelete(alarm.isDelete());
        dayAlarm.setMusic(alarm.getMusic());
        dayAlarm.setDescription(alarm.getDescription());
        dayAlarm.setEnable(alarm.isEnable());
        return dayAlarm;
    }
}

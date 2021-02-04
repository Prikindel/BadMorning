package com.invaderprogrammer.android.badmorning;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.preference.PreferenceManager;

import java.util.UUID;

public class PreferenceSettings {
    private static final String UUID_PREFERENCE = "uuidPreference";
    private static final String ACTIVITY_PREFERENCE = "activityPreference";


    public static String getUuidAlarm(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(UUID_PREFERENCE, null);
    }

    public static void setUuidAlarm(Context context, UUID id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(UUID_PREFERENCE, id.toString())
                .apply();
    }

    @SuppressLint("CommitPrefEdits")
    public static void clearUuidAlarm(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(UUID_PREFERENCE);
    }

    public static Boolean getActivityOpen(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ACTIVITY_PREFERENCE, false);
    }

    public static void setActivityOpen(Context context, Boolean open) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ACTIVITY_PREFERENCE, open)
                .apply();
    }
}

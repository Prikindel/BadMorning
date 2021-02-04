package com.invaderprogrammer.android.badmorning.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.invaderprogrammer.android.badmorning.database.AlarmDbSchema.AlarmTable;

public class AlarmBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "alarmsOfTheWeekBase.db";

    public AlarmBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AlarmTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AlarmTable.Cols.UUID + "," +
                AlarmTable.Cols.DAY + "," +
                AlarmTable.Cols.TIME + "," +
                AlarmTable.Cols.MODE + "," +
                AlarmTable.Cols.VIBRATION + "," +
                AlarmTable.Cols.DELETED + "," +
                AlarmTable.Cols.MUSIC + "," +
                AlarmTable.Cols.DESCRIPTION + "," +
                AlarmTable.Cols.ENABLED + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

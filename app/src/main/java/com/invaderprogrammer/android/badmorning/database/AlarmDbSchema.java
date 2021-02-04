package com.invaderprogrammer.android.badmorning.database;

public class AlarmDbSchema {
    public static final class AlarmTable {
        public static final String NAME = "alarms";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String DAY = "day";
            public static final String TIME = "time";
            public static final String MODE = "mode";
            public static final String VIBRATION = "vibration";
            public static final String DELETED = "deleted";
            public static final String MUSIC = "music";
            public static final String DESCRIPTION = "description";
            public static final String ENABLED = "enabled";
        }
    }
}

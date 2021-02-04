package com.invaderprogrammer.android.badmorning;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class AddAlarmActivity extends SingleFragmentActivity {
    private static final String EXTRA_ALARM_ID =
            "com.invaderprogrammer.android.badmorning.alarm_id";
    private static final String EXTRA_ALARM_DAY =
            "com.invaderprogrammer.android.badmorning.alarm_day";

    public static Intent newIntent(Context context, UUID alarmId, int day) {
        Intent intent = new Intent(context, AddAlarmActivity.class);
        intent.putExtra(EXTRA_ALARM_ID, alarmId);
        intent.putExtra(EXTRA_ALARM_DAY, day);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID alarmId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_ALARM_ID);
        int alarmDay = (int) getIntent()
                .getSerializableExtra(EXTRA_ALARM_DAY);
        return AddAlarmFragment.newInstance(alarmId, alarmDay);
    }
}

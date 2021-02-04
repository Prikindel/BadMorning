package com.invaderprogrammer.android.badmorning;

import android.support.v4.app.Fragment;

public class AlarmManagerActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return AlarmManagerFragment.newInstance();
    }
}

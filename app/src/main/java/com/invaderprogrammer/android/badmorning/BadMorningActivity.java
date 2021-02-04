package com.invaderprogrammer.android.badmorning;

import android.support.v4.app.Fragment;

public class BadMorningActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return BadMorningFragment.newInstance();
    }
}

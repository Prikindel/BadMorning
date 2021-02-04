package com.invaderprogrammer.android.badmorning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;

public class AlarmDialogFragment extends DialogFragment {
    private TimePicker mTimePicker;
    private Resources system;

    private static final String ARG_TIME = "time";

    public static final String EXTRA_TIME =
            "com.invaderprogrammer.android.badmorning.time";

    public static AlarmDialogFragment newInstance(Calendar calendar) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, calendar);

        AlarmDialogFragment fragment = new AlarmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        final Calendar calendar = (Calendar) getArguments().getSerializable(ARG_TIME);

        assert calendar != null;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_alarm, null);

        mTimePicker = v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }
        set_timepicker_text_colour();

        return new AlertDialog.Builder(getActivity(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.style.AppCompatAlertDialogStyle : AlertDialog.THEME_HOLO_DARK)
                .setView(v)
                .setTitle(R.string.alarm_dialog_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = 0;
                                int minute = 0;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    hour = mTimePicker.getHour();
                                    minute = mTimePicker.getMinute();
                                } else {
                                    hour = mTimePicker.getCurrentHour();
                                    minute = mTimePicker.getCurrentMinute();
                                }
                                Calendar time = calendar;
                                time.set(Calendar.HOUR_OF_DAY, hour);
                                time.set(Calendar.MINUTE, minute);
                                sendResult(Activity.RESULT_OK, time);
                            }
                        })
                .create();
    }

    private void set_timepicker_text_colour(){
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        NumberPicker hour_numberpicker = mTimePicker.findViewById(hour_numberpicker_id);
        NumberPicker minute_numberpicker = mTimePicker.findViewById(minute_numberpicker_id);
        NumberPicker ampm_numberpicker = mTimePicker.findViewById(ampm_numberpicker_id);

        set_numberpicker_text_colour(hour_numberpicker);
        set_numberpicker_text_colour(minute_numberpicker);
        set_numberpicker_text_colour(ampm_numberpicker);
    }

    private void set_numberpicker_text_colour(NumberPicker number_picker){
        final int count = number_picker.getChildCount();
        final int color = getResources().getColor(R.color.white);

        for(int i = 0; i < count; i++){
            View child = number_picker.getChildAt(i);

            try{
                Field wheelpaint_field = number_picker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelpaint_field.setAccessible(true);

                ((Paint)wheelpaint_field.get(number_picker)).setColor(color);
                ((EditText)child).setTextColor(color);
                number_picker.invalidate();
            }
            catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored){
            }
        }
    }

    private void sendResult(int resultCode, Calendar calendar) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, calendar);

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

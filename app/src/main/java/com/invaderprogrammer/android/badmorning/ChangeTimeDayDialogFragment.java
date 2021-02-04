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
import android.widget.RadioButton;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;

public class ChangeTimeDayDialogFragment extends DialogFragment {
    private TimePicker mTimePicker;
    private Resources system;
    private RadioButton mAdd;
    private RadioButton mReduce;

    private static final String ARG_TIME_MIN = "time_min";
    private static final String ARG_TIME_MAX = "time_max";

    public static final String EXTRA_TIME =
            "com.invaderprogrammer.android.badmorning.time";
    public static final String EXTRA_ADD =
            "com.invaderprogrammer.android.badmorning.add";

    private int hour_min;
    private int minute_min;
    private int hour_max;
    private int minute_max;
    private String[] string;
    private boolean add;
    private int minuteOfFive;

    public static ChangeTimeDayDialogFragment newInstance(Calendar calendar_min, Calendar calendar_max) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME_MIN, calendar_min);
        args.putSerializable(ARG_TIME_MAX, calendar_max);

        ChangeTimeDayDialogFragment fragment = new ChangeTimeDayDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        add = true;
        string = new String[12];
        minuteOfFive = (59) / 5;
        for (int i = 0; i < string.length; i++) {
            string[i] = String.valueOf(i * 5);
        }
        assert getArguments() != null;
        final Calendar calendar_min = (Calendar) getArguments().getSerializable(ARG_TIME_MIN);
        final Calendar calendar_max = (Calendar) getArguments().getSerializable(ARG_TIME_MAX);

        assert calendar_min != null;
        hour_min = calendar_min.get(Calendar.HOUR_OF_DAY);
        minute_min = calendar_min.get(Calendar.MINUTE);
        assert calendar_max != null;
        hour_max = calendar_max.get(Calendar.HOUR_OF_DAY);
        minute_max = calendar_max.get(Calendar.MINUTE);

        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_change_time_day, null);

        mTimePicker = v.findViewById(R.id.dialog_time_picker);
        setTimePicker();

        mAdd = v.findViewById(R.id.add_time);
        mReduce = v.findViewById(R.id.reduce_time);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add = true;
                setTimePicker();
            }
        });
        mReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add = false;
                setTimePicker();
            }
        });

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (add) {
                    if (hourOfDay == 23 - hour_max) {
                        minuteOfFive = (59 - minute_max) / 5;
                    } else {
                        minuteOfFive = (59) / 5;
                    }
                } else {
                    if (hourOfDay == hour_min) {
                        minuteOfFive = (minute_min) / 5;
                    } else {
                        minuteOfFive = (59) / 5;
                    }
                }
                set_timepicker_text_colour();
            }
        });

        return new AlertDialog.Builder(getActivity(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.style.AppCompatAlertDialogStyle : AlertDialog.THEME_HOLO_DARK)
                .setView(v)
                .setTitle(R.string.change_time_day_dialog)
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
                                Calendar time = Calendar.getInstance();
                                time.set(Calendar.HOUR_OF_DAY, hour);
                                time.set(Calendar.MINUTE, Integer.parseInt(string[minute]));
                                sendResult(Activity.RESULT_OK, time);
                            }
                        })
                .create();
    }

    private void setTimePicker() {
        mTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(0);
            mTimePicker.setMinute(0);
        } else {
            mTimePicker.setCurrentHour(0);
            mTimePicker.setCurrentMinute(0);
        }
        set_timepicker_text_colour();
    }

    private void set_timepicker_text_colour(){
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        NumberPicker hour_numberpicker = mTimePicker.findViewById(hour_numberpicker_id);
        final NumberPicker minute_numberpicker = mTimePicker.findViewById(minute_numberpicker_id);
        NumberPicker ampm_numberpicker = mTimePicker.findViewById(ampm_numberpicker_id);

        if(add) {
            hour_numberpicker.setMaxValue(23 - hour_max);

        } else {
            hour_numberpicker.setMaxValue(hour_min);
        }
        hour_numberpicker.setMinValue(0);
        minute_numberpicker.setMinValue(0);
        setMinutePicker(minute_numberpicker, minuteOfFive);

        set_numberpicker_text_colour(hour_numberpicker);
        set_numberpicker_text_colour(minute_numberpicker);
        set_numberpicker_text_colour(ampm_numberpicker);
    }

    private void setMinutePicker(NumberPicker minute_numberpicker, int minuteOfFive) {
        minute_numberpicker.setMaxValue(minuteOfFive);
        minute_numberpicker.setDisplayedValues(string);
        minute_numberpicker.setOnLongPressUpdateInterval(100);
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
        intent.putExtra(EXTRA_ADD, add);

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

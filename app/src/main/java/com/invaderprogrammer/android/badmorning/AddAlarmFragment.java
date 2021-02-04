package com.invaderprogrammer.android.badmorning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public class AddAlarmFragment extends Fragment {
    private DayAlarm mDayAlarm;
    private DayOfTheWeek mDayOfTheWeek;
    private Resources system;
    private UUID mAlarmId;
    private TimePicker mTimePicker;

    private TextView mTextViewSwitch;
    private TextView mAudioName;

    private Switch mVibration;
    private Switch mDelete;

    private ConstraintLayout mAudioLayout;
    private ConstraintLayout mVibrationLayout;
    private ConstraintLayout mDeleteLayout;
    private ConstraintLayout mDescriptionLayout;

    private LinearLayout mLinearLayout;

    private EditText mDescription;

    private ImageButton mMonday;
    private ImageButton mTuesday;
    private ImageButton mWednesday;
    private ImageButton mThursday;
    private ImageButton mFriday;
    private ImageButton mSaturday;
    private ImageButton mSunday;

    private static final String ARG_ALARM_ID = "alarm_id";
    private static final String ARG_ALARM_DAY = "alarm_day";

    private static final int AUDIO_CODE_INTENT = 3;
    private static final int REQUEST_PERMISSIONS = 1;

    private static boolean days[];

    public static Fragment newInstance(UUID alarmId, int alarmDay) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALARM_ID, alarmId);
        args.putSerializable(ARG_ALARM_DAY, alarmDay);

        AddAlarmFragment fragment = new AddAlarmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mAlarmId = (UUID) getArguments().getSerializable(ARG_ALARM_ID);
        mDayOfTheWeek = new DayOfTheWeek(Objects.requireNonNull(getActivity()), (int) getArguments().getSerializable(ARG_ALARM_DAY));
        if (mAlarmId == null) {
            mDayAlarm = new DayAlarm(Calendar.getInstance(), true);
            mDayAlarm.setVibration(true);
        } else {
            mDayAlarm = mDayOfTheWeek.getAlarm(mAlarmId);
        }

        days = new boolean[]{false, false, false, false, false, false, false};

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);

        // ImageButton
        mMonday = view.findViewById(R.id.monday);
        mTuesday = view.findViewById(R.id.tuesday);
        mWednesday = view.findViewById(R.id.wednesday);
        mThursday = view.findViewById(R.id.thursday);
        mFriday = view.findViewById(R.id.friday);
        mSaturday = view.findViewById(R.id.saturday);
        mSunday = view.findViewById(R.id.sunday);
        days[mDayOfTheWeek.getDAYOFTHEWEEK()] = true;
        setDaysImageButton();

        mMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[0] = !days[0];
                setDaysImageButton();
            }
        });
        mTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[1] = !days[1];
                setDaysImageButton();
            }
        });
        mWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[2] = !days[2];
                setDaysImageButton();
            }
        });
        mThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[3] = !days[3];
                setDaysImageButton();
            }
        });
        mFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[4] = !days[4];
                setDaysImageButton();
            }
        });
        mSaturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[5] = !days[5];
                setDaysImageButton();
            }
        });
        mSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days[6] = !days[6];
                setDaysImageButton();
            }
        });

        // TimePicker
        mTimePicker = view.findViewById(R.id.time_picker_add);
        mTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(mDayAlarm.getTime().get(Calendar.HOUR_OF_DAY));
            mTimePicker.setMinute(mDayAlarm.getTime().get(Calendar.MINUTE));
        } else {
            mTimePicker.setCurrentHour(mDayAlarm.getTime().get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(mDayAlarm.getTime().get(Calendar.MINUTE));
        }
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hideInputMethod();
            }
        });

        // On or Off
        mTextViewSwitch = view.findViewById(R.id.switch_alarm);
        mTextViewSwitch.setText(mDayAlarm.isModeAlarm() ? R.string.switch_on : R.string.switch_off);
        mTextViewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
            }
        });

        // Vibration
        mVibration = view.findViewById(R.id.vibration);
        mVibrationLayout = view.findViewById(R.id.vibration_layout);

        mVibration.setChecked(mDayAlarm.isVibration());
        mVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDayAlarm.setVibration(isChecked);
            }
        });
        mVibrationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
                mDayAlarm.setVibration(!mDayAlarm.isVibration());
                mVibration.setChecked(mDayAlarm.isVibration());
            }
        });

        set_timepicker_text_colour(mTimePicker);

        // Audio
        mAudioLayout = view.findViewById(R.id.audio_layout);
        mAudioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getResources().getString(R.string.music))
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mDayAlarm.getMusic());
                startActivityForResult(intent, AUDIO_CODE_INTENT);
            }
        });
        mAudioName = view.findViewById(R.id.audio_name_text);
        mAudioName.setText(getNameMusic());

        // Delete
        mDelete = view.findViewById(R.id.delete_alarm);
        mDeleteLayout = view.findViewById(R.id.delete_layout);
        mDelete.setChecked(mDayAlarm.isDelete());

        mDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDayAlarm.setDelete(isChecked);
            }
        });
        mDeleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
                mDayAlarm.setDelete(!mDayAlarm.isDelete());
                mDelete.setChecked(mDayAlarm.isDelete());
            }
        });

        // Description
        mDescription = view.findViewById(R.id.description_edit);
        mDescriptionLayout = view.findViewById(R.id.description_layout);
        mDescriptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDescription.requestFocus();
                mDescription.setSelection(mDescription.getText().length());
                showInputMethod();
            }
        });
        mDescription.setText(mDayAlarm.getDescription());
        mDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mDescription.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    showInputMethod();
                    mDescription.clearFocus();
                }
                return false;
            }
        });

        mLinearLayout = view.findViewById(R.id.line_layout);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputMethod();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideInputMethod();
    }

    private void setDaysImageButton() {
        mMonday.setBackground(getImageButton(0));
        mTuesday.setBackground(getImageButton(1));
        mWednesday.setBackground(getImageButton(2));
        mThursday.setBackground(getImageButton(3));
        mFriday.setBackground(getImageButton(4));
        mSaturday.setBackground(getImageButton(5));
        mSunday.setBackground(getImageButton(6));
    }

    private Drawable getImageButton(int i) {
        switch (i) {
            case 0:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.monday_white);
                } else {
                    return getResources().getDrawable(R.drawable.monday_gray);
                }
            case 1:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.tuesday_white);
                } else {
                    return getResources().getDrawable(R.drawable.tuesday_gray);
                }
            case 2:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.wednesday_white);
                } else {
                    return getResources().getDrawable(R.drawable.wednesday_gray);
                }
            case 3:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.thursday_white);
                } else {
                    return getResources().getDrawable(R.drawable.thursday_gray);
                }
            case 4:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.friday_white);
                } else {
                    return getResources().getDrawable(R.drawable.friday_gray);
                }
            case 5:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.saturday_white);
                } else {
                    return getResources().getDrawable(R.drawable.saturday_gray);
                }
            case 6:
                if (days[i]) {
                    return getResources().getDrawable(R.drawable.sunday_white);
                } else {
                    return getResources().getDrawable(R.drawable.sunday_gray);
                }
            default:
                return getResources().getDrawable(R.drawable.saturday_gray);
        }

    }

    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDescription.getWindowToken(), 0);
    }

    protected void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_add_alarm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_alarm:
                Calendar calendar = mDayAlarm.getTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
                    calendar.set(Calendar.MINUTE, mTimePicker.getMinute());
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
                    calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
                }
                mDayAlarm.setTime(calendar);
                mDayAlarm.setDescription(mDescription.getText().toString());
                if (mAlarmId == null && days[mDayOfTheWeek.getDAYOFTHEWEEK()]) {
                    mDayOfTheWeek.setAlarm(mDayAlarm);
                } else {
                    mDayOfTheWeek.updateAlarm(mDayAlarm);
                }

                int day = mDayOfTheWeek.getDAYOFTHEWEEK();
                for (int i = 0; i < 7; i++) {
                    if (i != day && days[i]) {
                        mDayOfTheWeek = new DayOfTheWeek(Objects.requireNonNull(getActivity()), i);
                        mDayOfTheWeek.setAlarm(DayAlarm.assign(mDayAlarm));
                    }
                }

                Objects.requireNonNull(getActivity()).finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void set_timepicker_text_colour(TimePicker timePicker){
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");


        final NumberPicker hour_numberpicker = timePicker.findViewById(hour_numberpicker_id);
        final NumberPicker minute_numberpicker = timePicker.findViewById(minute_numberpicker_id);

        set_numberpicker_text_colour(hour_numberpicker);
        set_numberpicker_text_colour(minute_numberpicker);
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

    private String getNameMusic() {
        @SuppressLint("Recycle") Cursor musicCursor = Objects.requireNonNull(getActivity()).getContentResolver().query(mDayAlarm.getMusic(), null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            if (mDayAlarm.getMusic().compareTo(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) == 0) {
                return getActivity().getResources().getString(R.string.default_music);
            } else {
                return musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == AUDIO_CODE_INTENT) {
            Uri toneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (toneUri != null) {
                mDayAlarm.setMusic(toneUri);
                mAudioName.setText(getNameMusic());
                if (getNameMusic() != null) {
                } else {
                    mAudioName.setText(toneUri.getLastPathSegment());
                }
            }
        }
    }
}

package com.invaderprogrammer.android.badmorning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import devlight.io.library.ntb.NavigationTabBar;

public class BadMorningFragment extends Fragment {


    private RecyclerView mAlarmRecyclerView;
    private AlarmAdapter mAlarmAdapter;
    private DayOfTheWeek mDayOfTheWeek;
    private NavigationTabBar mNavigationTabBar;
    private MenuItem mOnOffDayItem;

    private int mDay;
    private Calendar mTime;
    private DayAlarm mAlarmDay;
    private int mPosition;
    private AddAlarmEnum mEnum;
    private boolean isOnDay;

    private static final String DIALOG_ALARM = "DialogAlarm";

    private static final int REQUEST_TIME = 0;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_TIME_CHANGE = 2;

    private Handler handlerUI;
    private Runnable rannubleUI = new Runnable() {
        @Override
        public void run() {
            mAlarmAdapter.notifyDataSetChanged();
            updateTimeUI();
        }
    };

    public static Fragment newInstance() {
        return new BadMorningFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnum = AddAlarmEnum.none;

        handlerUI = new Handler();

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bad_morning, container, false);
        mAlarmRecyclerView = view.findViewById(R.id.recyclerTest);
        mAlarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initUI(view);

        mDayOfTheWeek = new DayOfTheWeek(getContext(), isDayOfCalendar());

        if (isAdded()) {
            mAlarmAdapter = new AlarmAdapter(mDayOfTheWeek.getAlarms());
        }
        setupAdapter();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEnum = AddAlarmEnum.add;
                Intent intent = AddAlarmActivity.newIntent(getActivity(), null, mDayOfTheWeek.getDAYOFTHEWEEK());
                startActivity(intent);
            }
        });

        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);

        return view;
    }

    private void updateTimeUI() {
        int timeUpdateUI = 60 - Calendar.getInstance().get(Calendar.SECOND);
        handlerUI.postDelayed(rannubleUI, timeUpdateUI * 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTimeUI();
        updateUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        handlerUI.removeCallbacks(rannubleUI);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_bad_morning, menu);

        mOnOffDayItem = menu.findItem(R.id.on_off_day);
        isOnDay = false;

        isOnDayOfTheWeek();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.on_off_day:
                if (mDayOfTheWeek.size() > 0) {
                    isOnDay = !isOnDay;
                    if (isOnDay) {
                        item.setIcon(R.drawable.ic_alarm_off);
                        item.setTitle(R.string.off);
                    } else {
                        item.setTitle(R.string.on);
                        item.setIcon(R.drawable.ic_alarm_on);
                    }
                    for (DayAlarm dayAlarm : mDayOfTheWeek.getAlarms()) {
                        dayAlarm.setModeAlarm(isOnDay);
                        if (dayAlarm.isEnable()) {
                            mDayOfTheWeek.deleteAlarm(dayAlarm);
                        }
                        mDayOfTheWeek.updateAlarmInDataBase(dayAlarm);
                    }
                    mDayOfTheWeek.getAlarms();
                    mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
                    mDayOfTheWeek.getAlarms();
                    mAlarmAdapter.notifyDataSetChanged();
                    startAlarm(true);
                }
                return true;
            case R.id.change_time_day:
                if(mDayOfTheWeek.getAlarmsWithoutRepiting().size() != 0) {
                    FragmentManager manager = getFragmentManager();
                    ChangeTimeDayDialogFragment dialog = ChangeTimeDayDialogFragment
                            .newInstance(mDayOfTheWeek.getAlarmNotEnable().get(0).getTime(), mDayOfTheWeek.getAlarmNotEnable().get(1).getTime());
                    dialog.setTargetFragment(BadMorningFragment.this, REQUEST_TIME_CHANGE);
                    assert manager != null;
                    dialog.show(manager, DIALOG_ALARM);
                } else {
                    Toast.makeText(getActivity(), "Нет будильников.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.delete_day:
                if(mDayOfTheWeek.size() != 0) {
                new AlertDialog.Builder(getActivity(),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.style.AppCompatAlertDialogStyle : AlertDialog.THEME_HOLO_DARK)
                        .setIcon(R.drawable.ic_delete_day)
                        .setTitle(R.string.delete_day)
                        .setMessage(R.string.description_delete_day)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDayOfTheWeek.clearAlarm();
                                mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
                                notifyDataSetChanged();
                                startAlarm(true);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                } else {
                    Toast.makeText(getActivity(), "Нет будильников.", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void isOnDayOfTheWeek () {
        isOnDay = false;
        for (DayAlarm dayAlarm: mDayOfTheWeek.getAlarms()) {
            if (dayAlarm.isModeAlarm()) {
                isOnDay = true;
                break;
            }
        }
        try {
            if (isOnDay) {
                mOnOffDayItem.setTitle(getActivity().getResources().getString(R.string.off));
                mOnOffDayItem.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_alarm_off));
            } else {
                mOnOffDayItem.setTitle(getActivity().getResources().getString(R.string.on));
                mOnOffDayItem.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_alarm_on));
            }
        } catch (NullPointerException ignored) {
        }
    }

    public void NTB(int index){
        mDay = index;
        mDayOfTheWeek = new DayOfTheWeek(getContext(), mDay);
        mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        mAlarmAdapter.notifyDataSetChanged();
        isOnDayOfTheWeek();
    }

    private void initUI(View view) {
        final String[] colors = getResources().getStringArray(R.array.seven_ntb);

        mNavigationTabBar = view.findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.monday),
                        Color.parseColor(colors[0]))
                        .title("Monday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.tuesday),
                        Color.parseColor(colors[1]))
                        .title("Tuesday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.wednesday),
                        Color.parseColor(colors[2]))
                        .title("Wednesday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.thursday),
                        Color.parseColor(colors[3]))
                        .title("Thursday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.friday),
                        Color.parseColor(colors[4]))
                        .title("Friday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.satuday),
                        Color.parseColor(colors[5]))
                        .title("Saturday")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.sunday),
                        Color.parseColor(colors[6]))
                        .title("Sunday")
                        .build()
        );

        if (mNavigationTabBar.getModelIndex() == -1) {
            mDay = isCalendarOfDay();
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 21 || (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 21 && Calendar.getInstance().get(Calendar.MINUTE) >= 40)) {
                if (mDay == 6) {
                    mDay = -1;
                }
                mDay++;
            }
        } else {
            mDay = isDayOfCalendar();
        }

        mNavigationTabBar.setModels(models);
        mNavigationTabBar.setModelIndex(mDay, false);

        mNavigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {
                NTB(index);
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {
            }
        });
    }

    private int isCalendarOfDay() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayOfWeek = 0;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = 1;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = 2;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = 3;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = 4;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = 5;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = 6;
                break;
        }

        return dayOfWeek;
    }

    private int isDayOfCalendar() {
        return mNavigationTabBar.getModelIndex();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mAlarmRecyclerView.setAdapter(mAlarmAdapter);
        }
    }

    private void updateUi() {
        mDayOfTheWeek.getAlarms();
        if(mDayOfTheWeek.size() != 0) {
            if (mEnum == AddAlarmEnum.settins) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView();
                    }
                }, 500);
                mAlarmAdapter.notifyItemChanged(mPosition);
                mAlarmAdapter.notifyItemMoved(mPosition, mDayOfTheWeek.getPosition(mAlarmDay.getId()));
                mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
            } else {

                mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
                notifyDataSetChanged();
                mAlarmRecyclerView.smoothScrollToPosition(mDayOfTheWeek.size() - 1);
            }
        }
        startAlarm(false);
        mEnum = AddAlarmEnum.none;
    }

    private void updateRecyclerView() {
        notifyDataSetChanged();
    }

    public class AlarmHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private TextView mTextLeft;
        private ImageButton mBottom;
        private LinearLayout mLinearLayout;
        private SwipeLayout mSwipeLayout;
        private ImageButton mSettings;
        private ImageButton mEdit;
        private ConstraintLayout mConstraintLayout;

        private DayAlarm mDayAlarm;

        @SuppressLint("CutPasteId")
        public AlarmHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_alarm);
            mSwipeLayout = itemView.findViewById(R.id.sample1);
            mLinearLayout = itemView.findViewById(R.id.bottom_wrapper);
            mBottom = itemView.findViewById(R.id.delete);
            mSettings = itemView.findViewById(R.id.settings);
            mEdit = itemView.findViewById(R.id.change);
            mConstraintLayout = itemView.findViewById(R.id.alarmLayout);
            mTextLeft = itemView.findViewById(R.id.time_left);
        }

        public void bingAlarmDay(final DayAlarm dayAlarm, final int position) {
            mDayAlarm = dayAlarm;
            mTextView.setText(dayAlarm.toString());

            setColorText();

            mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDayAlarm.setModeAlarm(!mDayAlarm.isModeAlarm());
                    mDayOfTheWeek.updateAlarm(mDayAlarm);
                    setColorText();
                    notifyDataSetChanged();
                    startAlarm(true);
                }
            });


            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    AlarmDialogFragment dialog = AlarmDialogFragment.newInstance(mDayAlarm.getTime());
                    dialog.setTargetFragment(BadMorningFragment.this, REQUEST_TIME);
                    assert manager != null;
                    dialog.show(manager, DIALOG_ALARM);
                    mAlarmDay = mDayAlarm;
                    mPosition = position;
                }
            });

            mSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPosition = position;
                    mAlarmDay = mDayAlarm;
                    mEnum = AddAlarmEnum.settins;
                    Intent intent = AddAlarmActivity.newIntent(getActivity(), mDayAlarm.getId(), mDayOfTheWeek.getDAYOFTHEWEEK());
                    startActivity(intent);
                }
            });

        }

        public void setColorText() {
            if (mDayAlarm.isModeAlarm()) {
                mTextLeft.setText(getTimeLeft());
                mTextLeft.setTextColor(Color.parseColor("#326C3A"));
                mTextView.setTextColor(Color.parseColor("#326C3A"));
            } else {
                mTextLeft.setText(R.string.switch_off);
                mTextLeft.setTextColor(Color.parseColor("#211F2E"));
                mTextView.setTextColor(Color.parseColor("#211F2E"));
            }
        }

        public String getTimeLeft() {
            String timeLeft = "Время до будильника: ";
            int day = 0;
            if (mDayOfTheWeek.getDAYOFTHEWEEK() < isCalendarOfDay()) {
                day = 6 - isCalendarOfDay() + mDayOfTheWeek.getDAYOFTHEWEEK();
            } else if (mDayOfTheWeek.getDAYOFTHEWEEK() > isCalendarOfDay()) {
                day = mDayOfTheWeek.getDAYOFTHEWEEK() - isCalendarOfDay();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, mDayAlarm.getTime().get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, mDayAlarm.getTime().get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            if (day == 0 && calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 0) {
                day = 7;
            }
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000 * 3600 * 24 * day);

            TimeZone tz = TimeZone.getDefault();
            long timeLeftMillis = calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() - tz.getRawOffset();
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(timeLeftMillis);

            day = time.get(Calendar.DAY_OF_MONTH) - 1;

            if (day != 0) {
                timeLeft += day;
                if (day == 1) {
                    timeLeft += " день ";
                } else if (day >= 2 && day <=4) {
                    timeLeft += " дня ";
                } else if (day > 4) {
                    timeLeft += " дней ";
                }
            }
            int hour = time.get(Calendar.HOUR_OF_DAY);
            int minute = time.get(Calendar.MINUTE);
            if (hour > 0) {
                timeLeft += hour + " ч ";
            }
            if (minute > 0) {
                timeLeft += minute + " мин";
            }

            if (day == 0 && hour == 0 && minute == 0) {
                timeLeft += "меньше минуты";
            }

            return timeLeft;
        }
    }

    public class AlarmAdapter extends RecyclerView.Adapter<AlarmHolder> {
        private List<DayAlarm> mAlarms;

        public AlarmAdapter(List<DayAlarm> alarms) {
            mAlarms = alarms;
        }

        public void setDayOfTheWeek(DayOfTheWeek value) {
            mAlarms = value.getAlarmsWithoutRepiting();
        }

        @NonNull
        @Override
        public AlarmHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.alarm, viewGroup, false);

            return new AlarmHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlarmHolder alarmHolder, @SuppressLint("RecyclerView") final int i) {
            alarmHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            alarmHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, alarmHolder.itemView.findViewById(R.id.bottom_wrapper));
            alarmHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Left, alarmHolder.itemView.findViewById(R.id.setting_wrapper));

            alarmHolder.mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {

                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {

                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });

            final DayAlarm dayAlarm = mAlarms.get(i);
            alarmHolder.bingAlarmDay(dayAlarm, i);

            alarmHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(dayAlarm, i);
                }
            });

            alarmHolder.mBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(dayAlarm, i);
                }
            });

        }

        private void delete(DayAlarm dayAlarm, int i) {
            mDayOfTheWeek.deleteAlarm(dayAlarm);
            setDayOfTheWeek(mDayOfTheWeek);
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, getItemCount());
            isOnDayOfTheWeek();
            startAlarm(true);
        }

        @Override
        public int getItemCount() {
            return mAlarms.size();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_TIME:
                mTime = (Calendar) data
                        .getSerializableExtra(AlarmDialogFragment.EXTRA_TIME);
                mAlarmDay.setTime(mTime);
                mDayOfTheWeek.updateAlarm(mAlarmDay);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView();
                    }
                }, 500);
                mAlarmAdapter.notifyItemChanged(mPosition);
                mAlarmAdapter.notifyItemMoved(mPosition, mDayOfTheWeek.getPosition(mAlarmDay.getId()));
                mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
                startAlarm(false);
                break;
            case REQUEST_TIME_CHANGE:
                Calendar calendar = (Calendar) data.getSerializableExtra(ChangeTimeDayDialogFragment.EXTRA_TIME);
                boolean add = (boolean) data.getSerializableExtra(ChangeTimeDayDialogFragment.EXTRA_ADD);
                changeTimeDay(add, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startAlarm (boolean isDelete) {
        SetAlarmManager.setAlarmManager(getActivity(), isDelete);
    }

    private void changeTimeDay(boolean add, int hour, int minute) {
        for (int i = 0; i < mDayOfTheWeek.size(); i++) {
            DayAlarm dayAlarm = mDayOfTheWeek.getAlarm(i);
            if (!dayAlarm.isEnable()) {
                Calendar calendar = dayAlarm.getTime();
                if (add) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + hour * 3600 * 1000 + minute * 60 * 1000);
                } else {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - hour * 3600 * 1000 - minute * 60 * 1000);
                }
                dayAlarm.setTime(calendar);
                mDayOfTheWeek.updateAlarmInDataBase(dayAlarm);
            }
        }
        mDayOfTheWeek.getAlarms();
        mAlarmAdapter.setDayOfTheWeek(mDayOfTheWeek);
        notifyDataSetChanged();
        startAlarm(false);
    }

}



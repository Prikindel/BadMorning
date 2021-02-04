package com.invaderprogrammer.android.badmorning;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public class AlarmManagerFragment extends Fragment
        implements View.OnTouchListener {
    private Vibrator mVibrator;
    private UUID mIdAlarm;
    private Point mSizeScreen;
    private float mSizeScreenY;
    private float mTouchMoveY;
    private float mDifferenceTouchY;
    private DayAlarm mDayAlarm;
    private Uri mAlert;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private Handler mHandler;
    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            NotificationAlarm.notificationAlarmCalled(Objects.requireNonNull(getActivity()), mDayAlarm);
            finish();
        }
    };
    private long timer = 1000*60*15;

    private LinearLayout mLinearLayoutPullUp;
    private ImageView mImagePullUp;
    private TextView mPullUp;
    private TextView mAlarmText;
    private TextView mTimeText;
    private Button mButton;
    private ConstraintLayout mConstraintLayout;

    public static AlarmManagerFragment newInstance() {
        return new AlarmManagerFragment();
    }

    private BroadcastReceiver vibrateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_SCREEN_OFF)) {
                startVibrate();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mHandler.postDelayed(myRunnable, timer);

        PreferenceSettings.setActivityOpen(getActivity(), true);

        Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String stringUuid = PreferenceSettings.getUuidAlarm(getActivity());
        if (stringUuid == null) {
            mDayAlarm = null;
            finish();
        }

        mIdAlarm = UUID.fromString(stringUuid);
        mDayAlarm = new DayOfTheWeek(getActivity(), 1).getAlarm(mIdAlarm);
        SetAlarmManager.setAlarmManager(getActivity(), false);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        getActivity().registerReceiver(vibrateReceiver, filter);
        startVibrate();

        mSizeScreen = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(mSizeScreen);
        mSizeScreenY = mSizeScreen.y / 4;

        mAlert = mDayAlarm.getMusic();
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(getActivity(), mAlert);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            try {
                mMediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (mDayAlarm.isEnable()) {
            NotificationAlarm.clearNotificationEnable(mDayAlarm.getKeyId());
        } else {
            NotificationAlarm.clearNotification(mDayAlarm.getKeyId());
        }
    }

    @SuppressLint({"SimpleDateFormat", "ClickableViewAccessibility", "SetTextI18n", "WrongConstant"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen_alarm, container, false);

        mLinearLayoutPullUp = view.findViewById(R.id.linear_layout_pull_up);
        mImagePullUp = view.findViewById(R.id.image_pull_up);
        mPullUp = view.findViewById(R.id.pull_up);
        mAlarmText = view.findViewById(R.id.alarm_text);
        mTimeText = view.findViewById(R.id.time_text);
        mButton = view.findViewById(R.id.button);
        mConstraintLayout = view.findViewById(R.id.constraint_layout);

        mLinearLayoutPullUp.setOnTouchListener(this);
        mImagePullUp.setOnTouchListener(this);
        mPullUp.setOnTouchListener(this);

        startAnimation();

        mAlarmText.setText(mDayAlarm.getDescription() + (mDayAlarm.isEnable() ? " (Отложен)" : ""));
        mTimeText.setText(mDayAlarm.toString());
        mTimeText.setTextSize(60);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = mDayAlarm.getTime();
                calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + 1000*60*10);
                DayAlarm dayAlarm = new DayAlarm(calendar, true);
                dayAlarm.setDelete(true);
                dayAlarm.setMusic(mDayAlarm.getMusic());
                dayAlarm.setDescription(mDayAlarm.getDescription());
                dayAlarm.setVibration(mDayAlarm.isVibration());
                dayAlarm.setEnable(true);

                DayOfTheWeek dayOfTheWeek = new DayOfTheWeek(Objects.requireNonNull(getActivity()), isCalendarOfDay(dayAlarm.getTime().get(Calendar.DAY_OF_WEEK)));
                dayOfTheWeek.setAlarm(dayAlarm);
                dayAlarm = dayOfTheWeek.getAlarm(dayAlarm.getId());

                if (!SetAlarmManager.setAlarmManager(getActivity(), false)) {
                    NotificationAlarm.notificationAlarm(getActivity(), dayAlarm);
                }


                finish();
            }
        });

        return view;
    }

    private void startAnimation() {
        ObjectAnimator heightAnimator = ObjectAnimator.ofFloat(mImagePullUp, "y", mPullUp.getTop() + 30, mImagePullUp.getBottom() - 30).setDuration(1500);
        heightAnimator.setRepeatCount(-1);

        ScaleAnimation scale = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1500);
        scale.setRepeatCount(-1);
        scale.setRepeatMode(Animation.REVERSE);

        AnimationSet set = new AnimationSet (true);
        set.addAnimation(scale);
        mButton.startAnimation(set);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(heightAnimator);
        animatorSet.start();
    }

    private void finish() {
        if (mDayAlarm.isEnable()) {
            NotificationAlarm.clearNotificationEnable(mDayAlarm.getKeyId());
        } else {
            NotificationAlarm.clearNotification(mDayAlarm.getKeyId());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(getActivity()).finishAndRemoveTask();
        } else {
            Objects.requireNonNull(getActivity()).finish();
        }
    }

    private void startVibrate() {
        if(mDayAlarm != null && mDayAlarm.isVibration()) {
            long[] pattern = {500, 800, 400, 800};
            mVibrator = (Vibrator) Objects.requireNonNull(getActivity()).getSystemService(Context.VIBRATOR_SERVICE);
            if (mVibrator.hasVibrator()) {
                mVibrator.vibrate(pattern, 2);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDayAlarm.isDelete()) {
            DayOfTheWeek dayOfTheWeek = new DayOfTheWeek(Objects.requireNonNull(getActivity()), 1);
            dayOfTheWeek.deleteAlarm(mDayAlarm);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(myRunnable);
        if(mDayAlarm != null && mDayAlarm.isVibration()) {
            mVibrator.cancel();
        }
        if(mDayAlarm != null && mDayAlarm.getMusic() != null && mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.stop();
        }
        Objects.requireNonNull(getActivity()).unregisterReceiver(vibrateReceiver);

        PreferenceSettings.setActivityOpen(getActivity(), false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());

        float y = current.y;
        float different = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchMoveY = 0;
                mDifferenceTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                different = mDifferenceTouchY - y;
                mTouchMoveY += different;
                if (Math.abs(different) > 2) {
                    mConstraintLayout.setMaxHeight((int) (mConstraintLayout.getHeight() - different));
                }
                mDifferenceTouchY = y;

                if (mTouchMoveY >= (mSizeScreen.y / 3)) {
                    finish();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchMoveY >= mSizeScreenY / 2) {
                    finish();
                } else {
                    mConstraintLayout.setMaxHeight(mSizeScreen.y);
                }
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    private static int isCalendarOfDay(int dayOfWeek) {

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
}

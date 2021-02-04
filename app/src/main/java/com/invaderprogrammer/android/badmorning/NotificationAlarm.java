package com.invaderprogrammer.android.badmorning;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

public class NotificationAlarm {
    private static final String NOTIFICATION_BUTTON_RECEIVER = "com.invaderprogrammer.android.badmorning.NOTIFICATION";
    private static NotificationManager notificationManagerCalled;
    private static NotificationManager notificationManager;
    private static NotificationManager notificationManagerEnable;
    private static final String NOTIFICATION_CHANNEL_ID_ALARM = "notification_channel";
    private static final String NOTIFICATION_CHANNEL_ID_CALLED = "my_notification_channel";
    private static final String NOTIFICATION_CHANNEL_ID_ENABLED = "notification_channel_enabled";

    public static void notificationAlarmCalled(Context context, DayAlarm dayAlarm) {
        notificationManagerCalled = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_CALLED, "Alarm CalLed", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 50, 100});
            notificationChannel.enableVibration(true);
            notificationManagerCalled.createNotificationChannel(notificationChannel);
        }

        Resources resources = context.getResources();
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, BadMorningActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_CALLED)
                .setVibrate(new long[]{0, 100, 50, 100})
                .setTicker(resources.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(dayAlarm.getDescription())
                .setContentText(resources.getString(R.string.notification_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(dayAlarm.getKeyId(), notification);
    }

    @SuppressLint("UseValueOf")
    public static void notificationAlarm(Context context, DayAlarm dayAlarm) {
        RemoteViews notificationLayouts = new RemoteViews(context.getPackageName(), R.layout.norification_layout);
        String CHANEL = NOTIFICATION_CHANNEL_ID_ALARM;
        String title = "Будильник в " + dayAlarm.toString();
        if (dayAlarm.isEnable()) {
            CHANEL = NOTIFICATION_CHANNEL_ID_ENABLED;
            title += " (отложен)";
        }
        notificationLayouts.setTextViewText(R.id.text_notification, "Можно отключить его.");
        notificationLayouts.setTextViewText(R.id.title_notification, title);
        Intent intent = new Intent(context, NotificationButtonReceiver.class);
        intent.putExtra(NotificationButtonReceiver.UUIDDAY, dayAlarm.getId().toString());
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        notificationLayouts.setOnClickPendingIntent(R.id.disable_notification,
                PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        String name = "Alarm";
        if (!dayAlarm.isEnable()) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        } else {
            notificationManagerEnable = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            name += "Enable";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 50, 100});
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(null, null);
            if (!dayAlarm.isEnable()) {
                notificationManager.createNotificationChannel(notificationChannel);
            } else {
                notificationManagerEnable.createNotificationChannel(notificationChannel);
            }
        }

        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, BadMorningActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CHANEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(notificationLayouts)
                .setContent(notificationLayouts)
                .setContentIntent(pi)
                .build();
        if (dayAlarm.isEnable()) {
            notification.flags |= Notification.FLAG_NO_CLEAR;
        } else {
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(dayAlarm.getKeyId(), notification);
    }

    public static void getNotificationChannel(Context context, DayAlarm dayAlarm) {
        String CHANEL = NOTIFICATION_CHANNEL_ID_ALARM;
        if (dayAlarm.isEnable()) {
            CHANEL = NOTIFICATION_CHANNEL_ID_ENABLED;
        }
        if (!dayAlarm.isEnable()) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.getNotificationChannel(CHANEL);
            }
        } else {
            notificationManagerEnable = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManagerEnable.getNotificationChannel(CHANEL);
            }
        }
    }

    public static void clearNotification(int id){
        try {
            notificationManager.cancel(id);
        } catch (Exception ignored) {

        }
    }

    public static void clearNotificationEnable(int id) {
        try {
            notificationManagerEnable.cancel(id);
        } catch (Exception ignored) {

        }
    }

    public static void clearNotification() {
        try {
            notificationManager.cancelAll();
        } catch (Exception ignored) {

        }
    }

    public static void clearNotificationCalled(){
        try {
            notificationManagerCalled.cancelAll();
        } catch (Exception ignored) {

        }
    }

    public static void clearNotificationEnable() {
        try {
            notificationManagerEnable.cancelAll();
        } catch (Exception ignored) {

        }
    }
}

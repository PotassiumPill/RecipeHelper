package com.example.recipehelper.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.recipehelper.R;
import com.example.recipehelper.Utils;
import com.example.recipehelper.activity.StartRecipeActivity;

public class NotificationsManager {
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private static final int TIMER_NOTIFICATION_ID = 0;
    private static final int TIMES_UP_NOTIFICATION_ID = 1;
    private static final int TIME_CLOSE_NOTIFICATION_ID = 2;

    private NotificationManager mNotificationManager;

    private Context mContext;
    int mPosition;
    int mMaxTime;



    public NotificationsManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                mContext.getResources().getString(R.string.notification_channel_title),
                NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription(mContext.getResources().getString(R.string.notification_channel_description));
        mNotificationManager.createNotificationChannel(notificationChannel);

    }

    public void sendTimerNotification(int position, int time) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            mPosition = position;
            mMaxTime = time;
            NotificationCompat.Builder notifyBuilder = getTimerNotificationBuilder(mPosition, time, false);
            mNotificationManager.notify(TIMER_NOTIFICATION_ID, notifyBuilder.build());
        }

    }

    public void updateTimerNotification(int time) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder notifyBuilder = getTimerNotificationBuilder(mPosition, time, false);
            notifyBuilder.setSilent(true);
            notifyBuilder.setAutoCancel(true);
            notifyBuilder.setOnlyAlertOnce(true);
            mNotificationManager.notify(TIMER_NOTIFICATION_ID, notifyBuilder.build());
        }
    }

    public void updateTimerOnPauseNotification(int time) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder notifyBuilder = getTimerNotificationBuilder(mPosition, time, true);
            notifyBuilder.setSilent(true);
            notifyBuilder.setAutoCancel(true);
            mNotificationManager.notify(TIMER_NOTIFICATION_ID, notifyBuilder.build());
        }
    }

    public void sendTimeUpNotification() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder notifyBuilder = getTimesUpNotificationBuilder();
            mNotificationManager.notify(TIMES_UP_NOTIFICATION_ID, notifyBuilder.build());
        }
    }

    public void sendTimeCloseNotification(int time) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder notifyBuilder = getTimeCloseNotificationBuilder(time);
            mNotificationManager.notify(TIME_CLOSE_NOTIFICATION_ID, notifyBuilder.build());
        }
    }

    public void cancelTimeUpNotification() {
        mNotificationManager.cancel(TIMES_UP_NOTIFICATION_ID);
        mNotificationManager.cancel(TIME_CLOSE_NOTIFICATION_ID);
    }

    public void cancelTimerNotification() {
        mNotificationManager.cancel(TIMER_NOTIFICATION_ID);
        mNotificationManager.cancel(TIME_CLOSE_NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getTimerNotificationBuilder(int position, int time, boolean onPause){

        Intent notificationIntent = new Intent(mContext, StartRecipeActivity.class);
        notificationIntent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, true);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext,
                TIMER_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopTimerIntent = new Intent(StartRecipeActivity.ACTION_STOP_TIMER);
        PendingIntent stopTimerPendingIntent = PendingIntent.getBroadcast(mContext, TIMER_NOTIFICATION_ID, stopTimerIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle(mContext.getResources().getString(R.string.timer_notification_title, position+1))
                .setContentText(Utils.secondsToTimeString(time))
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(mMaxTime, time, false)
                .addAction(R.drawable.ic_stat_stop,
                        mContext.getResources().getString(R.string.end_timer_label),
                        stopTimerPendingIntent)
                .setContentIntent(notificationPendingIntent);
        if(onPause) {
            Intent resumeTimerIntent = new Intent(StartRecipeActivity.ACTION_RESUME_TIMER);
            PendingIntent resumeTimerPendingIntent = PendingIntent.getBroadcast(mContext, TIMER_NOTIFICATION_ID, resumeTimerIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            notifyBuilder.addAction(R.drawable.ic_stat_resume,
                    mContext.getResources().getString(R.string.resume_timer_label),
                    resumeTimerPendingIntent);
        } else {
            Intent pauseTimerIntent = new Intent(StartRecipeActivity.ACTION_PAUSE_TIMER);
            PendingIntent pauseTimerPendingIntent = PendingIntent.getBroadcast(mContext, TIMER_NOTIFICATION_ID, pauseTimerIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            notifyBuilder.addAction(R.drawable.ic_stat_pause,
                    mContext.getResources().getString(R.string.pause_timer_label),
                    pauseTimerPendingIntent);
        }
        return notifyBuilder;
    }

    private NotificationCompat.Builder getTimesUpNotificationBuilder(){
        Intent notificationIntent = new Intent(mContext, StartRecipeActivity.class);
        notificationIntent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, true);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext,
                TIMES_UP_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle(mContext.getResources().getString(R.string.time_up_notification_title, mPosition + 1))
                .setContentText(mContext.getResources().getString(R.string.time_up_notification_description))
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder;
    }

    private NotificationCompat.Builder getTimeCloseNotificationBuilder(int time){
        Intent notificationIntent = new Intent(mContext, StartRecipeActivity.class);
        notificationIntent.putExtra(StartRecipeActivity.EXTRA_NOTIFICATION, true);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext,
                TIME_CLOSE_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle(mContext.getResources().getString(R.string.time_close_notification_title))
                .setContentText(mContext.getResources().getString(R.string.time_close_notification_description, mPosition + 1,
                        Utils.secondsToTimeString(time)))
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder;
    }

    public void setMaxTime(int maxTime) {
        mMaxTime = maxTime;
    }

    public void setStepPosition(int position) {
        mPosition = position;
    }

}

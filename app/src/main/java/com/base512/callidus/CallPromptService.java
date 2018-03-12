package com.base512.callidus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * TODO Add documentation for CallPromptService
 */

public class CallPromptService extends Service
{
    private static final String TAG = "CallPromptService";

    public static boolean isRunning = false;

    NotificationManager notificationManager;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        isRunning = true;

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "ongoing_call_prompt";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

      /* Create or update. */
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Ongoing call prompt",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_contact_phone_black_24dp)
                        .setColor(getColor(R.color.colorAccent))
                        .setContentTitle("Callidus is available")
                        .setContentText("Click to activate")
                        .setOngoing(true);

        Intent resultIntent = new Intent(this, CallerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(CallerActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                        );
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        notificationManager.notify(1, notification);

        Log.d(TAG, "onStart");
        return START_STICKY;
    }
}
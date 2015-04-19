package com.DataFinancial.JotemDown;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Random;


public class BackupAlarmReceiver extends BroadcastReceiver {

    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        Log.d(MainActivity.DEBUGTAG, "Alarm received");

        Utils util = new Utils();


        SharedPreferences prefs = ctx.getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        String destination = prefs.getString(ScheduleBackups.BACKUP_LOCATION, null));

        if (destination != null) {
            util.backupNotes("notes_JED.db", destination, ctx);
            backupNotify();
        }





//        ctx = context;
//
//        boolean srvcRunning = isReminderServiceRunning(ReminderService.class);
//
//        Intent srvcIntent = new Intent(context, ReminderService.class);
//
//        if (srvcRunning == true) {
//            context.stopService(srvcIntent);
//        }
//        context.startService(srvcIntent);
    }

    void backupNotify() {

        Random rn = new Random();
        int id = rn.nextInt(10) + 1;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.note_yellow).setContentTitle("Jot'emDown Backup").setContentText("Your notes have been backed up.");
        //mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        Intent resultIntent = new Intent(ctx, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, id, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = id;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private boolean isReminderServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

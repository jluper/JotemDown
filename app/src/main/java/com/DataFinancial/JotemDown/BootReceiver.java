package com.DataFinancial.JotemDown;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    DatabaseReminders db;

    private final String BACKUP_LOCATION = "BACKUP_LOCATION";
    private final String BACKUP_TIME = "BACKUP_TIME";
    private final String BACKUP_FREQUENCY = "BACKUP_FREQ";

    public void onReceive(Context context, Intent intent) {

        //start the service to check for pending reminders
        Intent srvcIntent = new Intent(context, ReminderService.class);
        srvcIntent.putExtra("boot", "true");
        context.startService(srvcIntent);

        // set backup alarm if was previously set
        SharedPreferences prefs = context.getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, Activity.MODE_PRIVATE);
        String tod = prefs.getString(BACKUP_TIME, null);
        String frq = prefs.getString(BACKUP_FREQUENCY, null);

        if (!TextUtils.isEmpty(tod) && !TextUtils.isEmpty(frq)) {

            String[] timeParts = tod.split(":");
            int hr = Integer.parseInt(timeParts[0]);
            int min = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hr);
            calendar.set(Calendar.MINUTE, min);

            int pendingIntentRequestCode = 99;
            Intent alarmIntent = new Intent(context, BackupAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(am.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES * Integer.parseInt(frq), pendingIntent);
        }
    }
}


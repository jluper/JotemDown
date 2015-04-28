package com.DataFinancial.JotemDown;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


public class BackupAlarmReceiver extends BroadcastReceiver {

    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;

        Utils util = new Utils();

        SharedPreferences prefs = ctx.getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        String address = prefs.getString(ScheduleBackups.BACKUP_LOCATION, null);
        String destination = prefs.getString(ScheduleBackups.BACKUP_DESTINATION, null);

        if (address != null) {
            if (destination.equals("googleDrive")) {
                util.backupNotesToGoogleDrive("notes_JED.db", address, ctx);
            } else {
                if (destination.equals("emailRecipient")) {
                    EmailBackup emailBackup = new EmailBackup();
                    emailBackup.sendEmailBackup(context,"jluper@triad.rr.com", "Jot\'emDown backup", "Jot\'emDown Notes backup attached...");
                } else {
                    Toast.makeText(context, "Unable to backup note..", Toast.LENGTH_LONG).show();
                }
            }
        }
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

package com.DataFinancial.NoteJackal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    DatabaseReminders db;
    static private int pendingIntentRequestCode = 0;
    static private PendingIntent pendingIntent;
    static private AlarmManager am;

    public void onReceive(Context context, Intent intent) {

        Intent srvcIntent = new Intent(context, ReminderService.class);
        srvcIntent.putExtra("boot", "true");
        context.startService(srvcIntent);
    }
}


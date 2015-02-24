package com.DataFinancial.NoteJackal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    DatabaseReminders db;

    public void onReceive(Context context, Intent intent) {

        Intent srvcIntent = new Intent(context, ReminderService.class);
        srvcIntent.putExtra("boot", "true");
        context.startService(srvcIntent);
    }
}


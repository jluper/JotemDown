package com.DataFinancial.JotemDown;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;

        boolean srvcRunning = isReminderServiceRunning(ReminderService.class);

        Intent srvcIntent = new Intent(context, ReminderService.class);

        if (srvcRunning == true) {
            context.stopService(srvcIntent);
        }
        context.startService(srvcIntent);
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

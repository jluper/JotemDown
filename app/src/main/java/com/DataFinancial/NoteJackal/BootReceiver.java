package com.DataFinancial.NoteJackal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

     DatabaseReminders db;
    static private int pendingIntentRequestCode = 0;
    static private PendingIntent pendingIntent;
    static private AlarmManager am;

    public void onReceive(Context context, Intent intent) {

        // Your code to execute when Boot Completd
        //**Schedule your Alarm Here**


        db = new DatabaseReminders(context);

        List<Reminder> reminders = db.getReminders();
        Toast.makeText(context, "# reminders = " + reminders.size(), Toast.LENGTH_LONG).show();
        for (int j = 0; j < reminders.size(); j++) {
            Toast.makeText(context, "reminders = " + reminders.get(j).toString(), Toast.LENGTH_LONG).show();
            String dateTime = reminders.get(j).getDate() + " " + reminders.get(j).getTime();
            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
            java.util.Date date;
            try {
                date = format.parse(dateTime);
                setReminderAlarm(context, date);
            } catch (ParseException e) {
                Toast.makeText(context, "Unable to set reminder alarm after boot.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void setReminderAlarm(Context context, java.util.Date dateTime) {

        Toast.makeText(context, "in setReminderAlarm.", Toast.LENGTH_LONG).show();
        pendingIntentRequestCode++;
        Intent alarmIntent = new Intent(context, ReminderAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        long diff = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}

//            Intent i = new Intent("com.DataFinancial.NoteJackal.reminder");
//            // add data
//            i.putExtra("message", "add_reminder");
//            i.putExtra("date_time", dateTime);
//            //LocalBroadcastManager.getInstance(context).sendBroadcast(i);
//            Log.d(MainActivity.DEBUGTAG, "send broadcast from boot receiver.");
//            context.sendBroadcast(i);
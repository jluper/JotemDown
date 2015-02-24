package com.DataFinancial.NoteJackal;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderService extends Service {

    static boolean running;
    static Thread t;
    static int startID;
    static private int pendingIntentRequestCode = 100;
    WakeLock wakeLock;
    SmsManager smsManager = SmsManager.getDefault();
    private String boot = "false";

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {

        running = false;
        t.interrupt();
        t = null;

        wakeLock.release();

        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startID = startId;
        running = true;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            boot = "true";
        }

        final DatabaseReminders db = new DatabaseReminders(this);
        final DatabaseNotes dbNotes = new DatabaseNotes(this);
        List<Reminder> reminders;

        reminders = db.getUnexpiredReminders();
        final List<Reminder> remList = reminders;

        Runnable r = new Runnable() {
            public void run() {

                SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
                Date today = new Date();

                for (int i = remList.size(); i > 0; i--) {

                    String reminderDateTime = remList.get(i - 1).getDate() + " " + remList.get(i - 1).getTime();
                    String strToday = df.format(today);

                    if (strToday.compareTo(reminderDateTime) >= 0) {

                        Note note;
                        note = dbNotes.getNote(remList.get(i - 1).getNoteId());

                        if (!remList.get(i - 1).getPhone().isEmpty()) {
                            smsManager.sendTextMessage(remList.get(i - 1).getPhone(), null, note.getBody(), null, null);
                        }

                        reminderNotify(remList.get(i - 1));

                        db.deleteReminder(remList.get(i - 1).getId());

                        if (remList.get(i - 1).getRecur().equals("true")) {
                            Utils utils = new Utils();
                            String newDate = utils.incrementDay(remList.get(i - 1).getDate());
                            //String newTime = utils.incrementMinute(remList.get(i-1).getTime(), 2);
                            //remList.get(i-1).setTime(newTime);
                            remList.get(i - 1).setDate(newDate);
                            String dateTime = remList.get(i - 1).getDate() + " " + remList.get(i - 1).getTime();

                            db.addReminder(remList.get(i - 1));

                            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
                            java.util.Date date;
                            try {
                                date = format.parse(dateTime);
                                setReminderAlarm(getApplicationContext(), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        remList.remove(i - 1);
                    } else {

                        if (boot.equals("true")) {
                            String dateTime = remList.get(i - 1).getDate() + " " + remList.get(i - 1).getTime();
                            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);

                            java.util.Date date;
                            try {
                                date = format.parse(dateTime);

                                setReminderAlarm(getApplicationContext(), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                stopSelfResult(startID);
            }
        };

        t = new Thread(r);
        t.start();

        return Service.START_NOT_STICKY;
    }


    private void setReminderAlarm(Context context, java.util.Date dateTime) {

        pendingIntentRequestCode++;
        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(context, ReminderAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        long diff = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }


    void reminderNotify(Reminder rem) {

        DatabaseNotes db = new DatabaseNotes(this);

        Note note = db.getNote(rem.getNoteId());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this).setSmallIcon(R.drawable.note_yellow).setContentTitle(getString(R.string.notification_title)).setContentText(note.getBody());

        Intent resultIntent = new Intent(this, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, rem.getNoteId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = rem.getNoteId();

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}	



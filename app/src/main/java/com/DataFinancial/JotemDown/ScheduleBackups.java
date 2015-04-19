package com.DataFinancial.JotemDown;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.Calendar;

/**
 * @author jluper
 */
public class ScheduleBackups extends ActionBarActivity  implements OnClickListener {

    public static final String BACKUP_LOCATION = "BACKUP_LOCATION";
    public static final String BACKUP_TIME = "BACKUP_TIME";
    public static final String BACKUP_FREQUENCY = "BACKUP_FREQ";


    private EditText location;
    private EditText timeOfDay;
    private EditText frequency;
    private Button btnSubmit;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;
    private GoogleAccountCredential mCredential;
    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;

    ImageButton btnEmail,btnClock,btnDrive;
    static final int CONTACT_PICKER_EMAIL_RESULT = 1002;
    static final int CONTACT_PICKER_PHONE_RESULT = 1003;
    static final int REQUEST_AUTHORIZATION = 2;
    static final int REQUEST_ACCOUNT_PICKER = 1;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_backups);
        Log.d(MainActivity.DEBUGTAG, "check0");

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Schedule Backups");
        actionBar.setDisplayShowTitleEnabled(true);

        Log.d(MainActivity.DEBUGTAG, "check 0.005");


        btnClock = (ImageButton) findViewById(R.id.btn_backup_time);
        btnClock.setOnClickListener(this);
        Log.d(MainActivity.DEBUGTAG, "check1.1");


        btnEmail = (ImageButton) findViewById(R.id.btn_backup_email);
        Log.d(MainActivity.DEBUGTAG, "check 0.15");

        btnEmail.setOnClickListener(this);
        Log.d(MainActivity.DEBUGTAG, "check1");

        btnDrive = (ImageButton) findViewById(R.id.btn_backup_drive);
        btnDrive.setOnClickListener(this);
        Log.d(MainActivity.DEBUGTAG, "check1.2");

        btnSubmit = (Button) findViewById(R.id.btn_backup_schedule);
        btnSubmit.setOnClickListener(this);
Log.d(MainActivity.DEBUGTAG, "check1.3");

        addListenerOnButton();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }
        Log.d(MainActivity.DEBUGTAG, "check2");

        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
        Log.d(MainActivity.DEBUGTAG, "check3");

        location = (EditText) findViewById(R.id.txt_location);
        timeOfDay = (EditText) findViewById(R.id.txt_backup_time);
        frequency = (EditText) findViewById(R.id.txt_backup_freq);
        Log.d(MainActivity.DEBUGTAG, "check4");

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String loc = prefs.getString(BACKUP_LOCATION, null);
        String tod = prefs.getString(BACKUP_TIME, null);
        String frq = prefs.getString(BACKUP_FREQUENCY, null);
        Log.d(MainActivity.DEBUGTAG, "check5");

        if (loc != null) {
            location.setText(prefs.getString(BACKUP_LOCATION, null));
        }
        if (tod != null) {
            timeOfDay.setText(prefs.getString(BACKUP_TIME, null));
        }
        if (frq != null) {
            frequency.setText(prefs.getString(BACKUP_FREQUENCY, null));
        }
        Log.d(MainActivity.DEBUGTAG, "check6");

    }


    @Override
    public void onResume() {

        super.onResume();

    }

    @Override
    public void onClick(View v) {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (v == btnClock) {

            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    // Display Selected time in textbox
                    if (minute > 9) {
                        timeOfDay.setText(hourOfDay + ":" + minute);
                    }
                    else {
                        timeOfDay.setText(hourOfDay + ":0" + minute);
                    }
                }
            }, mHour, mMinute, false);
            tpd.show();
        }

        if (v == btnDrive) {
            startActivityForResult(mCredential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
        }

        if (v == btnEmail) {
            startActivityForResult(contactPickerIntent, CONTACT_PICKER_EMAIL_RESULT);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri result;
        String id;
        Cursor cursor;
        String type;

        EditText txtAddr = (EditText) findViewById(R.id.txtAddress);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case CONTACT_PICKER_EMAIL_RESULT:

                    result = data.getData();
                    // get the contact id from the Uri
                    id = result.getLastPathSegment();

                    // query for everything email
                    cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

                    String emailAddress = "";
                    int emailType = -1;
                    while (cursor.moveToNext()) {
                        emailAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emailType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
//                        if (emailType == ContactsContract.CommonDataKinds.Email.TYPE_HOME) {
//                            break;
//                        }
                    }
                    if (emailAddress.isEmpty()) {
                        Toast.makeText(ScheduleBackups.this,"No email found, please type address.", Toast.LENGTH_LONG).show();
                    }

                    txtAddr.setText(emailAddress);
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        Log.d(MainActivity.DEBUGTAG, "acct name = " + accountName);

                        if (accountName != null) {
                            location.setText(accountName);
                        } else {
                            Toast.makeText(ScheduleBackups.this,"No account selected, please type account name.", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }

        } else {
            Toast.makeText(ScheduleBackups.this,"Selection not made.", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_note, menu);
        return true;
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        Intent i = new Intent(ScheduleBackups.this, MainActivity.class);

        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    public void addListenerOnButton() {

        btnSubmit = (Button) findViewById(R.id.btn_backup_schedule);

        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Validate inputs *********************************************************

                SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(BACKUP_LOCATION, location.getText().toString());
                editor.putString(BACKUP_TIME, timeOfDay.getText().toString());
                editor.putString(BACKUP_FREQUENCY, frequency.getText().toString());
                editor.apply();

                //Utils utils = new Utils();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 10);

                scheduleBackup(ScheduleBackups.this, calendar.getTime(), Integer.parseInt(frequency.getText().toString()));
            }
        });
    }

    static private PendingIntent pendingIntent;
    static private int pendingIntentRequestCode = 0;

    private void scheduleBackup(Context context, java.util.Date startTime, int freq) {

        Log.d(MainActivity.DEBUGTAG, "dateTime = " + startTime.toString());

        pendingIntentRequestCode++;
        Intent alarmIntent = new Intent(context, BackupAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(MainActivity.DEBUGTAG, "pendingIntent = " + pendingIntent.toString());
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // If the alarm has been set, cancel it since rescheduling here
        if (am!= null) {
            am.cancel(pendingIntent);
        }

        Calendar calendar = Calendar.getInstance();
//        Log.d(MainActivity.DEBUGTAG, "getTime = " + calendar.getTime().toString());
//        calendar.setTime(dateTime);
//
//        long diff = calendar.getTimeInMillis() - System.currentTimeMillis();
//        Log.d(MainActivity.DEBUGTAG, "diff = " + diff);
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            Log.d(MainActivity.DEBUGTAG, "call alarmManager");
//            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);
//        } else {
//            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        }

        // Set the alarm to start at approximately 2:00 p.m.
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 14);
//        calendar.set(Calendar.YEAR, 2014);
//        calendar.set(Calendar.MONTH, 4);
//        calendar.set(Calendar.MINUTE, 33);


        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        am.setInexactRepeating(am.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES*freq, pendingIntent);
    }

}


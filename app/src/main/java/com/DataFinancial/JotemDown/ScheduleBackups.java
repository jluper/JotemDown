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
    public static final String BACKUP_DESTINATION = "BACKUP_DEST";

    private EditText location;
    private EditText timeOfDay;
    private EditText frequency;
    private Button btnSubmit;
    private Button btnCancel;
    private String groupName;
    private String sortCol;
    private String sortName;
    private String sortDir;
    private String destination = "";
    private GoogleAccountCredential mCredential;
    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    Utils utils;

    ImageButton btnEmail,btnClock,btnDrive;
    static final int CONTACT_PICKER_EMAIL_RESULT = 1002;
    static final int CONTACT_PICKER_PHONE_RESULT = 1003;
    static final int REQUEST_AUTHORIZATION = 2;
    static final int REQUEST_ACCOUNT_PICKER = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_backups);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.note_yellow);
        actionBar.setTitle("Schedule Backup");
        actionBar.setDisplayShowTitleEnabled(true);

        ((EditText)findViewById(R.id.txt_location)).setEnabled(false);

        utils = new Utils();

        btnClock = (ImageButton) findViewById(R.id.btn_backup_time);
        btnClock.setOnClickListener(this);

        btnEmail = (ImageButton) findViewById(R.id.btn_backup_email);

        btnEmail.setOnClickListener(this);

        btnDrive = (ImageButton) findViewById(R.id.btn_backup_drive);
        btnDrive.setOnClickListener(this);

        btnSubmit = (Button) findViewById(R.id.btn_backup_schedule);
        btnSubmit.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btn_backup_cancel);
        btnCancel.setOnClickListener(this);

        addListenerOnSubmitButton();
        addListenerOnCancelButton();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
        }

        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));

        location = (EditText) findViewById(R.id.txt_location);
        timeOfDay = (EditText) findViewById(R.id.txt_backup_time);
        frequency = (EditText) findViewById(R.id.txt_backup_freq);

        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
        String loc = prefs.getString(BACKUP_LOCATION, null);
        String tod = prefs.getString(BACKUP_TIME, null);
        String frq = prefs.getString(BACKUP_FREQUENCY, null);
        destination = prefs.getString(BACKUP_DESTINATION, "");

        if (loc != null) {
            location.setText(prefs.getString(BACKUP_LOCATION, null));
        }
        if (tod != null) {
            timeOfDay.setText(prefs.getString(BACKUP_TIME, null));
        }
        if (frq != null) {
            frequency.setText(prefs.getString(BACKUP_FREQUENCY, null));
        }
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

                    location.setText(emailAddress);
                    destination = "emailRecipient";
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                        if (accountName != null) {
                            location.setText(accountName);
                            destination = "googleDrive";
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


    public void addListenerOnSubmitButton() {

        btnSubmit = (Button) findViewById(R.id.btn_backup_schedule);
        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                    // Validate inputs *********************************************************
                    if (!utils.isValidTimeHHMM(timeOfDay.getText().toString())) {
                        Toast.makeText(ScheduleBackups.this, "Invalid backup time.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!frequency.getText().toString().isEmpty()) {
                        if (Integer.parseInt(frequency.getText().toString()) < 1) {
                            Toast.makeText(ScheduleBackups.this, "Invalid backup frequency.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        Toast.makeText(ScheduleBackups.this, "Invalid backup frequency.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(BACKUP_LOCATION, location.getText().toString());
                    editor.putString(BACKUP_TIME, timeOfDay.getText().toString());
                    editor.putString(BACKUP_FREQUENCY, frequency.getText().toString());
                    editor.putString(BACKUP_DESTINATION, destination);
                    editor.apply();

                    String[] timeParts = timeOfDay.getText().toString().split(":");
                    int hr = Integer.parseInt(timeParts[0]);
                    int min = Integer.parseInt(timeParts[1]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hr);
                    calendar.set(Calendar.MINUTE, min);

                    scheduleBackup(ScheduleBackups.this, calendar, Integer.parseInt(frequency.getText().toString()));

                    Toast.makeText(ScheduleBackups.this, "Backup scheduled.", Toast.LENGTH_LONG).show();
                    Intent i;
                    i = new Intent(ScheduleBackups.this, MainActivity.class);
                    i.putExtra("group_name", groupName);
                    i.putExtra("sort_col", sortCol);
                    i.putExtra("sort_name", sortName);
                    i.putExtra("sort_dir", sortDir);
                    startActivity(i);
            }
        });
    }

    public void addListenerOnCancelButton() {

        btnCancel = (Button) findViewById(R.id.btn_backup_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() ==  R.id.btn_backup_cancel) {
                    pendingIntentRequestCode = 99;
                    Intent alarmIntent = new Intent(ScheduleBackups.this, BackupAlarmReceiver.class);
                    pendingIntent = PendingIntent.getBroadcast(ScheduleBackups.this, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    // If the alarm has been set, cancel it since rescheduling here
                    if (am!= null) {
                        am.cancel(pendingIntent);

                        SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(BACKUP_LOCATION, "");
                        editor.putString(BACKUP_TIME, "");
                        editor.putString(BACKUP_FREQUENCY, "");
                        editor.apply();

                        Toast.makeText(ScheduleBackups.this, "Scheduled backup canceled.", Toast.LENGTH_LONG).show();
                        Intent i;
                        i = new Intent(ScheduleBackups.this, MainActivity.class);
                        i.putExtra("group_name", groupName);
                        i.putExtra("sort_col", sortCol);
                        i.putExtra("sort_name", sortName);
                        i.putExtra("sort_dir", sortDir);
                        startActivity(i);
                    }
                }
            }
        });
    }

    static private PendingIntent pendingIntent;
    static private int pendingIntentRequestCode = 0;

    private void scheduleBackup(Context context, Calendar calendar, int freq) {

        pendingIntentRequestCode = 99;;
        Intent alarmIntent = new Intent(context, BackupAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // If the alarm has been set, cancel it since rescheduling here
        if (am!= null) {
            am.cancel(pendingIntent);
        }

        am.setInexactRepeating(am.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES * freq, pendingIntent);
    }

}


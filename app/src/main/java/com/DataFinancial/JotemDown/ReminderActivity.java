package com.DataFinancial.JotemDown;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends ActionBarActivity   implements OnClickListener {
 
	DatabaseReminders db = new DatabaseReminders(this);
    DatabaseNotes notesDb = new DatabaseNotes(this);
	Reminder reminder = new Reminder();
	private int noteId;
    private int groupId;
    private String groupName;
    ImageButton btnCalendar, btnTimePicker, btnContactPicker;
    EditText txtDate, txtTime, txtPhone;
    CheckBox chkRecurDaily;
    CheckBox chkVibrate;
    Button btnAdd, btnCancel;
    boolean update = false;
    int reminderId;
    private AlertDialog confirmDelete; 
    static private PendingIntent pendingIntent;
    static final int CONTACT_PICKER_RESULT = 1001;
    static final String REMINDER_INTENT = "com.DataFinancial.JotemDown.reminder";
	public static final String SHARED_PREF_FILE = "JotemDownSharedPreferences";
	public static final String LAST_REMINDER_PHONE = "LAST_REMINDER_PHONE";	
	static private int pendingIntentRequestCode = 0;
	public mReceiver reminderReceiver;
    static private AlarmManager am;
    private String sortCol;
    private String sortName;
    private String sortDir;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder);
 
    	android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.note_yellow);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.lbl_reminder_title);
        
        btnCalendar = (ImageButton) findViewById(R.id.btn_reminder_date);
        btnTimePicker = (ImageButton) findViewById(R.id.btn_reminder_time);
        btnContactPicker = (ImageButton) findViewById(R.id.btn_reminder_phone);
 
        txtDate = (EditText) findViewById(R.id.txt_reminder_date);
        txtTime = (EditText) findViewById(R.id.txt_reminder_time);
        txtPhone = (EditText) findViewById(R.id.txt_reminder_phone);
        chkRecurDaily = (CheckBox) findViewById(R.id.chk_recur_daily);
        chkVibrate = (CheckBox) findViewById(R.id.chk_vibrate);
        btnAdd = (Button) findViewById(R.id.btn_reminder_add);
        btnCancel = (Button) findViewById(R.id.btn_reminder_cancel);
        
        btnCalendar.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        btnContactPicker.setOnClickListener(this);
        
    	addAddReminderButtonListener();
    	addCancelReminderButtonListener();
    	
    	Bundle extras = getIntent().getExtras(); 
		if (extras != null) {
			noteId = extras.getInt("id");
            groupId = extras.getInt("group");
            groupName = extras.getString("group_name");
            sortCol = extras.getString("sort_col");
            sortName = extras.getString("sort_name");
            sortDir = extras.getString("sort_dir");
			Reminder rem = new Reminder();
			rem = db.getReminder(noteId);

			if (rem != null) {
				reminder = rem;
				txtDate.setText(Utils.convertDate(reminder.getDate(), "yy/MM/dd", "MM/dd/yy"));
				txtTime.setText(reminder.getTime());
				chkRecurDaily.setChecked(reminder.getRecur().equals("true") ? true : false);
                chkVibrate.setChecked(reminder.getVibrate().equals("true") ? true : false);
                txtPhone.setText(reminder.getPhone());
				btnAdd.setText(R.string.lbl_reminder_update);
				reminderId = reminder.getId();
				update = true;
			}
			else {
				btnCancel.setEnabled(false);
				
				SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				String lastPhone = prefs.getString(LAST_REMINDER_PHONE,  null);

				txtPhone.setText(lastPhone);
			}

            am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		}
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        super.onResume();

        Intent i = new Intent(ReminderActivity.this, MainActivity.class);

        i.putExtra("group", groupId);
        i.putExtra("group_name", groupName);
        i.putExtra("sort_col", sortCol);
        i.putExtra("sort_name", sortName);
        i.putExtra("sort_dir", sortDir);

        return i;

    }

    public static class mReceiver extends BroadcastReceiver {

        public mReceiver() {
        }

        @Override
			public void onReceive(Context context, Intent intent) {

                String message = intent.getStringExtra("message");
                if (message.equals("add_reminder")) {
		        	String dateTime = intent.getStringExtra("date_time");
                   	SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
		        	java.util.Date date;
					try {
						date = format.parse(dateTime);
                     } catch (ParseException e) {
						Toast.makeText(context, "Exception parsing reminder date: " + e.toString(), Toast.LENGTH_LONG).show();
					}        	
		        }		
			}
	};

   private void setReminderAlarm(Context context, java.util.Date dateTime) {

  	  pendingIntentRequestCode++;
  	  Intent alarmIntent = new Intent(context, ReminderAlarmReceiver.class);
      pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(dateTime);

      long diff = calendar.getTimeInMillis() - System.currentTimeMillis();

       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
           am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);
       } else {
           am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
       }
  }
   
   
    @Override
    public void onResume() {
      
  	super.onResume();
    }

    protected void onPause() {
	  // Unregister since the activity is not visible
	  //this.unregisterReceiver(this.mReceiver);
	  super.onPause();
   	} 
    
	public void addAddReminderButtonListener() {

		Button btnAdd = (Button) findViewById(R.id.btn_reminder_add);
		
		btnAdd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			
				Utils utils = new Utils();
				String reminderDate = txtDate.getText().toString();
				String reminderTime = txtTime.getText().toString();

				if (!utils.isValidDate(reminderDate)) {
					 Toast.makeText(ReminderActivity.this,"Invalid date...", Toast.LENGTH_LONG).show();		
					 return;
				}

				if (!utils.isValidTime(reminderTime)) {
					 Toast.makeText(ReminderActivity.this,"Invalid time...", Toast.LENGTH_LONG).show();		
					 return;
				}
                if (!Utils.isFuture(reminderDate + " " + reminderTime)) {
                    Toast.makeText(ReminderActivity.this,"Date in the past...", Toast.LENGTH_LONG).show();
                    return;
                }

				reminder.setNoteId(noteId);
		
				String convertedDate = Utils.convertDate(reminderDate, "MM/dd/yy", "yy/MM/dd");

				reminder.setDate(convertedDate);
				reminder.setTime(txtTime.getText().toString());
				reminder.setRecur(chkRecurDaily.isChecked() ? "true" : "false");
                reminder.setVibrate(chkVibrate.isChecked() ? "true" : "false");
				reminder.setPhone(txtPhone.getText().toString());
				

				try {
					String strDateTime = convertedDate + " " + txtTime.getText().toString();
					SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
					java.util.Date dateTime = (java.util.Date) format.parse(strDateTime);
					
					if (update == false) {			

						db.addReminder(reminder);
                        Note note = notesDb.getNote(reminder.getNoteId());
                        note.setHasReminder("true");

                        notesDb.updateNote(note);

						setReminderAlarm(ReminderActivity.this, dateTime);

			        	Intent i = new Intent(ReminderActivity.this, MainActivity.class);
						startActivity(i);
						}
					else {
						db.updateReminder(reminder);
						Toast.makeText(ReminderActivity.this,"Reminder updated...", Toast.LENGTH_LONG).show();	
						reminder = db.getReminder(reminder.getNoteId());
						setReminderAlarm(ReminderActivity.this, dateTime);
			        	
						Intent i = new Intent(ReminderActivity.this, MainActivity.class);
						startActivity(i);
					}
				} catch (Exception e) {
					Toast.makeText(ReminderActivity.this,"Exception adding reminder: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			
			    SharedPreferences prefs = getSharedPreferences(LockImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(LAST_REMINDER_PHONE, reminder.getPhone());
				editor.commit();
			}
		});
	}    
   

	public void addCancelReminderButtonListener() {

		Button btnCancel = (Button) findViewById(R.id.btn_reminder_cancel);

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			
				db.deleteReminder(reminderId);

                Note note = notesDb.getNote(reminder.getNoteId());
                note.setHasReminder("false");
                notesDb.updateNote(note);

				Toast.makeText(ReminderActivity.this,"Reminder deleted...", Toast.LENGTH_LONG).show();
				Intent i = new Intent(ReminderActivity.this, MainActivity.class);
				startActivity(i);
			}

		});
	}    
	
    @Override
    public void onClick(View v) {
 
        if (v == btnCalendar) {
         			
            // Process to get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
 
            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
 
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            txtDate.setText((monthOfYear + 1) + "/"
                                    + dayOfMonth + "/" + (year-2000));
 
                        }
                    }, mYear, mMonth, mDay);
            
            dpd.show();
        }
        if (v == btnTimePicker) {
 
            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
 
            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
 
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                int minute) {
                            // Display Selected time in textbox
                        	if (minute > 9) {
                        		txtTime.setText(hourOfDay + ":" + minute);
                        	}
                        	else {
                        		txtTime.setText(hourOfDay + ":0" + minute);                        	
                        	}
                        }
                    }, mHour, mMinute, false);
            tpd.show();
        }
        
        if (v == btnContactPicker) {        	       	
        	
        	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        	startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);       
        }
      
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case CONTACT_PICKER_RESULT:
            
            	Uri result = data.getData();
            	// get the contact id from the Uri
            	String id = result.getLastPathSegment();

            	// query for everything email
            	Cursor cursor = getContentResolver().query(
            	        Phone.CONTENT_URI, null,
            	        Phone.CONTACT_ID + "=?",
            	        new String[]{id}, null);
            	
            	String phone;
            	String type;
            	String homePhone = "";
            	String mobilePhone = "";
            	while (cursor.moveToNext()) {
            	    int dataIdx = cursor.getColumnIndex(Phone.DATA);
            	    int typeIdx = cursor.getColumnIndex(Phone.TYPE);
            	    phone = cursor.getString(dataIdx);
            	    type = cursor.getString(typeIdx);

            	    if (type == "1") { homePhone = phone; }
            	    if (Integer.parseInt(type) == Phone.TYPE_MOBILE) { 
            	    	mobilePhone = phone; 
            	    }
            	    if (Integer.parseInt(type) == Phone.TYPE_HOME) { 
            	    	homePhone = phone; 
            	    }
            	}
            	
            	phone = mobilePhone.isEmpty() ? homePhone: mobilePhone;

            	EditText txtPhone = (EditText) findViewById(R.id.txt_reminder_phone);
               	txtPhone.setText(phone);
                break;
            }

        } else {
            Toast.makeText(ReminderActivity.this,"Contact not selected.", Toast.LENGTH_LONG).show();
        }
    }
}
 


package com.DataFinancial.NoteJackal;


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
import android.util.Log;
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
	int noteId;
    ImageButton btnCalendar, btnTimePicker, btnContactPicker;
    EditText txtDate, txtTime, txtPhone;
    CheckBox chkRecurDaily;
    Button btnAdd, btnCancel;
    boolean update = false;
    int reminderId;
    private AlertDialog confirmDelete; 
    static private PendingIntent pendingIntent;

    static final int CONTACT_PICKER_RESULT = 1001;
    static final String REMINDER_INTENT = "com.DataFinancial.NoteJackal.reminder";
	public static final String SHARED_PREF_FILE = "NoteJackalSharedPreferences";	
	public static final String LAST_REMINDER_PHONE = "LAST_REMINDER_PHONE";	
	static private int pendingIntentRequestCode = 0;
	public mReceiver reminderReceiver;
    static private AlarmManager am;

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
			Log.d(MainActivity.DEBUGTAG, "note id from caller " + noteId );
			Reminder rem = new Reminder();
			rem = db.getReminder(noteId);
			if (rem != null) {
				reminder = rem;
				txtDate.setText(Utils.convertDate(reminder.getDate(), "yy/MM/dd", "MM/dd/yy"));
				txtTime.setText(reminder.getTime());
				chkRecurDaily.setChecked(reminder.getRecur().equals("true") ? true : false);
				txtPhone.setText(reminder.getPhone());
				btnAdd.setText(R.string.lbl_reminder_update);
				reminderId = reminder.getId();
				update = true;
			}
			else {
				btnCancel.setEnabled(false);
				
				SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
				String lastPhone = prefs.getString(LAST_REMINDER_PHONE,  null);
				//if (lastPhone)
				txtPhone.setText(lastPhone);
			}

            am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			//setOnReminderBroadcastReceiver();
				
			////Log.d(MainActivity.DEBUGTAG, "note from reminder act  " + reminder.toString());
		}
    }



		
		//IntentFilter intentFilter = new IntentFilter("com.DataFinancial.NoteJackal.reminder");


    public static class mReceiver extends BroadcastReceiver {

        public mReceiver() {
        }

        @Override
			public void onReceive(Context context, Intent intent) {

            Log.d(MainActivity.DEBUGTAG, "in onReceive in reminderactivity");
		        String message = intent.getStringExtra("message");
                Toast.makeText(context, "in alarm receiver", Toast.LENGTH_LONG);
		        if (message.equals("add_reminder")) {
		        	String dateTime = intent.getStringExtra("date_time");
                    Log.d(MainActivity.DEBUGTAG, "in onReceive dateTime extra = " + dateTime);
		        	SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
		        	java.util.Date date;
					try {
						date = format.parse(dateTime);
                        Log.d(MainActivity.DEBUGTAG, "in onReceive date = " + date.toString());
						setReminderAlarm(context, date);
					} catch (ParseException e) {
						Toast.makeText(context, "Unable to set reminder recurrence.", Toast.LENGTH_LONG)
						.show();
					}        	
		        }		
			}
	};
		//registering our receiver
		//IntentFilter intentFilter = new IntentFilter(REMINDER_INTENT);
		//this.registerReceiver(mReceiver, intentFilter);

   
   private static void setReminderAlarm(Context context, java.util.Date dateTime) {
   	
  	 Log.d(MainActivity.DEBUGTAG, "in setReminderAlarm in activity" + dateTime);
  	  pendingIntentRequestCode++;
  	  Intent alarmIntent = new Intent(context, ReminderAlarmReceiver.class);
      pendingIntent = PendingIntent.getBroadcast(context, pendingIntentRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      //AlarmManager am = (AlarmManager) am.getSystemService(Context.ALARM_SERVICE);

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(dateTime);

      long diff = calendar.getTimeInMillis() - System.currentTimeMillis();
      
      //am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
           am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);
       } else {
           am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
       }

       am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + diff, pendingIntent);

  }
   
   
    @Override
    public void onResume() {
      
      
  	//Log.d(MainActivity.DEBUGTAG, "before registerReceiver");
  	//IntentFilter intentFilter = new IntentFilter(REMINDER_INTENT);
	//this.registerReceiver(mReceiver, intentFilter);
	//Log.d(MainActivity.DEBUGTAG, "after registerReceiver");
	
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
				////Log.d(MainActivity.DEBUGTAG, "clicked Add button: " + reminderDate + ", " + reminderTime + ", " + chkRecurDaily.isChecked());
								
				if (!utils.isValidDate(reminderDate)) {
					 Toast.makeText(ReminderActivity.this,"Invalid date...", Toast.LENGTH_LONG).show();		
					 return;
				}
				
				if (!utils.isValidTime(reminderTime)) {
					 Toast.makeText(ReminderActivity.this,"Invalid time...", Toast.LENGTH_LONG).show();		
					 return;
				}
				
				//EditText txtDate = (EditText) findViewById(R.id.txt_reminder_date);
				//EditText txtTime = (EditText) findViewById(R.id.txt_reminder_time);
				//CheckBox chkRecur = (CheckBox) findViewById(R.id.chk_recur_daily);
					
				reminder.setNoteId(noteId);
		
				String convertedDate = Utils.convertDate(reminderDate, "MM/dd/yy", "yy/MM/dd");
				//Log.d(MainActivity.DEBUGTAG, "converted date = " + convertedDate);
				reminder.setDate(convertedDate);
				//Log.d(MainActivity.DEBUGTAG, "reminder date = " + reminder.getDate());
				reminder.setTime(txtTime.getText().toString());
				reminder.setRecur(chkRecurDaily.isChecked() ? "true" : "false");
				reminder.setPhone(txtPhone.getText().toString());
				

				try {
					String strDateTime = convertedDate + " " + txtTime.getText().toString();
					SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);
					java.util.Date dateTime = (java.util.Date) format.parse(strDateTime);
					
					if (update == false) {			
						
					
						//Log.d(MainActivity.DEBUGTAG, "dateTime string = " + strDateTime);
						//Log.d(MainActivity.DEBUGTAG, "dateTime = " + dateTime.toString());
						
						db.addReminder(reminder);
                        Log.d(MainActivity.DEBUGTAG, "reminder in add = " + reminder.toString());
                        Note note = notesDb.getNote(reminder.getNoteId());
                        note.setHasReminder("true");
                        Log.d(MainActivity.DEBUGTAG, "1) note with reminder  in ReminderActivity= " + note.toString());
                        notesDb.updateNote(note);
                        Log.d(MainActivity.DEBUGTAG, "2) note after update with reminder = " +  notesDb.getNote(note.getId()).toString());

						setReminderAlarm(ReminderActivity.this, dateTime);
// BEFORE ALARMS						
//						
//						
//						reminder = db.getReminder(reminder.getNoteId());
//						//Log.d(MainActivity.DEBUGTAG, "reminder from database = " + reminder.toString());
//						btnCancel.setEnabled(true);
						Toast.makeText(ReminderActivity.this,"Reminder added...", Toast.LENGTH_LONG).show();
//						
//						boolean srvcRunning = isReminderServiceRunning(ReminderService.class);
//						//Log.d(MainActivity.DEBUGTAG, "(add reminder) is service running: " + srvcRunning);						
//						Intent srvcIntent = new Intent(ReminderActivity.this, ReminderService.class);
//						if (srvcRunning == true) {
//							stopService(srvcIntent);
//						}						
//			        	startService(srvcIntent);
			        	
			        	Intent i = new Intent(ReminderActivity.this, MainActivity.class);
						startActivity(i);
						}
					else {
						db.updateReminder(reminder);
						Toast.makeText(ReminderActivity.this,"Reminder updated...", Toast.LENGTH_LONG).show();	
						reminder = db.getReminder(reminder.getNoteId());
						setReminderAlarm(ReminderActivity.this, dateTime);
						
						//Log.d(MainActivity.DEBUGTAG, "reminder from database = " + reminder.toString());
						
//	BEFORE ALARMS		boolean srvcRunning = isReminderServiceRunning(ReminderService.class);
//						//Log.d(MainActivity.DEBUGTAG, "(update reminder) is service running: " + srvcRunning);						
//						Intent srvcIntent = new Intent(ReminderActivity.this, ReminderService.class);
//						if (srvcRunning == true) {
//							stopService(srvcIntent);
//						}						
//			        	startService(srvcIntent);
			        	
						Intent i = new Intent(ReminderActivity.this, MainActivity.class);
						startActivity(i);
					}
				} catch (Exception e) {
					Toast.makeText(ReminderActivity.this,"Unable to add reminder." + e.getMessage(), Toast.LENGTH_LONG).show();					
				}
			
			    SharedPreferences prefs = getSharedPreferences(ImageActivity.SHARED_PREF_FILE, MODE_PRIVATE);
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
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
 
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
            TimePickerDialog tpd = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
 
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
            	
            	String phone = "";
            	String type;
            	String homePhone = "";
            	String mobilePhone = "";
            	while (cursor.moveToNext()) {
            	    int dataIdx = cursor.getColumnIndex(Phone.DATA);
            	    int typeIdx = cursor.getColumnIndex(Phone.TYPE);
            	    phone = cursor.getString(dataIdx);
            	    type = cursor.getString(typeIdx);
            	    //Log.d(MainActivity.DEBUGTAG, "phone: " + phone + " type: " + type);
            	    if (type == "1") { homePhone = phone; }
            	    if (Integer.parseInt(type) == Phone.TYPE_MOBILE) { 
            	    	mobilePhone = phone; 
            	    }
            	    if (Integer.parseInt(type) == Phone.TYPE_HOME) { 
            	    	homePhone = phone; 
            	    }
            	}
            	
            	phone = mobilePhone.isEmpty() ? homePhone: mobilePhone;
            	//Log.d(MainActivity.DEBUGTAG, "home phone: " + homePhone + " mobile phone: " + mobilePhone);
            	//Log.d(MainActivity.DEBUGTAG, "phone: " + phone);
            	
            	EditText txtPhone = (EditText) findViewById(R.id.txt_reminder_phone);
               	txtPhone.setText(phone);
                break;
            }

        } else {
        	Log.d(MainActivity.DEBUGTAG,"Contact not picked.");
        }
    }

      
    
 // handler for received Intents for the "reminderEvent" event 
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//      @Override
//      public void onReceive(Context context, Intent intent) {
//        // Extract data included in the Intent
//        String message = intent.getStringExtra("message");
//        
//        Log.d("receiver", "Got message: " + message);
//        if (message.equals("add_reminder")) {
//        	String dateTime = intent.getStringExtra("date_time");
//        	SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd", Locale.ENGLISH);
//        	java.util.Date date;
//			try {
//				date = format.parse(dateTime);
//				setReminderAlarm(date);
//			} catch (ParseException e) {
//				Toast.makeText(context, "Unable to set reminder recurrence.", Toast.LENGTH_LONG)
//				.show();
//			}        	
//        }
//      }
//    };

    
  
	
}
 


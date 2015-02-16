package com.DataFinancial.NoteJackal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseReminders extends SQLiteOpenHelper {

	private final String TABLE_REMINDERS = "REMINDERS";
	private static final String COL_ID = "ID";
	private static final String COL_NOTEID = "NOTEID";
	private static final String COL_DATE = "REMDATE";
	private static final String COL_TIME = "REMTIME";
	private static final String COL_RECUR = "RECUR";
	private static final String COL_PHONE = "PHONE";
	

	public DatabaseReminders(Context context) {
		super(context, "reminders.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
	
	
	public void createRemindersTable() {
		
		 SQLiteDatabase db = this.getWritableDatabase();
		 
			String sqlRemindersTable = String
					.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s STRING NOT NULL, %s STRING NOT NULL, %s STRING NOT NULL, %s STRING)",
							TABLE_REMINDERS, COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE);
			//Log.d(MainActivity.DEBUGTAG, "select " + sqlRemindersTable);
			
			db.execSQL(sqlRemindersTable);		
		
	}	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*	  Log.w(MySQLiteHelper.class.getName(),
	      "Upgrading database from version " + oldVersion + " to "
	          + newVersion + ", which will destroy all old data");
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
	  onCreate(db);*/
	}
	
	public long addReminder(Reminder reminder) {
		
		SQLiteDatabase db = getWritableDatabase();
				
		ContentValues values = new ContentValues();
		
		values.put(COL_NOTEID, reminder.getNoteId());
		values.put(COL_DATE, reminder.getDate());
		values.put(COL_TIME, reminder.getTime());
		values.put(COL_RECUR, reminder.getRecur());
		values.put(COL_PHONE, reminder.getPhone());
					
		Log.d(MainActivity.DEBUGTAG, "addReminder " + reminder.toString());			
		
		long row = db.insert(TABLE_REMINDERS,  null,  values);						
		//Log.d(MainActivity.DEBUGTAG, "row number on add = " + row);
		db.close();
		
		return row;
	}

	public List<Reminder> getReminders() {
		
		List<Reminder> reminders = new ArrayList<Reminder>();
		SQLiteDatabase db = getReadableDatabase();
				
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s ORDER BY %s",  COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_NOTEID);
		
		Cursor cursor = db.rawQuery(sql, null);
		
		while (cursor.moveToNext()) {
			Reminder reminder = new Reminder();
			reminder.setId(cursor.getInt(0));
			reminder.setNoteId(cursor.getInt(1));
			reminder.setDate(cursor.getString(2));
			reminder.setTime(cursor.getString(3));
			reminder.setRecur(cursor.getString(4));
			reminder.setPhone(cursor.getString(5));
			
			reminders.add(reminder);
		
		}
		
		db.close();
		
		return(reminders);
	}
	
	public List<Reminder> getUnexpiredReminders() {
		
		List<Reminder> reminders = new ArrayList<Reminder>();
		SQLiteDatabase db = getReadableDatabase();
				
    	SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
		Date today = new Date();
		String strToday = df.format(today);
		String[] strTodayParts = strToday.split(" ");
		String date = strTodayParts[0];
		String time = strTodayParts[1];
				
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s ORDER BY %s ASC",  COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_DATE);
		//Log.d(MainActivity.DEBUGTAG, "sql = " + sql);
		Cursor cursor = db.rawQuery(sql, null);
		//Log.d(MainActivity.DEBUGTAG, "cursr cnt = " + cursor.getCount());
		while (cursor.moveToNext()) {
			Reminder reminder = new Reminder();
			reminder.setId(cursor.getInt(0));
			reminder.setNoteId(cursor.getInt(1));
			reminder.setDate(cursor.getString(2));
			reminder.setTime(cursor.getString(3));
			reminder.setRecur(cursor.getString(4));
			reminder.setPhone(cursor.getString(5));
			
			//Log.d(MainActivity.DEBUGTAG, "reminder in get before test = " + reminder.toString());
			
			//if ((reminder.getDate().compareTo(date) > 0) || (reminder.getDate().compareTo(date) == 0 && reminder.getTime().compareTo(time) >= 0)) {
				//Log.d(MainActivity.DEBUGTAG, "reminder in get after = " + reminder.toString());
				reminders.add(reminder);
			//}					
		}
		
		db.close();		
		
		return(reminders);
	}
	
	public Reminder getReminder(int noteID) {
		
		String sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %s = %s ORDER BY %s",  COL_ID, COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, COL_PHONE, TABLE_REMINDERS, COL_NOTEID, noteID, COL_NOTEID);
		//Log.d(MainActivity.DEBUGTAG, "select " + sql);
		SQLiteDatabase db = getReadableDatabase();
		Reminder reminder = new Reminder();
		
		//String sql = String.format("SELECT %s, %s, %s, %s FROM %s WHERE % = %s ORDER BY %s",  COL_NOTEID, COL_DATE, COL_TIME, COL_RECUR, TABLE_REMINDERS, COL_NOTEID, noteID, COL_NOTEID);
		//Log.d(MainActivity.DEBUGTAG, "select " + sql);
		Cursor cursor = db.rawQuery(sql, null);
		
	    if (cursor.getCount() > 0)  {
	    	cursor.moveToFirst();
	    	reminder.setId(cursor.getInt(0));
			reminder.setNoteId(cursor.getInt(1));
			reminder.setDate(cursor.getString(2));
			reminder.setTime(cursor.getString(3));
			reminder.setRecur(cursor.getString(4));	    
			reminder.setPhone(cursor.getString(5));
	    }
	    else {
	    	//Log.d(MainActivity.DEBUGTAG, "No reminder found");
	    	return null;
	    }
			
		db.close();
		
		return(reminder);
	}
		
	public void recreateRemindersTable() {
		
		 SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
		  createRemindersTable();
	}
	
  public long updateReminder(Reminder reminder) {
	  
	  SQLiteDatabase db = this.getWritableDatabase();
	  
	    ContentValues values = new ContentValues();
	    
	    //values.put(COL_ID, note.getId());
	    values.put(COL_NOTEID, reminder.getNoteId());
	    values.put(COL_DATE, reminder.getDate());
	    values.put(COL_TIME, reminder.getTime());
	    values.put(COL_RECUR, reminder.getRecur());
	    values.put(COL_PHONE, reminder.getPhone());
	  		    
	    //Log.d(MainActivity.DEBUGTAG, "values= " + values.toString());
	   		    
	    long rowId = -1;
	    try {
	    	
	    	rowId = db.update(TABLE_REMINDERS, values, COL_ID + "=" + reminder.getId(), null);
	    	
		} catch (Exception e) {
			
			
			return rowId;   // should be -1
		}
	    
	    db.close();
	    
	    return rowId;
	  }

	  
	  public long deleteReminder(int id) {
		  
		  //Log.d(MainActivity.DEBUGTAG, "deleteReminder id = " +id);
		  
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		    long rowId = -1;
		    try {
		    	
		    	db.delete(TABLE_REMINDERS, COL_ID + "=" + id, null);

			} catch (Exception e) {
				
				return rowId;   // should be -1
			}
		    
		    db.close();
		    
		    return rowId;
	  }
	
	  public long deleteReminderByNoteId(int noteId) {
		  
		  //Log.d(MainActivity.DEBUGTAG, "deleteReminderByNoteId noteId = " + noteId);
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		    long rowId = -1;
		    try {
		    	
		    	db.delete(TABLE_REMINDERS, COL_NOTEID + "=" + noteId, null);

			} catch (Exception e) {
				
				return rowId;   // should be -1
			}
		    
		    db.close();
		    
		    return rowId;
	  }
	  
	public void deletePastReminders() {
		  
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		  //Log.d(MainActivity.DEBUGTAG,"delete reminder");
		    long rowId = -1;
		    try {
		    		    	
		    	SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
				Date today = new Date();
				String[] dateTime;
				dateTime = df.format(today).split(" ");
				//Log.d(MainActivity.DEBUGTAG,"date = " +  dateTime[0] + "time = " + dateTime[1]);
				
		    
	
			} catch (Exception e) {
				//Log.d(MainActivity.DEBUGTAG,"exception: " +  e.getMessage());		
			}
		    
		    db.close();	    
		   
	  }
}


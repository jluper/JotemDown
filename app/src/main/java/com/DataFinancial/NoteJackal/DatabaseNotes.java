package com.DataFinancial.NoteJackal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.List;

public class DatabaseNotes extends SQLiteOpenHelper {

	private final int DATABASE_VERSION = 1;
	private final String DATABASE_NAME = "NotesDb";
	public  final String TABLE_NOTES = "NOTES";
	public static final String COL_ID = "ID";
	public static final String COL_PRIORITY = "PRIORITY";
	public static final String COL_BODY = "BODY";
	public static final String COL_CREATE_DATE = "CREATEDATE";
	public static final String COL_EDIT_DATE = "EDITDATE";
	public static final String COL_LAT = "LATITUDE";
	public static final String COL_LON = "LONGITUDE";
    public static final String COL_REMINDER = "REMINDER";
	private SQLiteDatabase db;
	private File dbPath;
	private Context context;
	
	public String getDbPath() {
		
		Log.d(MainActivity.DEBUGTAG, "dbpath in NotesDatabase 1");
		db = this.getWritableDatabase();
		 Log.d(MainActivity.DEBUGTAG, "dbpath in NotesDatabase 2");
		String dbPath = db.getPath();
		  Log.d(MainActivity.DEBUGTAG, "dbpath in NotesDatabase = " + dbPath);
		return dbPath;
	}


	public DatabaseNotes(Context context) {
		super(context, "notes.db", null, 1);
		this.context = context;
	}


	public SQLiteDatabase getDb() {
		return this.getWritableDatabase();
	}


	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}


	@Override
	public void onCreate(SQLiteDatabase db) {


	}
	
	
	public void createNotesTable() {
		
		 //SQLiteDatabase db = this.getWritableDatabase();
		
		db = this.getWritableDatabase();
		
		String sqlNotesTable = String.format("create table if not exists %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",
				TABLE_NOTES, COL_ID, COL_PRIORITY, COL_BODY, COL_CREATE_DATE, COL_EDIT_DATE, COL_LAT, COL_LON, COL_REMINDER);

		////Log.d(MainActivity.DEBUGTAG,"Create sql..." + sqlNotesTable);

		db.execSQL(sqlNotesTable);
		
	}
	
	public List<Note> getAllNotes(String select, String order, String dir) {
	       List<Note> notes = new ArrayList<Note>();
	 
	       //SQLiteDatabase db = this.getWritableDatabase();
	       db = this.getWritableDatabase();
	       
	       String query;
	       
	       // 1. build the query
	       String searchText = (String) this.context.getResources().getText(R.string.txt_help_search);
	       //Log.d(MainActivity.DEBUGTAG, "searchtext, dir = " + searchText +  ", " + dir);
	       if (select == null) {
	    	   query = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " NOT LIKE '%" +  searchText + "%' " + " ORDER BY " + order + " " + dir;
	       }
	       else {
	    	   query = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COL_BODY + " LIKE '%" + select + "%' " + "ORDER BY " + order + " " + dir;
	       }
	       
	      // //Log.d(MainActivity.DEBUGTAG, "query = " + query);
	 
	       // 2. get reference to writable DB
	       //SQLiteDatabase db = this.getWritableDatabase();
	       Cursor cursor = db.rawQuery(query, null);
	 
	       
	      // //Log.d(MainActivity.DEBUGTAG, "in getAllNotes after cursor: ");
	       
	       // 3. go over each row, build note and add it to list
	       Note note = null;
	       if (cursor.moveToFirst()) {
	           do {
	               note = new Note();
	               note.setId(cursor.getInt(0));
	               note.setPriority(cursor.getInt(1));
	   	           note.setBody(cursor.getString(2));
		           note.setCreateDate(cursor.getString(3));
		           note.setEditDate(cursor.getString(4));
		           note.setLatitude(cursor.getString(5));
		           note.setLongitude(cursor.getString(6));
                   note.setHasReminder(cursor.getString(7));
	 
		          //Log.d(MainActivity.DEBUGTAG, "in getAllNotes = " + note.toString());
	               // Add note to books
	               notes.add(note);
	           } while (cursor.moveToNext());
	       }
	 
	       ////Log.d(MainActivity.DEBUGTAG, "getAllNotes: " + notes.toString());
	 
	       // return books
	       return notes;
	   }

	
	public void recreateNotesTable() {
		
		////Log.d(MainActivity.DEBUGTAG, "recreateNotesTable: ");
		// SQLiteDatabase db = this.getWritableDatabase();
		db = this.getWritableDatabase();
		  db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
		  createNotesTable();
		  
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
	  onCreate(db);
	}


	  public long addNote(Note note) {
		  
		  //SQLiteDatabase db = this.getWritableDatabase();
		  db = this.getWritableDatabase();
		  
		    ContentValues values = new ContentValues();
		    
		    //values.put(COL_ID, note.getId());
		    values.put(COL_PRIORITY, note.getPriority());
		    values.put(COL_BODY, note.getBody());
		    values.put(COL_CREATE_DATE, note.getCreateDate());
		    values.put(COL_EDIT_DATE, note.getEditDate());
            values.put(COL_REMINDER, note.getHasReminder());
		    
		    ////Log.d(MainActivity.DEBUGTAG,"in addNote: " + note.getCreateDate() + ", " + note.getEditDate() + ", " + note.getBody());
		    ////Log.d(MainActivity.DEBUGTAG,"Table: " + TABLE_NOTES);
		   // //Log.d(MainActivity.DEBUGTAG,"content: " + values.toString());
		    
		    long rowId = -1;
		    try {
		    	Log.d(MainActivity.DEBUGTAG,"before insert: " + note.toString());
		    	rowId = db.insert(TABLE_NOTES, null, values);
		    	////Log.d(MainActivity.DEBUGTAG,"row_id: " +  rowId);
			} catch (Exception e) {
				////Log.d(MainActivity.DEBUGTAG,"exception: " +  e.getMessage());
				
				return rowId;   // should be -1
			}
		    
		    db.close();
		    
		    return rowId;
		  }

	
	  public long deleteNote(int id) {
		  
		  //SQLiteDatabase db = this.getWritableDatabase();
		  db = this.getWritableDatabase();
		  DatabaseReminders dbReminders = new DatabaseReminders(context);
    
		    long rowId = -1;
		    try {
		    	dbReminders.deleteReminderByNoteId(id);
		    	db.delete(TABLE_NOTES, COL_ID + "=" + id, null);

			} catch (Exception e) {
				////Log.d(MainActivity.DEBUGTAG,"exception: " +  e.getMessage());
				
				return rowId;   // should be -1
			}
		    
		    db.close();
		    
		    return rowId;
		  }

	  
	  public long updateNote(Note note) {
		  
		  //SQLiteDatabase db = this.getWritableDatabase();
		  db = this.getWritableDatabase();
		  
		    ContentValues values = new ContentValues();
          Log.d(MainActivity.DEBUGTAG, "note in updateNote= " + note.toString());
		    //values.put(COL_ID, note.getId());
		    values.put(COL_PRIORITY, note.getPriority());
		    values.put(COL_BODY, note.getBody());
		    values.put(COL_CREATE_DATE, note.getCreateDate());
		    values.put(COL_EDIT_DATE, note.getEditDate());		    
		    values.put(COL_LAT, note.getLatitude());
		    values.put(COL_LON, note.getLongitude());
            values.put(COL_REMINDER, note.getHasReminder());

		    
		    
		    Log.d(MainActivity.DEBUGTAG, "values in update note= " + values.toString());
		    ////Log.d(MainActivity.DEBUGTAG,"in updateNote: " + note.getId() + ", " + note.getCreateDate() + ", " + note.getEditDate() + ", " + note.getBody());
		    ////Log.d(MainActivity.DEBUGTAG,"Table: " + TABLE_NOTES);
          Log.d(MainActivity.DEBUGTAG, "note with reminder = " + note.toString());
		    
		    long rowId = -1;
		    try {
		    	
		    	rowId = db.update(TABLE_NOTES, values, COL_ID + "=" + note.getId(), null);
		    	////Log.d(MainActivity.DEBUGTAG, " where "  + COL_ID + "=" + note.getId());
			} catch (Exception e) {
				////Log.d(MainActivity.DEBUGTAG,"exception: " +  e.getMessage());
				
				return rowId;   // should be -1
			}
		    
		    db.close();
		    
		    return rowId;
		  }
	  
	  
	  public Note getNote(int id) {
		  
		    //SQLiteDatabase db = this.getReadableDatabase();
		  db = this.getWritableDatabase();
		 
		    String query = "select * from " + TABLE_NOTES + " where "  + COL_ID + "=" + id;
		    Cursor cursor = db.rawQuery(query, null);
		
		    Note note = new Note();
		    if (cursor != null) {
		        cursor.moveToFirst();

                note.setId(cursor.getInt(0));
		        note.setPriority(cursor.getInt(1));
		        note.setBody(cursor.getString(2));
		        note.setCreateDate(cursor.getString(3));
		        note.setEditDate(cursor.getString(4));
                note.setLatitude(cursor.getString(5));
                note.setLongitude(cursor.getString(6));
                note.setHasReminder(cursor.getString(7));
		        
		    ////Log.d(MainActivity.DEBUGTAG,note.getCreateDate() + ", " + note.getEditDate() + ", " + note.getBody());
		    }
		         
		    // return contact
		    return note;
	}

	  

	  public void clearNotesTable() {
		  
		  //SQLiteDatabase db = this.getWritableDatabase();
		  db = this.getWritableDatabase();
		  db.delete(TABLE_NOTES, null,null);
	  }
	  
}
